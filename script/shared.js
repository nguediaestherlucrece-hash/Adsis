/* ===== ADSIS – shared.js ===== */
import { getUser, logout, applyPermissionVisibility, getLoginPath } from './auth.js';

// ── Auth guard + remplissage topbar ──────────────────────────────────────────
(function authGuard() {
    const user = getUser();
    if (!user) {
        window.location.href = getLoginPath();
        return;
    }

    const fullName = `${user.prenom || ''} ${user.nom || ''}`.trim() || 'Utilisateur';

    // Remplir les éléments portant les attributs data-user-*
    document.querySelectorAll('[data-user-name]').forEach(el => el.textContent = fullName);
    document.querySelectorAll('[data-user-email]').forEach(el => el.textContent = user.email || '');

    // Compat anciens IDs hardcodés
    ['user-name-top', 'user-name-prof'].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.textContent = fullName;
    });
    ['user-email-top', 'user-email-prof'].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.textContent = user.email || '';
    });

    // Initiales dans les avatars
    const initials = [user.prenom, user.nom].map(s => s?.[0] || '').join('').toUpperCase() || 'U';
    document.querySelectorAll('.avatar, [data-user-initials]').forEach(el => el.textContent = initials);

    // Appliquer les restrictions de permissions (masque les éléments data-requires-permission)
    applyPermissionVisibility();
})();

// Logout est géré spécifiquement dans profile-unified.js (avec modale)

// ── Sidebar toggle ────────────────────────────────────────────────────────────
(function initSidebar() {
    const sidebar     = document.getElementById('sidebar');
    const overlay     = document.getElementById('sidebar-overlay');
    const burger      = document.getElementById('burger');
    const sidebarClose = document.getElementById('sidebar-close');

    if (!sidebar) return;

    function openSidebar()  {
        sidebar.classList.add('open');
        if (overlay) overlay.classList.add('open');
        document.body.style.overflow = 'hidden';
    }
    function closeSidebar() {
        sidebar.classList.remove('open');
        if (overlay) overlay.classList.remove('open');
        document.body.style.overflow = '';
    }

    burger?.addEventListener('click', e => {
        e.stopPropagation();
        sidebar.classList.contains('open') ? closeSidebar() : openSidebar();
    });
    sidebarClose?.addEventListener('click', closeSidebar);
    overlay?.addEventListener('click', closeSidebar);
    document.addEventListener('keydown', e => { if (e.key === 'Escape') closeSidebar(); });
    sidebar.querySelectorAll('.nav-item').forEach(item => {
        item.addEventListener('click', () => { if (window.innerWidth <= 768) closeSidebar(); });
    });
})();

// ── Date helper ───────────────────────────────────────────────────────────────
function formatDate(d) {
    d = d || new Date();
    return d.toLocaleDateString('fr-FR', {
        weekday: 'long', day: 'numeric', month: 'long', year: 'numeric',
    });
}
const dateEl = document.getElementById('page-date');
if (dateEl) dateEl.textContent = formatDate();

const DAYS = ['Dimanche', 'Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi'];
function todayName() { return DAYS[new Date().getDay()]; }

