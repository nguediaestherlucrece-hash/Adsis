package com.ISI.Appel_classe.security.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Service de gestion des tokens JWT
 *
 * Cette classe gère l'ensemble du cycle de vie des tokens JWT :
 * - Génération des tokens (accès et rafraîchissement)
 * - Validation et extraction des données des tokens
 * - Gestion de l'expiration
 *
 * Annotations Spring :
 * - @Service : Enregistre cette classe comme bean de service dans Spring
 * - @Slf4j : Fournit un logger (log) pour enregistrer les événements
 *
 * Sécurité :
 * - Utilise HMAC-SHA512 pour signer les tokens (algorithme fort)
 * - La clé secrète est lue depuis application.properties (env variable en production)
 * - Les tokens sont signés numériquement pour empêcher la manipulation
 */
@Service
@Slf4j
public class JwtService {

    /**
     * Clé secrète pour signer les tokens JWT
     *
     * Lue depuis application.properties : ${jwt.secret}
     * IMPORTANT: Doit être complexe et longue (min 64 caractères)
     * En production, utiliser une variable d'environnement au lieu de commiter en Git
     */
    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * Temps d'expiration du token d'accès en millisecondes
     *
     * Lue depuis application.properties : ${jwt.expiration}
     * Par défaut : 3600000 ms = 1 heure
     *
     * Après ce délai, le token ne sera plus accepté pour accéder aux ressources
     */
    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    /**
     * Temps d'expiration du token de rafraîchissement en millisecondes
     *
     * Lue depuis application.properties : ${jwt.refresh-expiration}
     * Par défaut : 604800000 ms = 7 jours
     *
     * Ce token plus long permet de générer un nouveau token d'accès sans se reconnecter
     */
    @Value("${jwt.refresh-expiration}")
    private Long refreshTokenExpiration;

    /**
     * Générer une clé secrète pour signer les tokens
     *
     * Processus :
     * 1. Récupère la clé secrète en texte depuis le fichier config
     * 2. La convertit en bytes (UTF-8)
     * 3. La transforme en clé HMAC-SHA compatible JJWT
     *
     * Cette clé est utilisée pour signer numériquement chaque token
     * Seule la clé correcte peut valider la signature
     *
     * @return La clé secrète au format SecretKey
     */
    private SecretKey getSigningKey() {
        // Convertir la chaîne de caractères en tableau de bytes
        byte[] decodedKey = jwtSecret.getBytes(StandardCharsets.UTF_8);

        // Générer une clé HMAC-SHA compatible avec JJWT
        return Keys.hmacShaKeyFor(decodedKey);
    }

    /**
     * Générer un Access Token à partir d'une Authentication Spring Security
     *
     * Processus :
     * 1. Récupère les détails utilisateur de l'objet Authentication
     * 2. Extrait le username (email dans notre cas)
     * 3. Appelle generateAccessToken(email) pour créer le token
     *
     * Cette méthode est généralement appelée après une connexion réussie
     *
     * @param authentication Objet contenant les infos de l'utilisateur authentifié
     * @return Le token d'accès JWT généré
     */
    public String generateAccessToken(Authentication authentication) {
        // Récupère l'objet UserDetails depuis le principal de l'authentification
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Extrait le username (qui est l'email) et génère le token
        return generateAccessToken(userDetails.getUsername());
    }

    /**
     * Générer un Access Token à partir d'un email utilisateur
     *
     * Processus :
     * 1. Obtient la date/heure actuelle
     * 2. Calcule la date d'expiration (maintenant + durée)
     * 3. Crée un JWT avec :
     *    - subject (sujet) = email utilisateur
     *    - issuedAt = date d'émission
     *    - expiration = date d'expiration
     *    - Signe avec la clé secrète et algo HS512
     * 4. Retourne le token compact (base64)
     *
     * Le token généré peut être décodé et lu par le client, mais pas modifié
     * (car la signature seraient invalide)
     *
     * @param email L'email de l'utilisateur
     * @return Le token d'accès JWT signé
     */
    public String generateAccessToken(String email) {
        // Enregistre l'action dans les logs
        Date now = new Date();

        // Calcule la date d'expiration (maintenant + durée du token)
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        // Construit et retourne le token
        return Jwts.builder()
                // Subject = identifiant unique de l'utilisateur (email)
                .subject(email)
                // Date d'émission du token
                .issuedAt(now)
                // Date d'expiration du token
                .expiration(expiryDate)
                // Signe le token avec la clé secrète et algorithme HS512
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                // Convertit en chaîne base64 compacte
                .compact();
    }

    /**
     * Générer un Refresh Token
     *
     * Similar au access token mais :
     * - Durée de vie plus longue (7 jours par défaut)
     * - Contient un claim additionnel "type": "refresh" pour l'identifier
     * - Utilisé UNIQUEMENT pour obtenir un nouveau access token
     *
     * Processus d'utilisation :
     * 1. Access token expire après 1 heure
     * 2. Client utilise le refresh token pour GET un nouveau access token
     * 3. Serveur valide le refresh token et génère un nouveau access token
     * 4. Le refresh token peut être réutilisé jusqu'à expiration (7 jours)
     *
     * @param email L'email de l'utilisateur
     * @return Le token de rafraîchissement JWT signé
     */
    public String generateRefreshToken(String email) {
        Date now = new Date();

        // Calcule la date d'expiration avec la durée du refresh token
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .subject(email)
                // Ajoute une donnée custom ("claim") pour identifier le type de token
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Extraire l'email (username) du token JWT
     *
     * Processus :
     * 1. Récupère tous les claims (données) du token
     * 2. Extrait le subject (sujet) qui est l'email
     *
     * IMPORTANT: Cette méthode NE VALIDE PAS le token
     * Elle l'utilise seulement après validation réussie
     *
     * @param token Le token JWT
     * @return L'email extrait du token
     */
    public String getEmailFromToken(String token) {
        // Récupère tous les claims du token et extrait le subject
        return getAllClaimsFromToken(token).getSubject();
    }

    /**
     * Vérifier si le token JWT est expiré
     *
     * Processus :
     * 1. Récupère les claims du token
     * 2. Extrait la date d'expiration
     * 3. Compare avec la date actuelle
     * 4. Retourne true si expiré, false sinon
     *
     * En cas d'exception (token invalide, mal formé, etc), considère le token comme expiré
     *
     * @param token Le token JWT à vérifier
     * @return true si le token est expiré, false sinon
     */
    public boolean isTokenExpired(String token) {
        try {
            // Récupère tous les claims
            Date expiration = getAllClaimsFromToken(token).getExpiration();

            // Compare la date d'expiration avec maintenant
            // before() retourne true si expiration < maintenant (= token expiré)
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            // Catch la exception si le token est expiré
            log.debug("Token JWT expiré: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Valider le token JWT complet
     *
     * Cette méthode effectue une validation complète du token :
     * 1. Vérifie la signature (que le token n'a pas été modifié)
     * 2. Vérifie l'expiration
     * 3. Vérifie la structure générale du token
     *
     * Retourne true UNIQUEMENT si le token est valide ET non expiré
     *
     * Gestion des erreurs :
     * - Signature invalide → log et false
     * - Token mal formé → log et false
     * - Token expiré → log et false
     * - Algo non supporté → log et false
     * - Claims vides → log et false
     *
     * @param token Le token JWT à valider
     * @return true si le token est valide et non expiré, false sinon
     */
    public boolean validateToken(String token) {
        try {
            // Crée un parser (lecteur) JWT
            Jwts.parser()
                    // Configure la clé pour vérifier la signature
                    .verifyWith(getSigningKey())
                    .build()
                    // Parse et valide le token
                    // Lève une exception si la signature est invalide
                    .parseSignedClaims(token);

            // Si on arrive ici, la signature est valide
            // Maintenant vérifier l'expiration
            return !isTokenExpired(token);

        } catch (SecurityException ex) {
            // La signature du token n'est pas valide (token modifié ou mauvaise clé)
            log.error("Signature JWT invalide: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            // Le format du token est incorrect (pas un JWT valide)
            log.error("Token JWT non valide: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            // Le token a expiré
            log.error("Token JWT expiré: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            // L'algorithme de signature n'est pas supporté
            log.error("Token JWT non supporté: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            // Les claims du token sont vides
            log.error("Claims JWT vides: {}", ex.getMessage());
        }

        // Si une exception est catch, le token est invalide
        return false;
    }

    /**
     * Extraire TOUS les claims (données) du token JWT
     *
     * Les "claims" sont les données encodées dans le token.
     * Chaque token contient un body avec :
     * - subject (sujet) = email
     * - issuedAt = date d'émission
     * - expiration = date d'expiration
     * - Et éventuellement d'autres données custom
     *
     * IMPORTANT: Cette méthode NE VALIDE PAS le token
     * Elle doit être utilisée APRÈS une validation réussie
     *
     * Cette méthode est privée car elle est utilisée en interne
     * par les autres méthodes du service
     *
     * @param token Le token JWT
     * @return Un objet Claims contenant toutes les données du token
     */
    private Claims getAllClaimsFromToken(String token) {
        // Crée un parser JWT
        return Jwts.parser()
                // Configure la clé pour vérifier la signature
                .verifyWith(getSigningKey())
                .build()
                // Parse le token (valide la signature)
                // Lève une exception si la signature est invalide
                .parseSignedClaims(token)
                // Récupère le body (les données) du token
                .getPayload();
    }

    /**
     * Obtenir le temps d'expiration du token en millisecondes
     *
     * Cette valeur peut être utilisée côté client pour :
     * - Calculer quand le token expirera
     * - Planifier un rafraîchissement automatique
     *
     * @param token Le token JWT
     * @return La durée de validité du token en millisecondes (3600000 = 1h)
     */
    public Long getExpirationTime(String token) {
        try {
            // Retourne la durée configurée dans le fichier properties
            return jwtExpiration;
        } catch (Exception e) {
            // En cas d'erreur, enregistre et retourne null
            log.error("Erreur lors de la récupération du temps d'expiration: {}", e.getMessage());
            return null;
        }
    }
}
