// ==================== DONNÉES ====================
const matieres = [
    { id: 1, nom: "Algorithmique", couleur: "#0f22f1", bgClass: "bg-matiere-1" },
    { id: 2, nom: "Bases de données", couleur: "#2d6a4f", bgClass: "bg-matiere-2" },
    { id: 3, nom: "Réseaux", couleur: "#eb1919", bgClass: "bg-matiere-3" },
    { id: 4, nom: "Systèmes d'exploitation", couleur: "#ab1be4", bgClass: "bg-matiere-4" },
    { id: 5, nom: "Programmation Web", couleur: "#f6940a", bgClass: "bg-matiere-5" },
    { id: 6, nom: "Anglais", couleur: "#1093fe", bgClass: "bg-matiere-6" },
    { id: 7, nom: "Math Discrètes", couleur: "#f318aa", bgClass: "bg-matiere-7" }
];

const enseignants = [
    { id: 101, nom: "Dr. Martin",   couleur: "#b15e5e", matieres: [1, 3, 5] }, // Algo, Réseaux, Web
    { id: 102, nom: "Prof. Dubois", couleur: "#b15e5e", matieres: [2, 4] },    // BD, Systèmes
    { id: 103, nom: "Mme. Lambert", couleur: "#b15e5e", matieres: [6] },       // Anglais
    { id: 104, nom: "M. Chevalier", couleur: "#b15e5e", matieres: [7] },       // Maths
    { id: 105, nom: "Pr. Bernard",  couleur: "#b15e5e", matieres: [1, 2] }     // Algo et BD
];

const evenements = [
    { id: 201, nom: "Pause déjeuner",    couleur: "#d97706", bgClass: "bg-evenement" },
    { id: 202, nom: "Journée culturelle", couleur: "#059669", bgClass: "bg-evenement" },
    { id: 203, nom: "Conférence",        couleur: "#7c3aed", bgClass: "bg-evenement" },
    { id: 204, nom: "Atelier",           couleur: "#b45309", bgClass: "bg-evenement" }
];

let coursesList = [];            // badges disponibles
let resizeState = {
    active: false, type: null, startCell: null, startElement: null,
    startJour: 0, startHoraire: 0, startRowspan: 1, startColspan: 1, currentCell: null
};

// Créneaux horaires
const horaires = [
    "08:00 - 09:00", "09:00 - 10:00", "10:00 - 11:00",
    "11:00 - 12:00", "12:00 - 13:00", "13:00 - 14:00", "14:00 - 15:00",
    "15:00 - 16:00", "16:00 - 17:00"
];

// ==================== INITIALISATION TABLEAU ====================
const tbody = document.getElementById('timetableBody');
const classeDisplay = document.getElementById('classeDisplay');
const classeSelect = document.getElementById('classeSelect');

function renderTable() {
    tbody.innerHTML = '';
    horaires.forEach((h, idx) => {
        const tr = document.createElement('tr');
        tr.innerHTML = `<td class="fw-semibold text-secondary">${h}</td>`;
        for (let j = 0; j < 6; j++) {
            const td = document.createElement('td');
            td.dataset.horaire = idx;
            td.dataset.jour = j;
            td.classList.add('dropzone');
            tr.appendChild(td);
        }
        tbody.appendChild(tr);
    });
}
renderTable();

// ==================== SÉLECTEURS ====================
const matiereSelect = document.getElementById('matiereSelect');
const enseignantSelect = document.getElementById('enseignantSelect');
const evenementSelect = document.getElementById('evenementSelect');

// Remplir matières
matieres.forEach(m => {
    const opt = document.createElement('option');
    opt.value = m.id;
    opt.textContent = m.nom;
    opt.dataset.couleur = m.couleur;
    opt.dataset.bgClass = m.bgClass;
    matiereSelect.appendChild(opt);
});

// Remplir événements
evenements.forEach(e => {
    const opt = document.createElement('option');
    opt.value = e.id;
    opt.textContent = e.nom;
    opt.dataset.couleur = e.couleur;
    opt.dataset.bgClass = e.bgClass;
    evenementSelect.appendChild(opt);
});

// Fonction de filtrage des enseignants selon la matière sélectionnée
function filterEnseignantsByMatiere(matiereId) {
    enseignantSelect.innerHTML = ''; // vider
    if (!matiereId) return;
    const filtered = enseignants.filter(e => e.matieres.includes(parseInt(matiereId)));
    filtered.forEach(e => {
        const opt = document.createElement('option');
        opt.value = e.id;
        opt.textContent = e.nom;
        opt.dataset.couleur = e.couleur;
        opt.dataset.matiereId = matiereId; // on stocke la matière associée
        enseignantSelect.appendChild(opt);
    });
}

// Filtre initial
if (matiereSelect.options.length > 0) {
    filterEnseignantsByMatiere(matiereSelect.value);
}

// Mise à jour du filtre à chaque changement de matière
matiereSelect.addEventListener('change', function() {
    filterEnseignantsByMatiere(this.value);
});

// ==================== BOUTONS D'AJOUT ====================
document.getElementById('addMatiereBtn').addEventListener('click', () => {
    const opt = matiereSelect.options[matiereSelect.selectedIndex];
    const matiere = {
        nom: opt.textContent,
        type: 'matiere',
        couleur: opt.dataset.couleur,
        bgClass: opt.dataset.bgClass,
        id: opt.value,
        matiereId: opt.value   // pour identification
    };
    coursesList.push(matiere);
    renderCoursesDraggable();
});

document.getElementById('addEnseignantBtn').addEventListener('click', () => {
    if (!matiereSelect.value) {
        alert("Veuillez d'abord sélectionner une matière.");
        return;
    }
    const opt = enseignantSelect.options[enseignantSelect.selectedIndex];
    if (!opt) {
        alert("Aucun enseignant disponible pour cette matière.");
        return;
    }
    const enseignant = {
        nom: opt.textContent,
        type: 'enseignant',
        couleur: opt.dataset.couleur,
        bgClass: '',
        id: opt.value,
        matiereId: matiereSelect.value   // matière associée
    };
    coursesList.push(enseignant);
    renderCoursesDraggable();
});

document.getElementById('addEvenementBtn').addEventListener('click', () => {
    const opt = evenementSelect.options[evenementSelect.selectedIndex];
    const evenement = {
        nom: opt.textContent,
        type: 'evenement',
        couleur: opt.dataset.couleur,
        bgClass: opt.dataset.bgClass,
        id: opt.value
    };
    coursesList.push(evenement);
    renderCoursesDraggable();
});

// ==================== AFFICHAGE DES BADGES ====================
function renderCoursesDraggable() {
    const container = document.getElementById('coursesContainer');
    container.innerHTML = '';
    coursesList.forEach((item, index) => {
        const badge = document.createElement('span');
        badge.className = `element-pill ${item.type}`;
        if (item.bgClass) badge.classList.add(item.bgClass);
        badge.style.backgroundColor = item.couleur;
        badge.setAttribute('draggable', 'true');
        badge.dataset.nom = item.nom;
        badge.dataset.type = item.type;
        badge.dataset.couleur = item.couleur;
        badge.dataset.bgClass = item.bgClass || '';
        if (item.matiereId) badge.dataset.matiereId = item.matiereId;
        badge.dataset.index = index;
        const icon = item.type === 'matiere' ? 'fa-book' : (item.type === 'enseignant' ? 'fa-chalkboard-teacher' : 'fa-calendar-alt');
        badge.innerHTML = `<i class="fas ${icon}"></i> ${item.nom}`;
        container.appendChild(badge);
    });
}

// ==================== DRAG & DROP ====================
document.addEventListener('dragstart', (e) => {
    if (e.target.classList.contains('element-pill')) {
        const el = e.target;
        e.dataTransfer.setData('text/plain', JSON.stringify({
            nom: el.dataset.nom,
            type: el.dataset.type,
            couleur: el.dataset.couleur,
            bgClass: el.dataset.bgClass,
            matiereId: el.dataset.matiereId
        }));
        e.dataTransfer.effectAllowed = 'move';
    }
});

document.querySelectorAll('.dropzone').forEach(zone => {
    zone.addEventListener('dragover', (e) => {
        e.preventDefault();
        zone.classList.add('droppable-hover');
    });
    zone.addEventListener('dragleave', () => {
        zone.classList.remove('droppable-hover');
    });
    zone.addEventListener('drop', (e) => {
        e.preventDefault();
        zone.classList.remove('droppable-hover');
        try {
            const element = JSON.parse(e.dataTransfer.getData('text/plain'));
            handleDropOnCell(zone, element);
        } catch (error) {}
    });
});

function handleDropOnCell(cell, element) {
    if (element.type === 'matiere') {
        if (!isCellFree(cell)) {
            alert("Cette case est déjà occupée");
            return;
        }
        createCourseElement(cell, {
            type: 'matiere',
            matiere: element.nom,
            enseignants: [],
            couleur: element.couleur,
            bgClass: element.bgClass,
            matiereId: element.matiereId
        }, 1, 1);
    }
    else if (element.type === 'enseignant') {
        if (cell.children.length === 0) {
            alert("Déposez d'abord une matière");
            return;
        }
        const cours = cell.children[0];
        if (cours.dataset.type !== 'matiere') {
            alert("On ne peut ajouter un enseignant que sur une matière");
            return;
        }
        // Vérifier que l'enseignant est bien associé à cette matière
        if (element.matiereId && element.matiereId !== cours.dataset.matiereId) {
            alert("Cet enseignant n'enseigne pas cette matière");
            return;
        }
        let enseignantsList = cours.dataset.enseignants ? cours.dataset.enseignants.split(',') : [];
        if (!enseignantsList.includes(element.nom)) {
            enseignantsList.push(element.nom);
            cours.dataset.enseignants = enseignantsList.join(',');
            let ensDiv = cours.querySelector('.enseignants');
            if (ensDiv) {
                ensDiv.innerHTML = enseignantsList.map(e => `<span>${e}</span>`).join(' / ');
            } else {
                const newDiv = document.createElement('div');
                newDiv.className = 'enseignants';
                newDiv.innerHTML = enseignantsList.map(e => `<span>${e}</span>`).join(' / ');
                cours.appendChild(newDiv);
            }
        }
    }
    else if (element.type === 'evenement') {
        if (!isCellFree(cell)) {
            alert("Cette case est déjà occupée");
            return;
        }
        createCourseElement(cell, {
            type: 'evenement',
            matiere: element.nom,
            enseignants: [],
            couleur: element.couleur,
            bgClass: element.bgClass
        }, 1, 1);
    }
}

function isCellFree(cell) {
    return cell.children.length === 0 && cell.style.display !== 'none';
}

// ==================== CRÉATION D'UN COURS AVEC FUSION ====================
function createCourseElement(cell, data, rowspan = 1, colspan = 1) {
    cell.innerHTML = '';
    const div = document.createElement('div');
    div.className = `course-slot ${data.bgClass || ''}`;
    div.style.backgroundColor = data.couleur;
    div.dataset.type = data.type;
    div.dataset.matiere = data.matiere || '';
    div.dataset.enseignants = data.enseignants ? data.enseignants.join(',') : '';
    if (data.matiereId) div.dataset.matiereId = data.matiereId;
    div.dataset.rowspan = rowspan;
    div.dataset.colspan = colspan;

    let content = `<div class="matiere-nom">${data.matiere}</div>`;
    if (data.enseignants && data.enseignants.length > 0) {
        content += `<div class="enseignants">${data.enseignants.map(e => `<span>${e}</span>`).join(' / ')}</div>`;
    }
    if (rowspan > 1 || colspan > 1) {
        if (rowspan > 1) content += `<span style="font-size:0.6rem; display:block;">${rowspan}h</span>`;
        if (colspan > 1) content += `<span style="font-size:0.6rem; display:block;">${colspan}j</span>`;
    }
    content += `<div class="resize-handle vertical" title="Étirer verticalement"></div><div class="resize-handle horizontal" title="Étirer horizontalement"></div>`;
    div.innerHTML = content;
    cell.appendChild(div);

    if (rowspan > 1 || colspan > 1) {
        mergeCells(cell, rowspan, colspan);
    }
    addResizeListeners(div, cell);
}

function mergeCells(startCell, rowspan, colspan) {
    const sj = parseInt(startCell.dataset.jour);
    const sh = parseInt(startCell.dataset.horaire);
    for (let h = 0; h < rowspan; h++) {
        for (let j = 0; j < colspan; j++) {
            if (h === 0 && j === 0) continue;
            const cell = document.querySelector(`td[data-horaire="${sh + h}"][data-jour="${sj + j}"]`);
            if (cell) {
                cell.innerHTML = '';
                cell.style.display = 'none';
            }
        }
    }
    if (rowspan > 1) startCell.rowSpan = rowspan;
    if (colspan > 1) startCell.colSpan = colspan;
}

// ==================== GESTIONNAIRES D'ÉTIREMENT ====================
function addResizeListeners(element, cell) {
    const v = element.querySelector('.resize-handle.vertical');
    const h = element.querySelector('.resize-handle.horizontal');
    if (v) v.addEventListener('mousedown', (e) => { e.preventDefault(); e.stopPropagation(); startResize('vertical', cell, element); });
    if (h) h.addEventListener('mousedown', (e) => { e.preventDefault(); e.stopPropagation(); startResize('horizontal', cell, element); });
}

function startResize(type, startCell, element) {
    resizeState = {
        active: true, type, startCell, startElement: element,
        startJour: parseInt(startCell.dataset.jour),
        startHoraire: parseInt(startCell.dataset.horaire),
        startRowspan: parseInt(element.dataset.rowspan) || 1,
        startColspan: parseInt(element.dataset.colspan) || 1,
        currentCell: null
    };
    document.addEventListener('mousemove', handleResizeMove);
    document.addEventListener('mouseup', stopResize);
}

function handleResizeMove(e) {
    if (!resizeState.active) return;
    const targetCell = document.elementsFromPoint(e.clientX, e.clientY).find(el => el.classList.contains('dropzone') && el.style.display !== 'none');
    if (!targetCell) return;
    const targetJour = parseInt(targetCell.dataset.jour);
    const targetHoraire = parseInt(targetCell.dataset.horaire);
    document.querySelectorAll('.dropzone').forEach(cell => cell.classList.remove('resize-active'));
    if (resizeState.type === 'vertical') {
        const start = resizeState.startHoraire;
        const end = targetHoraire;
        const maxRows = horaires.length - start;
        const newRowspan = Math.min(Math.abs(end - start) + 1, maxRows);
        for (let h = 0; h < newRowspan; h++) {
            const cell = document.querySelector(`td[data-horaire="${start + h}"][data-jour="${resizeState.startJour}"]`);
            if (cell && cell.style.display !== 'none') cell.classList.add('resize-active');
        }
    } else {
        const start = resizeState.startJour;
        const end = targetJour;
        const maxCols = 6 - start;
        const newColspan = Math.min(Math.abs(end - start) + 1, maxCols);
        for (let j = 0; j < newColspan; j++) {
            const cell = document.querySelector(`td[data-horaire="${resizeState.startHoraire}"][data-jour="${start + j}"]`);
            if (cell && cell.style.display !== 'none') cell.classList.add('resize-active');
        }
    }
    resizeState.currentCell = targetCell;
}

function stopResize(e) {
    if (!resizeState.active || !resizeState.currentCell) { cleanupResize(); return; }
    const targetCell = resizeState.currentCell;
    const targetJour = parseInt(targetCell.dataset.jour);
    const targetHoraire = parseInt(targetCell.dataset.horaire);
    if (resizeState.type === 'vertical') {
        const start = resizeState.startHoraire;
        const end = targetHoraire;
        const maxRows = horaires.length - start;
        const newRowspan = Math.min(Math.abs(end - start) + 1, maxRows);
        if (newRowspan > 1) {
            let canResize = true;
            for (let h = 0; h < newRowspan; h++) {
                const cell = document.querySelector(`td[data-horaire="${start + h}"][data-jour="${resizeState.startJour}"]`);
                if (cell && cell.children.length > 0 && cell !== resizeState.startCell) canResize = false;
                if (cell && cell.style.display === 'none' && cell !== resizeState.startCell) canResize = false;
            }
            if (canResize) {
                const data = {
                    type: resizeState.startElement.dataset.type,
                    matiere: resizeState.startElement.dataset.matiere,
                    enseignants: resizeState.startElement.dataset.enseignants ? resizeState.startElement.dataset.enseignants.split(',') : [],
                    couleur: resizeState.startElement.style.backgroundColor,
                    bgClass: resizeState.startElement.className.split(' ')[1],
                    matiereId: resizeState.startElement.dataset.matiereId
                };
                for (let h = 0; h < newRowspan; h++) {
                    const cell = document.querySelector(`td[data-horaire="${start + h}"][data-jour="${resizeState.startJour}"]`);
                    if (cell && cell !== resizeState.startCell) {
                        cell.innerHTML = '';
                        cell.style.display = '';
                        cell.rowSpan = 1; cell.colSpan = 1;
                    }
                }
                resizeState.startCell.innerHTML = '';
                resizeState.startCell.rowSpan = 1; resizeState.startCell.colSpan = 1;
                createCourseElement(resizeState.startCell, data, newRowspan, resizeState.startColspan);
            }
        }
    } else {
        const start = resizeState.startJour;
        const end = targetJour;
        const maxCols = 6 - start;
        const newColspan = Math.min(Math.abs(end - start) + 1, maxCols);
        if (newColspan > 1) {
            let canResize = true;
            for (let j = 0; j < newColspan; j++) {
                const cell = document.querySelector(`td[data-horaire="${resizeState.startHoraire}"][data-jour="${start + j}"]`);
                if (cell && cell.children.length > 0 && cell !== resizeState.startCell) canResize = false;
                if (cell && cell.style.display === 'none' && cell !== resizeState.startCell) canResize = false;
            }
            if (canResize) {
                const data = {
                    type: resizeState.startElement.dataset.type,
                    matiere: resizeState.startElement.dataset.matiere,
                    enseignants: resizeState.startElement.dataset.enseignants ? resizeState.startElement.dataset.enseignants.split(',') : [],
                    couleur: resizeState.startElement.style.backgroundColor,
                    bgClass: resizeState.startElement.className.split(' ')[1],
                    matiereId: resizeState.startElement.dataset.matiereId
                };
                for (let j = 0; j < newColspan; j++) {
                    const cell = document.querySelector(`td[data-horaire="${resizeState.startHoraire}"][data-jour="${start + j}"]`);
                    if (cell && cell !== resizeState.startCell) {
                        cell.innerHTML = '';
                        cell.style.display = '';
                        cell.rowSpan = 1; cell.colSpan = 1;
                    }
                }
                resizeState.startCell.innerHTML = '';
                resizeState.startCell.rowSpan = 1; resizeState.startCell.colSpan = 1;
                createCourseElement(resizeState.startCell, data, resizeState.startRowspan, newColspan);
            }
        }
    }
    cleanupResize();
}

function cleanupResize() {
    document.querySelectorAll('.dropzone').forEach(cell => cell.classList.remove('resize-active'));
    resizeState.active = false;
    document.removeEventListener('mousemove', handleResizeMove);
    document.removeEventListener('mouseup', stopResize);
}

// ==================== SUPPRESSION PAR DOUBLE-CLIC ====================
tbody.addEventListener('dblclick', function(e) {
    const td = e.target.closest('td');
    if (td && td.children.length) {
        const cours = td.children[0];
        const rowspan = parseInt(cours.dataset.rowspan) || 1;
        const colspan = parseInt(cours.dataset.colspan) || 1;
        const sj = parseInt(td.dataset.jour);
        const sh = parseInt(td.dataset.horaire);
        for (let h = 0; h < rowspan; h++) {
            for (let j = 0; j < colspan; j++) {
                const cell = document.querySelector(`td[data-horaire="${sh + h}"][data-jour="${sj + j}"]`);
                if (cell) {
                    cell.innerHTML = '';
                    cell.style.display = '';
                    cell.rowSpan = 1; cell.colSpan = 1;
                }
            }
        }
    }
});

// ==================== GESTION DE LA CLASSE ET DE LA SEMAINE ====================
classeSelect.addEventListener('change', function() {
    classeDisplay.textContent = this.value;
});

const semaineInput = document.getElementById('semaineInput');
const debutSemaine = document.getElementById('debutSemaine');
const finSemaine = document.getElementById('finSemaine');

function updateSemaineDates() {
    const weekValue = semaineInput.value;
    if (weekValue) {
        const [year, week] = weekValue.split('-W');
        const firstDay = new Date(year, 0, 1);
        firstDay.setDate(firstDay.getDate() + (week - 1) * 7);
        const lastDay = new Date(firstDay);
        lastDay.setDate(lastDay.getDate() + 6);
        const options = { day: 'numeric', month: 'long', year: 'numeric' };
        debutSemaine.textContent = firstDay.toLocaleDateString('fr-FR', options);
        finSemaine.textContent = lastDay.toLocaleDateString('fr-FR', options);
    }
}
semaineInput.addEventListener('change', updateSemaineDates);
updateSemaineDates();

// ==================== SAUVEGARDE ET CHARGEMENT ====================
document.getElementById('saveBtn').addEventListener('click', function() {
    const courses = {};
    document.querySelectorAll('.course-slot').forEach(cours => {
        const cell = cours.closest('td');
        if (cell) {
            const jour = cell.dataset.jour;
            const horaire = cell.dataset.horaire;
            courses[`${jour}-${horaire}`] = {
                type: cours.dataset.type,
                matiere: cours.dataset.matiere,
                enseignants: cours.dataset.enseignants ? cours.dataset.enseignants.split(',') : [],
                couleur: cours.style.backgroundColor,
                bgClass: cours.className.split(' ')[1],
                rowspan: cours.dataset.rowspan || 1,
                colspan: cours.dataset.colspan || 1,
                matiereId: cours.dataset.matiereId,
                isStart: true
            };
        }
    });
    const classe = classeSelect.value;
    const semaine = semaineInput.value;
    const key = `emploi_${classe}_${semaine}`;
    localStorage.setItem(key, JSON.stringify(courses));
    alert('Emploi du temps enregistré !');
    updateSaveList();
});

document.getElementById('loadSaveBtn').addEventListener('click', function() {
    const select = document.getElementById('saveSelect');
    const key = select.value;
    if (!key) return;
    const saved = localStorage.getItem(key);
    if (saved) {
        document.querySelectorAll('.dropzone').forEach(cell => {
            cell.innerHTML = '';
            cell.style.display = '';
            cell.rowSpan = 1; cell.colSpan = 1;
        });
        const data = JSON.parse(saved);
        Object.keys(data).forEach(cellId => {
            const [jour, horaire] = cellId.split('-').map(Number);
            const cell = document.querySelector(`td[data-jour="${jour}"][data-horaire="${horaire}"]`);
            if (cell && data[cellId].isStart) {
                const cours = data[cellId];
                createCourseElement(cell, cours, cours.rowspan || 1, cours.colspan || 1);
            }
        });
    }
});

function updateSaveList() {
    const select = document.getElementById('saveSelect');
    select.innerHTML = '<option value="">Charger une sauvegarde...</option>';
    for (let i = 0; i < localStorage.length; i++) {
        const key = localStorage.key(i);
        if (key.startsWith('emploi_')) {
            const opt = document.createElement('option');
            opt.value = key;
            const parts = key.split('_');
            opt.textContent = `${parts[1]} - Semaine ${parts[2]}`;
            select.appendChild(opt);
        }
    }
}

// ==================== NOUVEL EMPLOI DU TEMPS ====================
document.getElementById('newBtn').addEventListener('click', function() {
    if (confirm('Créer un nouvel emploi du temps ? Les données non enregistrées seront perdues.')) {
        document.querySelectorAll('.dropzone').forEach(cell => {
            cell.innerHTML = '';
            cell.style.display = '';
            cell.rowSpan = 1; cell.colSpan = 1;
        });
        coursesList = [];
        renderCoursesDraggable();
    }
});

// ==================== EXPORT PDF ====================
document.getElementById('exportPdfBtn').addEventListener('click', function() {
    const timetableCard = document.getElementById('timetableCard');
    const clone = timetableCard.cloneNode(true);
    
    clone.style.width = '100%';
    clone.style.padding = '20px';
    clone.style.backgroundColor = 'white';
    
    const opt = {
        margin: [0.5, 0.5, 0.5, 0.5],
        filename: `emploi_du_temps_${classeDisplay.textContent}_semaine_${semaineInput.value}.pdf`,
        image: { type: 'jpeg', quality: 0.98 },
        html2canvas: { scale: 2, backgroundColor: '#ffffff' },
        jsPDF: { unit: 'in', format: 'a4', orientation: 'landscape' }
    };
    
    html2pdf().set(opt).from(clone).save();
});


// ==================== INITIALISATION ====================
updateSaveList();
renderCoursesDraggable();