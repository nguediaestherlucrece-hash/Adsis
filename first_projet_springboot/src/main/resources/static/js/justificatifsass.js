import { requireRole, getUser, authFetch } from './auth.js';

// Guard : réservé aux assistants pédagogiques autorisés
if (!requireRole(['ASSISTANT_PEDAGOGIQUE'])) {
    throw new Error('Unauthorized access');
}

const user = getUser();
const API_BASE_URL = 'http://localhost:8081/api';

// État global de la page
let currentJustificatifs = [];
let currentPage = 0;
const pageSize = 10;
let totalPages = 0;
let totalElements = 0;
let selectedJustificatif = null;
let detailModal = null;

// Messages de Notification toast
function showToast(message, type = 'info') {
    const cfg = {
        success: { icon: 'bi-check-circle-fill', label: 'Succès', bg: '#1a7f5a', delay: 3500 },
        danger:  { icon: 'bi-x-circle-fill',     label: 'Erreur', bg: '#c0392b', delay: 6000 },
        warning: { icon: 'bi-exclamation-triangle-fill', label: 'Attention', bg: '#d68910', delay: 5000 },
        info:    { icon: 'bi-info-circle-fill',   label: 'Info',   bg: '#1a6fa8', delay: 4000 },
    };
    const c = cfg[type] || cfg.info;
    const container = document.getElementById('toastContainer');
    if (!container) return;
    const el = document.createElement('div');
    el.className = 'toast border-0 rounded-3 shadow-lg overflow-hidden';
    el.setAttribute('role', 'alert');
    el.setAttribute('data-bs-delay', c.delay);
    el.style.minWidth = '300px';
    el.innerHTML = `
        <div class="toast-header border-0 text-white py-2 px-3" style="background:${c.bg}">
            <i class="bi ${c.icon} me-2"></i>
            <strong class="me-auto">${c.label}</strong>
            <button type="button" class="btn-close btn-close-white ms-2" data-bs-dismiss="toast"></button>
        </div>
        <div class="toast-body text-dark bg-white small py-2 px-3">${message}</div>`;
    container.appendChild(el);
    const toast = new bootstrap.Toast(el);
    toast.show();
    el.addEventListener('hidden.bs.toast', () => el.remove());
}

// Nettoyage et formatage HTML basique
function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
              .replace(/"/g, '&quot;').replace(/'/g, '&#039;');
}

function formatDate(dateStr) {
    if (!dateStr) return '—';
    try {
        const d = new Date(dateStr);
        return d.toLocaleDateString('fr-FR');
    } catch {
        return dateStr;
    }
}

// Charger les justificatifs avec filtres
async function loadJustificatifs() {
    try {
        const tbody = document.getElementById('justificatifsBody');
        const noData = document.getElementById('noDataMsg');
        tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4"><span class="spinner-border spinner-border-sm me-2"></span>Chargement...</td></tr>';
        
        const filterStatut = document.getElementById('filterStatut').value;
        const filterDateDebut = document.getElementById('filterDateDebut').value;
        const filterDateFin = document.getElementById('filterDateFin').value;

        const params = new URLSearchParams({
            assistantId: user.id,
            page: currentPage,
            size: pageSize
        });

        if (filterStatut) params.append('statut', filterStatut);
        if (filterDateDebut) params.append('dateSoumissionDebut', filterDateDebut);
        if (filterDateFin) params.append('dateSoumissionFin', filterDateFin);

        const res = await authFetch(`/api/justificatifs/assistant/all?${params}`);
        if (!res || !res.ok) {
            tbody.innerHTML = '';
            noData.style.display = '';
            return;
        }

        const data = await res.json();
        currentJustificatifs = data.content || [];
        totalPages = data.totalPages || 0;
        totalElements = data.totalElements || 0;

        // Si une recherche textuelle locale est saisie, on filtre le tableau
        const searchTerm = (document.getElementById('searchStudent').value || '').toLowerCase().trim();
        let itemsToRender = currentJustificatifs;
        
        if (searchTerm) {
            itemsToRender = currentJustificatifs.filter(j => {
                const nom = (j.etudiantNom || '').toLowerCase();
                const prenom = (j.etudiantPrenom || '').toLowerCase();
                return nom.includes(searchTerm) || prenom.includes(searchTerm);
            });
        }

        renderTable(itemsToRender);
        renderPagination();
        updatePendingCounter();
    } catch (err) {
        console.error(err);
        showToast('Impossible de charger les justificatifs.', 'danger');
    }
}

// Rendu du tableau
function renderTable(items) {
    const tbody = document.getElementById('justificatifsBody');
    const noData = document.getElementById('noDataMsg');

    if (items.length === 0) {
        tbody.innerHTML = '';
        noData.style.display = '';
        return;
    }
    noData.style.display = 'none';

    const statusClasses = {
        'EN_ATTENTE': 'en_attente',
        'ACCEPTE': 'accepte',
        'REJETE': 'rejete'
    };

    const statusLabels = {
        'EN_ATTENTE': 'En attente',
        'ACCEPTE': 'Accepté',
        'REJETE': 'Rejeté'
    };

    const statusIcons = {
        'EN_ATTENTE': '<i class="bi bi-hourglass-split me-1"></i>',
        'ACCEPTE': '<i class="bi bi-check-circle-fill me-1"></i>',
        'REJETE': '<i class="bi bi-x-circle-fill me-1"></i>'
    };

    tbody.innerHTML = items.map(j => {
        const studentInitials = ((j.etudiantNom || 'U').charAt(0) + (j.etudiantPrenom || '').charAt(0)).toUpperCase();
        const scClass = statusClasses[j.statutValidation] || 'en_attente';
        const scLabel = statusLabels[j.statutValidation] || j.statutValidation;
        const scIcon = statusIcons[j.statutValidation] || '';
        const seanceInfo = j.coursNom ? `${escapeHtml(j.coursNom)}` : 'N/A';
        const seanceDateTime = j.seanceDate ? `${formatDate(j.seanceDate)}` : '';

        return `
            <tr>
                <td>
                    <div class="d-flex align-items-center gap-2">
                        <span class="student-avatar">${studentInitials}</span>
                        <div>
                            <div class="fw-bold">${escapeHtml(j.etudiantNom)} ${escapeHtml(j.etudiantPrenom)}</div>
                            <small class="text-muted">${escapeHtml(j.etudiantMatricule || 'Sans matricule')}</small>
                        </div>
                    </div>
                </td>
                <td>
                    <div class="fw-semibold">${escapeHtml(j.motif)}</div>
                    <small class="text-muted d-block text-truncate" style="max-width: 200px;">${escapeHtml(j.commentaire || '')}</small>
                </td>
                <td>
                    <div class="fw-semibold text-primary">${seanceInfo}</div>
                    <small class="text-muted">${seanceDateTime} ${j.seanceHeureDebut ? '(' + j.seanceHeureDebut + ' - ' + j.seanceHeureFin + ')' : ''}</small>
                </td>
                <td>${formatDate(j.dateSoumission)}</td>
                <td>
                    <span class="status-badge ${scClass}">
                        ${scIcon}${scLabel}
                    </span>
                </td>
                <td class="text-end">
                    <button class="btn btn-primary btn-sm rounded-3 px-3 view-details-btn" data-id="${j.id}">
                        <i class="bi bi-eye me-1"></i>Dossier
                    </button>
                </td>
            </tr>
        `;
    }).join('');

    // Attacher les écouteurs d'événements aux boutons de détails
    document.querySelectorAll('.view-details-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const id = btn.getAttribute('data-id');
            openDetailModal(id);
        });
    });
}

// Rendu de la pagination
function renderPagination() {
    const controls = document.getElementById('paginationControls');
    const info = document.getElementById('paginationInfo');
    
    const start = totalElements > 0 ? currentPage * pageSize + 1 : 0;
    const end = Math.min(start + pageSize - 1, totalElements);
    
    info.textContent = totalElements > 0 
        ? `${start}–${end} de ${totalElements} justificatifs`
        : 'Aucun justificatif';

    controls.innerHTML = '';
    if (totalPages <= 1) return;

    // Précédent
    controls.innerHTML += `
        <li class="page-item ${currentPage === 0 ? 'disabled' : ''}">
            <a class="page-link" href="#" data-page="${currentPage - 1}">‹</a>
        </li>
    `;

    // Pages
    for (let i = 0; i < totalPages; i++) {
        controls.innerHTML += `
            <li class="page-item ${i === currentPage ? 'active' : ''}">
                <a class="page-link" href="#" data-page="${i}">${i + 1}</a>
            </li>
        `;
    }

    // Suivant
    controls.innerHTML += `
        <li class="page-item ${currentPage >= totalPages - 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" data-page="${currentPage + 1}">›</a>
        </li>
    `;

    // Attacher les clics de pagination
    controls.querySelectorAll('.page-link').forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const page = parseInt(link.getAttribute('data-page'));
            if (page >= 0 && page < totalPages) {
                currentPage = page;
                loadJustificatifs();
            }
        });
    });
}

// Mettre à jour le compteur dynamique de justificatifs en attente
async function updatePendingCounter() {
    try {
        const params = new URLSearchParams({
            assistantId: user.id,
            statut: 'EN_ATTENTE',
            page: 0,
            size: 1
        });
        const res = await authFetch(`/api/justificatifs/assistant/all?${params}`);
        if (res && res.ok) {
            const data = await res.json();
            const count = data.totalElements || 0;
            const badge = document.getElementById('pendingCounterBadge');
            if (badge) {
                badge.textContent = `${count} en attente`;
                if (count > 0) {
                    badge.className = 'badge bg-warning text-dark px-3 py-2 rounded-pill fw-bold';
                } else {
                    badge.className = 'badge bg-secondary text-white px-3 py-2 rounded-pill fw-bold';
                }
            }
        }
    } catch (err) {
        console.warn('Erreur lors de la mise à jour du compteur :', err);
    }
}

// Ouvre la modal de détail d'un justificatif
async function openDetailModal(id) {
    try {
        const res = await authFetch(`/api/justificatifs/${id}`);
        if (!res || !res.ok) {
            showToast('Impossible de récupérer les détails.', 'danger');
            return;
        }

        const j = await res.json();
        selectedJustificatif = j;

        // Remplir les informations
        document.getElementById('modalStudentInitials').textContent = ((j.etudiantNom || 'U').charAt(0) + (j.etudiantPrenom || '').charAt(0)).toUpperCase();
        document.getElementById('modalStudentName').textContent = `${j.etudiantNom} ${j.etudiantPrenom}`;
        document.getElementById('modalStudentInfo').textContent = `${j.etudiantMatricule || 'Matricule inconnu'} — ${j.etudiantClasse || 'Classe inconnue'}`;
        
        document.getElementById('modalMatiere').textContent = j.coursNom || 'Cours inconnu';
        
        const dateStr = j.seanceDate ? formatDate(j.seanceDate) : 'Date inconnue';
        const timeStr = j.seanceHeureDebut ? `de ${j.seanceHeureDebut} à ${j.seanceHeureFin}` : '';
        let seanceLine = `${dateStr} ${timeStr}`;

        // Ajout des heures précises si disponibles
        if (j.heuresAbsentes > 0 || j.heuresPresentes > 0) {
            const hAbs  = j.heuresAbsentes;
            const hPres = j.heuresPresentes;
            const totalH = hAbs + hPres;
            if (hPres > 0 && hAbs > 0) {
                // Présence partielle
                seanceLine += `\n${hAbs}h absente${hAbs > 1 ? 's' : ''} / ${hPres}h présente${hPres > 1 ? 's' : ''} sur ${totalH}h`;
            } else {
                seanceLine += `\n${hAbs}h absente${hAbs > 1 ? 's' : ''} sur ${totalH}h`;
            }
        }
        document.getElementById('modalSeanceDate').textContent = seanceLine;
        
        document.getElementById('modalMotif').textContent = j.motif;
        document.getElementById('modalCommentaire').textContent = j.commentaire || 'Aucun commentaire fourni par l\'étudiant.';

        // Vider le champ motif de rejet
        document.getElementById('rejectionReasonInput').value = '';

        // Complément d'information
        const complementSection = document.getElementById('modalComplementSection');
        if (j.complementCommentaire) {
            document.getElementById('modalComplementCommentaire').textContent = j.complementCommentaire;
            document.getElementById('modalComplementDate').textContent = `Fourni le : ${formatDate(j.dateComplement)}`;
            complementSection.style.display = 'block';
        } else {
            complementSection.style.display = 'none';
        }

        // Gestion de l'historique ou du formulaire
        const decisionSection = document.getElementById('decisionFormSection');
        const statusDetailSection = document.getElementById('modalStatusDetailSection');
        const modalStatusBadge = document.getElementById('modalStatusBadge');
        const modalValidationInfo = document.getElementById('modalValidationInfo');
        const modalMotifRejetDisplay = document.getElementById('modalMotifRejetDisplay');

        if (j.statutValidation === 'EN_ATTENTE') {
            decisionSection.style.display = 'block';
            statusDetailSection.style.display = 'none';
        } else {
            decisionSection.style.display = 'none';
            statusDetailSection.style.display = 'block';
            
            // Badge statut
            modalStatusBadge.textContent = j.statutValidation === 'ACCEPTE' ? 'Accepté' : 'Rejeté';
            modalStatusBadge.className = j.statutValidation === 'ACCEPTE' 
                ? 'badge bg-success text-white rounded-pill px-3 py-1' 
                : 'badge bg-danger text-white rounded-pill px-3 py-1';

            // Infos validateur
            const valDate = j.dateValidation ? formatDate(j.dateValidation) : 'Date inconnue';
            modalValidationInfo.textContent = `Dossier traité par ${escapeHtml(j.validateurNom || 'un assistant')} le ${valDate}`;

            // Motif de rejet
            if (j.statutValidation === 'REJETE' && j.motifRejet) {
                modalMotifRejetDisplay.style.display = 'block';
                modalMotifRejetDisplay.querySelector('span').textContent = j.motifRejet;
            } else {
                modalMotifRejetDisplay.style.display = 'none';
            }
        }

        // Pièce jointe — téléchargement uniquement (pas de prévisualisation iframe)
        const noDoc = document.getElementById('noDocPlaceholder');
        const downloadSection = document.getElementById('btnDownloadDocSection');
        const downloadBtn = document.getElementById('btnDownloadDoc');

        // Utiliser le document de complément en priorité s'il existe
        let docPath = j.complementDocumentUrl || j.documentUrl;

        if (docPath) {
            // S'assurer que le chemin commence par un '/'
            if (!docPath.startsWith('/') && !docPath.startsWith('http')) {
                docPath = '/' + docPath;
            }
            downloadBtn.href = docPath;
            downloadSection.style.display = 'block';
            noDoc.style.display = 'none';
        } else {
            noDoc.style.display = 'block';
            downloadSection.style.display = 'none';
        }

        // Afficher la modale
        if (!detailModal) {
            detailModal = new bootstrap.Modal(document.getElementById('justificatifDetailModal'));
        }
        detailModal.show();
    } catch (err) {
        console.error(err);
        showToast('Erreur lors du chargement des détails.', 'danger');
    }
}

// Soumission de la validation (Accepter / Rejeter)
async function submitDecision(statutValidation) {
    if (!selectedJustificatif) return;
    
    const reasonInput = document.getElementById('rejectionReasonInput');
    const reason = reasonInput.value.trim();

    if (statutValidation === 'REJETE') {
        if (!reason) {
            showToast('Le motif de rejet est obligatoire.', 'warning');
            reasonInput.focus();
            return;
        }
        if (reason.length < 10) {
            showToast('Le motif de rejet doit contenir au moins 10 caractères.', 'warning');
            reasonInput.focus();
            return;
        }
    }

    const payload = {
        statutValidation: statutValidation
    };

    if (statutValidation === 'REJETE') {
        payload.motifRejet = reason;
    }

    try {
        const res = await authFetch(`/api/justificatifs/${selectedJustificatif.id}/validate?assistantId=${user.id}`, {
            method: 'PUT',
            body: JSON.stringify(payload)
        });

        if (res && res.ok) {
            showToast(statutValidation === 'ACCEPTE' ? 'Justificatif validé avec succès.' : 'Justificatif rejeté.', 'success');
            if (detailModal) detailModal.hide();
            await loadJustificatifs();
        } else {
            const errText = await res.text();
            showToast('Erreur : ' + errText, 'danger');
        }
    } catch (err) {
        console.error(err);
        showToast('Erreur réseau lors de la validation.', 'danger');
    }
}

// Reset filtres
function resetFilters() {
    document.getElementById('searchStudent').value = '';
    document.getElementById('filterStatut').value = 'EN_ATTENTE';
    document.getElementById('filterDateDebut').value = '';
    document.getElementById('filterDateFin').value = '';
    currentPage = 0;
    loadJustificatifs();
}

// Initialisation au chargement du DOM
document.addEventListener('DOMContentLoaded', () => {
    loadJustificatifs();

    // Event listeners des filtres
    document.getElementById('filterStatut').addEventListener('change', () => {
        currentPage = 0;
        loadJustificatifs();
    });
    
    document.getElementById('filterDateDebut').addEventListener('change', () => {
        currentPage = 0;
        loadJustificatifs();
    });
    
    document.getElementById('filterDateFin').addEventListener('change', () => {
        currentPage = 0;
        loadJustificatifs();
    });

    document.getElementById('btnResetFilters').addEventListener('click', resetFilters);

    // Recherche avec debounce
    let debounceTimer;
    document.getElementById('searchStudent').addEventListener('input', () => {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(() => {
            currentPage = 0;
            loadJustificatifs();
        }, 400);
    });

    // Décisions boutons
    document.getElementById('btnAcceptJustificatif').addEventListener('click', () => {
        submitDecision('ACCEPTE');
    });

    document.getElementById('btnRejectJustificatif').addEventListener('click', () => {
        submitDecision('REJETE');
    });
});
