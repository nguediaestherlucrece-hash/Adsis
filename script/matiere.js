// Données initiales (reprises de l'image)
let matieres = [
    { id: 1, code: 'MATH', nom: 'Mathématiques', heures: 6, parentId: null },
    { id: 2, code: 'FR', nom: 'Français', heures: 5, parentId: 1 },
    { id: 3, code: 'SVP', nom: 'Sciences Physiques', heures: 4, parentId: null },
    { id: 4, code: 'SVT', nom: 'SVT', heures: 3, parentId: 3 },
    { id: 5, code: 'PHY', nom: 'Sciences Physiques', heures: 4, parentId: null },
    { id: 6, code: 'EPS', nom: 'Éducation Physique', heures: 2, parentId: 5 },
    { id: 7, code: 'ANG', nom: 'Anglais', heures: 3, parentId: null },
    { id: 8, code: 'ART', nom: 'Arts Plastiques', heures: 2, parentId: 7 }
];

let nextId = 9;

// Fonction pour mettre à jour le select des parents
function updateParentSelect() {
    const select = document.getElementById('parentSelect');
    select.innerHTML = '<option value="">-- Aucun parent (matière principale) --</option>';
    matieres.forEach(m => {
        const option = document.createElement('option');
        option.value = m.id;
        option.textContent = `${m.code} - ${m.nom}`;
        select.appendChild(option);
    });
}

// Fonction pour afficher les matières en arbre
function renderMatieres() {
    const container = document.getElementById('matieresContainer');
    const totalSpan = document.getElementById('totalMatieres');
    const totalFooter = document.getElementById('totalMatieresFooter');
    const totalHeuresSpan = document.getElementById('totalHeures');
    const moyenneSpan = document.getElementById('moyenneHeures');

    // Construire l'arbre
    const racines = matieres.filter(m => m.parentId === null);
    const totalHeures = matieres.reduce((acc, m) => acc + m.heures, 0);
    const moyenne = (totalHeures / matieres.length).toFixed(1);

    totalSpan.textContent = matieres.length;
    totalFooter.textContent = matieres.length;
    totalHeuresSpan.textContent = totalHeures + 'h';
    moyenneSpan.textContent = moyenne + 'h';

    // Fonction récursive pour générer le HTML
    function renderItem(matiere, niveau = 0) {
        const enfants = matieres.filter(m => m.parentId === matiere.id);
        let html = `
            <div class="matiere-item d-flex align-items-center ${niveau > 0 ? 'child-item' : ''}" style="margin-left: ${niveau * 2}rem;">
                <span class="matiere-code">${matiere.code}</span>
                <span class="matiere-nom">${matiere.nom}</span>
                <span class="matiere-heures">${matiere.heures}h / semaine</span>
                <button class="btn btn-sm btn-outline-danger ms-2" onclick="supprimerMatiere(${matiere.id})"><i class="fas fa-trash"></i></button>
            </div>
        `;
        enfants.forEach(enfant => {
            html += renderItem(enfant, niveau + 1);
        });
        return html;
    }

    let htmlString = '';
    racines.forEach(racine => {
        htmlString += renderItem(racine);
    });
    container.innerHTML = htmlString;
}

// Fonction pour ajouter une matière
function ajouterMatiere() {
    const code = document.getElementById('codeInput').value.trim();
    const nom = document.getElementById('nomInput').value.trim();
    const heures = parseFloat(document.getElementById('heuresInput').value);
    const parentId = document.getElementById('parentSelect').value ? parseInt(document.getElementById('parentSelect').value) : null;

    if (!code || !nom || isNaN(heures) || heures <= 0) {
        alert('Veuillez remplir tous les champs correctement.');
        return;
    }

    const nouvelle = {
        id: nextId++,
        code: code,
        nom: nom,
        heures: heures,
        parentId: parentId
    };
    matieres.push(nouvelle);
    updateParentSelect();
    renderMatieres();

    // Réinitialiser le formulaire
    document.getElementById('codeInput').value = '';
    document.getElementById('nomInput').value = '';
    document.getElementById('heuresInput').value = '';
    document.getElementById('parentSelect').value = '';
}

// Fonction pour supprimer une matière (et ses enfants)
function supprimerMatiere(id) {
    // Supprimer récursivement les enfants
    const enfants = matieres.filter(m => m.parentId === id).map(m => m.id);
    enfants.forEach(enfantId => supprimerMatiere(enfantId));
    matieres = matieres.filter(m => m.id !== id);
    updateParentSelect();
    renderMatieres();
}

// Initialisation
document.addEventListener('DOMContentLoaded', () => {
    updateParentSelect();
    renderMatieres();
    document.getElementById('ajouterBtn').addEventListener('click', ajouterMatiere);

    // Rendre la fonction supprimer accessible globalement
    window.supprimerMatiere = supprimerMatiere;
});