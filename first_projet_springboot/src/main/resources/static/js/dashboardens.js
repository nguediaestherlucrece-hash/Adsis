/* ===== ADSIS – dashboardens.js — Dashboard enseignant dynamique ===== */
import { requireRole, authFetch, getUser } from './auth.js';

// Guard : seuls les ENSEIGNANTs accèdent à cette page
if (!requireRole(['ENSEIGNANT'])) { throw new Error('Unauthorized'); }

// Anime le défilement d'un nombre (0 jusqu'à la valeur finale)
function animateCount(el, target, suffix = '', duration = 700) {
    if (!el) return;
    const start = performance.now();
    const update = (now) => {
        const t = Math.min((now - start) / duration, 1);
        el.textContent = Math.round(t * target) + suffix;
        if (t < 1) requestAnimationFrame(update);
    };
    requestAnimationFrame(update);
}

// Formate et affiche la date actuelle
document.addEventListener('DOMContentLoaded', () => {
    const dateEl = document.getElementById('page-date');
    if (dateEl) {
        const now = new Date();
        dateEl.textContent = now.toLocaleDateString('fr-FR', {
            weekday: 'long', day: 'numeric', month: 'long', year: 'numeric'
        });
    }
    loadDashboard();
});

async function loadDashboard() {
    const user = getUser();
    if (!user || !user.id) {
        console.error('Aucun utilisateur connecté');
        return;
    }

    try {
        const res = await authFetch(`/api/dashboard/enseignant/${user.id}`);
        if (!res || !res.ok) {
            console.error('Erreur chargement dashboard:', res?.status);
            renderEmptyDashboard();
            return;
        }

        const data = await res.json();
        renderKPIs(data);
        renderNextCourse(data.prochainCours);
        renderCoursJour(data.coursJour || []);
    } catch (err) {
        console.error('Erreur dashboard:', err);
        renderEmptyDashboard();
    }
}

function renderKPIs(data) {
    animateCount(document.getElementById('stat-presence'), data.tauxPresence || 0, '%');
    animateCount(document.getElementById('stat-heures'), data.heuresSemaine || 0, 'h');
    animateCount(document.getElementById('stat-cours'), data.coursAujourdhui || 0);
    animateCount(document.getElementById('stat-etudiants'), data.etudiantsTotal || 0);
}

function renderNextCourse(cours) {
    const section = document.getElementById('next-course-section');
    if (!section) return;

    if (!cours) {
        section.innerHTML = `
            <div class="no-course-card">
                <p>🎉 Aucun cours à venir aujourd'hui</p>
            </div>`;
        return;
    }

    const heureDebut = cours.heureDebut ? cours.heureDebut.substring(0, 5) : '';
    const heureFin = cours.heureFin ? cours.heureFin.substring(0, 5) : '';

    section.innerHTML = `
        <div class="next-course-card">
            <div class="next-course-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/>
                    <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/>
                </svg>
            </div>
            <div class="next-course-info">
                <h3>${cours.coursIntitule || 'Cours'}</h3>
                <div class="next-course-meta">
                    ${cours.classeNom ? `<span class="course-badge"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>${cours.classeNom}</span>` : ''}
                    ${heureDebut ? `<span class="course-badge"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>${heureDebut} – ${heureFin}</span>` : ''}
                    ${cours.nombreTotal ? `<span class="course-badge"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="13" height="13"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>${cours.nombreTotal} étudiants</span>` : ''}
                </div>
            </div>
            <a href="/enseignant/appel?seanceId=${cours.id}" class="btn-appel">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                    <path d="M9 11l3 3L22 4"/>
                    <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
                </svg>
                Lancer l'appel
            </a>
        </div>`;
}

function renderCoursJour(cours) {
    const container = document.getElementById('today-list');
    if (!container) return;

    if (!cours || cours.length === 0) {
        container.innerHTML = `<div class="empty-today">Aucun cours aujourd'hui.</div>`;
        return;
    }

    const COLORS = ['#3B55E6', '#7C3AED', '#0d9488', '#f59e0b', '#ef4444', '#10b981', '#6366f1', '#f97316'];

    container.innerHTML = cours.map((c, i) => {
        const heureDebut = c.heureDebut ? c.heureDebut.substring(0, 5) : '';
        const heureFin = c.heureFin ? c.heureFin.substring(0, 5) : '';
        const color = c.couleur || COLORS[i % COLORS.length];
        const appelStatut = c.appelStatut === 'clôturé' ? '✅ Clôturé' : (c.appelStatut === 'ouvert' ? '🟢 En cours' : '');

        return `
            <a href="/enseignant/appel?seanceId=${c.id}" class="today-item" style="text-decoration:none;color:inherit;">
                <div class="today-strip" style="background:${color};"></div>
                <div class="today-info">
                    <div class="course-name">${c.coursIntitule || 'Cours'}</div>
                    <div class="course-sub">${c.classeNom || ''} ${appelStatut ? '· ' + appelStatut : ''}</div>
                </div>
                <div class="today-time">${heureDebut} – ${heureFin}</div>
            </a>`;
    }).join('');
}

function renderEmptyDashboard() {
    animateCount(document.getElementById('stat-presence'), 0, '%');
    animateCount(document.getElementById('stat-heures'), 0, 'h');
    animateCount(document.getElementById('stat-cours'), 0);
    animateCount(document.getElementById('stat-etudiants'), 0);

    const section = document.getElementById('next-course-section');
    if (section) {
        section.innerHTML = `
            <div class="no-course-card">
                <p>Impossible de charger les données. Vérifiez votre connexion.</p>
            </div>`;
    }

    const container = document.getElementById('today-list');
    if (container) {
        container.innerHTML = `<div class="empty-today">Aucune donnée disponible.</div>`;
    }
}
