# Guide de Déploiement

## 📋 Prérequis

### Environnement de Développement
- Java 17 ou supérieur
- Maven 3.6+
- MySQL 8.0+
- Git

### Environnement de Production
- Serveur Linux (Ubuntu 20.04+ recommandé)
- Java 17 JRE
- MySQL 8.0+
- Nginx (optionnel, pour reverse proxy)
- Redis (recommandé pour le cache en production)

## 🔧 Configuration

### 1. Base de Données

#### Créer la base de données
```sql
CREATE DATABASE appel_classe_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'appel_user'@'localhost' IDENTIFIED BY 'secure_password';
GRANT ALL PRIVILEGES ON appel_classe_db.* TO 'appel_user'@'localhost';
FLUSH PRIVILEGES;
```

#### Importer le schéma
```bash
mysql -u appel_user -p appel_classe_db < DataBase/appel_classe_db.sql
```

### 2. Configuration de l'Application

#### application.properties (Production)
```properties
# Server
server.port=8080
server.servlet.context-path=/

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/appel_classe_db?useSSL=true&serverTimezone=UTC
spring.datasource.username=appel_user
spring.datasource.password=secure_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.properties.hibernate.format_sql=false

# JWT
jwt.secret=your-very-long-and-secure-secret-key-here-minimum-256-bits
jwt.expiration=86400000

# CORS
cors.allowed-origins=https://yourdomain.com

# Logging
logging.level.root=INFO
logging.level.com.ISI.Appel_classe=INFO
logging.file.name=logs/application.log

# File Upload
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Redis (si utilisé)
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.password=
```

## 🚀 Déploiement

### Option 1: JAR Exécutable

#### 1. Build du projet
```bash
cd first_projet_springboot
mvn clean package -DskipTests
```

#### 2. Copier le JAR sur le serveur
```bash
scp target/Appel-classe-0.0.1-SNAPSHOT.jar user@server:/opt/appel-classe/
```

#### 3. Créer un service systemd
```bash
sudo nano /etc/systemd/system/appel-classe.service
```

```ini
[Unit]
Description=Appel Classe Application
After=syslog.target network.target

[Service]
User=appel
WorkingDirectory=/opt/appel-classe
ExecStart=/usr/bin/java -jar /opt/appel-classe/Appel-classe-0.0.1-SNAPSHOT.jar
SuccessExitStatus=143
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

#### 4. Démarrer le service
```bash
sudo systemctl daemon-reload
sudo systemctl enable appel-classe
sudo systemctl start appel-classe
sudo systemctl status appel-classe
```

### Option 2: Docker

#### 1. Créer un Dockerfile
```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/Appel-classe-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### 2. Créer docker-compose.yml
```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: appel_classe_db
      MYSQL_USER: appel_user
      MYSQL_PASSWORD: secure_password
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./DataBase/appel_classe_db.sql:/docker-entrypoint-initdb.d/init.sql

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/appel_classe_db
      SPRING_DATASOURCE_USERNAME: appel_user
      SPRING_DATASOURCE_PASSWORD: secure_password
      SPRING_REDIS_HOST: redis
    depends_on:
      - mysql
      - redis

volumes:
  mysql_data:
  redis_data:
```

#### 3. Build et démarrer
```bash
docker-compose up -d
```

## 🔒 Sécurité

### 1. Firewall
```bash
# Autoriser uniquement les ports nécessaires
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable
```

### 2. SSL/TLS avec Let's Encrypt
```bash
sudo apt install certbot python3-certbot-nginx
sudo certbot --nginx -d yourdomain.com
```

### 3. Nginx Reverse Proxy
```nginx
server {
    listen 80;
    server_name yourdomain.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name yourdomain.com;

    ssl_certificate /etc/letsencrypt/live/yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/yourdomain.com/privkey.pem;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

## 📊 Monitoring

### 1. Logs
```bash
# Voir les logs en temps réel
sudo journalctl -u appel-classe -f

# Logs de l'application
tail -f /opt/appel-classe/logs/application.log
```

### 2. Health Check
```bash
curl http://localhost:8080/actuator/health
```

### 3. Métriques (Ajouter Spring Boot Actuator)
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

```properties
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized
```

## 🔄 Mise à Jour

### 1. Backup de la base de données
```bash
mysqldump -u appel_user -p appel_classe_db > backup_$(date +%Y%m%d_%H%M%S).sql
```

### 2. Arrêter l'application
```bash
sudo systemctl stop appel-classe
```

### 3. Remplacer le JAR
```bash
cp Appel-classe-0.0.1-SNAPSHOT.jar /opt/appel-classe/
```

### 4. Redémarrer l'application
```bash
sudo systemctl start appel-classe
```

## 🧪 Tests de Production

### 1. Test de connectivité
```bash
curl -I https://yourdomain.com/api/health
```

### 2. Test d'authentification
```bash
curl -X POST https://yourdomain.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password"}'
```

### 3. Test de performance
```bash
# Installer Apache Bench
sudo apt install apache2-utils

# Test de charge
ab -n 1000 -c 10 https://yourdomain.com/api/health
```

## 📈 Optimisations

### 1. JVM Options
```bash
java -Xms512m -Xmx2g -XX:+UseG1GC -jar app.jar
```

### 2. Connection Pool
```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
```

### 3. Cache Redis
Remplacer le HashMap par Redis pour le cache des codes PIN:

```java
@Configuration
@EnableCaching
public class RedisConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        return template;
    }
}
```

## 🔧 Troubleshooting

### Application ne démarre pas
```bash
# Vérifier les logs
sudo journalctl -u appel-classe -n 100

# Vérifier le port
sudo netstat -tulpn | grep 8080

# Vérifier la connexion MySQL
mysql -u appel_user -p -h localhost appel_classe_db
```

### Erreurs de mémoire
```bash
# Augmenter la mémoire JVM
java -Xms1g -Xmx4g -jar app.jar
```

### Problèmes de performance
```bash
# Activer les métriques
curl http://localhost:8080/actuator/metrics

# Vérifier les connexions MySQL
SHOW PROCESSLIST;
```

## 📝 Checklist de Déploiement

- [ ] Base de données créée et configurée
- [ ] Schéma importé
- [ ] Configuration de production en place
- [ ] JWT secret généré et sécurisé
- [ ] Firewall configuré
- [ ] SSL/TLS activé
- [ ] Nginx configuré (si utilisé)
- [ ] Service systemd créé
- [ ] Logs configurés
- [ ] Backup automatique configuré
- [ ] Monitoring en place
- [ ] Tests de production effectués

## 🆘 Support

En cas de problème:
1. Vérifier les logs
2. Consulter la documentation
3. Tester les endpoints avec Postman
4. Vérifier la configuration

---

**Note**: Ce guide suppose un déploiement sur un serveur Linux. Adaptez les commandes selon votre environnement.
