/* ===== ADSIS – appel.js ===== */

const schedule = window.ADSIS_SCHEDULE || [];
const students = window.ADSIS_STUDENTS || [];

// ── Read session id from URL ───────────────────────────────────────────────────
const urlParams = new URLSearchParams(window.location.search);
const sessionId = parseInt(urlParams.get('id')) || (schedule[0] && schedule[0].id);
const session   = schedule.find(s => s.id === sessionId) || schedule[0] || {
  id: 0, course: 'Séance', class: '', room: '', start: '', end: '', day: '', color: '#3B55E6'
};

// ── Populate session info ─────────────────────────────────────────────────────
document.getElementById('session-course').textContent = session.course;
document.getElementById('session-meta').textContent   =
  [session.class, session.room, `${session.start} – ${session.end}`].filter(Boolean).join(' • ');
document.getElementById('page-title').textContent = 'Séance d\'appel';

// ── Attendance state: { studentId: 'present' | 'absent' | 'retard' } ──────────
const attendance = {};
students.forEach(s => { attendance[s.id] = 'absent'; }); // default: absent

// ── Update counters ───────────────────────────────────────────────────────────
function updateCounters() {
  const vals = Object.values(attendance);
  document.getElementById('cnt-present').textContent = vals.filter(v => v === 'present').length;
  document.getElementById('cnt-retard').textContent  = vals.filter(v => v === 'retard').length;
  document.getElementById('cnt-absent').textContent  = vals.filter(v => v === 'absent').length;
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
const tabScan   = document.getElementById('tab-scan');
const tabList   = document.getElementById('tab-list');
const panelScan = document.getElementById('panel-scan');
const panelList = document.getElementById('panel-list');

function activateTab(tab) {
  if (tab === 'scan') {
    tabScan.classList.add('active');  tabScan.setAttribute('aria-selected','true');
    tabList.classList.remove('active'); tabList.setAttribute('aria-selected','false');
    panelScan.classList.remove('hidden');
    panelList.classList.add('hidden');
  } else {
    tabList.classList.add('active');  tabList.setAttribute('aria-selected','true');
    tabScan.classList.remove('active'); tabScan.setAttribute('aria-selected','false');
    panelList.classList.remove('hidden');
    panelScan.classList.add('hidden');
    stopScan();
  }
}

tabScan.addEventListener('click', () => activateTab('scan'));
tabList.addEventListener('click', () => activateTab('list'));

// ═══════════════════════════════════════════════════════════════════════════════
// QR SCAN
// ═══════════════════════════════════════════════════════════════════════════════
let html5QrCode = null;
let scanning    = false;

const btnStart  = document.getElementById('btn-start-scan');
const btnStop   = document.getElementById('btn-stop-scan');
const camError  = document.getElementById('camera-error');
const scanResult= document.getElementById('scan-result');
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
      () => {} // suppress errors
    );
    scanning = true;
    btnStart.style.display = 'none';
    btnStop.style.display  = 'inline-flex';
  } catch (err) {
    // Try front camera
    try {
      await html5QrCode.start(
        { facingMode: 'user' },
        config,
        onQrSuccess,
        () => {}
      );
      scanning = true;
      btnStart.style.display = 'none';
      btnStop.style.display  = 'inline-flex';
    } catch (e2) {
      camError.style.display = 'flex';
    }
  }
}

async function stopScan() {
  if (!scanning || !html5QrCode) return;
  try { await html5QrCode.stop(); } catch {}
  scanning = false;
  btnStart.style.display = 'inline-flex';
  btnStop.style.display  = 'none';
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
btnStop.addEventListener('click',  stopScan);

// ═══════════════════════════════════════════════════════════════════════════════
// STUDENT LIST
// ═══════════════════════════════════════════════════════════════════════════════
const studentList = document.getElementById('student-list');
const searchInput = document.getElementById('student-search');

function getInitials(name) {
  return name.split(' ').map(w => w[0]).join('').slice(0,2).toUpperCase();
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
      <button class="status-btn ${attendance[student.id]==='present'?'selected':''}"
        data-id="${student.id}" data-status="present" title="Présent">P</button>
      <button class="status-btn ${attendance[student.id]==='retard'?'selected':''}"
        data-id="${student.id}" data-status="retard" title="Retard">R</button>
      <button class="status-btn ${attendance[student.id]==='absent'?'selected':''}"
        data-id="${student.id}" data-status="absent" title="Absent">A</button>
    </div>
  `;

  row.querySelectorAll('.status-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const id     = parseInt(btn.dataset.id);
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
// CLÔTURER L'APPEL
// ═══════════════════════════════════════════════════════════════════════════════
const modalOverlay = document.getElementById('modal-overlay');
const modalText    = document.getElementById('modal-text');
const modalCancel  = document.getElementById('modal-cancel');
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
  const user  = JSON.parse(localStorage.getItem('adsis_user') || '{}');
  const token = localStorage.getItem('adsis_token') || '';

  const payload = {
    sessionId: session.id,
    course:    session.course,
    class:     session.class,
    date:      new Date().toISOString(),
    teacher:   user.name || '',
    records:   students.map(s => ({
      studentId:  s.id,
      name:       s.name,
      matricule:  s.matricule,
      status:     attendance[s.id] || 'absent',
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
    req.onerror   = e => reject(e.target.error);
  });
}

async function saveToIndexedDB(payload) {
  try {
    const db  = await openDB();
    const tx  = db.transaction('appels', 'readwrite');
    const store = tx.objectStore('appels');
    store.add({ ...payload, savedAt: new Date().toISOString(), synced: false });
    await new Promise((res, rej) => { tx.oncomplete = res; tx.onerror = rej; });
    db.close();
  } catch (err) {
    // Fallback to localStorage
    const key = `adsis_appel_${Date.now()}`;
    try { localStorage.setItem(key, JSON.stringify(payload)); } catch {}
  }
}