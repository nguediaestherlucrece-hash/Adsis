/* ===== ADSIS – emploiens.js — Emploi du temps enseignant dynamique ===== */
import { requireRole, authFetch, getUser } from './auth.js';

// Guard : réservé aux enseignants
if (!requireRole(['ENSEIGNANT'])) { throw new Error('Unauthorized'); }

const WEEK_DAYS = ['Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi'];
const TODAY_IDX = new Date().getDay(); // 0=Dim, 1=Lun...
const TODAY = ['Dimanche', 'Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi'][TODAY_IDX];

const DAY_MAP = {
    'MONDAY': 'Lundi', 'TUESDAY': 'Mardi', 'WEDNESDAY': 'Mercredi',
    'THURSDAY': 'Jeudi', 'FRIDAY': 'Vendredi', 'SATURDAY': 'Samedi', 'SUNDAY': 'Dimanche'
};

const MONTHS_FR = ['janvier','février','mars','avril','mai','juin','juillet','août','septembre','octobre','novembre','décembre'];

const container = document.getElementById('schedule-container');
const btnSemaine = document.getElementById('btn-semaine');
const btnJour = document.getElementById('btn-jour');
const daySelectWrap = document.getElementById('day-select-wrap');
const daySelect = document.getElementById('day-select');

// Navigation semaine
const btnPrevWeek = document.getElementById('btn-prev-week');
const btnNextWeek = document.getElementById('btn-next-week');
const btnTodayWeek = document.getElementById('btn-today-week');
const weekLabel = document.getElementById('week-label');

let currentView = 'semaine';
let currentDay = WEEK_DAYS.includes(TODAY) ? TODAY : 'Lundi';
daySelect.value = currentDay;

// ── Gestion semaine offset ─────────────────────────────────────────────────
let weekOffset = 0; // 0 = semaine courante, -1 = passée, +1 = future

function getWeekRange(offset) {
    const now = new Date();
    // Lundi de la semaine courante
    const day = now.getDay();
    const diff = (day === 0 ? -6 : 1 - day); // Lun=1
    const monday = new Date(now);
    monday.setDate(now.getDate() + diff + (offset * 7));
    monday.setHours(0, 0, 0, 0);

    const sunday = new Date(monday);
    sunday.setDate(monday.getDate() + 6);

    return { start: monday, end: sunday };
}

function formatDate(d) {
    // YYYY-MM-DD
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${dd}`;
}

function updateWeekLabel() {
    const range = getWeekRange(weekOffset);
    const d1 = range.start.getDate();
    const m1 = MONTHS_FR[range.start.getMonth()];
    const d2 = range.end.getDate();
    const m2 = MONTHS_FR[range.end.getMonth()];

    if (m1 === m2) {
        weekLabel.textContent = `${d1} – ${d2} ${m1}`;
    } else {
        weekLabel.textContent = `${d1} ${m1} – ${d2} ${m2}`;
    }

    // Mettre en surbrillance si c'est la semaine courante
    btnTodayWeek.style.display = weekOffset === 0 ? 'none' : '';
}

let schedule = []; // Chargé dynamiquement

// ── Collect unique classes ─────────────────────────────────────────────────────
let CLASS_COLORS = {};
let allClasses = [];
const activeClasses = new Set();

// ── Checkbox filter UI ────────────────────────────────────────────────────────
const filterToggleBtn = document.getElementById('class-filter-toggle');
const filterPanel = document.getElementById('class-filter-panel');
const filterCount = document.getElementById('filter-count');
const filterResetBtn = document.getElementById('class-filter-reset');
const checkboxContainer = document.getElementById('class-checkboxes');

function buildCheckboxes() {
    checkboxContainer.innerHTML = '';
    allClasses.forEach(function (cls) {
        var count = schedule.filter(function (s) { return s.class === cls; }).length;
        var item = document.createElement('label');
        item.className = 'class-checkbox-item';

        var cb = document.createElement('input');
        cb.type = 'checkbox';
        cb.value = cls;
        cb.checked = activeClasses.has(cls);
        cb.addEventListener('change', function () {
            if (cb.checked) activeClasses.add(cls);
            else activeClasses.delete(cls);
            updateFilterBadge();
            applyFilter();
        });

        var dot = document.createElement('span');
        dot.className = 'class-checkbox-dot';
        dot.style.background = CLASS_COLORS[cls] || '#3B55E6';

        var lbl = document.createElement('span');
        lbl.className = 'class-checkbox-label';
        lbl.textContent = cls;

        var cnt = document.createElement('span');
        cnt.className = 'class-checkbox-count';
        cnt.textContent = count + ' cours';

        item.appendChild(cb);
        item.appendChild(dot);
        item.appendChild(lbl);
        item.appendChild(cnt);
        checkboxContainer.appendChild(item);
    });
}

function updateFilterBadge() {
    var hidden = allClasses.length - activeClasses.size;
    if (hidden === 0) {
        filterCount.style.display = 'none';
        filterToggleBtn.classList.remove('filter-active');
        filterResetBtn.textContent = 'Tout sélectionner';
    } else {
        filterCount.style.display = 'inline-flex';
        filterCount.textContent = activeClasses.size + '/' + allClasses.length;
        filterToggleBtn.classList.add('filter-active');
        filterResetBtn.textContent = 'Tout sélectionner';
    }
}

function applyFilter() {
    container.querySelectorAll('.course-card').forEach(function (card) {
        var cls = card.getAttribute('data-class');
        card.style.display = (!cls || activeClasses.has(cls)) ? '' : 'none';
    });
    container.querySelectorAll('.day-group').forEach(function (group) {
        var cards = group.querySelectorAll('.course-card');
        if (!cards.length) return;
        var allHidden = Array.from(cards).every(function (c) { return c.style.display === 'none'; });
        group.style.opacity = allHidden ? '0.4' : '1';
    });
}

// Toggle dropdown
filterToggleBtn.addEventListener('click', function (e) {
    e.stopPropagation();
    var open = filterPanel.style.display !== 'none';
    filterPanel.style.display = open ? 'none' : 'block';
    filterToggleBtn.setAttribute('aria-expanded', String(!open));
    filterToggleBtn.classList.toggle('open', !open);
});

// Prevent clicks inside the panel from closing it
filterPanel.addEventListener('click', function (e) {
    e.stopPropagation();
});

document.addEventListener('click', function (e) {
    if (!filterToggleBtn.contains(e.target)) {
        filterPanel.style.display = 'none';
        filterToggleBtn.setAttribute('aria-expanded', 'false');
        filterToggleBtn.classList.remove('open');
    }
});

filterResetBtn.addEventListener('click', function () {
    allClasses.forEach(function (c) { activeClasses.add(c); });
    buildCheckboxes();
    updateFilterBadge();
    applyFilter();
});

// ── Chargement dynamique des séances ──────────────────────────────────────────
async function loadSchedule() {
    const user = getUser();
    if (!user || !user.id) {
        console.error('Aucun utilisateur connecté');
        container.innerHTML = '<div class="empty-state">Impossible de charger l\'emploi du temps. Aucun utilisateur connecté.</div>';
        return;
    }

    updateWeekLabel();

    const range = getWeekRange(weekOffset);
    const dateDebut = formatDate(range.start);
    const dateFin = formatDate(range.end);

    try {
        const res = await authFetch(`/api/seances/enseignant/${user.id}/periode?dateDebut=${dateDebut}&dateFin=${dateFin}`);
        if (!res || !res.ok) {
            console.error('Erreur chargement séances:', res?.status);
            container.innerHTML = '<div class="empty-state">Aucune séance trouvée pour cette période.</div>';
            return;
        }

        const data = await res.json();

        if (!data || data.length === 0) {
            container.innerHTML = '<div class="empty-state">Aucune séance prévue pour cette semaine.</div>';
            // Vider les filtres
            schedule = [];
            allClasses = [];
            activeClasses.clear();
            buildCheckboxes();
            return;
        }
        
        // Mapper les DTOs vers le format interne
        schedule = data.map(dto => {
            // Convertir la date en jour de semaine
            let day = '';
            if (dto.date) {
                const dateObj = new Date(dto.date + 'T00:00:00');
                const dayOfWeek = dateObj.toLocaleDateString('en-US', { weekday: 'long' }).toUpperCase();
                day = DAY_MAP[dayOfWeek] || '';
            }

            return {
                id: dto.id,
                course: dto.coursIntitule || 'Cours',
                class: dto.classeNom || '',
                room: '', // pas de salle dans le DTO actuel
                start: dto.heureDebut ? dto.heureDebut.substring(0, 5) : '',
                end: dto.heureFin ? dto.heureFin.substring(0, 5) : '',
                day: day,
                color: dto.couleur || '#3B55E6',
                seanceId: dto.id,
                appelStatut: dto.appelStatut,
                appelId: dto.appelId,
                nombrePresents: dto.nombrePresents,
                nombreAbsents: dto.nombreAbsents,
                nombreRetards: dto.nombreRetards,
                nombreTotal: dto.nombreTotal,
            };
        });

        // Construire les classes et couleurs
        CLASS_COLORS = {};
        schedule.forEach(function (s) { CLASS_COLORS[s.class] = s.color; });
        allClasses = Object.keys(CLASS_COLORS).sort();
        activeClasses.clear();
        allClasses.forEach(c => activeClasses.add(c));

        buildCheckboxes();
        render();

    } catch (err) {
        console.error('Erreur emploi du temps:', err);
        container.innerHTML = '<div class="empty-state">Erreur de chargement. Vérifiez votre connexion.</div>';
    }
}

// ── Build course card ─────────────────────────────────────────────────────────
function buildCard(c, showDay) {
    var card = document.createElement('div');
    card.className = 'course-card';
    card.setAttribute('data-class', c.class);
    card.setAttribute('role', 'button');
    card.setAttribute('tabindex', '0');
    card.setAttribute('aria-label', 'Ouvrir la séance de ' + c.course);

    var dayMeta = showDay
        ? '<div class="card-meta-item"><svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>' + c.day + '</div>'
        : '';

    card.innerHTML =
        '<div class="card-color-bar" style="background:' + c.color + ';"></div>' +
        '<div class="card-body">' +
        '<div class="card-header-row">' +
        '<span class="card-course-name">' + c.course + '</span>' +
        '<span class="class-chip" style="background:' + c.color + ';">' + c.class + '</span>' +
        '</div>' +
        '<div class="card-meta">' +
        '<div class="card-meta-item"><svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>' + c.start + ' – ' + c.end + '</div>' +
        (c.room ? '<div class="card-meta-item"><svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>' + c.room + '</div>' : '') +
        dayMeta +
        '</div>' +
        '</div>' +
        '<div class="card-footer">' +
        '<button class="open-session-btn">Ouvrir la séance <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg></button>' +
        '</div>';

    function openSession() { window.location.href = '/enseignant/appel?seanceId=' + c.seanceId; }
    card.addEventListener('click', openSession);
    card.addEventListener('keydown', function (e) { if (e.key === 'Enter' || e.key === ' ') openSession(); });

    if (!activeClasses.has(c.class)) card.style.display = 'none';
    return card;
}

// ── Render week ──────────────────────────────────────────────────────────────
function renderWeek() {
    container.innerHTML = '';
    var wrap = document.createElement('div');
    wrap.className = 'week-view';

    // Calculer les dates de la semaine pour le badge "Auj."
    const range = getWeekRange(weekOffset);
    const todayStr = formatDate(new Date());

    WEEK_DAYS.forEach(function (day, idx) {
        var dayCourses = schedule.filter(function (s) { return s.day === day; })
            .sort(function (a, b) { return a.start.localeCompare(b.start); });

        var group = document.createElement('div');
        group.className = 'day-group';

        // Calculer la date de ce jour
        const dayDate = new Date(range.start);
        dayDate.setDate(range.start.getDate() + idx);
        const isToday = formatDate(dayDate) === todayStr;

        var labelEl = document.createElement('div');
        labelEl.className = 'day-label';

        const dayNum = dayDate.getDate();
        labelEl.innerHTML = day + ' <span style="font-size:0.68rem;color:var(--text-muted);">' + dayNum + '</span>' + (isToday ? ' <span class="today-badge">Auj.</span>' : '');
        group.appendChild(labelEl);

        var col = document.createElement('div');
        col.className = 'courses-row';

        if (dayCourses.length === 0) {
            col.innerHTML = '<p class="no-course-day">—</p>';
        } else {
            dayCourses.forEach(function (c, i) {
                var card = buildCard(c, false);
                card.style.animationDelay = (i * 0.06) + 's';
                col.appendChild(card);
            });
        }

        group.appendChild(col);
        wrap.appendChild(group);
    });

    container.appendChild(wrap);
    applyFilter();
}

// ── Render day ──────────────────────────────────────────────────────────────
function renderDay(day) {
    container.innerHTML = '';
    var dayCourses = schedule.filter(function (s) { return s.day === day; })
        .sort(function (a, b) { return a.start.localeCompare(b.start); });

    if (dayCourses.length === 0) {
        container.innerHTML = '<div class="empty-state">Aucun cours ce jour.</div>';
        return;
    }
    var grid = document.createElement('div');
    grid.className = 'day-view-grid';
    dayCourses.forEach(function (c, i) {
        var card = buildCard(c, true);
        card.style.animationDelay = (i * 0.07) + 's';
        grid.appendChild(card);
    });
    container.appendChild(grid);
    applyFilter();
}

// ── Render dispatch ───────────────────────────────────────────────────────────
function render() {
    if (currentView === 'semaine') renderWeek();
    else renderDay(currentDay);
}

// ── Events : vue toggle ───────────────────────────────────────────────────────
btnSemaine.addEventListener('click', function () {
    currentView = 'semaine';
    btnSemaine.classList.add('active');
    btnJour.classList.remove('active');
    daySelectWrap.style.display = 'none';
    render();
});
btnJour.addEventListener('click', function () {
    currentView = 'jour';
    btnJour.classList.add('active');
    btnSemaine.classList.remove('active');
    daySelectWrap.style.display = 'flex';
    render();
});
daySelect.addEventListener('change', function () {
    currentDay = daySelect.value;
    render();
});

// ── Events : navigation semaine ───────────────────────────────────────────────
btnPrevWeek.addEventListener('click', function () {
    weekOffset--;
    loadSchedule();
});
btnNextWeek.addEventListener('click', function () {
    weekOffset++;
    loadSchedule();
});
btnTodayWeek.addEventListener('click', function () {
    weekOffset = 0;
    loadSchedule();
});

// Charger les données au démarrage
loadSchedule();
