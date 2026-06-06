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
    // Filter all filterable elements across all view types
    var selectors = '.course-card, .week-grid-course, .timeline-event';
    container.querySelectorAll(selectors).forEach(function (el) {
        var cls = el.getAttribute('data-class');
        el.style.display = (!cls || activeClasses.has(cls)) ? '' : 'none';
    });
    // Dim day groups if all cards hidden (legacy week view)
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

            const isEvt = dto.isEvent || false;
            const cleanNom = isEvt ? cleanEventNameString(dto.nomEvenement || 'Événement') : '';

            return {
                id: dto.id,
                course: isEvt ? cleanNom : (dto.coursIntitule || 'Cours'),
                class: dto.classeNom || '',
                room: '', // pas de salle dans le DTO actuel
                start: dto.heureDebut ? dto.heureDebut.substring(0, 5) : '',
                end: dto.heureFin ? dto.heureFin.substring(0, 5) : '',
                day: day,
                color: dto.couleur || (isEvt ? '#d97706' : '#3B55E6'),
                seanceId: dto.id,
                isEvent: isEvt,
                nomEvenement: cleanNom,
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

// ── Vérifie si une séance est dans la fenêtre d'appel (±15 min) ──────────────
function isSeanceActive(c) {
    var now = new Date();
    var todayStr = now.toISOString().split('T')[0];

    // Calculer la date de la séance à partir du jour de la semaine + weekOffset
    var range = getWeekRange(weekOffset);
    var dayIdx = WEEK_DAYS.indexOf(c.day);
    if (dayIdx < 0) return false;
    var seanceDate = new Date(range.start);
    seanceDate.setDate(range.start.getDate() + dayIdx);
    var seanceDateStr = formatDate(seanceDate);

    if (seanceDateStr !== todayStr) return false;

    var parts = c.start.split(':');
    var sh = parseInt(parts[0]), sm = parseInt(parts[1]);
    var partsE = c.end.split(':');
    var eh = parseInt(partsE[0]), em = parseInt(partsE[1]);

    var startMin = sh * 60 + sm - 15;
    var endMin = eh * 60 + em + 15;
    var nowMin = now.getHours() * 60 + now.getMinutes();

    return nowMin >= startMin && nowMin <= endMin;
}

function getSeanceStatus(c) {
    var now = new Date();
    var todayStr = now.toISOString().split('T')[0];
    var range = getWeekRange(weekOffset);
    var dayIdx = WEEK_DAYS.indexOf(c.day);
    if (dayIdx < 0) return 'future';
    var seanceDate = new Date(range.start);
    seanceDate.setDate(range.start.getDate() + dayIdx);
    var seanceDateStr = formatDate(seanceDate);

    if (seanceDateStr < todayStr) return 'past';
    if (seanceDateStr > todayStr) return 'future';

    var partsE = c.end.split(':');
    var eh = parseInt(partsE[0]), em = parseInt(partsE[1]);
    var endMin = eh * 60 + em + 15;
    var nowMin = now.getHours() * 60 + now.getMinutes();

    if (nowMin > endMin) return 'past';
    return 'future';
}

// ── Build course card ─────────────────────────────────────────────────────────
function buildCard(c, showDay) {
    var card = document.createElement('div');
    card.className = 'course-card';
    if (!c.isEvent) {
        card.setAttribute('data-class', c.class);
        card.setAttribute('role', 'button');
        card.setAttribute('tabindex', '0');
        card.setAttribute('aria-label', 'Ouvrir la séance de ' + c.course);
    } else {
        card.setAttribute('aria-label', 'Événement : ' + c.course);
    }

    var dayMeta = showDay
        ? '<div class="card-meta-item"><svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>' + c.day + '</div>'
        : '';

    var footerHtml = '';
    if (c.isEvent) {
        footerHtml = '<div class="card-footer card-footer-locked" style="background:#fef3c7; color:#b45309;">' +
            '<span class="locked-label"><svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg> Événement</span>' +
            '</div>';
    } else {
        var canOpen = isSeanceActive(c);
        var status = canOpen ? '' : getSeanceStatus(c);
        if (canOpen) {
            footerHtml = '<div class="card-footer">' +
                '<button class="open-session-btn">Lancer l\'appel <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg></button>' +
                '</div>';
        } else {
            var label = status === 'past' ? 'Séance passée' : 'Séance à venir';
            footerHtml = '<div class="card-footer card-footer-locked">' +
                '<span class="locked-label"><svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg> ' + label + '</span>' +
                '</div>';
        }
    }

    card.innerHTML =
        '<div class="card-color-bar" style="background:' + c.color + ';"></div>' +
        '<div class="card-body">' +
        '<div class="card-header-row">' +
        '<span class="card-course-name">' + c.course + '</span>' +
        (c.isEvent ? '' : '<span class="class-chip" style="background:' + c.color + ';">' + c.class + '</span>') +
        '</div>' +
        '<div class="card-meta">' +
        '<div class="card-meta-item"><svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>' + c.start + ' – ' + c.end + '</div>' +
        (c.room ? '<div class="card-meta-item"><svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>' + c.room + '</div>' : '') +
        dayMeta +
        '</div>' +
        '</div>' +
        footerHtml;

    if (!c.isEvent && canOpen) {
        function openSession() { window.location.href = '/enseignant/appel?seanceId=' + c.seanceId; }
        card.addEventListener('click', openSession);
        card.addEventListener('keydown', function (e) { if (e.key === 'Enter' || e.key === ' ') openSession(); });
    }

    if (!c.isEvent && !activeClasses.has(c.class)) card.style.display = 'none';
    return card;
}

// ── Render week — Grille emploi du temps (style assistant) ──────────────────
function renderWeek() {
    container.innerHTML = '';

    var range = getWeekRange(weekOffset);
    var todayStr = formatDate(new Date());

    // Déterminer la plage horaire utilisée
    var allTimes = schedule.map(function (s) { return s.start; }).concat(schedule.map(function (s) { return s.end; }));
    var START_HOUR = 7, END_HOUR = 18;
    if (allTimes.length > 0) {
        var minH = Math.min.apply(null, allTimes.map(function (t) { return parseInt(t.split(':')[0]); }));
        var maxH = Math.max.apply(null, allTimes.map(function (t) { return parseInt(t.split(':')[0]); }));
        START_HOUR = Math.min(START_HOUR, minH);
        END_HOUR = Math.max(END_HOUR, maxH + 1);
    }

    // Générer les créneaux horaires
    var timeSlots = [];
    for (var h = START_HOUR; h < END_HOUR; h++) {
        timeSlots.push((h < 10 ? '0' : '') + h + ':00');
    }

    // Table wrapper
    var card = document.createElement('div');
    card.className = 'week-grid-card';

    var tableWrap = document.createElement('div');
    tableWrap.style.cssText = 'overflow-x:auto;';

    var table = document.createElement('table');
    table.className = 'week-grid-table';

    // Header : Horaires | Lundi | Mardi | ...
    var thead = document.createElement('thead');
    var headerRow = document.createElement('tr');
    var thTime = document.createElement('th');
    thTime.className = 'week-grid-th-time';
    thTime.textContent = 'Horaires';
    headerRow.appendChild(thTime);

    WEEK_DAYS.forEach(function (day, idx) {
        var dayDate = new Date(range.start);
        dayDate.setDate(range.start.getDate() + idx);
        var isToday = formatDate(dayDate) === todayStr;
        var dayNum = dayDate.getDate();

        var th = document.createElement('th');
        th.className = 'week-grid-th-day' + (isToday ? ' week-grid-today' : '');
        th.innerHTML = '<div class="week-grid-day-name">' + day.substring(0, 3) + '</div>' +
            '<div class="week-grid-day-num' + (isToday ? ' today-circle' : '') + '">' + dayNum + '</div>';
        headerRow.appendChild(th);
    });
    thead.appendChild(headerRow);
    table.appendChild(thead);

    // Pré-calculer les occupations pour le rowspan
    // occupied[dayIdx][hour] = { course, rowspan } si un cours occupe cette cellule
    var occupied = {};
    WEEK_DAYS.forEach(function (day, dayIdx) {
        occupied[dayIdx] = {};
    });

    // Placer les cours avec leur durée
    schedule.forEach(function (c) {
        var dayIdx = WEEK_DAYS.indexOf(c.day);
        if (dayIdx < 0) return;
        var sh = parseInt(c.start.split(':')[0]);
        var sm = parseInt(c.start.split(':')[1]) || 0;
        var eh = parseInt(c.end.split(':')[0]);
        var em = parseInt(c.end.split(':')[1]) || 0;
        var durationHours = Math.max(1, Math.ceil((eh * 60 + em - sh * 60 - sm) / 60));

        // Marquer la cellule de départ
        if (!occupied[dayIdx][sh]) {
            occupied[dayIdx][sh] = { course: c, rowspan: durationHours };
        }
        // Marquer les cellules couvertes
        for (var h = 1; h < durationHours; h++) {
            occupied[dayIdx][sh + h] = { covered: true };
        }
    });

    // Body : une ligne par créneau horaire
    var tbody = document.createElement('tbody');
    timeSlots.forEach(function (slot) {
        var slotHour = parseInt(slot.split(':')[0]);
        var tr = document.createElement('tr');

        // Cellule horaire
        var tdTime = document.createElement('td');
        tdTime.className = 'week-grid-time';
        tdTime.textContent = slot;
        tr.appendChild(tdTime);

        // Cellule par jour
        WEEK_DAYS.forEach(function (day, idx) {
            var occ = occupied[idx][slotHour];

            // Cellule couverte par un rowspan au-dessus → ne pas ajouter de <td>
            if (occ && occ.covered) return;

            var dayDate = new Date(range.start);
            dayDate.setDate(range.start.getDate() + idx);
            var isToday = formatDate(dayDate) === todayStr;

            var td = document.createElement('td');
            td.className = 'week-grid-cell' + (isToday ? ' week-grid-cell-today' : '');

            if (occ && occ.course) {
                var c = occ.course;
                var rowspan = occ.rowspan;
                if (rowspan > 1) {
                    td.rowSpan = rowspan;
                    td.style.height = (60 * rowspan) + 'px';
                }

                var canOpen = !c.isEvent && isSeanceActive(c);
                var block = document.createElement('div');
                block.className = 'week-grid-course' + (canOpen ? ' week-grid-course-active' : '');
                block.style.background = c.isEvent ? '#fde68a' : c.color;
                block.style.color = c.isEvent ? '#92400e' : '';
                block.style.borderLeft = c.isEvent ? '3px solid ' + c.color : '';
                if (!c.isEvent) {
                    block.setAttribute('data-class', c.class);
                }
                if (rowspan > 1) {
                    block.style.minHeight = ((60 * rowspan) - 6) + 'px';
                }

                block.innerHTML = '<div class="week-grid-course-name" style="font-weight:600;">' + (c.isEvent ? '📅 ' + c.course : c.course) + '</div>' +
                    (c.isEvent ? '' : '<div class="week-grid-course-info">' + c.class + '</div>') +
                    '<div class="week-grid-course-time"' + (c.isEvent ? ' style="color:#78350f;"' : '') + '>' + c.start + ' – ' + c.end + '</div>';

                if (canOpen) {
                    block.addEventListener('click', function () {
                        window.location.href = '/enseignant/appel?seanceId=' + c.seanceId;
                    });
                }

                td.appendChild(block);
            }

            tr.appendChild(td);
        });

        tbody.appendChild(tr);
    });
    table.appendChild(tbody);

    tableWrap.appendChild(table);
    card.appendChild(tableWrap);
    container.appendChild(card);
    applyFilter();
}

// ── Render day — iPhone Calendar Timeline ───────────────────────────────────
function renderDay(day) {
    container.innerHTML = '';

    var dayCourses = schedule.filter(function (s) { return s.day === day; })
        .sort(function (a, b) { return a.start.localeCompare(b.start); });

    // Calculer la date du jour sélectionné
    var range = getWeekRange(weekOffset);
    var dayIdx = WEEK_DAYS.indexOf(day);
    var dayDate = new Date(range.start);
    dayDate.setDate(range.start.getDate() + (dayIdx >= 0 ? dayIdx : 0));
    var todayStr = formatDate(new Date());
    var isToday = formatDate(dayDate) === todayStr;

    // Header du jour
    var header = document.createElement('div');
    header.style.cssText = 'display:flex;align-items:center;gap:10px;margin-bottom:14px;animation:fadeUp 0.3s both;';
    var dayNum = dayDate.getDate();
    var monthFr = MONTHS_FR[dayDate.getMonth()];
    header.innerHTML = '<div style="text-align:center;min-width:48px;">' +
        '<div style="font-size:0.65rem;font-weight:700;text-transform:uppercase;letter-spacing:0.8px;color:' + (isToday ? 'var(--primary,#3B55E6)' : '#94a3b8') + ';">' + day.substring(0, 3) + '</div>' +
        '<div style="font-size:1.5rem;font-weight:800;line-height:1;' + (isToday ? 'background:var(--primary,#3B55E6);color:#fff;border-radius:50%;width:40px;height:40px;display:flex;align-items:center;justify-content:center;margin:2px auto 0;' : 'color:#1e293b;margin-top:2px;') + '">' + dayNum + '</div>' +
        '</div>' +
        '<div style="font-size:0.88rem;font-weight:600;color:#64748b;">' + monthFr + ' ' + dayDate.getFullYear() +
        (dayCourses.length > 0 ? ' · <span style="color:#1e293b;">' + dayCourses.length + ' cours</span>' : '') + '</div>';
    container.appendChild(header);

    if (dayCourses.length === 0) {
        container.innerHTML += '<div class="empty-state" style="background:#fff;border-radius:14px;border:1px solid #f0f0f0;box-shadow:0 2px 8px rgba(0,0,0,0.04);">Aucun cours ce jour.</div>';
        return;
    }

    // Timeline heures (07h - 20h)
    var START_HOUR = 7;
    var END_HOUR = 20;
    var HOUR_HEIGHT = 60; // px par heure

    var timeline = document.createElement('div');
    timeline.className = 'day-timeline';

    // Container relatif pour les events
    var wrapper = document.createElement('div');
    wrapper.style.position = 'relative';

    // Générer les lignes horaires
    for (var h = START_HOUR; h <= END_HOUR; h++) {
        var hourRow = document.createElement('div');
        hourRow.className = 'timeline-hour-row';
        hourRow.style.height = HOUR_HEIGHT + 'px';

        var label = document.createElement('div');
        label.className = 'timeline-hour-label';
        label.textContent = (h < 10 ? '0' : '') + h + ':00';

        var slotArea = document.createElement('div');
        slotArea.className = 'timeline-slot-area';

        hourRow.appendChild(label);
        hourRow.appendChild(slotArea);
        wrapper.appendChild(hourRow);
    }

    // Placer les événements absolument
    dayCourses.forEach(function (c) {
        var parts = c.start.split(':');
        var sh = parseInt(parts[0]), sm = parseInt(parts[1]);
        var partsE = c.end.split(':');
        var eh = parseInt(partsE[0]), em = parseInt(partsE[1]);

        var topMin = (sh - START_HOUR) * 60 + sm;
        var durationMin = (eh - START_HOUR) * 60 + em - topMin;
        if (durationMin < 15) durationMin = 15;

        var topPx = (topMin / 60) * HOUR_HEIGHT;
        var heightPx = (durationMin / 60) * HOUR_HEIGHT;

        var isEvt = c.isEvent || false;
        var canOpen = !isEvt && isSeanceActive(c);
        var status = canOpen ? '' : getSeanceStatus(c);

        var ev = document.createElement('div');
        ev.className = 'timeline-event' + (canOpen ? '' : ' timeline-event-locked');
        if (!isEvt) {
            ev.setAttribute('data-class', c.class);
        }
        var bgCol = isEvt ? '#fde68a' : c.color;
        var textCol = isEvt ? '#92400e' : '#fff';
        var borderLeft = isEvt ? '3px solid ' + c.color : '';
        ev.style.cssText = 'top:' + topPx + 'px;height:' + heightPx + 'px;background:' + bgCol + ';color:' + textCol + ';border-left:' + borderLeft + ';left:64px;right:6px;';

        var innerHtml = '<div class="timeline-event-title" style="font-weight:600;">' + (isEvt ? '📅 ' + c.course : c.course) + '</div>';
        if (heightPx >= 48) {
            innerHtml += '<div class="timeline-event-subtitle" style="color:' + (isEvt ? '#78350f' : 'inherit') + ';">' + (isEvt ? '' : c.class + (c.room ? ' · ' + c.room : '')) + '</div>';
        }
        innerHtml += '<div class="timeline-event-time" style="color:' + (isEvt ? '#78350f' : 'inherit') + ';">' + c.start + ' – ' + c.end;
        if (!isEvt && !canOpen) {
            var statusLabel = status === 'past' ? ' · Passé' : ' · À venir';
            innerHtml += '<span style="opacity:0.7;">' + statusLabel + '</span>';
        }
        innerHtml += '</div>';
        ev.innerHTML = innerHtml;

        if (canOpen) {
            ev.addEventListener('click', function () {
                window.location.href = '/enseignant/appel?seanceId=' + c.seanceId;
            });
        }

        wrapper.appendChild(ev);
    });

    // Ligne "maintenant"
    if (isToday) {
        var now = new Date();
        var nowMin = (now.getHours() - START_HOUR) * 60 + now.getMinutes();
        if (nowMin >= 0 && nowMin <= (END_HOUR - START_HOUR) * 60) {
            var nowLine = document.createElement('div');
            nowLine.className = 'timeline-now-line';
            nowLine.style.top = ((nowMin / 60) * HOUR_HEIGHT) + 'px';
            wrapper.appendChild(nowLine);
        }
    }

    timeline.appendChild(wrapper);
    container.appendChild(timeline);
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

function cleanEventNameString(nom) {
    if (!nom) return 'Événement';
    const days = /\b(lundi|mardi|mercredi|jeudi|vendredi|samedi|dimanche|monday|tuesday|wednesday|thursday|friday|saturday|sunday)\b/gi;
    return nom
        .replace(/\b(evt)\b/gi, '')
        .replace(/\b\d+\b/g, '')
        .replace(days, '')
        .replace(/_/g, ' ')
        .replace(/\s+/g, ' ')
        .trim() || 'Événement';
}
