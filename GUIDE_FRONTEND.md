# 📖 Guide du Développeur Frontend — ADSIS

> **Ce guide explique comment coder sur le frontend du projet ADSIS après la migration vers une architecture monolithique Spring Boot + Thymeleaf.**

---

## 🏗️ Architecture Actuelle

Le projet est désormais un **monolithe** : le backend (API REST) et le frontend (HTML/CSS/JS) vivent dans le même projet Spring Boot.

```
first_projet_springboot/
└── src/main/
    ├── java/                          ← Backend (Java)
    │   └── .../config/web/
    │       ├── AdminPageController     ← Sert les pages admin
    │       ├── AssistantPageController ← Sert les pages assistant
    │       ├── EnseignantPageController← Sert les pages enseignant
    │       └── SharedPageController    ← Sert les pages partagées
    │
    └── resources/
        ├── templates/                 ← 📝 VOS FICHIERS HTML (Thymeleaf)
        │   ├── fragments/             ← Composants partagés (sidebar, topbar, head)
        │   │   ├── bootstrap-layout.html   ← Admin + Assistant
        │   │   └── enseignant-layout.html  ← Enseignant
        │   ├── admin/                 ← Pages admin
        │   ├── assistant/             ← Pages assistant
        │   ├── enseignant/            ← Pages enseignant
        │   └── shared/                ← Pages partagées (profil)
        │
        └── static/                    ← 📝 VOS FICHIERS CSS / JS / IMAGES
            ├── css/                   ← Feuilles de style
            ├── js/                    ← Scripts JavaScript
            ├── assets/                ← Images (logo, etc.)
            ├── icons/                 ← Icônes PWA
            ├── manifest.json          ← Configuration PWA
            └── sw.js                  ← Service Worker PWA
```

---

## 🚀 Comment Lancer le Projet

### Prérequis
- **Java 17+** installé
- **Maven** installé (ou utiliser le wrapper `./mvnw`)
- **MySQL** en cours d'exécution avec la base `appel_classe_db`

### Démarrer

```bash
cd first_projet_springboot
mvn spring-boot:run
```

L'application est accessible sur : **http://localhost:8081**

### URLs principales

| Module | URL |
|--------|-----|
| Login | http://localhost:8081/enseignant/login |
| Dashboard Enseignant | http://localhost:8081/enseignant/dashboard |
| Emploi Enseignant | http://localhost:8081/enseignant/emploi |
| Appel | http://localhost:8081/enseignant/appel |
| Dashboard Admin | http://localhost:8081/admin/tableaudebord |
| Gestion Staff | http://localhost:8081/admin/gestionutilisateur |
| Gestion Accès | http://localhost:8081/admin/gestionacces |
| Cycles & Filières | http://localhost:8081/admin/cycles |
| Dashboard Assistant | http://localhost:8081/assistant/tableaudebord |
| Matières | http://localhost:8081/assistant/matiere |
| Étudiants | http://localhost:8081/assistant/gestionetudiant |
| Emploi du temps | http://localhost:8081/assistant/emploidutemps |
| Profil | http://localhost:8081/shared/profil |

> ⚠️ **On ne peut plus ouvrir les fichiers `.html` directement dans le navigateur.** Il faut toujours passer par `http://localhost:8081/...`

---

## ✏️ Comment Modifier une Page

### 1. Trouver le bon fichier

Exemple : tu veux modifier la page **Gestion des matières** de l'assistant.

1. L'URL est `/assistant/matiere`
2. Le contrôleur est dans `AssistantPageController.java` → il retourne `"assistant/matiere"`
3. Le fichier HTML est donc : `templates/assistant/matiere.html`

### 2. Structure d'un fichier HTML template

Voici la structure type d'un template :

```html
<!DOCTYPE html>
<html lang="fr" xmlns:th="http://www.thymeleaf.org">

<!-- ① Le <head> est généré automatiquement par le fragment -->
<head th:replace="~{fragments/bootstrap-layout :: head('Titre de la page', 'nom-du-css.css')}"></head>

<body>
    <div class="container-fluid">
        <div class="row">

            <!-- ② La sidebar est générée automatiquement par le fragment -->
            <div th:replace="~{fragments/bootstrap-layout :: sidebar-assistant('matiere')}"></div>

            <!-- ③ TON CONTENU SPÉCIFIQUE ICI -->
            <div class="col-lg-10 col-md-9 col-sm-12 p-3 p-md-4 main-content">
                <h5>Ma page</h5>
                <!-- ... ton HTML ... -->
            </div>
        </div>
    </div>

    <!-- ④ Tes scripts JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    <script type="module" th:src="@{/js/mon-script.js}"></script>
</body>
</html>
```

### 3. Ce que tu dois savoir sur Thymeleaf

**Tu n'as besoin de connaître que 3 choses :**

| Syntaxe | Ce que ça fait | Exemple |
|---------|---------------|---------|
| `th:replace="~{...}"` | Remplace l'élément par un fragment | `th:replace="~{fragments/bootstrap-layout :: sidebar-admin('staff')}"` |
| `th:src="@{/chemin}"` | Génère le chemin vers un fichier statique | `th:src="@{/js/admin.js}"` |
| `th:href="@{/chemin}"` | Génère le chemin vers un fichier CSS | `th:href="@{/css/tableaudebord.css}"` |

**C'est tout.** Thymeleaf ne sert qu'à assembler les morceaux de HTML. Aucune logique métier ne passe par Thymeleaf.

> 💡 **Toutes les données viennent toujours de vos fichiers JavaScript via `fetch('/api/...')`**. Rien ne change de ce côté.

---

## 📁 Où Mettre Mes Fichiers

| Type de fichier | Emplacement | Exemple |
|-----------------|------------|---------|
| Page HTML | `templates/{module}/` | `templates/admin/ma-page.html` |
| Fichier CSS | `static/css/` | `static/css/ma-page.css` |
| Fichier JS | `static/js/` | `static/js/ma-page.js` |
| Image | `static/assets/` | `static/assets/mon-image.png` |

### Comment référencer dans le HTML

```html
<!-- CSS → dans le fragment head (paramètre pageCss) -->
<head th:replace="~{fragments/bootstrap-layout :: head('Ma Page', 'ma-page.css')}"></head>

<!-- JS → avec th:src -->
<script type="module" th:src="@{/js/ma-page.js}"></script>

<!-- Image → chemin absolu depuis static/ -->
<img src="/assets/image.png" alt="Logo">
```

> ⚠️ **Ne jamais utiliser de chemins relatifs** comme `../style/` ou `../script/`. Toujours utiliser des chemins absolus depuis la racine (`/css/`, `/js/`, `/assets/`).

---

## 🧩 Les Fragments (Composants Partagés)

Les fragments sont dans `templates/fragments/`. Ils contiennent les éléments partagés entre toutes les pages d'un module.

### Pour les modules Admin et Assistant : `bootstrap-layout.html`

| Fragment | Usage | Paramètres |
|----------|-------|------------|
| `head(pageTitle, pageCss)` | Génère le `<head>` avec Bootstrap + ton CSS | `pageTitle` = titre affiché, `pageCss` = nom du fichier CSS |
| `sidebar-admin(activePage)` | Sidebar admin avec le lien actif | `'tableaudebord'`, `'staff'`, `'acces'`, `'cycles'` |
| `sidebar-assistant(activePage)` | Sidebar assistant avec le lien actif | `'tableaudebord'`, `'matiere'`, `'etudiant'`, `'emploi'` |
| `topbar-admin(pageTitle)` | Topbar admin | Titre de la page |

### Pour le module Enseignant : `enseignant-layout.html`

| Fragment | Usage | Paramètres |
|----------|-------|------------|
| `head(pageTitle, pageCss)` | Génère le `<head>` avec manifest PWA + ton CSS | `pageTitle` = titre, `pageCss` = nom du CSS |
| `sidebar(activePage)` | Sidebar enseignant | `'dashboard'`, `'emploi'`, `'appel'`, `'profil'` |
| `topbar(pageTitle)` | Topbar enseignant | Titre de la page |

### Modifier la sidebar

Si tu veux **ajouter un lien** à la sidebar admin, modifie **uniquement** le fichier :
```
templates/fragments/bootstrap-layout.html
```
Le changement sera automatiquement appliqué à **toutes les pages admin**.

---

## 📄 Comment Créer une Nouvelle Page

### Étape 1 : Créer le template HTML

Créer `templates/assistant/ma-nouvelle-page.html` :

```html
<!DOCTYPE html>
<html lang="fr" xmlns:th="http://www.thymeleaf.org">

<head th:replace="~{fragments/bootstrap-layout :: head('Ma Nouvelle Page', 'ma-nouvelle-page.css')}"></head>

<body>
    <div class="container-fluid">
        <div class="row">
            <div th:replace="~{fragments/bootstrap-layout :: sidebar-assistant('ma-page')}"></div>

            <div class="col-lg-10 col-md-9 col-sm-12 p-3 p-md-4 main-content">
                <h5>Ma Nouvelle Page</h5>
                <div id="mon-contenu"></div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    <script type="module" th:src="@{/js/ma-nouvelle-page.js}"></script>
</body>
</html>
```

### Étape 2 : Créer le CSS et le JS

- `static/css/ma-nouvelle-page.css`
- `static/js/ma-nouvelle-page.js`

### Étape 3 : Ajouter la route dans le contrôleur

Ouvrir `AssistantPageController.java` et ajouter :

```java
@GetMapping("/ma-nouvelle-page")
public String maNouvellePage() { return "assistant/ma-nouvelle-page"; }
```

### Étape 4 : Autoriser la route (si nécessaire)

Si la page doit être accessible sans authentification, ajouter la route dans `SecurityConfig.java` :

```java
.requestMatchers("/assistant/ma-nouvelle-page").permitAll()
```

### Étape 5 : Ajouter le lien dans la sidebar (optionnel)

Modifier `templates/fragments/bootstrap-layout.html`, section `sidebar-assistant` :

```html
<a class="nav-link" th:classappend="${activePage == 'ma-page'} ? ' active'"
   href="/assistant/ma-nouvelle-page"><i class="bi bi-star"></i>Ma Page</a>
```

---

## 🔌 Comment Appeler les API Backend

**Rien ne change !** Vos fichiers JS appellent toujours les API REST de la même manière :

```javascript
// Exemple dans static/js/ma-nouvelle-page.js
const token = localStorage.getItem('adsis_token');

const response = await fetch('http://localhost:8081/api/mon-endpoint', {
    headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
    }
});

const data = await response.json();
// Utiliser data pour remplir le DOM...
```

> 💡 Le fichier `static/js/auth.js` gère déjà l'authentification JWT et le refresh token pour vous. Importez-le si nécessaire.

---

## 📱 PWA (Module Enseignant uniquement)

Le module enseignant dispose d'un mode hors-ligne (PWA). Les fichiers concernés :

| Fichier | Rôle |
|---------|------|
| `static/manifest.json` | Config PWA (nom, icônes, couleurs) |
| `static/sw.js` | Service Worker (cache offline) |
| `static/js/pwa.js` | Enregistrement du SW + bannière offline |

Les templates enseignant incluent automatiquement `pwa.js` via :
```html
<script th:src="@{/js/pwa.js}"></script>
```

Si tu ajoutes une page enseignant, **ajoute cette ligne** avant `</body>`.

---

## ⚠️ Erreurs Courantes

### ❌ Ouvrir un fichier HTML directement dans le navigateur
Les fichiers dans `templates/` contiennent du Thymeleaf (`th:replace`, `th:src`). Le navigateur ne comprend pas cette syntaxe. Il faut **toujours** passer par `http://localhost:8081/...`.

### ❌ Utiliser des chemins relatifs (`../style/`, `../script/`)
Toujours utiliser des chemins absolus : `/css/`, `/js/`, `/assets/`.

### ❌ Oublier `}` dans `th:src`
```html
<!-- ❌ FAUX -->
<script th:src="@{/js/mon-script.js"></script>

<!-- ✅ CORRECT -->
<script th:src="@{/js/mon-script.js}"></script>
```

### ❌ Modifier la sidebar dans chaque page
La sidebar est dans `fragments/bootstrap-layout.html` ou `fragments/enseignant-layout.html`. **Modifie-la une seule fois**, elle sera mise à jour partout.

### ❌ Mettre de la logique métier dans le HTML
Thymeleaf ne sert qu'au layout (assemblage de fragments). **Toutes les données** doivent venir du JavaScript via les API REST.

---

## 📋 Résumé en 30 Secondes

1. **Spring Boot doit tourner** → `mvn spring-boot:run`
2. **HTML** → `templates/{module}/page.html`
3. **CSS** → `static/css/page.css`
4. **JS** → `static/js/page.js`
5. **Sidebar/Topbar** → `templates/fragments/` (1 seul fichier par design system)
6. **Nouvelle route** → Ajouter `@GetMapping` dans le bon `PageController`
7. **Données** → Toujours via `fetch('/api/...')` dans le JS
