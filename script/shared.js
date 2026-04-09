/* ===== ADSIS – shared.js ===== */

// ── Auth guard ────────────────────────────────────────────────────────────────
function getUser() {
  try { return JSON.parse(localStorage.getItem('adsis_user') || 'null'); }
  catch { return null; }
}

(function authGuard() {
  const user = getUser();
  if (!user) { window.location.href = 'login.html'; return; }

  ['user-name-top','user-name-prof'].forEach(id => {
    const el = document.getElementById(id);
    if (el) el.textContent = user.name || 'Utilisateur';
  });
  ['user-email-top','user-email-prof'].forEach(id => {
    const el = document.getElementById(id);
    if (el) el.textContent = user.email || '';
  });

  const initials = (user.name || 'U').split(' ').map(w => w[0]).join('').slice(0,2).toUpperCase();
  document.querySelectorAll('.avatar').forEach(el => el.textContent = initials);
  document.querySelectorAll('#sb-etab, .sb-etab').forEach(el => {
    el.textContent = user.etablissement || '';
  });
})();

// ── Sidebar toggle ─────────────────────────────────────────────────────────────
(function initSidebar() {
  const sidebar      = document.getElementById('sidebar');
  const overlay      = document.getElementById('sidebar-overlay');
  const burger       = document.getElementById('burger');
  const sidebarClose = document.getElementById('sidebar-close');

  if (!sidebar) return;

  function openSidebar() {
    sidebar.classList.add('open');
    if (overlay) overlay.classList.add('open');
    document.body.style.overflow = 'hidden';
  }

  function closeSidebar() {
    sidebar.classList.remove('open');
    if (overlay) overlay.classList.remove('open');
    document.body.style.overflow = '';
  }

  // Burger click
  if (burger) {
    burger.addEventListener('click', function(e) {
      e.stopPropagation();
      if (sidebar.classList.contains('open')) closeSidebar();
      else openSidebar();
    });
  }

  // Close ✕ button inside sidebar
  if (sidebarClose) {
    sidebarClose.addEventListener('click', closeSidebar);
  }

  // Overlay click
  if (overlay) {
    overlay.addEventListener('click', closeSidebar);
  }

  // Close on Escape
  document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') closeSidebar();
  });

  // Close sidebar when navigating (click on nav-item)
  sidebar.querySelectorAll('.nav-item').forEach(function(item) {
    item.addEventListener('click', function() {
      // Only close on mobile
      if (window.innerWidth <= 768) closeSidebar();
    });
  });
})();

// ── Logout ────────────────────────────────────────────────────────────────────
document.getElementById('btn-logout')?.addEventListener('click', function() {
  localStorage.removeItem('adsis_user');
  localStorage.removeItem('adsis_token');
  window.location.href = 'login.html';
});

// ── Date helper ───────────────────────────────────────────────────────────────
function formatDate(d) {
  d = d || new Date();
  return d.toLocaleDateString('fr-FR', {
    weekday: 'long', day: 'numeric', month: 'long', year: 'numeric'
  });
}

const dateEl = document.getElementById('page-date');
if (dateEl) dateEl.textContent = formatDate();

// ── Day name helper ───────────────────────────────────────────────────────────
const DAYS = ['Dimanche','Lundi','Mardi','Mercredi','Jeudi','Vendredi','Samedi'];
function todayName() { return DAYS[new Date().getDay()]; }

// ── Demo timetable data (shared across pages) ─────────────────────────────────
window.ADSIS_SCHEDULE = [
  { id:1, course:'Mathématiques',      class:'L1 Info A', room:'Salle A101', day:'Lundi',    start:'08:00', end:'10:00', color:'#3B55E6' },
  { id:2, course:'Algorithmes',        class:'L2 Info B', room:'Salle B203', day:'Lundi',    start:'10:30', end:'12:30', color:'#10b981' },
  { id:3, course:'Base de données',    class:'L1 Info A', room:'Salle C105', day:'Mardi',    start:'14:00', end:'16:00', color:'#7C3AED' },
  { id:4, course:'Réseaux',            class:'L3 Info C', room:'Salle D210', day:'Mercredi', start:'08:00', end:'10:00', color:'#f59e0b' },
  { id:5, course:'Syst. exploitation', class:'L2 Info B', room:'Salle B101', day:'Mercredi', start:'13:00', end:'15:00', color:'#ef4444' },
  { id:6, course:'Programmation Web',  class:'L1 Info A', room:'Salle C105', day:'Jeudi',    start:'09:00', end:'11:00', color:'#0d9488' },
  { id:7, course:'Intelligence Artif.',class:'L3 Info C', room:'Salle A210', day:'Vendredi', start:'14:00', end:'16:00', color:'#e11d48' },
];

// Demo students
window.ADSIS_STUDENTS = [
  { id:1,  name:'Amara Diallo',     matricule:'2024001' },
  { id:2,  name:'Fatou Ndiaye',     matricule:'2024002' },
  { id:3,  name:'Moussa Traoré',    matricule:'2024003' },
  { id:4,  name:'Aïcha Koné',       matricule:'2024004' },
  { id:5,  name:'Seydou Coulibaly', matricule:'2024005' },
  { id:6,  name:'Mariam Sissoko',   matricule:'2024006' },
  { id:7,  name:'Ibrahim Touré',    matricule:'2024007' },
  { id:8,  name:'Kadiatou Bah',     matricule:'2024008' },
  { id:9,  name:'Oumar Sow',        matricule:'2024009' },
  { id:10, name:'Hawa Camara',      matricule:'2024010' },
  { id:11, name:'Souleymane Diop',  matricule:'2024011' },
  { id:12, name:'Mariama Barry',    matricule:'2024012' },
  { id:13, name:'Aliou Baldé',      matricule:'2024013' },
  { id:14, name:'Fatoumata Sy',     matricule:'2024014' },
  { id:15, name:'Mamadou Keïta',    matricule:'2024015' },
];