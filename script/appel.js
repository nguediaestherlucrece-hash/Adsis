/* ===== ADSIS – appel.js ===== */
import { requireRole, requirePermission, getLoginPath } from './auth.js';

// Guard : enseignants uniquement avec permission ATTENDANCE_MANAGE
if (!requireRole(['ENSEIGNANT'])) { throw new Error('Unauthorized'); }
if (!requirePermission('ATTENDANCE_MANAGE')) {
    window.location.href = getLoginPath();
    throw new Error('Permission refusée');
}

const schedule = window.ADSIS_SCHEDULE || [];
const students = window.ADSIS_STUDENTS || [];

// ── Session selector: populate options ────────────────────────────────────────
const sessionSelectEl = document.getElementById('session-select');
const DAYS_ORDER = ['Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi', 'Dimanche'];

// Sort schedule by day then start time
const sortedSchedule = [...schedule].sort((a, b) => {
  const di = DAYS_ORDER.indexOf(a.day) - DAYS_ORDER.indexOf(b.day);
  if (di !== 0) return di;
  return a.start.localeCompare(b.start);
});

sortedSchedule.forEach(s => {
  const opt = document.createElement('option');
  opt.value = s.id;
  opt.textContent = `${s.day} · ${s.start} – ${s.course} (${s.class})`;
  sessionSelectEl.appendChild(opt);
});

// ── Read session id from URL ───────────────────────────────────────────────────
const urlParams = new URLSearchParams(window.location.search);
let sessionId = parseInt(urlParams.get('id')) || (schedule[0] && schedule[0].id);
let session = schedule.find(s => s.id === sessionId) || schedule[0] || {
  id: 0, course: 'Séance', class: '', room: '', start: '', end: '', day: '', color: '#3B55E6'
};

// Sync select to current session
sessionSelectEl.value = session.id;

// ── Session change handler ─────────────────────────────────────────────────────
function loadSession(newId) {
  const newSession = schedule.find(s => s.id === newId);
  if (!newSession) return;
  session = newSession;
  sessionId = newSession.id;

  // Update banner
  document.getElementById('session-course').textContent = session.course;
  document.getElementById('session-meta').textContent =
    [session.class, session.room, `${session.start} – ${session.end}`].filter(Boolean).join(' • ');

  // Reset attendance for new session
  students.forEach(s => { attendance[s.id] = 'absent'; });
  updateCounters();
  renderList();

  // Reset QR gen state if it was inited
  qrGenInited = false;
  const qrCanvas = document.getElementById('qr-canvas-session');
  if (qrCanvas) qrCanvas.innerHTML = '';
  const fsCanvas = document.getElementById('qr-fullscreen-canvas');
  if (fsCanvas) fsCanvas.innerHTML = '';
  clearInterval(qrTimerInterval);

  // Update QR labels if visible
  const labelCourse = document.getElementById('qr-label-course');
  if (labelCourse) labelCourse.textContent = session.course;
  const labelMeta = document.getElementById('qr-label-meta');
  if (labelMeta) labelMeta.textContent =
    [session.class, session.room, `${session.start}–${session.end}`].filter(Boolean).join(' • ');

  showToast(`Séance changée : ${session.course} — ${session.day} ${session.start}`, 'info', 3000);
}

sessionSelectEl.addEventListener('change', () => {
  loadSession(parseInt(sessionSelectEl.value));
});

// ── Populate session info (initial) ──────────────────────────────────────────
document.getElementById('session-course').textContent = session.course;
document.getElementById('session-meta').textContent =
  [session.class, session.room, `${session.start} – ${session.end}`].filter(Boolean).join(' • ');
document.getElementById('page-title').textContent = 'Séance d\'appel';

// ── Attendance state: { studentId: 'present' | 'absent' | 'retard' } ──────────
const attendance = {};
students.forEach(s => { attendance[s.id] = 'absent'; }); // default: absent

// ── Update counters ───────────────────────────────────────────────────────────
function updateCounters() {
  const vals = Object.values(attendance);
  document.getElementById('cnt-present').textContent = vals.filter(v => v === 'present').length;
  document.getElementById('cnt-retard').textContent = vals.filter(v => v === 'retard').length;
  document.getElementById('cnt-absent').textContent = vals.filter(v => v === 'absent').length;
}
updateCounters();

// ── Toast ─────────────────────────────────────────────────────────────────────
let toastTimer = null;
function showToast(msg, type = 'info', duration = 3000) {
  const toast = document.getElementById('toast');
  toast.textContent = msg;
  toast.className = `toast ${type}-toast show`;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => { toast.className = 'toast'; }, duration);
}

// ── TABS ─────────────────────────────────────────────────────────────────────
const tabScan = document.getElementById('tab-scan');
const tabQrgen = document.getElementById('tab-qrgen');
const tabList = document.getElementById('tab-list');
const panelScan = document.getElementById('panel-scan');
const panelQrgen = document.getElementById('panel-qrgen');
const panelList = document.getElementById('panel-list');

function activateTab(tab) {
  // Reset all
  [tabScan, tabQrgen, tabList].forEach(t => { t.classList.remove('active'); t.setAttribute('aria-selected', 'false'); });
  [panelScan, panelQrgen, panelList].forEach(p => p.classList.add('hidden'));

  if (tab === 'scan') {
    tabScan.classList.add('active'); tabScan.setAttribute('aria-selected', 'true');
    panelScan.classList.remove('hidden');
  } else if (tab === 'qrgen') {
    tabQrgen.classList.add('active'); tabQrgen.setAttribute('aria-selected', 'true');
    panelQrgen.classList.remove('hidden');
    stopScan();
    initQRGen();
  } else {
    tabList.classList.add('active'); tabList.setAttribute('aria-selected', 'true');
    panelList.classList.remove('hidden');
    stopScan();
  }
}

tabScan.addEventListener('click', () => activateTab('scan'));
tabQrgen.addEventListener('click', () => activateTab('qrgen'));
tabList.addEventListener('click', () => activateTab('list'));

// ═══════════════════════════════════════════════════════════════════════════════
// QR SCAN
// ═══════════════════════════════════════════════════════════════════════════════
let html5QrCode = null;
let scanning = false;

const btnStart = document.getElementById('btn-start-scan');
const btnStop = document.getElementById('btn-stop-scan');
const camError = document.getElementById('camera-error');
const scanResult = document.getElementById('scan-result');
const scanResultText = document.getElementById('scan-result-text');
let resultTimer = null;

function showScanResult(name) {
  scanResultText.textContent = `${name} marqué présent !`;
  scanResult.style.display = 'flex';
  clearTimeout(resultTimer);
  resultTimer = setTimeout(() => { scanResult.style.display = 'none'; }, 3000);
}

async function startScan() {
  if (scanning) return;

  // Check camera permission
  try {
    await navigator.mediaDevices.getUserMedia({ video: true });
  } catch (e) {
    camError.style.display = 'flex';
    btnStart.style.display = 'none';
    return;
  }

  camError.style.display = 'none';

  if (!html5QrCode) {
    html5QrCode = new Html5Qrcode('reader');
  }

  const config = {
    fps: 10,
    qrbox: { width: 200, height: 200 },
    aspectRatio: 1.0,
  };

  try {
    await html5QrCode.start(
      { facingMode: 'environment' },
      config,
      onQrSuccess,
      () => { } // suppress errors
    );
    scanning = true;
    btnStart.style.display = 'none';
    btnStop.style.display = 'inline-flex';
  } catch (err) {
    // Try front camera
    try {
      await html5QrCode.start(
        { facingMode: 'user' },
        config,
        onQrSuccess,
        () => { }
      );
      scanning = true;
      btnStart.style.display = 'none';
      btnStop.style.display = 'inline-flex';
    } catch (e2) {
      camError.style.display = 'flex';
    }
  }
}

async function stopScan() {
  if (!scanning || !html5QrCode) return;
  try { await html5QrCode.stop(); } catch { }
  scanning = false;
  btnStart.style.display = 'inline-flex';
  btnStop.style.display = 'none';
}

// QR code format: ADSIS:<studentId>:<matricule>
function onQrSuccess(decodedText) {
  const parts = decodedText.split(':');
  let studentId = null;

  if (parts[0] === 'ADSIS' && parts[1]) {
    studentId = parseInt(parts[1]);
  } else {
    // Try raw numeric id
    studentId = parseInt(decodedText);
  }

  const student = students.find(s => s.id === studentId);
  if (!student) {
    showToast('QR code non reconnu.', 'error');
    return;
  }

  if (attendance[student.id] === 'present') {
    showToast(`${student.name} est déjà présent.`, 'info');
    return;
  }

  attendance[student.id] = 'present';
  updateCounters();
  updateStudentRow(student.id);
  showScanResult(student.name);
  showToast(`✓ ${student.name} marqué présent`, 'success');
}

btnStart.addEventListener('click', startScan);
btnStop.addEventListener('click', stopScan);

// ═══════════════════════════════════════════════════════════════════════════════
// STUDENT LIST
// ═══════════════════════════════════════════════════════════════════════════════
const studentList = document.getElementById('student-list');
const searchInput = document.getElementById('student-search');

function getInitials(name) {
  return name.split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase();
}

function buildStudentRow(student) {
  const row = document.createElement('div');
  row.className = 'student-row';
  row.id = `row-${student.id}`;
  row.dataset.name = student.name.toLowerCase();

  row.innerHTML = `
        <div class="student-avatar">${getInitials(student.name)}</div>
        <div class="student-info">
          <div class="student-name">${student.name}</div>
          <div class="student-mat">${student.matricule}</div>
        </div>
        <div class="status-group" role="group" aria-label="Statut de ${student.name}">
          <button class="status-btn ${attendance[student.id] === 'present' ? 'selected' : ''}"
            data-id="${student.id}" data-status="present" title="Présent">P</button>
          <button class="status-btn ${attendance[student.id] === 'retard' ? 'selected' : ''}"
            data-id="${student.id}" data-status="retard" title="Retard">R</button>
          <button class="status-btn ${attendance[student.id] === 'absent' ? 'selected' : ''}"
            data-id="${student.id}" data-status="absent" title="Absent">A</button>
        </div>
      `;

  row.querySelectorAll('.status-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const id = parseInt(btn.dataset.id);
      const status = btn.dataset.status;
      attendance[id] = status;
      updateStudentRow(id);
      updateCounters();
    });
  });

  return row;
}

function updateStudentRow(studentId) {
  const row = document.getElementById(`row-${studentId}`);
  if (!row) return;
  const status = attendance[studentId];
  row.className = `student-row is-${status}`;
  row.querySelectorAll('.status-btn').forEach(btn => {
    btn.classList.toggle('selected', btn.dataset.status === status);
  });
}

function renderList() {
  studentList.innerHTML = '';
  students.forEach(s => {
    studentList.appendChild(buildStudentRow(s));
  });
}
renderList();

// Search filter
searchInput.addEventListener('input', () => {
  const q = searchInput.value.toLowerCase();
  studentList.querySelectorAll('.student-row').forEach(row => {
    row.style.display = row.dataset.name.includes(q) ? '' : 'none';
  });
});

// Mark all present
document.getElementById('btn-mark-all-present').addEventListener('click', () => {
  students.forEach(s => { attendance[s.id] = 'present'; });
  students.forEach(s => updateStudentRow(s.id));
  updateCounters();
  showToast('Tous les étudiants marqués présents.', 'success');
});



// ═══════════════════════════════════════════════════════════════════════════════
// QR PRÉSENCE – GÉNÉRATION
// ═══════════════════════════════════════════════════════════════════════════════

let qrGenInited = false;
let sessionQRCode = null;   // QRCode instance – séance
let fsQRCode = null;   // QRCode instance – fullscreen
let studentQRCode = null;   // QRCode instance – étudiant sélectionné
let qrTimerInterval = null;
let qrTimeLeft = 120;    // 2 minutes in seconds
let currentToken = '';
let selectedStudentId = null;

// ── Mode toggle ───────────────────────────────────────────────────────────────
const modeSessionBtn = document.getElementById('qrgen-mode-session');
const modeStudentBtn = document.getElementById('qrgen-mode-student');
const sessionPanel = document.getElementById('qrgen-session-panel');
const studentPanel = document.getElementById('qrgen-student-panel');

modeSessionBtn?.addEventListener('click', () => {
  modeSessionBtn.classList.add('active');
  modeStudentBtn.classList.remove('active');
  sessionPanel.style.display = '';
  studentPanel.style.display = 'none';
});

modeStudentBtn?.addEventListener('click', () => {
  modeStudentBtn.classList.add('active');
  modeSessionBtn.classList.remove('active');
  studentPanel.style.display = '';
  sessionPanel.style.display = 'none';
  renderStudentPicker();
});

// ── Token generator (pseudo-random, changes each renewal) ─────────────────────
function genToken() {
  return `ADSIS-SESSION:${session.id}:${Date.now()}:${Math.random().toString(36).slice(2, 8).toUpperCase()}`;
}

// ── Init QR gen (called once when tab is first opened) ────────────────────────
function initQRGen() {
  if (qrGenInited) return;
  qrGenInited = true;

  // Set labels
  document.getElementById('qr-label-course').textContent = session.course;
  document.getElementById('qr-label-meta').textContent =
    [session.class, session.room, `${session.start}–${session.end}`].filter(Boolean).join(' • ');
  document.getElementById('qr-fs-course').textContent = session.course;
  document.getElementById('qr-fs-meta').textContent =
    [session.class, session.room, `${session.start}–${session.end}`].filter(Boolean).join(' • ');

  generateSessionQR();
}

// ── Generate/renew session QR ─────────────────────────────────────────────────
function generateSessionQR() {
  currentToken = genToken();

  // Clear previous
  const container = document.getElementById('qr-canvas-session');
  const containerFS = document.getElementById('qr-fullscreen-canvas');
  container.innerHTML = '';
  containerFS.innerHTML = '';

  // Hide expired overlay
  const expiredOverlay = document.getElementById('qr-expired-overlay');
  if (expiredOverlay) expiredOverlay.style.display = 'none';

  // QR size — responsive
  const qrSize = Math.min(220, window.innerWidth - 100);

  try {
    sessionQRCode = new QRCode(container, {
      text: currentToken,
      width: qrSize,
      height: qrSize,
      colorDark: '#1a1f36',
      colorLight: '#ffffff',
      correctLevel: QRCode.CorrectLevel.H,
    });

    fsQRCode = new QRCode(containerFS, {
      text: currentToken,
      width: Math.min(320, window.innerWidth - 80),
      height: Math.min(320, window.innerWidth - 80),
      colorDark: '#ffffff',
      colorLight: '#0f172a',
      correctLevel: QRCode.CorrectLevel.H,
    });
  } catch (e) {
    container.innerHTML = '<p style="color:var(--danger);font-size:.85rem;">Erreur de génération du QR code.</p>';
  }

  // Start / restart timer
  startQRTimer();
}

window.renewQR = generateSessionQR; // for inline onclick

// ── Timer ─────────────────────────────────────────────────────────────────────
function startQRTimer() {
  clearInterval(qrTimerInterval);
  qrTimeLeft = 120;
  updateTimerDisplay();

  qrTimerInterval = setInterval(() => {
    qrTimeLeft--;
    updateTimerDisplay();

    if (qrTimeLeft <= 0) {
      clearInterval(qrTimerInterval);
      // Show expired overlay
      const expiredOverlay = document.getElementById('qr-expired-overlay');
      if (expiredOverlay) {
        expiredOverlay.style.display = 'flex';
        // Blur QR to make it obviously invalid
        const canvas = document.querySelector('#qr-canvas-session canvas');
        if (canvas) canvas.style.filter = 'blur(6px)';
      }
    }
  }, 1000);
}

function updateTimerDisplay() {
  const mins = String(Math.floor(qrTimeLeft / 60)).padStart(2, '0');
  const secs = String(qrTimeLeft % 60).padStart(2, '0');
  const str = `${mins}:${secs}`;
  const expiring = qrTimeLeft <= 30;

  ['qr-timer', 'qr-fs-timer'].forEach(id => {
    const el = document.getElementById(id);
    if (!el) return;
    el.textContent = str;
    el.classList.toggle('expiring', expiring);
  });
}

// ── Renew button ──────────────────────────────────────────────────────────────
document.getElementById('btn-refresh-qr')?.addEventListener('click', generateSessionQR);

// ── Fullscreen ────────────────────────────────────────────────────────────────
const fsModal = document.getElementById('qr-fullscreen-modal');
const fsCloseBtn = document.getElementById('qr-fullscreen-close');

document.getElementById('btn-fullscreen-qr')?.addEventListener('click', () => {
  fsModal.style.display = 'flex';
  document.body.style.overflow = 'hidden';
});

fsCloseBtn?.addEventListener('click', closeFullscreen);
fsModal?.addEventListener('click', e => { if (e.target === fsModal) closeFullscreen(); });
document.addEventListener('keydown', e => { if (e.key === 'Escape') closeFullscreen(); });

function closeFullscreen() {
  fsModal.style.display = 'none';
  document.body.style.overflow = '';
}

// ── Download session QR ───────────────────────────────────────────────────────
document.getElementById('btn-download-qr')?.addEventListener('click', () => {
  const canvas = document.querySelector('#qr-canvas-session canvas');
  if (!canvas) return;

  // Draw a decorated version with label
  const label = session.course || 'Séance';
  const meta = [session.class, session.room].filter(Boolean).join(' • ');
  const padding = 20;
  const textH = 52;

  const out = document.createElement('canvas');
  out.width = canvas.width + padding * 2;
  out.height = canvas.height + padding * 2 + textH;
  const ctx = out.getContext('2d');

  // White bg
  ctx.fillStyle = '#fff';
  ctx.roundRect(0, 0, out.width, out.height, 16);
  ctx.fill();

  // QR
  ctx.drawImage(canvas, padding, padding);

  // Label
  ctx.fillStyle = '#1a1f36';
  ctx.font = 'bold 16px system-ui, sans-serif';
  ctx.textAlign = 'center';
  ctx.fillText(label, out.width / 2, canvas.height + padding + 24);

  ctx.fillStyle = '#6b7280';
  ctx.font = '13px system-ui, sans-serif';
  ctx.fillText(meta, out.width / 2, canvas.height + padding + 44);

  const a = document.createElement('a');
  a.download = `QR_${label.replace(/\s+/g, '-')}_${session.class || 'seance'}.png`;
  a.href = out.toDataURL('image/png');
  a.click();
});

// ═══════════════════════════════════════════════════════════════════════════════
// QR PAR ÉTUDIANT
// ═══════════════════════════════════════════════════════════════════════════════

function renderStudentPicker() {
  const listEl = document.getElementById('qrgen-student-list');
  if (!listEl) return;
  listEl.innerHTML = '';

  students.forEach(s => {
    const item = document.createElement('div');
    item.className = 'qrgen-stu-item';
    item.dataset.id = s.id;
    item.dataset.name = s.name.toLowerCase();

    const initials = s.name.split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase();
    item.innerHTML = `
          <div class="qrgen-stu-avatar">${initials}</div>
          <div class="qrgen-stu-info">
            <div class="qrgen-stu-name">${s.name}</div>
            <div class="qrgen-stu-mat">${s.matricule}</div>
          </div>
        `;

    item.addEventListener('click', () => {
      listEl.querySelectorAll('.qrgen-stu-item').forEach(i => i.classList.remove('selected'));
      item.classList.add('selected');
      showStudentQR(s);
    });

    listEl.appendChild(item);
  });
}

// Student search filter
document.getElementById('qrgen-student-search')?.addEventListener('input', function () {
  const q = this.value.toLowerCase();
  document.querySelectorAll('.qrgen-stu-item').forEach(item => {
    item.style.display = item.dataset.name.includes(q) ? '' : 'none';
  });
});

function showStudentQR(student) {
  selectedStudentId = student.id;
  const container = document.getElementById('qrgen-student-qr');
  container.innerHTML = '';

  // QR data: ADSIS:<studentId>:<matricule>:<sessionId>
  const qrData = `ADSIS:${student.id}:${student.matricule}:${session.id}`;
  const qrSize = Math.min(180, window.innerWidth - 160);

  const box = document.createElement('div');
  box.className = 'qrgen-stu-qr-box';

  const canvasWrap = document.createElement('div');
  canvasWrap.id = `stu-qr-${student.id}`;

  box.innerHTML = `
        <div class="qrgen-stu-qr-name">${student.name}</div>
      `;
  box.insertBefore(canvasWrap, box.firstChild);

  const matEl = document.createElement('div');
  matEl.className = 'qrgen-stu-qr-mat';
  matEl.textContent = student.matricule;
  box.appendChild(matEl);

  // Buttons row
  const btnRow = document.createElement('div');
  btnRow.className = 'qrgen-stu-btn-row';

  const btnDl = document.createElement('button');
  btnDl.className = 'btn-qrgen-action btn-stu-dl btn-download-qr';
  btnDl.innerHTML = `
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
          <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
          <polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/>
        </svg>
        Télécharger
      `;

  const btnMark = document.createElement('button');
  btnMark.className = 'btn-qrgen-action btn-stu-mark';
  btnMark.innerHTML = `
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
          <polyline points="20 6 9 17 4 12"/>
        </svg>
        Marquer présent
      `;

  btnRow.appendChild(btnDl);
  btnRow.appendChild(btnMark);
  box.appendChild(btnRow);
  container.appendChild(box);

  // Generate QR
  try {
    studentQRCode = new QRCode(canvasWrap, {
      text: qrData,
      width: qrSize,
      height: qrSize,
      colorDark: '#1a1f36',
      colorLight: '#ffffff',
      correctLevel: QRCode.CorrectLevel.H,
    });
  } catch (e) {
    canvasWrap.textContent = 'Erreur QR';
  }

  // Download student QR
  btnDl.addEventListener('click', () => {
    const canvas = canvasWrap.querySelector('canvas');
    if (!canvas) return;
    const a = document.createElement('a');
    a.download = `QR_${student.name.replace(/\s+/g, '-')}.png`;
    a.href = canvas.toDataURL('image/png');
    a.click();
  });

  // Mark present manually
  btnMark.addEventListener('click', () => {
    attendance[student.id] = 'present';
    updateCounters();
    updateStudentRow(student.id);
    showToast(`✓ ${student.name} marqué présent`, 'success');
    btnMark.disabled = true;
    btnMark.style.opacity = '0.6';
    btnMark.textContent = 'Présent ✓';
  });
}

// ═══════════════════════════════════════════════════════════════════════════════
const modalOverlay = document.getElementById('modal-overlay');
const modalText = document.getElementById('modal-text');
const modalCancel = document.getElementById('modal-cancel');
const modalConfirm = document.getElementById('modal-confirm');

document.getElementById('btn-cloture').addEventListener('click', () => {
  const vals = Object.values(attendance);
  const p = vals.filter(v => v === 'present').length;
  const r = vals.filter(v => v === 'retard').length;
  const a = vals.filter(v => v === 'absent').length;
  modalText.textContent =
    `Résumé : ${p} présent(s), ${r} en retard, ${a} absent(s). Confirmer la clôture ?`;
  modalOverlay.style.display = 'flex';
});

modalCancel.addEventListener('click', () => { modalOverlay.style.display = 'none'; });
modalOverlay.addEventListener('click', e => { if (e.target === modalOverlay) modalOverlay.style.display = 'none'; });

modalConfirm.addEventListener('click', async () => {
  modalOverlay.style.display = 'none';
  await clotureAppel();
});

async function clotureAppel() {
  stopScan();
  const user = JSON.parse(localStorage.getItem('adsis_user') || '{}');
  const token = localStorage.getItem('adsis_token') || '';

  const payload = {
    sessionId: session.id,
    course: session.course,
    class: session.class,
    date: new Date().toISOString(),
    teacher: user.name || '',
    records: students.map(s => ({
      studentId: s.id,
      name: s.name,
      matricule: s.matricule,
      status: attendance[s.id] || 'absent',
    })),
  };

  // Try to send to server
  let sent = false;
  try {
    const res = await fetch('/api/appel/cloture', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
      },
      body: JSON.stringify(payload),
    });
    if (res.ok) sent = true;
  } catch { /* offline */ }

  if (!sent) {
    // Store in IndexedDB for offline sync
    await saveToIndexedDB(payload);
    showToast('Appel sauvegardé hors ligne. Il sera synchronisé à la reconnexion.', 'info', 5000);
  } else {
    showToast('Appel clôturé et envoyé avec succès !', 'success', 4000);
  }

  setTimeout(() => { window.location.href = 'dashboardens.html'; }, 2500);
}

// ── IndexedDB helper ──────────────────────────────────────────────────────────
function openDB() {
  return new Promise((resolve, reject) => {
    const req = indexedDB.open('adsis_db', 1);
    req.onupgradeneeded = e => {
      const db = e.target.result;
      if (!db.objectStoreNames.contains('appels')) {
        db.createObjectStore('appels', { keyPath: 'id', autoIncrement: true });
      }
    };
    req.onsuccess = e => resolve(e.target.result);
    req.onerror = e => reject(e.target.error);
  });
}

async function saveToIndexedDB(payload) {
  try {
    const db = await openDB();
    const tx = db.transaction('appels', 'readwrite');
    const store = tx.objectStore('appels');
    store.add({ ...payload, savedAt: new Date().toISOString(), synced: false });
    await new Promise((res, rej) => { tx.oncomplete = res; tx.onerror = rej; });
    db.close();
  } catch (err) {
    // Fallback to localStorage
    const key = `adsis_appel_${Date.now()}`;
    try { localStorage.setItem(key, JSON.stringify(payload)); } catch { }
  }
}


