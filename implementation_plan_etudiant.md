# Plan de Révision et d'Implémentation — Version Finale

## Contexte

Plusieurs fonctionnalités frontend ont été mal implémentées ou sont incomplètes. Ce plan couvre **3 chantiers majeurs** avec des **modifications backend** pour le chantier 1.

### Décisions validées

| Question | Décision |
|----------|----------|
| Champ crédit | **Nouveau champ `credit`** ajouté au modèle backend |
| Filtres étudiants | **Cascading** : Niveau → Spécialité → Classe |
| Modification de séance | **Oui**, on peut changer la matière |
| Suppression/modification groupée | **Oui**, toutes les séances du `groupId` sont impactées |

---

## Chantier 1 : Page Matières — Crédit & Taux Horaire

### 1.1 Modifications Backend (nouveau champ `credit`)

#### [MODIFY] [CoursUE.java](file:///c:/Users/yohan/Documents/inge3_ISI_FR/projet%20tutore/code/25_26_ING_3_ISI_FR_GROUPE6/first_projet_springboot/src/main/java/com/ISI/Appel_classe/Gestion_des_emplois_du_temps/model/CoursUE.java)
- Ajouter le champ `private Integer credit;` après `volumeHoraire` (ligne 30)

#### [MODIFY] [coursUECreateDTO.java](file:///c:/Users/yohan/Documents/inge3_ISI_FR/projet%20tutore/code/25_26_ING_3_ISI_FR_GROUPE6/first_projet_springboot/src/main/java/com/ISI/Appel_classe/Gestion_des_emplois_du_temps/dto/request/coursUECreateDTO.java)
- Ajouter `private Integer credit;`

#### [MODIFY] [coursUEUpdateDTO.java](file:///c:/Users/yohan/Documents/inge3_ISI_FR/projet%20tutore/code/25_26_ING_3_ISI_FR_GROUPE6/first_projet_springboot/src/main/java/com/ISI/Appel_classe/Gestion_des_emplois_du_temps/dto/request/coursUEUpdateDTO.java)
- Ajouter `private Integer credit;`

#### [MODIFY] [CoursUEDTO.java](file:///c:/Users/yohan/Documents/inge3_ISI_FR/projet%20tutore/code/25_26_ING_3_ISI_FR_GROUPE6/first_projet_springboot/src/main/java/com/ISI/Appel_classe/Gestion_des_emplois_du_temps/dto/response/CoursUEDTO.java)
- Ajouter `private Integer credit;`

#### [MODIFY] [CoursUEMapper.java](file:///c:/Users/yohan/Documents/inge3_ISI_FR/projet%20tutore/code/25_26_ING_3_ISI_FR_GROUPE6/first_projet_springboot/src/main/java/com/ISI/Appel_classe/Gestion_des_emplois_du_temps/mappers/CoursUEMapper.java)
- Ajouter `dto.setCredit(cours.getCredit());` dans la méthode `toDTO()` (après ligne 21)

#### [MODIFY] [CoursUEServiceImpl.java](file:///c:/Users/yohan/Documents/inge3_ISI_FR/projet%20tutore/code/25_26_ING_3_ISI_FR_GROUPE6/first_projet_springboot/src/main/java/com/ISI/Appel_classe/Gestion_des_emplois_du_temps/service/impl/CoursUEServiceImpl.java)
- Dans `createCours()` (ligne 76) : ajouter `cours.setCredit(dto.getCredit());`
- Dans `updateCours()` (ligne 106) : ajouter `cours.setCredit(dto.getCredit());`

### 1.2 Modifications Frontend

#### [MODIFY] [matiere.html](file:///c:/Users/yohan/Documents/inge3_ISI_FR/projet%20tutore/code/25_26_ING_3_ISI_FR_GROUPE6/assistant/matiere.html)

Ajouter un champ `Crédit` dans le modal de création/modification (entre `Intitulé` et `Spécialité`, après ligne 75) :
```html
<div class="mb-3">
    <label class="form-label fw-semibold">Crédit</label>
    <input type="number" class="form-control" id="creditInput" min="1" max="30" placeholder="Nombre de crédits">
    <small class="text-muted">Taux horaire calculé : <span id="tauxHoraireDisplay">0</span> h</small>
</div>
```

#### [MODIFY] [matiere.js](file:///c:/Users/yohan/Documents/inge3_ISI_FR/projet%20tutore/code/25_26_ING_3_ISI_FR_GROUPE6/script/matiere.js)

1. **`renderMatieres()`** — Ajouter badges Crédit + Taux horaire dans la vue liste :
   ```js
   const credit = matiere.credit || 0;
   const tauxHoraire = credit * 15;
   // badges : "Crédit: X" + "Taux: Yh"
   ```

2. **`openCreateModal()`** — Reset `creditInput`, `tauxHoraireDisplay`

3. **`openEditModal()`** — Pré-remplir `creditInput` avec `m.credit`, afficher le taux

4. **`saveMatiereBtn` handler** — Lire le crédit et l'envoyer dans le DTO :
   ```js
   dto.credit = parseInt(document.getElementById('creditInput').value) || 0;
   ```

5. **Event listener `creditInput`** — Calcul dynamique du taux horaire en temps réel

---

## Chantier 2 : Page Emploi du Temps — Modal de Configuration

### 2.1 Modifications HTML

#### [MODIFY] [emploidutemps.html](file:///c:/Users/yohan/Documents/inge3_ISI_FR/projet%20tutore/code/25_26_ING_3_ISI_FR_GROUPE6/assistant/emploidutemps.html)

Ajouter un modal `#seanceConfigModal` (après ligne 196) :

```html
<!-- Modal Configuration de Séance -->
<div class="modal fade" id="seanceConfigModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg rounded-4">
            <div class="modal-header bg-primary text-white">
                <h6 class="modal-title fw-bold" id="seanceConfigTitle">
                    <i class="bi bi-gear me-2"></i>Configuration de la séance
                </h6>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <input type="hidden" id="seanceEditId" value="">
                <input type="hidden" id="seanceEditGroupId" value="">
                <input type="hidden" id="seanceCellJour" value="">
                <input type="hidden" id="seanceCellHoraire" value="">
                <!-- Matière (modifiable) -->
                <div class="mb-3">
                    <label class="form-label fw-semibold">Matière</label>
                    <select class="form-select" id="seanceMatiereSelect"></select>
                </div>
                <!-- Enseignants (checkboxes dynamiques) -->
                <div class="mb-3">
                    <label class="form-label fw-semibold">Enseignants de cette matière</label>
                    <div id="seanceEnseignantsList" class="border rounded-3 p-2"
                         style="max-height:200px;overflow-y:auto;">
                        <p class="text-muted small mb-0">Sélectionnez une matière</p>
                    </div>
                </div>
                <!-- Couleur -->
                <div class="mb-3 d-flex align-items-center gap-3">
                    <label class="form-label fw-semibold mb-0">Couleur</label>
                    <input type="color" class="form-control form-control-color" id="seanceCouleurInput" value="#0f22f1">
                </div>
            </div>
            <div class="modal-footer border-0 d-flex justify-content-between">
                <button type="button" class="btn btn-danger" id="seanceDeleteBtn" style="display:none;">
                    <i class="fas fa-trash me-1"></i>Supprimer
                </button>
                <div class="d-flex gap-2">
                    <button type="button" class="btn btn-light" data-bs-dismiss="modal">Annuler</button>
                    <button type="button" class="btn btn-primary" id="seanceSaveBtn">
                        <i class="fas fa-save me-1"></i>Enregistrer
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>
```

### 2.2 Modifications JavaScript

#### [MODIFY] [emploi.js](file:///c:/Users/yohan/Documents/inge3_ISI_FR/projet%20tutore/code/25_26_ING_3_ISI_FR_GROUPE6/script/emploi.js)

**Changements majeurs** :

1. **`handleDropOnCell()` (ligne 780-835)** — Pour `type === 'matiere'` :
   - ❌ Ne plus créer la séance immédiatement
   - ✅ Stocker la cellule cible en mémoire (`pendingDrop = { cell, jourIdx, horaireIdx }`)
   - ✅ Pré-remplir le modal : matière droppée sélectionnée, ses enseignants en checkboxes, couleur du badge
   - ✅ Mettre `seanceEditId` à vide (mode création)
   - ✅ Cacher le bouton Supprimer
   - ✅ Ouvrir le modal

2. **Nouveau : `seanceMatiereSelect` change handler** :
   - Quand la matière change dans le select → recharger dynamiquement les enseignants correspondants
   - Mettre à jour la couleur proposée

3. **Nouveau : `seanceSaveBtn` click handler** :
   - **Mode création** (`seanceEditId` vide) : `POST /api/seances` avec les données du modal
   - **Mode édition** (`seanceEditId` rempli) : `PUT /api/seances/{id}` + mettre à jour toutes les séances du `groupId`
   - Recréer l'élément visuel sur la grille, fermer le modal

4. **Nouveau : `seanceDeleteBtn` click handler** :
   - Confirmation via `showConfirm()`
   - Supprimer toutes les séances du même `groupId` via `DELETE /api/seances/{id}` pour chacune
   - Réinitialiser les cellules du DOM, fermer le modal

5. **Modifier `createCourseElement()` (ligne 955-961)** — Clic gauche → ouvrir le modal en mode édition :
   - Pré-remplir la matière, cocher les enseignants déjà assignés, couleur
   - Mettre `seanceEditId` à l'ID de la séance
   - Récupérer le `groupId` et les `linkedIds`
   - Afficher le bouton Supprimer

6. **Supprimer l'ancienne logique `type === 'enseignant'`** (lignes 836-915) — Plus de drag & drop enseignant

7. **Supprimer le color picker au clic droit** (lignes 963-995) — Géré par le modal

8. **Optionnel : simplifier/masquer le panel "Enseignants"** dans la colonne gauche (lignes 116-122) — Plus utile car les enseignants sont choisis dans le modal

---

## Chantier 3 : Page Gestion Étudiants — Refonte Complète

### 3.1 Refonte HTML

#### [MODIFY] [gestionetudiant.html](file:///c:/Users/yohan/Documents/inge3_ISI_FR/projet%20tutore/code/25_26_ING_3_ISI_FR_GROUPE6/assistant/gestionetudiant.html)

Réécriture du contenu principal pour reproduire le style pro de `gestionutilisateur.html` :

**Structure du contenu principal** :
```
┌─────────────────────────────────────────────────────────┐
│ Topbar : [🔍 Rechercher...] ─── [Assistant Pédagogique] │
├─────────────────────────────────────────────────────────┤
│ Titre: "Gestion des étudiants"                          │
│ Sous-titre + Boutons [+ Ajouter] [📥📤 Import/Export]   │
├─────────────────────────────────────────────────────────┤
│ Filtres cascading:                                       │
│ [Niveau ▾] [Spécialité ▾] [Classe ▾]                   │
│ Badge total: "X étudiant(s)"                            │
├─────────────────────────────────────────────────────────┤
│ Table dans card shadow rounded-4 :                       │
│ | Étudiant | Matricule | Email | Classe | Statut | ⚙️  |│
│ | (avatar) |           |       |        |(badge) |edit/del│
└─────────────────────────────────────────────────────────┘
```

**Modals à refaire/ajouter** :

1. **Modal Création/Modification** (`#etudiantModal`, `modal-lg`) :
   ```
   ┌────────────────────────────────────────┐
   │ [NOM *]          │ [PRÉNOM *]          │
   │ [EMAIL *]        │ [MATRICULE]         │
   │ [DATE NAISSANCE] │ [TÉLÉPHONE]         │
   │ [CLASSE ▾]       │                     │
   └────────────────────────────────────────┘
   ```

2. **Modal Visualisation** (`#viewEtudiantModal`) :
   - Avatar + nom complet + badge rôle
   - Grille de détails : email, matricule, date naissance, téléphone, classe, statut, date création, nombre de présences

3. **Modal Import/Export** (`#importExportModal`) avec tabs :
   - **Tab Import** :
     - Dropdown classe cible (obligatoire)
     - File input (`.xlsx`, `.xls`)
     - Bouton "Télécharger le modèle" → `GET /api/etudiants/template/excel`
     - Bouton "Importer" → `POST /api/etudiants/import/excel`
     - Zone de résultat (succès/erreurs)
   - **Tab Export** :
     - Dropdown classe (ou "Toutes")
     - Bouton "Exporter" → génération côté client avec SheetJS

### 3.2 Refonte CSS

#### [MODIFY] [gestionetudiant.css](file:///c:/Users/yohan/Documents/inge3_ISI_FR/projet%20tutore/code/25_26_ING_3_ISI_FR_GROUPE6/style/gestionetudiant.css)

Ajouter :
- `.topbar` + `.search-box` (repris de `tableaudebord.css`)
- `.filter-tab` + `.filter-tab.active` (repris de `gestionutilisateur.html`)
- `.student-avatar` (circle coloré, initiales)
- `.status-badge` (ACTIF = vert, INACTIF = rouge)
- Table styling : `table.align-middle`, hover sur les lignes
- Responsive amélioré

### 3.3 Réécriture JavaScript complète

#### [MODIFY] [gestionetudiant.js](file:///c:/Users/yohan/Documents/inge3_ISI_FR/projet%20tutore/code/25_26_ING_3_ISI_FR_GROUPE6/script/gestionetudiant.js)

> [!CAUTION]
> **Réécriture quasi-totale**. L'ancien code utilise `localStorage` et ne fait aucun appel API pour le CRUD étudiant. Tout est à refaire.

**Architecture du nouveau code** :

```
1. CONFIG : API_BASE_URL
2. VARIABLES GLOBALES : etudiants[], classes[], specialites[], niveaux[]
3. UTILITAIRES : showToast(), showConfirm(), escapeHtml()
4. API LOADERS :
   - loadNiveaux()     → GET /api/niveaux
   - loadSpecialites() → GET /api/specialites
   - loadClasses()     → GET /api/classes
   - loadEtudiants()   → GET /api/etudiants
5. FILTRES (cascading) :
   - onNiveauChange()     → filtre spécialités par niveau
   - onSpecialiteChange() → filtre classes par spécialité
   - onClasseChange()     → filtre étudiants par classe
   - onSearchInput()      → filtre textuel (nom, prénom, matricule, email)
   - applyAllFilters()    → combine tous les filtres
6. RENDU :
   - populateFilterSelects()
   - renderEtudiantsTable(filteredList)
7. CRUD ÉTUDIANT :
   - openCreateModal()     → vide le modal, mode création
   - openEditModal(id)     → pré-remplit depuis l'API
   - openViewModal(id)     → modal lecture seule
   - saveEtudiant()        → POST ou PUT selon mode
   - deleteEtudiant(id)    → DELETE avec confirmation
8. IMPORT/EXPORT :
   - openImportExportModal()
   - downloadTemplate()    → GET /api/etudiants/template/excel → blob download
   - importExcel()         → POST /api/etudiants/import/excel (FormData)
   - exportExcel()         → SheetJS côté client depuis données API
9. INIT : DOMContentLoaded → charge tout
```

**Détails importants du filtrage cascading** :
```js
// Quand un niveau est sélectionné
function onNiveauChange() {
    const niveauId = filtreNiveau.value;
    // Filtrer les spécialités qui appartiennent à ce niveau
    const filteredSpecs = niveauId ? specialites.filter(s => s.niveauId == niveauId) : specialites;
    populateSelect(filtreSpecialite, filteredSpecs);
    onSpecialiteChange(); // Cascader
}

// Quand une spécialité est sélectionnée
function onSpecialiteChange() {
    const specId = filtreSpecialite.value;
    // Filtrer les classes qui appartiennent à cette spécialité
    const filteredClasses = specId ? classes.filter(c => c.specialiteId == specId) : classes;
    populateSelect(filtreClasse, filteredClasses);
    applyAllFilters(); // Rerendre la table
}
```

---

## Fichiers impactés — Résumé complet

### Backend (6 fichiers)

| Fichier | Action | Description |
|---------|--------|-------------|
| [CoursUE.java](file:///c:/Users/yohan/Documents/inge3_ISI_FR/projet%20tutore/code/25_26_ING_3_ISI_FR_GROUPE6/first_projet_springboot/src/main/java/com/ISI/Appel_classe/Gestion_des_emplois_du_temps/model/CoursUE.java) | MODIFY | Ajouter `private Integer credit;` |
| [coursUECreateDTO.java](file:///c:/Users/yohan/Documents/inge3_ISI_FR/projet%20tutore/code/25_26_ING_3_ISI_FR_GROUPE6/first_projet_springboot/src/main/java/com/ISI/Appel_classe/Gestion_des_emplois_du_temps/dto/request/coursUECreateDTO.java) | MODIFY | Ajouter `private Integer credit;` |
| [coursUEUpdateDTO.java](file:///c:/Users/yohan/Documents/inge3_ISI_FR/projet%20tutore/code/25_26_ING_3_ISI_FR_GROUPE6/first_projet_springboot/src/main/java/com/ISI/Appel_classe/Gestion_des_emplois_du_temps/dto/request/coursUEUpdateDTO.java) | MODIFY | Ajouter `private Integer credit;` |
| [CoursUEDTO.java](file:///c:/Users/yohan/Documents/inge3_ISI_FR/projet%20tutore/code/25_26_ING_3_ISI_FR_GROUPE6/first_projet_springboot/src/main/java/com/ISI/Appel_classe/Gestion_des_emplois_du_temps/dto/response/CoursUEDTO.java) | MODIFY | Ajouter `private Integer credit;` |
| [CoursUEMapper.java](file:///c:/Users/yohan/Documents/inge3_ISI_FR/projet%20tutore/code/25_26_ING_3_ISI_FR_GROUPE6/first_projet_springboot/src/main/java/com/ISI/Appel_classe/Gestion_des_emplois_du_temps/mappers/CoursUEMapper.java) | MODIFY | Mapper le champ `credit` |
| [CoursUEServiceImpl.java](file:///c:/Users/yohan/Documents/inge3_ISI_FR/projet%20tutore/code/25_26_ING_3_ISI_FR_GROUPE6/first_projet_springboot/src/main/java/com/ISI/Appel_classe/Gestion_des_emplois_du_temps/service/impl/CoursUEServiceImpl.java) | MODIFY | Setter credit dans create/update |

### Frontend (7 fichiers)

| Fichier | Action | Description |
|---------|--------|-------------|
| [matiere.html](file:///c:/Users/yohan/Documents/inge3_ISI_FR/projet%20tutore/code/25_26_ING_3_ISI_FR_GROUPE6/assistant/matiere.html) | MODIFY | Ajout champ crédit dans modal |
| [matiere.js](file:///c:/Users/yohan/Documents/inge3_ISI_FR/projet%20tutore/code/25_26_ING_3_ISI_FR_GROUPE6/script/matiere.js) | MODIFY | Affichage crédit/taux + lecture/écriture |
| [emploidutemps.html](file:///c:/Users/yohan/Documents/inge3_ISI_FR/projet%20tutore/code/25_26_ING_3_ISI_FR_GROUPE6/assistant/emploidutemps.html) | MODIFY | Ajout modal configuration séance |
| [emploi.js](file:///c:/Users/yohan/Documents/inge3_ISI_FR/projet%20tutore/code/25_26_ING_3_ISI_FR_GROUPE6/script/emploi.js) | MODIFY | Refonte logique drag & drop + modal |
| [gestionetudiant.html](file:///c:/Users/yohan/Documents/inge3_ISI_FR/projet%20tutore/code/25_26_ING_3_ISI_FR_GROUPE6/assistant/gestionetudiant.html) | MODIFY | Refonte UI complète |
| [gestionetudiant.js](file:///c:/Users/yohan/Documents/inge3_ISI_FR/projet%20tutore/code/25_26_ING_3_ISI_FR_GROUPE6/script/gestionetudiant.js) | MODIFY | Réécriture complète (API + filtres + import/export) |
| [gestionetudiant.css](file:///c:/Users/yohan/Documents/inge3_ISI_FR/projet%20tutore/code/25_26_ING_3_ISI_FR_GROUPE6/style/gestionetudiant.css) | MODIFY | Ajout styles table/filtres/avatar |

---

## Ordre d'exécution

1. **Chantier 1 — Backend** : Ajouter le champ `credit` (6 fichiers Java)
2. **Chantier 1 — Frontend** : Matières HTML + JS (crédit/taux horaire)
3. **Chantier 2** : Emploi du temps HTML + JS (modal de config)
4. **Chantier 3** : Gestion Étudiants CSS + HTML + JS (refonte complète)

---

## Verification Plan

### Chantier 1
- Créer une matière avec crédit = 3 → vérifier `credit=3` en base + `taux = 45h` affiché
- Modifier le crédit → vérifier la mise à jour
- Vérifier calcul temps réel dans le modal

### Chantier 2
- Drop matière sur grille → modal s'ouvre (pas de création immédiate)
- Sélectionner enseignants + couleur → Enregistrer → séance visible sur grille
- Changer matière dans le modal → enseignants rechargés dynamiquement
- Cliquer séance existante → modal édition avec valeurs pré-remplies
- Supprimer séance groupée → toutes les séances du groupId disparaissent
- Modifier matière/enseignants d'une séance groupée → toutes les séances du groupe sont MAJ

### Chantier 3
- Page charge étudiants depuis API (plus de localStorage)
- Filtres cascading : Niveau filtre Spécialité filtre Classe filtre liste
- Recherche textuelle fonctionne
- Création étudiant (tous champs) → `POST /api/etudiants` → apparaît dans la table
- Modification → `PUT /api/etudiants/{id}` → données mises à jour
- Vue détaillée → tous les champs affichés
- Import Excel → modal avec classe cible → `POST /api/etudiants/import/excel` → résultat affiché
- Export Excel → télécharge un .xlsx avec les données filtrées
- Télécharger modèle → `GET /api/etudiants/template/excel` → fichier téléchargé
