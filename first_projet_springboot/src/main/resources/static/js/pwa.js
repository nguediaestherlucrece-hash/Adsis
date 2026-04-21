// ═══ ADSIS – PWA Registration & Offline Sync ═══

// ── Enregistrement du Service Worker ──
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/sw.js')
      .then(reg => {
        console.log('[PWA] Service Worker enregistré :', reg.scope);

        // Vérifier les mises à jour toutes les 30 min
        setInterval(() => reg.update(), 30 * 60 * 1000);
      })
      .catch(err => console.warn('[PWA] Échec de l\'enregistrement du SW :', err));
  });
}

// ── Détection en ligne / hors-ligne ──
const offlineBanner = (() => {
  const el = document.createElement('div');
  el.id = 'offline-banner';
  el.setAttribute('role', 'alert');
  el.innerHTML = `
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
      <line x1="1" y1="1" x2="23" y2="23"/><path d="M16.72 11.06A10.94 10.94 0 0 1 19 12.55"/><path d="M5 12.55a10.94 10.94 0 0 1 5.17-2.39"/>
      <path d="M10.71 5.05A16 16 0 0 1 22.56 9"/><path d="M1.42 9a15.91 15.91 0 0 1 4.7-2.88"/><path d="M8.53 16.11a6 6 0 0 1 6.95 0"/><line x1="12" y1="20" x2="12.01" y2="20"/>
    </svg>
    <span>Mode hors-ligne – Les données seront synchronisées au retour en ligne</span>
  `;
  Object.assign(el.style, {
    display: 'none',
    position: 'fixed',
    bottom: '0',
    left: '0',
    right: '0',
    background: 'linear-gradient(135deg, #f59e0b, #d97706)',
    color: '#fff',
    padding: '12px 20px',
    fontSize: '0.88rem',
    fontWeight: '600',
    zIndex: '10000',
    textAlign: 'center',
    boxShadow: '0 -2px 12px rgba(0,0,0,0.15)',
    gap: '8px',
    alignItems: 'center',
    justifyContent: 'center',
    fontFamily: "'Segoe UI', system-ui, sans-serif"
  });
  document.body.appendChild(el);
  return el;
})();

function updateOnlineStatus() {
  if (navigator.onLine) {
    offlineBanner.style.display = 'none';
    // Déclencher la synchronisation
    if ('serviceWorker' in navigator && navigator.serviceWorker.controller) {
      navigator.serviceWorker.ready.then(reg => {
        if (reg.sync) reg.sync.register('sync-attendance');
      });
    }
  } else {
    offlineBanner.style.display = 'flex';
  }
}

window.addEventListener('online', updateOnlineStatus);
window.addEventListener('offline', updateOnlineStatus);
updateOnlineStatus();

// ── API de mise en file d'attente hors-ligne ──
window.queueOfflineRequest = async function(url, method, headers, body) {
  const db = await openOfflineDB();
  const tx = db.transaction('queue', 'readwrite');
  tx.objectStore('queue').add({ url, method, headers, body, timestamp: Date.now() });
  return tx.complete;
};

function openOfflineDB() {
  return new Promise((resolve, reject) => {
    const req = indexedDB.open('adsis-offline', 1);
    req.onupgradeneeded = () => {
      req.result.createObjectStore('queue', { keyPath: 'id', autoIncrement: true });
    };
    req.onsuccess = () => resolve(req.result);
    req.onerror = () => reject(req.error);
  });
}
