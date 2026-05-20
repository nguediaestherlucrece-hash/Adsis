import { requireRole, authFetch } from './auth.js';

// Guard : réservé aux assistants pédagogiques, surveillants et admins
if (!requireRole(['ASSISTANT_PEDAGOGIQUE', 'SURVEILLANT', 'ADMIN'])) { throw new Error('Unauthorized'); }

const API_BASE_URL = 'http://localhost:8081/api';

let isEditorMode = false;
let currentEdt = null;
let matieres = [];
let specialites = [];
let classes = [];
let semestres = [];

// Créneaux horaires (index → heure réelle)
const horaires = [
    { label: "08:00 - 09:00", debut: "08:00:00", fin: "09:00:00" },
    { label: "09:00 - 10:00", debut: "09:00:00", fin: "10:00:00" },
    { label: "10:00 - 11:00", debut: "10:00:00", fin: "11:00:00" },
    { label: "11:00 - 12:00", debut: "11:00:00", fin: "12:00:00" },
    { label: "12:00 - 13:00", debut: "12:00:00", fin: "13:00:00" },
    { label: "13:00 - 14:00", debut: "13:00:00", fin: "14:00:00" },
    { label: "14:00 - 15:00", debut: "14:00:00", fin: "15:00:00" },
    { label: "15:00 - 16:00", debut: "15:00:00", fin: "16:00:00" },
    { label: "16:00 - 17:00", debut: "16:00:00", fin: "17:00:00" }
];

const joursSemaine = ["Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"];

const colors = ["#0f22f1", "#2d6a4f", "#eb1919", "#ab1be4", "#f6940a", "#1093fe", "#f318aa", "#b15e5e", "#059669"];

// Références DOM
const listingView = document.getElementById('listingView');
const editorView = document.getElementById('editorView');
const edtListContainer = document.getElementById('edtListContainer');
const backToListBtn = document.getElementById('backToListBtn');

// Modal DOM
const modalClasse = document.getElementById('modalClasse');
const modalDateDebut = document.getElementById('modalDateDebut');
const modalDateFin = document.getElementById('modalDateFin');
const modalSubmitBtn = document.getElementById('modalSubmitBtn');
const modalEdtId = document.getElementById('modalEdtId');
const edtModalTitle = document.getElementById('edtModalTitle');
const modalSemestre = document.getElementById('modalSemestre');

// Editor DOM
const timetableBody = document.getElementById('timetableBody');
const timetableHeaders = document.getElementById('timetableHeaders');
const classeDisplay = document.getElementById('editorClasseBadge');
const debutSemaineText = document.getElementById('debutSemaine');
const finSemaineText = document.getElementById('finSemaine');
const headerDebutSemaine = document.getElementById('headerDebutSemaine');
const headerFinSemaine = document.getElementById('headerFinSemaine');
const matieresContainerEditor = document.getElementById('matieresContainer');
const enseignantsContainer = document.getElementById('enseignantsContainer');

function showToast(message, type = 'info') {
    const config = {
        success: { icon: 'bi-check-circle-fill', label: 'Succès', bg: '#1a7f5a', delay: 3500 },
        danger: { icon: 'bi-x-circle-fill', label: 'Erreur', bg: '#c0392b', delay: 6000 },
        warning: { icon: 'bi-exclamation-triangle-fill', label: 'Attention', bg: '#d68910', delay: 5000 },
        info: { icon: 'bi-info-circle-fill', label: 'Info', bg: '#1a6fa8', delay: 4000 },
    };
    const c = config[type] || config.info;
    const container = document.getElementById('toastContainer');
    const toastEl = document.createElement('div');
    toastEl.className = 'toast border-0 rounded-3 shadow-lg overflow-hidden';
    toastEl.setAttribute('role', 'alert');
    toastEl.setAttribute('data-bs-delay', c.delay);
    toastEl.style.minWidth = '280px';
    toastEl.style.maxWidth = '360px';
    toastEl.innerHTML = `
        <div class="toast-header border-0 text-white py-2 px-3" style="background:${c.bg}">
            <i class="bi ${c.icon} me-2"></i><strong class="me-auto">${c.label}</strong>
            <button type="button" class="btn-close btn-close-white ms-2" data-bs-dismiss="toast"></button>
        </div>
        <div class="toast-body text-dark bg-white small py-2 px-3">${message}</div>`;
    container.appendChild(toastEl);
    new bootstrap.Toast(toastEl).show();
    toastEl.addEventListener('hidden.bs.toast', () => toastEl.remove());
}

function showConfirm(title = 'Confirmer', message = 'Êtes-vous sûr ?') {
    return new Promise((resolve) => {
        const modalEl = document.getElementById('confirmModal');
        document.getElementById('confirmModalTitle').innerText = title;
        document.getElementById('confirmModalMessage').innerText = message;
        const modal = new bootstrap.Modal(modalEl);
        let resolved = false;
        const done = (result) => {
            if (resolved) return; resolved = true;
            document.getElementById('confirmOkBtn').replaceWith(document.getElementById('confirmOkBtn').cloneNode(true));
            document.getElementById('confirmCancelBtn').replaceWith(document.getElementById('confirmCancelBtn').cloneNode(true));
            modal.hide(); resolve(result);
        };
        document.getElementById('confirmOkBtn').addEventListener('click', () => done(true), { once: true });
        document.getElementById('confirmCancelBtn').addEventListener('click', () => done(false), { once: true });
        modalEl.addEventListener('hidden.bs.modal', () => done(false), { once: true });
        modal.show();
    });
}


// ==================== UTILITAIRES ====================
function formatDate(dateStr) {
    if (!dateStr) return '...';
    let d;
    if (Array.isArray(dateStr)) {
        // Jackson serializes LocalDate as [year, month, day]
        d = new Date(dateStr[0], dateStr[1] - 1, dateStr[2]);
    } else {
        d = new Date(dateStr);
    }
    if (isNaN(d.getTime())) return '...';
    return d.toLocaleDateString('fr-FR', {
        day: '2-digit', month: 'long', year: 'numeric'
    });
}

/** Calcule la date de fin à partir de dateDebut + nombreJours (jours ouvrés Lun-Sam) */
function calculateDateFin(dateDebut, nbJours) {
    const d = new Date(dateDebut);
    let count = 0;
    while (count < nbJours) {
        const dow = d.getDay(); // 0=dim, 1=lun .. 6=sam
        if (dow >= 1 && dow <= 6) count++;
        if (count < nbJours) d.setDate(d.getDate() + 1);
    }
    return d.toISOString().split('T')[0]; // YYYY-MM-DD
}

/** Parse une date qui peut être un tableau Jackson [y,m,d] ou une string ISO */
function parseDate(val) {
    if (!val) return null;
    if (Array.isArray(val)) return new Date(val[0], val[1] - 1, val[2]);
    return new Date(val);
}

/** Retourne la date réelle d'un jour de la grille (index 0=Lundi) en fonction de dateDebut */
function getDateForJour(jourIdx) {
    if (!currentEdt || !currentEdt.dateDebut) return null;
    const d = parseDate(currentEdt.dateDebut);
    if (!d || isNaN(d.getTime())) return null;
    const dow = d.getDay();
    if (dow === 0) d.setDate(d.getDate() + 1);
    d.setDate(d.getDate() + jourIdx);
    return d.toISOString().split('T')[0];
}

/** Trouve le jour index (0-5) et l'horaire index (0-8) à partir d'une date et heureDebut */
function findGridPosition(seanceDate, heureDebut) {
    let jourIdx = -1;
    if (currentEdt && currentEdt.dateDebut) {
        const base = parseDate(currentEdt.dateDebut);
        const baseDow = base.getDay();
        if (baseDow === 0) base.setDate(base.getDate() + 1);
        const seanceD = parseDate(seanceDate);
        const diffDays = Math.round((seanceD - base) / (1000 * 60 * 60 * 24));
        if (diffDays >= 0 && diffDays < 6) jourIdx = diffDays;
    }

    let horaireIdx = -1;
    if (heureDebut) {
        const hStr = heureDebut.substring(0, 5);
        horaireIdx = horaires.findIndex(h => h.debut.substring(0, 5) === hStr);
    }
    return { jourIdx, horaireIdx };
}

/** Calcule la durée en créneaux entre heureDebut et heureFin */
function calculateDuration(heureDebut, heureFin) {
    if (!heureDebut || !heureFin) return 1;
    const [h1, m1] = heureDebut.split(':').map(Number);
    const [h2, m2] = heureFin.split(':').map(Number);
    return Math.max(1, (h2 * 60 + m2 - h1 * 60 - m1) / 60);
}

/** Retourne le nom de la classe à partir de classeId */
function getClasseNom(edt) {
    if (edt.classeNom) return edt.classeNom;
    if (edt.classeId) {
        const c = classes.find(cl => cl.id == edt.classeId);
        if (c) return c.nomClasse;
    }
    return 'Classe inconnue';
}

/** Retourne l'année académique depuis le DTO (ou via la list semestres) */
function getAnneeNom(edt) {
    if (edt.anneeNom) return edt.anneeNom;
    if (edt.semestreId && semestres.length > 0) {
        const sem = semestres.find(s => s.id == edt.semestreId);
        if (sem && sem.anneeNom) return sem.anneeNom;
    }
    return null;
}

/** Retourne le nom du semestre depuis le DTO (ou via la list semestres) */
function getSemestreNom(edt) {
    if (edt.semestreNom) return edt.semestreNom;
    if (edt.semestreId && semestres.length > 0) {
        const sem = semestres.find(s => s.id == edt.semestreId);
        if (sem) return sem.nomSemestre || sem.idSemestre;
    }
    return null;
}


async function init() {
    const safeLoad = (path) => fetch(`${API_BASE_URL}${path}`).then(r => r.ok ? r.json() : []).catch(() => []);
    [classes, specialites, semestres] = await Promise.all([
        safeLoad('/classes'),
        safeLoad('/specialites'),
        safeLoad('/semestres'),
    ]);
    populateModalClasse();
    populateModalSemestre();
    await fetchEmploisDuTemps();

    // Contraindre dateFin quand dateDebut change
    modalDateDebut.addEventListener('change', constrainDateFin);
}

function constrainDateFin() {
    const debut = modalDateDebut.value;
    if (!debut) { modalDateFin.min = ''; modalDateFin.max = ''; return; }
    const d = new Date(debut);
    modalDateFin.min = debut; // même jour min
    // max = debut + 5 jours calendaires (pour couvrir Lun→Sam)
    const maxD = new Date(d);
    maxD.setDate(maxD.getDate() + 5);
    modalDateFin.max = maxD.toISOString().split('T')[0];
    // Si dateFin actuelle est hors bornes, reset
    if (modalDateFin.value && (modalDateFin.value < modalDateFin.min || modalDateFin.value > modalDateFin.max)) {
        modalDateFin.value = '';
    }
}

/** Calcule le nombre de jours ouvrés entre deux dates (inclus) */
function countWorkDays(dateDebutStr, dateFinStr) {
    const d1 = new Date(dateDebutStr);
    const d2 = new Date(dateFinStr);
    let count = 0;
    const cur = new Date(d1);
    while (cur <= d2) {
        const dow = cur.getDay();
        if (dow >= 1 && dow <= 6) count++;
        cur.setDate(cur.getDate() + 1);
    }
    return Math.max(1, count);
}

function populateModalClasse() {
    modalClasse.innerHTML = '<option value="">-- Sélectionnez une classe --</option>';
    classes.forEach(c => {
        const sp = specialites.find(s => s.id == c.specialiteId);
        const opt = document.createElement('option');
        opt.value = c.id;
        opt.textContent = sp ? `${c.nomClasse} (${sp.nom || sp.nomSpecialite || sp.idSpecialite})` : c.nomClasse;
        opt.dataset.specialiteId = c.specialiteId || '';
        modalClasse.appendChild(opt);
    });
}

function populateModalSemestre() {
    if (!modalSemestre) return;
    modalSemestre.innerHTML = '<option value="">-- Sélectionnez un semestre --</option>';
    semestres.forEach(s => {
        const opt = document.createElement('option');
        opt.value = s.id;
        const anneeLabel = s.anneeNom ? ` (${s.anneeNom})` : '';
        opt.textContent = (s.nomSemestre || s.idSemestre || `Semestre ${s.id}`) + anneeLabel;
        modalSemestre.appendChild(opt);
    });
}

async function fetchEmploisDuTemps() {
    try {
        const response = await fetch(`${API_BASE_URL}/emplois-du-temps`);
        if (response.ok) {
            const edts = await response.json();
            renderEdtList(edts);
        } else {
            edtListContainer.innerHTML = `<div class="col-12 text-center text-muted p-5"><p>Erreur de chargement (${response.status}).</p></div>`;
        }
    } catch (e) {
        console.error("Erreur API EDT", e);
        edtListContainer.innerHTML = `<div class="col-12 text-center text-muted p-5"><p>Erreur réseau.</p></div>`;
    }
}

function renderEdtList(edts) {
    if (edts.length === 0) {
        edtListContainer.innerHTML = `
            <div class="col-12 text-center p-5">
                <div class="empty-state-edt">
                    <i class="bi bi-calendar2-x"></i>
                    <h6>Aucun emploi du temps</h6>
                    <p>Cliquez sur « + Nouvel emploi du temps » pour commencer.</p>
                </div>
            </div>`;
        return;
    }

    const accentColors = ['#3b82f6', '#22c55e', '#a855f7', '#f97316', '#14b8a6', '#ef4444', '#6366f1', '#ec4899'];

    let html = '';
    edts.forEach((edt, idx) => {
        const classeNom = getClasseNom(edt);
        const anneeNom = getAnneeNom(edt);
        const semestreNom = getSemestreNom(edt);
        let annSemText = '';
        if (anneeNom && semestreNom) annSemText = `${anneeNom} | ${semestreNom}`;
        else if (anneeNom) annSemText = anneeNom;
        else if (semestreNom) annSemText = semestreNom;
        else annSemText = 'Non assigné';

        const accent = accentColors[idx % accentColors.length];
        const initials = classeNom.substring(0, 2).toUpperCase();

        html += `
            <div class="col-lg-4 col-md-6 col-sm-6 mb-4">
                <div class="edt-card-premium" onclick="openEditor(${edt.id})" style="border-left: 4px solid ${accent};">
                    <div class="edt-card-header">
                        <div class="edt-avatar" style="background: ${accent}15; color: ${accent};">
                            <i class="bi bi-calendar3"></i>
                        </div>
                        <div class="edt-title-zone">
                            <h6 class="edt-classe-name">${classeNom}</h6>
                            <span class="edt-semester-badge">${annSemText}</span>
                        </div>
                    </div>
                    <div class="edt-card-details">
                        <div class="edt-detail-row">
                            <i class="bi bi-calendar-range" style="color: ${accent};"></i>
                            <span>${formatDate(edt.dateDebut)} → ${formatDate(edt.dateFin)}</span>
                        </div>
                        <div class="edt-detail-row">
                            <i class="bi bi-grid-3x3-gap" style="color: ${accent};"></i>
                            <span>${edt.nombreJours || 6} jours programmés</span>
                        </div>
                    </div>
                    <div class="edt-card-actions">
                        <button class="edt-action-btn edit" title="Modifier les infos" onclick="event.stopPropagation(); openEditEdtModal(${edt.id})">
                            <i class="bi bi-pencil-square"></i>
                        </button>
                        <button class="edt-action-btn delete" title="Supprimer" onclick="event.stopPropagation(); deleteEdt(${edt.id})">
                            <i class="bi bi-trash3"></i>
                        </button>
                    </div>
                </div>
            </div>
        `;
    });
    edtListContainer.innerHTML = html;
}


modalSubmitBtn.addEventListener('click', async () => {
    const classeId = modalClasse.value;
    const dateDebut = modalDateDebut.value;
    const dateFin = modalDateFin.value;
    const editId = modalEdtId.value;
    const semestreId = modalSemestre ? modalSemestre.value : null;

    if (!classeId || !dateDebut || !dateFin) {
        showToast("Veuillez remplir tous les champs obligatoires (classe, date début, date fin).", "warning");
        return;
    }

    const nombreJours = countWorkDays(dateDebut, dateFin);

    const dto = {
        idEmploi: `EDT-${Date.now()}`,
        dateDebut: dateDebut,
        dateFin: dateFin,
        nombreJours: nombreJours,
        classeId: parseInt(classeId)
    };
    if (semestreId) dto.semestreId = parseInt(semestreId);

    try {
        let response;
        if (editId) {
            response = await fetch(`${API_BASE_URL}/emplois-du-temps/${editId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(dto)
            });
        } else {
            response = await fetch(`${API_BASE_URL}/emplois-du-temps`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(dto)
            });
        }
        if (response.ok) {
            const result = await response.json();
            bootstrap.Modal.getInstance(document.getElementById('createEdtModal'))?.hide();
            showToast(editId ? "Emploi du temps modifié." : "Emploi du temps créé.", "success");
            if (editId) {
                switchToList();
            } else {
                openEditor(result.id, result);
            }
        } else {
            const errText = await response.text();
            console.error("Erreur création/modif EDT:", errText);
            showToast("Erreur : " + errText, "danger");
        }
    } catch (e) {
        console.error(e);
        showToast("Erreur réseau", "danger");
    }
});

// Ouvrir le modal en mode édition (bouton crayon)
window.openEditEdtModal = async function (edtId) {
    try {
        const res = await fetch(`${API_BASE_URL}/emplois-du-temps/${edtId}`);
        if (!res.ok) return;
        const edt = await res.json();

        edtModalTitle.textContent = "Modifier l'emploi du temps";
        modalEdtId.value = edt.id;
        modalDateDebut.value = edt.dateDebut;
        constrainDateFin();
        modalDateFin.value = edt.dateFin;
        modalSubmitBtn.textContent = "Enregistrer";

        if (edt.classeId) {
            modalClasse.value = edt.classeId;
        } else {
            const classeData = classes.find(c => c.nomClasse === edt.classeNom);
            if (classeData) modalClasse.value = classeData.id;
        }

        if (modalSemestre && edt.semestreId) {
            modalSemestre.value = edt.semestreId;
        }

        new bootstrap.Modal(document.getElementById('createEdtModal')).show();
    } catch (e) { console.error(e); }
};

document.getElementById('createEdtModal').addEventListener('hidden.bs.modal', () => {
    edtModalTitle.textContent = "Créer un emploi du temps";
    modalEdtId.value = '';
    modalSubmitBtn.textContent = "Créer et Éditer";
    modalClasse.value = '';
    modalDateDebut.value = '';
    modalDateFin.value = '';
    modalDateFin.min = '';
    modalDateFin.max = '';
    if (modalSemestre) modalSemestre.value = '';
});

// Supprimer un EDT
window.deleteEdt = async function (edtId) {
    const confirmed = await showConfirm("Supprimer l'emploi du temps ?", "Cette action supprimera cet emploi du temps et toutes ses séances de façon irréversible.");
    if (!confirmed) return;
    try {
        const res = await fetch(`${API_BASE_URL}/emplois-du-temps/${edtId}`, { method: 'DELETE' });
        if (res.ok || res.status === 204) {
            showToast("Emploi du temps supprimé.", "success");
            await fetchEmploisDuTemps();
        } else {
            const errText = await res.text();
            console.error("Erreur suppression EDT:", errText);
            showToast("Erreur lors de la suppression : " + errText, "danger");
        }
    } catch (e) { console.error(e); showToast("Erreur réseau", "danger"); }
};


function switchToEditor() {
    listingView.classList.remove('active');
    editorView.classList.add('active');
    isEditorMode = true;
}

function switchToList() {
    editorView.classList.remove('active');
    listingView.classList.add('active');
    isEditorMode = false;
    currentEdt = null;
    fetchEmploisDuTemps();
}

backToListBtn.addEventListener('click', switchToList);

window.openEditor = async function (edtId, edtDataPreloaded = null) {
    switchToEditor();
    timetableBody.innerHTML = '<tr><td colspan="7" class="text-center p-5"><i class="fas fa-spinner fa-spin"></i> Chargement...</td></tr>';

    try {
        if (!edtDataPreloaded) {
            const res = await fetch(`${API_BASE_URL}/emplois-du-temps/${edtId}`);
            if (res.ok) currentEdt = await res.json();
            else throw new Error("EDT introuvable");
        } else {
            currentEdt = edtDataPreloaded;
        }

        // MAJ infos header + timetableCard
        const classeNom = getClasseNom(currentEdt);
        classeDisplay.textContent = classeNom;
        const debutStr = formatDate(currentEdt.dateDebut);
        const finStr = formatDate(currentEdt.dateFin);
        debutSemaineText.textContent = debutStr;
        finSemaineText.textContent = finStr;
        if (headerDebutSemaine) headerDebutSemaine.textContent = debutStr;
        if (headerFinSemaine) headerFinSemaine.textContent = finStr;

        // Afficher année et semestre dans le header
        const anneeBadge = document.getElementById('editorAnneeBadge');
        const semestreBadge = document.getElementById('editorSemestreBadge');
        const anneeNom = getAnneeNom(currentEdt);
        const semestreNom = getSemestreNom(currentEdt);
        if (anneeNom && anneeBadge) {
            anneeBadge.textContent = anneeNom;
            anneeBadge.style.display = '';
        } else if (anneeBadge) {
            anneeBadge.style.display = 'none';
        }
        if (semestreNom && semestreBadge) {
            semestreBadge.textContent = semestreNom;
            semestreBadge.style.display = '';
        } else if (semestreBadge) {
            semestreBadge.style.display = 'none';
        }

        // Charger les matières par spécialité de la classe
        let specId = null;
        if (currentEdt.classeId) {
            const c = classes.find(cl => cl.id == currentEdt.classeId);
            if (c) specId = c.specialiteId;
        }
        if (!specId) {
            const c = classes.find(cl => cl.nomClasse === currentEdt.classeNom);
            if (c) specId = c.specialiteId;
        }

        if (specId) {
            await fetchMatieresBySpecialite(specId);
        } else {
            await fetchAllMatieres();
        }

        // Build grid
        buildTimetableGrid(currentEdt.nombreJours || 6);

        // Fetch seances existantes
        await fetchSeancesForEdt(currentEdt.id);

    } catch (e) {
        console.error("Erreur openEditor:", e);
        switchToList();
    }
};


async function fetchMatieresBySpecialite(specId) {
    try {
        const res = await fetch(`${API_BASE_URL}/cours-ue/specialite/${specId}`);
        if (res.ok) {
            matieres = await res.json();
        } else {
            await fetchAllMatieres();
            return;
        }
    } catch (e) { console.error(e); }
    renderMatiereBadges();
}

async function fetchAllMatieres() {
    try {
        const res = await fetch(`${API_BASE_URL}/cours-ue`);
        if (res.ok) matieres = await res.json();
    } catch (e) { console.error(e); }
    renderMatiereBadges();
}


// Stockage local des couleurs de matière (modifiable par l'utilisateur)
let matiereColors = {};

function renderMatiereBadges() {
    matieresContainerEditor.innerHTML = '';
    if (matieres.length === 0) {
        matieresContainerEditor.innerHTML = '<p class="text-muted small mb-0">Aucune matière pour cette spécialité.</p>';
        return;
    }
    matieres.forEach((m, idx) => {
        const color = matiereColors[m.id] || colors[idx % colors.length];
        matiereColors[m.id] = color; // stocker la couleur par défaut

        const wrapper = document.createElement('div');
        wrapper.className = 'd-flex align-items-center mb-1 w-100';

        const badge = document.createElement('span');
        badge.className = 'element-pill flex-grow-1';
        badge.style.backgroundColor = color;
        badge.setAttribute('draggable', 'true');
        badge.dataset.json = JSON.stringify({
            type: 'matiere', id: m.id, nom: m.intitule, couleur: color, codeUE: m.codeUE,
            enseignants: m.enseignants || []
        });
        badge.innerHTML = `<i class="fas fa-book me-1"></i> ${m.intitule}`;

        // Bouton color picker
        const colorBtn = document.createElement('input');
        colorBtn.type = 'color';
        colorBtn.value = color;
        colorBtn.title = 'Changer la couleur';
        colorBtn.style.cssText = 'width:24px;height:24px;border:none;cursor:pointer;margin-left:4px;border-radius:50%;padding:0;';
        colorBtn.addEventListener('input', (e) => {
            const newColor = e.target.value;
            matiereColors[m.id] = newColor;
            badge.style.backgroundColor = newColor;
            badge.dataset.json = JSON.stringify({
                type: 'matiere', id: m.id, nom: m.intitule, couleur: newColor, codeUE: m.codeUE,
                enseignants: m.enseignants || []
            });
        });

        wrapper.appendChild(badge);
        wrapper.appendChild(colorBtn);
        matieresContainerEditor.appendChild(wrapper);
    });
}


function renderEnseignantBadgesForMatiere(matiereId) {
    const matiere = matieres.find(m => m.id == matiereId);
    enseignantsContainer.innerHTML = '';
    if (!matiere || !matiere.enseignants || matiere.enseignants.length === 0) {
        enseignantsContainer.innerHTML = '<p class="text-muted small mb-0">Aucun enseignant assigné à cette matière.</p>';
        return;
    }
    matiere.enseignants.forEach(e => {
        const badge = document.createElement('span');
        badge.className = 'element-pill enseignant-sub-pill';
        badge.style.backgroundColor = '#5a5c69';
        badge.setAttribute('draggable', 'true');
        badge.dataset.json = JSON.stringify({
            type: 'enseignant', id: e.id, nom: `${e.nom} ${e.prenom || ''}`.trim(), matiereId: matiereId
        });
        badge.innerHTML = `<i class="fas fa-chalkboard-teacher me-1"></i> ${e.nom} ${e.prenom || ''}`;
        enseignantsContainer.appendChild(badge);
    });
}


function buildTimetableGrid(nbJours) {
    timetableHeaders.innerHTML = '<th style="min-width: 100px;">Horaires</th>';
    for (let i = 0; i < nbJours; i++) {
        const th = document.createElement('th');
        th.style.minWidth = '100px';
        th.textContent = joursSemaine[i];
        timetableHeaders.appendChild(th);
    }

    timetableBody.innerHTML = '';
    horaires.forEach((h, idx) => {
        const tr = document.createElement('tr');
        tr.innerHTML = `<td class="fw-semibold text-secondary">${h.label}</td>`;
        for (let j = 0; j < nbJours; j++) {
            const td = document.createElement('td');
            td.dataset.horaire = idx;
            td.dataset.jour = j;
            td.classList.add('dropzone');
            tr.appendChild(td);
        }
        timetableBody.appendChild(tr);
    });

    attachDropzoneListeners();
}

async function fetchSeancesForEdt(edtId) {
    try {
        const res = await fetch(`${API_BASE_URL}/seances/emploi-du-temps/${edtId}`);
        if (res.ok) {
            const seances = await res.json();

            // Regrouper les séances par groupId pour reconstituer les blocs multi-jour
            const groups = {};
            const ungrouped = [];
            seances.forEach(s => {
                if (s.groupId) {
                    if (!groups[s.groupId]) groups[s.groupId] = [];
                    groups[s.groupId].push(s);
                } else {
                    ungrouped.push(s);
                }
            });

            // Traiter les séances sans groupe (individuelles)
            ungrouped.forEach(s => renderSingleSeance(s));

            // Traiter les groupes multi-jour
            Object.values(groups).forEach(group => {
                // Trier par date puis par jour
                group.sort((a, b) => new Date(a.date) - new Date(b.date));
                const first = group[0];
                const { jourIdx, horaireIdx } = findGridPosition(first.date, first.heureDebut);
                if (jourIdx < 0 || horaireIdx < 0) return;

                const cell = document.querySelector(`td[data-jour="${jourIdx}"][data-horaire="${horaireIdx}"]`);
                if (!cell) return;

                const matiere = matieres.find(m => m.id == first.coursId);
                const colorIdx = matiere ? matieres.indexOf(matiere) % colors.length : 0;
                const duration = calculateDuration(first.heureDebut, first.heureFin);
                const colspan = group.length;
                const linkedIds = group.map(s => s.id);

                // Détecter si le groupe est un événement spécial (coursId null ou idSeance EVT-)
                const isEvent = !first.coursId || (first.idSeance && first.idSeance.startsWith('EVT-'));
                
                if (isEvent) {
                    let eventName = 'Événement';
                    if (first.idSeance && first.idSeance.startsWith('EVT-')) {
                        const parts = first.idSeance.split('-');
                        eventName = parts.slice(1, -1).join(' ').replace(/_/g, ' ') || 'Événement';
                    }
                    const iconMap = { 'Pause': 'coffee', 'TPE': 'user-clock' };
                    createEventElement(cell, {
                        nom: eventName,
                        couleur: first.couleur || '#d97706',
                        icon: iconMap[eventName] || 'calendar-alt',
                        dbId: first.id,
                        linkedIds: linkedIds
                    }, duration, colspan);
                } else {
                    const data = {
                        dbId: first.id,
                        type: 'matiere',
                        matiere: matiere ? matiere.intitule : 'Séance',
                        matiereId: first.coursId,
                        enseignants: [],
                        enseignantIds: first.enseignantIds || [],
                        couleur: first.couleur || (matiereColors[first.coursId]) || colors[colorIdx],
                        linkedIds: linkedIds,
                        date: first.date || '',
                        heureDebut: first.heureDebut ? (typeof first.heureDebut === 'string' ? first.heureDebut : String(first.heureDebut)) : '',
                        heureFin:   first.heureFin   ? (typeof first.heureFin   === 'string' ? first.heureFin   : String(first.heureFin))   : ''
                    };

                    if (first.enseignantIds && first.enseignantIds.length > 0 && matiere && matiere.enseignants) {
                        data.enseignants = first.enseignantIds.map(eid => {
                            const ens = matiere.enseignants.find(e => e.id == eid);
                            return ens ? `${ens.nom} ${ens.prenom || ''}`.trim() : null;
                        }).filter(Boolean);
                    }

                    createCourseElement(cell, data, duration, colspan);
                }
            });
        }
    } catch (e) { console.error("Erreur fetch seances:", e); }
}

function renderSingleSeance(s) {
    const { jourIdx, horaireIdx } = findGridPosition(s.date, s.heureDebut);
    if (jourIdx < 0 || horaireIdx < 0) return;

    const cell = document.querySelector(`td[data-jour="${jourIdx}"][data-horaire="${horaireIdx}"]`);
    if (!cell) return;

    const duration = calculateDuration(s.heureDebut, s.heureFin);

    // Détecter les événements spéciaux (coursId null ou idSeance commence par EVT-)
    const isEvent = !s.coursId || (s.idSeance && s.idSeance.startsWith('EVT-'));
    if (isEvent) {
        // Extraire le nom de l'événement depuis idSeance (EVT-Pause-1234567890)
        let eventName = 'Événement';
        if (s.idSeance && s.idSeance.startsWith('EVT-')) {
            const parts = s.idSeance.split('-');
            eventName = parts.slice(1, -1).join(' ').replace(/_/g, ' ') || 'Événement';
        }
        const iconMap = { 'Pause': 'coffee', 'TPE': 'user-clock' };
        createEventElement(cell, {
            nom: eventName,
            couleur: s.couleur || '#d97706',
            icon: iconMap[eventName] || 'calendar-alt',
            dbId: s.id
        }, duration, 1);
        return;
    }

    const matiere = matieres.find(m => m.id == s.coursId);
    const colorIdx = matiere ? matieres.indexOf(matiere) % colors.length : 0;

    const data = {
        dbId: s.id,
        type: 'matiere',
        matiere: matiere ? matiere.intitule : 'Séance',
        matiereId: s.coursId,
        enseignants: [],
        enseignantIds: s.enseignantIds || [],
        couleur: s.couleur || (matiereColors[s.coursId]) || colors[colorIdx],
        date: s.date || '',
        heureDebut: s.heureDebut ? (typeof s.heureDebut === 'string' ? s.heureDebut : String(s.heureDebut)) : '',
        heureFin:   s.heureFin   ? (typeof s.heureFin   === 'string' ? s.heureFin   : String(s.heureFin))   : ''
    };

    if (s.enseignantIds && s.enseignantIds.length > 0 && matiere && matiere.enseignants) {
        data.enseignants = s.enseignantIds.map(eid => {
            const ens = matiere.enseignants.find(e => e.id == eid);
            return ens ? `${ens.nom} ${ens.prenom || ''}`.trim() : null;
        }).filter(Boolean);
    }

    createCourseElement(cell, data, duration, 1);
}


document.addEventListener('dragstart', (e) => {
    if (e.target.classList.contains('element-pill')) {
        e.dataTransfer.setData('text/plain', e.target.dataset.json);
        e.dataTransfer.effectAllowed = 'move';
    }
});

function attachDropzoneListeners() {
    document.querySelectorAll('.dropzone').forEach(zone => {
        zone.addEventListener('dragover', (e) => {
            e.preventDefault();
            zone.classList.add('droppable-hover');
        });
        zone.addEventListener('dragleave', () => {
            zone.classList.remove('droppable-hover');
        });
        zone.addEventListener('drop', async (e) => {
            e.preventDefault();
            zone.classList.remove('droppable-hover');
            try {
                const rawData = e.dataTransfer.getData('text/plain');
                if (!rawData) return;
                const element = JSON.parse(rawData);
                await handleDropOnCell(zone, element);
            } catch (error) { console.error("Erreur drop:", error); }
        });
    });
}

function isCellFree(cell) {
    return cell.children.length === 0 && cell.style.display !== 'none';
}

async function handleDropOnCell(cell, element) {
    if (element.type === 'event') {
        if (!isCellFree(cell)) {
            showToast("Cette case est déjà occupée.", "warning");
            return;
        }

        // Sauvegarder l'événement en BD comme une séance spéciale (coursId = null)
        const jourIdx = parseInt(cell.dataset.jour);
        const horaireIdx = parseInt(cell.dataset.horaire);
        const dateSeance = getDateForJour(jourIdx);
        if (!dateSeance) { showToast('Impossible de déterminer la date.', 'warning'); return; }

        const dto = {
            idSeance: `EVT-${element.nom.replace(/\s+/g, '_')}-${Date.now()}`,
            date: dateSeance,
            heureDebut: horaires[horaireIdx].debut,
            heureFin: horaires[horaireIdx].fin,
            statut: false,
            coursId: null,
            enseignantIds: [],
            emploiDuTempsId: currentEdt.id,
            couleur: element.couleur || '#d97706',
            groupId: null
        };

        try {
            const res = await fetch(`${API_BASE_URL}/seances`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(dto)
            });
            if (res.ok) {
                const saved = await res.json();
                element.dbId = saved.id;
                createEventElement(cell, element);
                showToast(`Événement "${element.nom}" enregistré.`, 'success');
            } else {
                const errText = await res.text();
                showToast('Erreur : ' + errText, 'danger');
            }
        } catch (e) {
            console.error(e);
            showToast('Erreur réseau.', 'danger');
        }
        return;
    }
    if (element.type === 'matiere') {
        if (!isCellFree(cell)) {
            showToast("Cette case est déjà occupée.", "warning");
            return;
        }

        // Ouvrir le modal de configuration au lieu de créer directement
        openSeanceConfigModal(null, cell, element);
    }
    else if (element.type === 'enseignant') {
        if (cell.children.length === 0) {
            showToast("Déposez d'abord une matière sur cette case.", "warning");
            return;
        }
        const coursBlock = cell.children[0];

        if (coursBlock.dataset.matiereId != element.matiereId) {
            showToast("Cet enseignant n'est pas assigné à la matière de cette séance.", "warning");
            return;
        }

        const dbId = coursBlock.dataset.dbId;
        if (!dbId) return;

        try {
            const resGet = await fetch(`${API_BASE_URL}/seances/${dbId}`);
            if (!resGet.ok) return;
            const seance = await resGet.json();

            // Build the new enseignantIds list (add, don't replace) — max 2
            let currentIds = Array.isArray(seance.enseignantIds) ? [...seance.enseignantIds] : [];
            const newId = parseInt(element.id);

            if (currentIds.includes(newId)) {
                showToast("Cet enseignant est déjà assigné à cette séance.", "warning");
                return;
            }
            if (currentIds.length >= 2) {
                showToast("Maximum 2 enseignants par séance.", "warning");
                return;
            }
            currentIds.push(newId);

            const updateDto = {
                idSeance: seance.idSeance,
                date: seance.date,
                heureDebut: seance.heureDebut,
                heureFin: seance.heureFin,
                statut: seance.statut,
                coursId: seance.coursId,
                enseignantIds: currentIds,
                emploiDuTempsId: seance.emploiDuTempsId,
                couleur: seance.couleur,
                groupId: seance.groupId
            };

            const resPut = await fetch(`${API_BASE_URL}/seances/${dbId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(updateDto)
            });

            if (resPut.ok) {
                // Resolve names from matieres data
                const matiere = matieres.find(m => m.id == coursBlock.dataset.matiereId);
                const allNames = currentIds.map(eid => {
                    if (matiere && matiere.enseignants) {
                        const ens = matiere.enseignants.find(e => e.id == eid);
                        if (ens) return `${ens.nom} ${ens.prenom || ''}`.trim();
                    }
                    return null;
                }).filter(Boolean);

                coursBlock.dataset.enseignants = allNames.join(',');
                coursBlock.dataset.enseignantIds = JSON.stringify(currentIds);
                let ensDiv = coursBlock.querySelector('.enseignants');
                const ensHtml = allNames.map(n => `<span>${n}</span>`).join(' / ');
                if (ensDiv) {
                    ensDiv.innerHTML = ensHtml;
                } else {
                    const newDiv = document.createElement('div');
                    newDiv.className = 'enseignants';
                    newDiv.innerHTML = ensHtml;
                    coursBlock.appendChild(newDiv);
                }
                showToast(`Enseignant ${element.nom} assigné (${currentIds.length}/2).`, "success");
            }
        } catch (e) { console.error(e); }
    }
}


function createCourseElement(cell, data, rowspan = 1, colspan = 1) {
    cell.innerHTML = '';
    const div = document.createElement('div');
    div.className = 'course-slot';
    div.style.backgroundColor = data.couleur;
    div.dataset.type = data.type;
    div.dataset.matiere = data.matiere || '';
    div.dataset.matiereId = data.matiereId || '';
    div.dataset.enseignants = data.enseignants ? data.enseignants.join(',') : '';
    div.dataset.rowspan = rowspan;
    div.dataset.colspan = colspan;
    div.dataset.dbId = data.dbId;
    div.dataset.linkedIds = data.linkedIds ? JSON.stringify(data.linkedIds) : JSON.stringify([data.dbId]);
    div.dataset.groupId = data.groupId || '';
    div.dataset.couleur = data.couleur || '';

    let content = `<div class="matiere-nom">${data.matiere}</div>`;
    if (data.enseignants && data.enseignants.length > 0 && data.enseignants[0]) {
        content += `<div class="enseignants">${data.enseignants.map(e => `<span>${e}</span>`).join(' / ')}</div>`;
    }

    const durationLabel = [];
    if (rowspan > 1) durationLabel.push(`${rowspan}h`);
    if (colspan > 1) durationLabel.push(`${colspan}j`);
    if (durationLabel.length > 0) {
        content += `<span style="font-size:0.6rem; display:block;">${durationLabel.join(' × ')}</span>`;
    }

    // ── Bouton "Appel" si la séance est aujourd'hui dans la fenêtre ±15 min ────────────
    if (data.date && data.heureDebut && data.heureFin) {
        const today   = new Date().toISOString().split('T')[0];
        const dateStr = typeof data.date === 'string' ? data.date : String(data.date);
        if (dateStr === today) {
            const toMin = t => { const p = String(t).split(':').map(Number); return p[0] * 60 + (p[1] || 0); };
            const nowMin  = new Date().getHours() * 60 + new Date().getMinutes();
            const inWindow = nowMin >= toMin(data.heureDebut) - 15 && nowMin <= toMin(data.heureFin) + 15;
            if (inWindow) {
                content += `<a href="/enseignant/appel?seanceId=${data.dbId}"
                               onclick="event.stopPropagation()"
                               class="appel-link-btn"
                               title="Démarrer l'appel de cette séance">
                               🎓 Appel
                            </a>`;
            }
        }
    }

    content += `<div class="resize-handle rh-top" title="Étirer vers le haut"></div>`;
    content += `<div class="resize-handle rh-bottom" title="Étirer vers le bas"></div>`;
    content += `<div class="resize-handle rh-left" title="Étirer vers la gauche"></div>`;
    content += `<div class="resize-handle rh-right" title="Étirer vers la droite"></div>`;

    div.innerHTML = content;
    cell.appendChild(div);

    // Clic gauche : ouvrir le modal d'édition de la séance
    // Guard: ignore the click that is fired immediately after a resize drag ends.
    div.addEventListener('click', (e) => {
        e.stopPropagation();
        if (_wasResizing) {
            _wasResizing = false; // reset after consuming once
            return;
        }
        openSeanceConfigModalForEdit(div, cell);
    });

    // Clic droit : color picker pour changer la couleur de la séance
    div.addEventListener('contextmenu', (e) => {
        e.preventDefault();
        e.stopPropagation();
        const picker = document.createElement('input');
        picker.type = 'color';
        picker.value = data.couleur || '#0f22f1';
        picker.style.cssText = 'position:fixed;opacity:0;pointer-events:none;';
        document.body.appendChild(picker);
        picker.addEventListener('input', async (ev) => {
            const newColor = ev.target.value;
            div.style.backgroundColor = newColor;
            div.dataset.couleur = newColor;
            // Mettre à jour en base toutes les séances liées
            const ids = JSON.parse(div.dataset.linkedIds || '[]');
            for (const id of ids) {
                try {
                    const r = await fetch(`${API_BASE_URL}/seances/${id}`);
                    if (!r.ok) continue;
                    const seance = await r.json();
                    seance.couleur = newColor;
                    await fetch(`${API_BASE_URL}/seances/${id}`, {
                        method: 'PUT',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(seance)
                    });
                } catch (err) { console.error(err); }
            }
            showToast('Couleur mise à jour.', 'success');
        });
        picker.addEventListener('change', () => picker.remove());
        picker.click();
    });

    if (rowspan > 1 || colspan > 1) {
        mergeCells(cell, rowspan, colspan);
    }

    attachResizeHandlers(div, cell);
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

function unmergeCells(startCell, rowspan, colspan) {
    const sj = parseInt(startCell.dataset.jour);
    const sh = parseInt(startCell.dataset.horaire);
    for (let h = 0; h < rowspan; h++) {
        for (let j = 0; j < colspan; j++) {
            const cell = document.querySelector(`td[data-horaire="${sh + h}"][data-jour="${sj + j}"]`);
            if (cell) {
                if (h !== 0 || j !== 0) {
                    cell.innerHTML = '';
                    cell.style.display = '';
                }
                cell.rowSpan = 1;
                cell.colSpan = 1;
            }
        }
    }
    startCell.rowSpan = 1;
    startCell.colSpan = 1;
}



let resizeState = {
    active: false,
    direction: null,
    anchorCell: null,
    element: null,
    origJour: 0,
    origHoraire: 0,
    origRowspan: 1,
    origColspan: 1,
    previewRowspan: 1,
    previewColspan: 1,
    previewJour: 0,
    previewHoraire: 0
};

// Flag used to prevent the click event from opening the modal when the user
// just finished a resize drag. Set to true in handleResizeEnd, cleared after
// the next click event has been evaluated.
let _wasResizing = false;

function attachResizeHandlers(element, cell) {
    const handleMap = {
        '.rh-top': 'top',
        '.rh-bottom': 'bottom',
        '.rh-left': 'left',
        '.rh-right': 'right'
    };
    Object.entries(handleMap).forEach(([selector, direction]) => {
        const handle = element.querySelector(selector);
        if (handle) {
            handle.addEventListener('mousedown', (e) => {
                e.preventDefault();
                e.stopPropagation();
                startResize(direction, cell, element);
            });
        }
    });
}

function startResize(direction, cell, element) {
    const origJour = parseInt(cell.dataset.jour);
    const origHoraire = parseInt(cell.dataset.horaire);
    const origRowspan = parseInt(element.dataset.rowspan) || 1;
    const origColspan = parseInt(element.dataset.colspan) || 1;

    resizeState = {
        active: true,
        direction,
        anchorCell: cell,
        element,
        origJour,
        origHoraire,
        origRowspan,
        origColspan,
        previewRowspan: origRowspan,
        previewColspan: origColspan,
        previewJour: origJour,
        previewHoraire: origHoraire
    };

    document.addEventListener('mousemove', handleResizeMove);
    document.addEventListener('mouseup', handleResizeEnd);
    document.body.style.userSelect = 'none';
}

/**
 * Trouve la cellule de la grille sous le pointeur en utilisant la géométrie
 * du tableau — fonctionne même sur les cellules masquées (display:none).
 */
function getCellFromPoint(clientX, clientY) {
    const table = document.getElementById('timetable');
    if (!table) return null;

    // Déterminer la colonne (jour) à partir des <th> du header
    const ths = Array.from(document.querySelectorAll('#timetableHeaders th'));
    let dayIdx = -1;
    for (let j = 1; j < ths.length; j++) {
        const r = ths[j].getBoundingClientRect();
        if (clientX >= r.left && clientX <= r.right) {
            dayIdx = j - 1;
            break;
        }
    }
    if (dayIdx < 0) return null;

    // Déterminer la ligne (horaire) à partir des <tr> du tbody
    const rows = table.querySelectorAll('tbody tr');
    let horaireIdx = -1;
    for (let h = 0; h < rows.length; h++) {
        const r = rows[h].getBoundingClientRect();
        if (clientY >= r.top && clientY <= r.bottom) {
            horaireIdx = h;
            break;
        }
    }
    if (horaireIdx < 0) return null;

    return document.querySelector(`td[data-horaire="${horaireIdx}"][data-jour="${dayIdx}"]`) || null;
}

function handleResizeMove(e) {
    if (!resizeState.active) return;

    const targetCell = getCellFromPoint(e.clientX, e.clientY);
    if (!targetCell) return;

    const targetJ = parseInt(targetCell.dataset.jour);
    const targetH = parseInt(targetCell.dataset.horaire);
    const { origJour, origHoraire, origRowspan, origColspan, direction } = resizeState;
    const nbJours = currentEdt ? (currentEdt.nombreJours || 6) : 6;

    let newJour     = resizeState.previewJour;
    let newHoraire  = resizeState.previewHoraire;
    let newRowspan  = resizeState.previewRowspan;
    let newColspan  = resizeState.previewColspan;

    if (direction === 'bottom') {
        newJour    = origJour;
        newHoraire = origHoraire;
        newRowspan = Math.max(1, targetH - origHoraire + 1);
        newRowspan = Math.min(newRowspan, horaires.length - origHoraire);
        newColspan = origColspan;
    } else if (direction === 'top') {
        const bottomH = origHoraire + origRowspan - 1;
        newHoraire    = Math.min(targetH, bottomH);
        newHoraire    = Math.max(0, newHoraire);
        newRowspan    = bottomH - newHoraire + 1;
        newRowspan    = Math.max(1, newRowspan);
        newJour       = origJour;
        newColspan    = origColspan;
    } else if (direction === 'right') {
        newJour    = origJour;
        newHoraire = origHoraire;
        newColspan = Math.max(1, targetJ - origJour + 1);
        newColspan = Math.min(newColspan, nbJours - origJour);
        newRowspan = origRowspan;
    } else if (direction === 'left') {
        const rightJ = origJour + origColspan - 1;
        newJour      = Math.min(targetJ, rightJ);
        newJour      = Math.max(0, newJour);
        newColspan   = rightJ - newJour + 1;
        newColspan   = Math.max(1, newColspan);
        newHoraire   = origHoraire;
        newRowspan   = origRowspan;
    }

    if (newRowspan !== resizeState.previewRowspan || newColspan !== resizeState.previewColspan ||
        newJour !== resizeState.previewJour || newHoraire !== resizeState.previewHoraire) {

        // Réinitialiser tous les highlights
        document.querySelectorAll('.dropzone').forEach(c => {
            c.classList.remove('resize-active', 'resize-shrink');
        });

        let canResize = true;

        // Vérifier que les cellules du nouveau bloc sont libres ou font partie du bloc courant
        for (let h = 0; h < newRowspan; h++) {
            for (let j = 0; j < newColspan; j++) {
                const c = document.querySelector(`td[data-horaire="${newHoraire + h}"][data-jour="${newJour + j}"]`);
                if (!c) { canResize = false; break; }
                const isPartOfCurrent = isWithinBlock(c, origJour, origHoraire, origColspan, origRowspan);
                if (!isPartOfCurrent && (c.children.length > 0 || c.style.display === 'none')) {
                    canResize = false;
                }
            }
            if (!canResize) break;
        }

        if (canResize) {
            resizeState.previewRowspan = newRowspan;
            resizeState.previewColspan = newColspan;
            resizeState.previewJour    = newJour;
            resizeState.previewHoraire = newHoraire;

            // ── Cellules CONSERVÉES (dans le nouveau bloc) → resize-active (jaune) ──
            for (let h = 0; h < newRowspan; h++) {
                for (let j = 0; j < newColspan; j++) {
                    const c = document.querySelector(`td[data-horaire="${newHoraire + h}"][data-jour="${newJour + j}"]`);
                    if (c) c.classList.add('resize-active');
                }
            }

            // ── Cellules SUPPRIMÉES (dans l'ancien bloc mais PAS dans le nouveau) → resize-shrink (rouge) ──
            for (let h = 0; h < origRowspan; h++) {
                for (let j = 0; j < origColspan; j++) {
                    const ch = origHoraire + h;
                    const cj = origJour + j;
                    const inNew = (ch >= newHoraire && ch < newHoraire + newRowspan &&
                                   cj >= newJour    && cj < newJour    + newColspan);
                    if (!inNew) {
                        const c = document.querySelector(`td[data-horaire="${ch}"][data-jour="${cj}"]`);
                        if (c) c.classList.add('resize-shrink');
                    }
                }
            }

            // ── Afficher la taille courante sur le bloc ──────────────────────────
            const el = resizeState.element;
            if (el) {
                let sizeLabel = el.querySelector('.resize-size-label');
                if (!sizeLabel) {
                    sizeLabel = document.createElement('div');
                    sizeLabel.className = 'resize-size-label';
                    el.appendChild(sizeLabel);
                }
                const parts = [];
                if (newRowspan > 0) parts.push(`${newRowspan}h`);
                if (newColspan > 1) parts.push(`${newColspan}j`);
                sizeLabel.textContent = parts.join(' × ');
            }
        }
    }
}

function isWithinBlock(cell, blockJour, blockHoraire, blockColspan, blockRowspan) {
    const cj = parseInt(cell.dataset.jour);
    const ch = parseInt(cell.dataset.horaire);
    return cj >= blockJour && cj < blockJour + blockColspan &&
           ch >= blockHoraire && ch < blockHoraire + blockRowspan;
}

async function handleResizeEnd() {
    document.removeEventListener('mousemove', handleResizeMove);
    document.removeEventListener('mouseup', handleResizeEnd);
    document.body.style.userSelect = '';
    document.querySelectorAll('.dropzone').forEach(c => c.classList.remove('resize-active', 'resize-shrink'));

    // Retirer le label de taille live
    if (resizeState.element) {
        const lbl = resizeState.element.querySelector('.resize-size-label');
        if (lbl) lbl.remove();
    }

    if (!resizeState.active) return;
    resizeState.active = false;

    const { anchorCell, element, origJour, origHoraire, origRowspan, origColspan,
        previewRowspan, previewColspan, previewJour, previewHoraire } = resizeState;

    // Mark that a resize just ended so the following click event is ignored.
    _wasResizing = true;
    // Safety: reset the flag after a short delay in case no click follows
    // (e.g. mouseup outside the element).
    setTimeout(() => { _wasResizing = false; }, 300);

    if (previewRowspan === origRowspan && previewColspan === origColspan &&
        previewJour === origJour && previewHoraire === origHoraire) return;

    const dataSlot = {
        dbId: element.dataset.dbId,
        type: element.dataset.type,
        matiere: element.dataset.matiere,
        matiereId: element.dataset.matiereId,
        enseignants: element.dataset.enseignants ? element.dataset.enseignants.split(',').filter(e => e) : [],
        couleur: element.style.backgroundColor,
        linkedIds: JSON.parse(element.dataset.linkedIds || '[]')
    };

    unmergeCells(anchorCell, origRowspan, origColspan);

    const newHeureDebut = horaires[previewHoraire].debut;
    const newHeureFin = horaires[Math.min(previewHoraire + previewRowspan - 1, horaires.length - 1)].fin;

    const oldDays = [];
    for (let j = 0; j < origColspan; j++) oldDays.push(origJour + j);
    const newDays = [];
    for (let j = 0; j < previewColspan; j++) newDays.push(previewJour + j);

    const addedDays = newDays.filter(d => !oldDays.includes(d));
    const removedDays = oldDays.filter(d => !newDays.includes(d));
    const keptDays = newDays.filter(d => oldDays.includes(d));

    const groupId = element.dataset.groupId || `G-${Date.now()}`;
    let jourToId = {};
    try {
        const primaryId = dataSlot.linkedIds[0] || dataSlot.dbId;
        const resPrimary = await fetch(`${API_BASE_URL}/seances/${primaryId}`);
        if (!resPrimary.ok) throw new Error("Séance primaire introuvable");
        const primarySeance = await resPrimary.json();
        const couleur = element.dataset.couleur || primarySeance.couleur || dataSlot.couleur;

        for (let i = 0; i < dataSlot.linkedIds.length; i++) {
            if (i < oldDays.length) jourToId[oldDays[i]] = dataSlot.linkedIds[i];
        }

        for (const day of keptDays) {
            const seanceId = jourToId[day];
            if (!seanceId) continue;
            const dateForDay = getDateForJour(day);
            const updateDto = {
                idSeance: primarySeance.idSeance + (day !== origJour ? `-${day}` : ''),
                date: dateForDay,
                heureDebut: newHeureDebut,
                heureFin: newHeureFin,
                statut: primarySeance.statut,
                coursId: primarySeance.coursId,
                enseignantIds: primarySeance.enseignantIds || [],
                emploiDuTempsId: primarySeance.emploiDuTempsId,
                couleur: couleur,
                groupId: groupId
            };
            await fetch(`${API_BASE_URL}/seances/${seanceId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(updateDto)
            });
        }

        for (const day of addedDays) {
            const dateForDay = getDateForJour(day);
            const createDto = {
                idSeance: dataSlot.type === 'event' 
                            ? `EVT-${element.dataset.eventName.replace(/\s+/g, '_')}-${Date.now()}-${day}` 
                            : `S-${Date.now()}-${day}`,
                date: dateForDay,
                heureDebut: newHeureDebut,
                heureFin: newHeureFin,
                statut: primarySeance.statut,
                coursId: primarySeance.coursId,
                enseignantIds: primarySeance.enseignantIds || [],
                emploiDuTempsId: primarySeance.emploiDuTempsId,
                couleur: couleur,
                groupId: groupId
            };
            const res = await fetch(`${API_BASE_URL}/seances`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(createDto)
            });
            if (res.ok) {
                const created = await res.json();
                jourToId[day] = created.id;
            }
        }

        for (const day of removedDays) {
            const seanceId = jourToId[day];
            if (!seanceId) continue;
            await fetch(`${API_BASE_URL}/seances/${seanceId}`, { method: 'DELETE' });
            delete jourToId[day];
        }

    } catch (e) {
        console.error("Erreur resize API:", e);
    }

    const newLinkedIds = newDays.map(d => jourToId[d]).filter(id => id);
    const newCell = document.querySelector(`td[data-horaire="${previewHoraire}"][data-jour="${previewJour}"]`);
    if (newCell) {
        dataSlot.linkedIds = newLinkedIds;
        dataSlot.dbId = newLinkedIds[0] || dataSlot.dbId;
        createCourseElement(newCell, dataSlot, previewRowspan, previewColspan);
    }
}


timetableBody.addEventListener('dblclick', async function (e) {
    const td = e.target.closest('td');
    if (td && td.children.length) {
        const confirmed = await showConfirm("Supprimer cette séance ?", "La séance sera supprimée définitivement.");
        if (!confirmed) return;

        const cours = td.children[0];
        const linkedIds = JSON.parse(cours.dataset.linkedIds || '[]');
        const rowspan = parseInt(cours.dataset.rowspan) || 1;
        const colspan = parseInt(cours.dataset.colspan) || 1;

        for (const id of linkedIds) {
            try {
                await fetch(`${API_BASE_URL}/seances/${id}`, { method: 'DELETE' });
            } catch (err) { console.error(err); }
        }

        unmergeCells(td, rowspan, colspan);
        td.innerHTML = '';
        showToast("Séance supprimée.", "success");
    }
});


document.getElementById('exportPdfBtn').addEventListener('click', function () {
    if (!currentEdt) return;

    // Create a wrapper div to contain the header + the timetable
    const wrapper = document.createElement('div');
    // Using 1200px width ensures a generous aspect ratio layout for A4 Landscape
    wrapper.style.width = '1200px'; 
    wrapper.style.padding = '20px';
    wrapper.style.backgroundColor = 'white';
    wrapper.style.color = '#333';

    // Build Header text
    const anneeBadge = document.getElementById('editorAnneeBadge');
    const semestreBadge = document.getElementById('editorSemestreBadge');
    let annSemTxt = '';
    if (anneeBadge && anneeBadge.style.display !== 'none') annSemTxt += anneeBadge.textContent;
    if (semestreBadge && semestreBadge.style.display !== 'none') annSemTxt += (annSemTxt ? ' | ' : '') + semestreBadge.textContent;

    const headerHtml = `
        <div style="text-align: center; margin-bottom: 25px; border-bottom: 2px solid #4e73df; padding-bottom: 15px; font-family: sans-serif;">
            <h2 style="margin: 0 0 10px 0; color: #2c3e50; font-size: 26px; font-weight: bold;">Emploi du temps - ${classeDisplay.textContent}</h2>
            <p style="margin: 5px 0; font-size: 18px; font-weight: 500;">
                Du ${debutSemaineText.textContent} au ${finSemaineText.textContent}
            </p>
            ${annSemTxt ? `<p style="margin: 5px 0; font-size: 15px; color: #666;">${annSemTxt}</p>` : ''}
        </div>
    `;
    wrapper.innerHTML = headerHtml;

    // Clone the timetable table itself (not the whole card, to remove surrounding UI)
    const timetable = document.getElementById('timetable');
    const cloneTable = timetable.cloneNode(true);

    // Clean up UI-specific elements
    cloneTable.querySelectorAll('.resize-handle').forEach(h => h.style.display = 'none');
    cloneTable.querySelectorAll('.course-slot[style*="opacity: 0.5"]').forEach(pill => pill.remove());

    // Basic scale normalizations strictly for print clarity without breaking Absolute positioning
    cloneTable.style.width = '100%';
    cloneTable.style.tableLayout = 'fixed';
    
    // Reduce row height slightly to ensure the overall height avoids pagebreaks
    cloneTable.querySelectorAll('td').forEach(td => {
        td.style.height = '60px'; // Slightly smaller than the UI's 65px
        // Notice we do NOT mess with .course-slot positioning.
        // It remains 'absolute' as dictated by the original CSS which works perfectly.
    });
    
    // Force pill text to be nicely nested in case of small overlaps
    cloneTable.querySelectorAll('.course-slot').forEach(pill => {
       pill.style.boxShadow = 'none'; // Cleaner print without shadows
    });

    wrapper.appendChild(cloneTable);

    // Configuration for a single A4 landscape page fit
    const opt = {
        margin: [5, 5, 5, 5], // Small margins to maximize printable area
        filename: `emploi_du_temps_${classeDisplay.textContent.replace(/\s+/g, '_')}_S${currentEdt.id}.pdf`,
        image: { type: 'jpeg', quality: 1 },
        html2canvas: {
            scale: 2,
            windowWidth: 1200, 
            useCORS: true,
            logging: false
        },
        jsPDF: { unit: 'mm', format: 'a4', orientation: 'landscape' },
        pagebreak: { avoid: 'tr' }
    };

    html2pdf().set(opt).from(wrapper).save();
});

// Bouton "Enregistrer" — les séances sont déjà sauvées en temps réel, on affiche juste un toast de confirmation
document.getElementById('saveBtn')?.addEventListener('click', function () {
    showToast("Emploi du temps enregistré avec succès !", "success");
});


// ============ SEANCE CONFIG MODAL FUNCTIONS ============

function openSeanceConfigModal(existingSeanceId, cell, dropElement) {
    const modal = new bootstrap.Modal(document.getElementById('seanceConfigModal'));
    document.getElementById('seanceEditId').value = existingSeanceId || '';
    document.getElementById('seanceEditGroupId').value = '';
    document.getElementById('seanceCellJour').value = cell.dataset.jour;
    document.getElementById('seanceCellHoraire').value = cell.dataset.horaire;
    document.getElementById('seanceDeleteBtn').style.display = existingSeanceId ? '' : 'none';
    document.getElementById('seanceConfigTitle').innerHTML = existingSeanceId
        ? '<i class="bi bi-pencil me-2"></i>Modifier la séance'
        : '<i class="bi bi-plus-circle me-2"></i>Nouvelle séance';

    // Populate matière select
    populateSeanceMatiereSelect(dropElement ? parseInt(dropElement.id) : null);

    // Couleur
    const defaultColor = dropElement ? (dropElement.couleur || '#0f22f1') : '#0f22f1';
    document.getElementById('seanceCouleurInput').value = defaultColor;

    modal.show();
}

function openSeanceConfigModalForEdit(div, cell) {
    const modal = new bootstrap.Modal(document.getElementById('seanceConfigModal'));
    const dbId = div.dataset.dbId;
    const matiereId = div.dataset.matiereId;
    const linkedIds = JSON.parse(div.dataset.linkedIds || '[]');
    const groupId = div.dataset.groupId || '';

    document.getElementById('seanceEditId').value = dbId;
    document.getElementById('seanceEditGroupId').value = groupId;
    document.getElementById('seanceCellJour').value = cell.dataset.jour;
    document.getElementById('seanceCellHoraire').value = cell.dataset.horaire;
    document.getElementById('seanceDeleteBtn').style.display = '';
    document.getElementById('seanceConfigTitle').innerHTML = '<i class="bi bi-pencil me-2"></i>Modifier la séance';

    // Populate matière select
    populateSeanceMatiereSelect(parseInt(matiereId));

    // Couleur
    const couleur = div.dataset.couleur || div.style.backgroundColor || '#0f22f1';
    document.getElementById('seanceCouleurInput').value = rgbToHex(couleur);

    // Pre-check enseignants
    setTimeout(() => {
        const ensIds = JSON.parse(div.dataset.enseignantIds || '[]');
        ensIds.forEach(eid => {
            const cb = document.querySelector(`#seanceEnseignantsList input[value="${eid}"]`);
            if (cb) cb.checked = true;
        });
    }, 100);

    // Store linkedIds for delete
    document.getElementById('seanceEditId').dataset.linkedIds = JSON.stringify(linkedIds);

    modal.show();
}

function populateSeanceMatiereSelect(selectedId) {
    const sel = document.getElementById('seanceMatiereSelect');
    sel.innerHTML = '<option value="">-- Sélectionner --</option>';
    matieres.forEach(m => {
        const opt = document.createElement('option');
        opt.value = m.id;
        opt.textContent = m.intitule;
        if (m.id === selectedId) opt.selected = true;
        sel.appendChild(opt);
    });
    // Trigger enseignant list population
    populateSeanceEnseignants(selectedId);
    sel.addEventListener('change', (e) => {
        populateSeanceEnseignants(parseInt(e.target.value));
    });
}

function populateSeanceEnseignants(matiereId) {
    const container = document.getElementById('seanceEnseignantsList');
    const matiere = matieres.find(m => m.id == matiereId);
    if (!matiere || !matiere.enseignants || matiere.enseignants.length === 0) {
        container.innerHTML = '<p class="text-muted small mb-0">Aucun enseignant pour cette matière.</p>';
        return;
    }
    container.innerHTML = '';
    matiere.enseignants.forEach(e => {
        const div = document.createElement('div');
        div.className = 'form-check';
        div.innerHTML = `
            <input class="form-check-input" type="checkbox" value="${e.id}" id="seanceEns_${e.id}">
            <label class="form-check-label" for="seanceEns_${e.id}">
                <i class="fas fa-chalkboard-teacher me-1"></i>${e.nom} ${e.prenom || ''}
            </label>
        `;
        container.appendChild(div);
    });
}

function rgbToHex(color) {
    if (color.startsWith('#')) return color;
    const match = color.match(/\d+/g);
    if (!match || match.length < 3) return '#0f22f1';
    return '#' + match.slice(0, 3).map(c => parseInt(c).toString(16).padStart(2, '0')).join('');
}

// SAVE handler for seance config modal
document.getElementById('seanceSaveBtn')?.addEventListener('click', async () => {
    const matiereId = parseInt(document.getElementById('seanceMatiereSelect').value);
    if (!matiereId) {
        showToast('Veuillez sélectionner une matière.', 'warning');
        return;
    }

    const jourIdx = parseInt(document.getElementById('seanceCellJour').value);
    const horaireIdx = parseInt(document.getElementById('seanceCellHoraire').value);
    const couleur = document.getElementById('seanceCouleurInput').value;
    const existingId = document.getElementById('seanceEditId').value;

    // Collect checked enseignant IDs
    const enseignantIds = [];
    document.querySelectorAll('#seanceEnseignantsList input:checked').forEach(cb => {
        enseignantIds.push(parseInt(cb.value));
    });

    const dateSeance = getDateForJour(jourIdx);
    const heureDebut = horaires[horaireIdx].debut;
    const heureFin = horaires[horaireIdx].fin;

    if (!dateSeance) {
        showToast('Impossible de déterminer la date.', 'warning');
        return;
    }

    const matiere = matieres.find(m => m.id == matiereId);
    const matiereNom = matiere ? matiere.intitule : 'Séance';

    try {
        if (existingId) {
            // UPDATE existing seance
            const resGet = await fetch(`${API_BASE_URL}/seances/${existingId}`);
            if (!resGet.ok) { showToast('Séance introuvable.', 'danger'); return; }
            const seance = await resGet.json();

            const updateDto = {
                idSeance: seance.idSeance,
                date: seance.date,
                heureDebut: seance.heureDebut,
                heureFin: seance.heureFin,
                statut: seance.statut,
                coursId: matiereId,
                enseignantIds: enseignantIds,
                emploiDuTempsId: seance.emploiDuTempsId,
                couleur: couleur,
                groupId: seance.groupId
            };

            const resPut = await fetch(`${API_BASE_URL}/seances/${existingId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(updateDto)
            });

            if (resPut.ok) {
                showToast('Séance mise à jour.', 'success');
            } else {
                showToast('Erreur lors de la mise à jour.', 'danger');
            }
        } else {
            // CREATE new seance
            const groupId = `G-${Date.now()}`;
            const dto = {
                idSeance: `S-${Date.now()}`,
                date: dateSeance,
                heureDebut: heureDebut,
                heureFin: heureFin,
                statut: false,
                coursId: matiereId,
                enseignantIds: enseignantIds,
                emploiDuTempsId: currentEdt.id,
                couleur: couleur,
                groupId: groupId
            };

            const res = await fetch(`${API_BASE_URL}/seances`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(dto)
            });

            if (res.ok) {
                showToast(`${matiereNom} ajouté.`, 'success');
            } else {
                const errText = await res.text();
                showToast('Erreur : ' + errText, 'danger');
            }
        }

        // Refresh the grid
        bootstrap.Modal.getInstance(document.getElementById('seanceConfigModal'))?.hide();
        buildTimetableGrid(currentEdt.nombreJours || 6);
        await fetchSeancesForEdt(currentEdt.id);
    } catch (e) {
        console.error(e);
        showToast('Erreur réseau.', 'danger');
    }
});

// DELETE handler for seance config modal
document.getElementById('seanceDeleteBtn')?.addEventListener('click', async () => {
    const dbId = document.getElementById('seanceEditId').value;
    if (!dbId) return;

    if (!confirm('Supprimer cette séance ?')) return;

    const linkedIds = JSON.parse(document.getElementById('seanceEditId').dataset.linkedIds || '[]');
    const idsToDelete = linkedIds.length > 0 ? linkedIds : [parseInt(dbId)];

    try {
        for (const id of idsToDelete) {
            await fetch(`${API_BASE_URL}/seances/${id}`, { method: 'DELETE' });
        }
        showToast('Séance(s) supprimée(s).', 'success');

        bootstrap.Modal.getInstance(document.getElementById('seanceConfigModal'))?.hide();
        buildTimetableGrid(currentEdt.nombreJours || 6);
        await fetchSeancesForEdt(currentEdt.id);
    } catch (e) {
        console.error(e);
        showToast('Erreur lors de la suppression.', 'danger');
    }
});


// ── Événement spécial : création d'un block visuel dans la grille ──
function createEventElement(cell, eventData, rowspan = 1, colspan = 1) {
    cell.innerHTML = '';
    const div = document.createElement('div');
    div.className = 'course-slot';
    div.style.backgroundColor = eventData.couleur || '#d97706';
    div.dataset.type = 'event';
    div.dataset.eventName = eventData.nom || 'Événement';
    div.dataset.couleur = eventData.couleur || '#d97706';
    div.dataset.rowspan = rowspan;
    div.dataset.colspan = colspan;
    if (eventData.dbId) {
        div.dataset.dbId = eventData.dbId;
        div.dataset.linkedIds = eventData.linkedIds ? JSON.stringify(eventData.linkedIds) : JSON.stringify([eventData.dbId]);
    }

    const iconClass = eventData.icon || 'calendar-alt';
    div.innerHTML = `
        <div class="matiere-nom"><i class="fas fa-${iconClass} me-1"></i>${eventData.nom}</div>
        <div class="resize-handle rh-top" title="Étirer vers le haut"></div>
        <div class="resize-handle rh-bottom" title="Étirer vers le bas"></div>
        <div class="resize-handle rh-left" title="Étirer vers la gauche"></div>
        <div class="resize-handle rh-right" title="Étirer vers la droite"></div>
    `;

    cell.appendChild(div);

    // Clic pour supprimer (avec suppression en BD si dbId existe)
    div.addEventListener('click', async (e) => {
        e.stopPropagation();
        if (!confirm(`Retirer "${eventData.nom}" de cette case ?`)) return;

        const ids = JSON.parse(div.dataset.linkedIds || '[]');
        if (ids.length > 0) {
            try {
                for (const id of ids) {
                    await fetch(`${API_BASE_URL}/seances/${id}`, { method: 'DELETE' });
                }
            } catch (err) { console.error('Erreur suppression événement:', err); }
        } else if (div.dataset.dbId) {
            try {
                await fetch(`${API_BASE_URL}/seances/${div.dataset.dbId}`, { method: 'DELETE' });
            } catch (err) { console.error('Erreur suppression événement:', err); }
        }
        cell.innerHTML = '';
        showToast(`"${eventData.nom}" supprimé.`, 'info');
    });

    // Clic droit pour changer la couleur
    div.addEventListener('contextmenu', (e) => {
        e.preventDefault();
        e.stopPropagation();
        const picker = document.createElement('input');
        picker.type = 'color';
        picker.value = eventData.couleur || '#d97706';
        picker.style.cssText = 'position:fixed;opacity:0;pointer-events:none;';
        document.body.appendChild(picker);
        picker.click();
        picker.addEventListener('input', async (ev) => {
            const newColor = ev.target.value;
            div.style.backgroundColor = newColor;
            div.dataset.couleur = newColor;
            // Mettre à jour en base toutes les séances liées
            const ids = JSON.parse(div.dataset.linkedIds || '[]');
            for (const id of ids) {
                try {
                    const r = await fetch(`${API_BASE_URL}/seances/${id}`);
                    if (!r.ok) continue;
                    const seance = await r.json();
                    seance.couleur = newColor;
                    await fetch(`${API_BASE_URL}/seances/${id}`, {
                        method: 'PUT',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(seance)
                    });
                } catch (err) { console.error(err); }
            }
            showToast('Couleur mise à jour.', 'success');
        });
        picker.addEventListener('change', () => picker.remove());
    });

    if (rowspan > 1 || colspan > 1) {
        mergeCells(cell, rowspan, colspan);
    }
    attachResizeHandlers(div, cell);
}

// ── Bouton ajout événement custom ──
document.addEventListener('DOMContentLoaded', () => {
    const addBtn = document.getElementById('addCustomEventBtn');
    if (addBtn) {
        addBtn.addEventListener('click', () => {
            const nameInput = document.getElementById('customEventName');
            const colorInput = document.getElementById('customEventColor');
            const name = nameInput.value.trim();
            if (!name) { showToast('Veuillez saisir un nom pour l\'événement.', 'warning'); return; }
            const color = colorInput.value || '#6366f1';

            const container = document.getElementById('evenementsContainer');
            const badge = document.createElement('span');
            badge.className = 'element-pill';
            badge.style.cssText = `background:${color};cursor:grab;`;
            badge.setAttribute('draggable', 'true');
            badge.dataset.json = JSON.stringify({type:'event', nom:name, couleur:color, icon:'calendar-alt'});
            badge.innerHTML = `<i class="fas fa-calendar-alt me-1"></i>${name}`;
            container.appendChild(badge);

            nameInput.value = '';
            showToast(`Événement "${name}" ajouté.`, 'success');
        });
    }
});

document.addEventListener('DOMContentLoaded', init);