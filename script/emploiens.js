/* ===== ADSIS – emploiens.js ===== */

const schedule  = window.ADSIS_SCHEDULE || [];
const WEEK_DAYS = ['Lundi','Mardi','Mercredi','Jeudi','Vendredi','Samedi'];
const TODAY     = ['Dimanche','Lundi','Mardi','Mercredi','Jeudi','Vendredi','Samedi'][new Date().getDay()];

const container      = document.getElementById('schedule-container');
const btnSemaine     = document.getElementById('btn-semaine');
const btnJour        = document.getElementById('btn-jour');
const daySelectWrap  = document.getElementById('day-select-wrap');
const daySelect      = document.getElementById('day-select');

// ── State ─────────────────────────────────────────────────────────────────────
let currentView = 'semaine';
let currentDay  = WEEK_DAYS.includes(TODAY) ? TODAY : 'Lundi';

// Pre-select today in dropdown
daySelect.value = currentDay;

// ── Build one course card ─────────────────────────────────────────────────────
function buildCard(c, showDay = false) {
  const card = document.createElement('div');
  card.className = 'course-card';
  card.setAttribute('role', 'button');
  card.setAttribute('tabindex', '0');
  card.setAttribute('aria-label', `Ouvrir la séance de ${c.course}`);

  card.innerHTML = `
    <div class="card-color-bar" style="background:${c.color};"></div>
    <div class="card-body">
      <div class="card-header-row">
        <span class="card-course-name">${c.course}</span>
        <span class="class-chip" style="background:${c.color};">${c.class}</span>
      </div>
      <div class="card-meta">
        <div class="card-meta-item">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
          </svg>
          ${c.start} – ${c.end}
        </div>
        <div class="card-meta-item">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
            <circle cx="12" cy="10" r="3"/>
          </svg>
          ${c.room}
        </div>
        ${showDay ? `
        <div class="card-meta-item">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="4" width="18" height="18" rx="2"/>
            <line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/>
            <line x1="3" y1="10" x2="21" y2="10"/>
          </svg>
          ${c.day}
        </div>` : ''}
      </div>
    </div>
    <div class="card-footer">
      <button class="open-session-btn">
        Ouvrir la séance
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="9 18 15 12 9 6"/>
        </svg>
      </button>
    </div>
  `;

  // Navigate to appel page with session id
  function openSession() {
    window.location.href = `appel.html?id=${c.id}`;
  }

  card.addEventListener('click', openSession);
  card.addEventListener('keydown', e => { if (e.key === 'Enter' || e.key === ' ') openSession(); });

  return card;
}

// ── Render week view ──────────────────────────────────────────────────────────
function renderWeek() {
  container.innerHTML = '';
  const wrap = document.createElement('div');
  wrap.className = 'week-view';

  WEEK_DAYS.forEach(day => {
    const dayCourses = schedule
      .filter(s => s.day === day)
      .sort((a,b) => a.start.localeCompare(b.start));

    const group = document.createElement('div');
    group.className = 'day-group';

    const isToday = day === TODAY;
    group.innerHTML = `
      <div class="day-label">
        ${day}
        ${isToday ? '<span class="today-badge">Aujourd\'hui</span>' : ''}
      </div>
    `;

    const row = document.createElement('div');
    row.className = 'courses-row';

    if (dayCourses.length === 0) {
      row.innerHTML = `<p class="no-course-day">Aucun cours ce jour.</p>`;
    } else {
      dayCourses.forEach((c, i) => {
        const card = buildCard(c, false);
        card.style.animationDelay = (i * 0.06) + 's';
        row.appendChild(card);
      });
    }

    group.appendChild(row);
    wrap.appendChild(group);
  });

  container.appendChild(wrap);
}

// ── Render day view ───────────────────────────────────────────────────────────
function renderDay(day) {
  container.innerHTML = '';
  const dayCourses = schedule
    .filter(s => s.day === day)
    .sort((a,b) => a.start.localeCompare(b.start));

  if (dayCourses.length === 0) {
    container.innerHTML = `
      <div style="text-align:center;padding:48px 0;color:var(--text-muted);">
        Aucun cours ce jour.
      </div>`;
    return;
  }

  const grid = document.createElement('div');
  grid.className = 'day-view';

  dayCourses.forEach((c, i) => {
    const card = buildCard(c, true);
    card.style.animationDelay = (i * 0.07) + 's';
    grid.appendChild(card);
  });

  container.appendChild(grid);
}

// ── Render dispatch ───────────────────────────────────────────────────────────
function render() {
  if (currentView === 'semaine') {
    renderWeek();
  } else {
    renderDay(currentDay);
  }
}

// ── Toggle buttons ────────────────────────────────────────────────────────────
btnSemaine.addEventListener('click', () => {
  currentView = 'semaine';
  btnSemaine.classList.add('active');
  btnJour.classList.remove('active');
  daySelectWrap.style.display = 'none';
  render();
});

btnJour.addEventListener('click', () => {
  currentView = 'jour';
  btnJour.classList.add('active');
  btnSemaine.classList.remove('active');
  daySelectWrap.style.display = 'flex';
  render();
});

daySelect.addEventListener('change', () => {
  currentDay = daySelect.value;
  render();
});

// ── Initial render ────────────────────────────────────────────────────────────
render();