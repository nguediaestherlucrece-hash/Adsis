// ═══ ADSIS – Service Worker ═══
// Stratégie : Network-first partout (fiabilité > performance)
// Le cache sert uniquement de fallback hors-ligne

const CACHE_NAME = 'adsis-v12';
const API_CACHE  = 'adsis-v12-api';

// Assets à pré-cacher pour le mode hors-ligne
const STATIC_ASSETS = [
  // ── Partagés ──
  '/css/sidebar.css',
  '/js/shared.js',
  '/js/auth.js',
  '/js/api.js',
  '/js/login.js',
  '/js/profile-unified.js',
  '/js/pwa.js',
  '/manifest.json',
  '/css/profil.css',
  // ── Enseignant ──
  '/css/shared-ens.css',
  '/css/dashboardens.css',
  '/css/emploiens.css',
  '/css/appel.css',
  '/css/profilens.css',
  '/js/dashboardens.js',
  '/js/emploiens.js',
  '/js/appel.js',
  // ── Étudiant ──
  '/css/dashboardetu.css',
  '/css/emploietu.css',
  '/css/absences.css',
  '/css/justifications.css',
  '/css/appeletu.css',
  '/css/etudiant-shared.css',
  '/js/dashboardetu.js',
  '/js/emploietu.js',
  '/js/absences.js',
  '/js/justifications.js',
  '/js/appeletu.js',
  '/js/profiletu.js',
];

// ── Install ──
self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => Promise.allSettled(
        STATIC_ASSETS.map(url =>
          cache.add(url).catch(err => console.warn('[SW] Skip:', url, err.message))
        )
      ))
      .then(() => self.skipWaiting())
  );
});

// ── Activate : nettoyage des anciens caches ──
self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then(keys =>
      Promise.all(
        keys.filter(k => k !== CACHE_NAME && k !== API_CACHE).map(k => caches.delete(k))
      )
    ).then(() => self.clients.claim())
  );
});

// ── Helper : Network-first avec fallback cache ──
async function networkFirst(request, cacheName) {
  try {
    const response = await fetch(request);
    if (response.ok && request.method === 'GET') {
      const cache = await caches.open(cacheName);
      cache.put(request, response.clone());
    }
    return response;
  } catch (err) {
    const cached = await caches.match(request);
    return cached || new Response('Offline', { status: 503 });
  }
}

// ── Fetch : TOUT en Network-first ──
self.addEventListener('fetch', (event) => {
  const url = new URL(event.request.url);

  // API GET → Network-first, cache API séparé (pas de POST/PUT/DELETE)
  if (url.pathname.startsWith('/api/')) {
    if (event.request.method !== 'GET') return;
    event.respondWith(networkFirst(event.request, API_CACHE));
    return;
  }

  // Assets statiques (CSS/JS/images) → Network-first, fallback cache
  if (url.pathname.match(/\.(css|js|png|jpg|svg|woff2?|ico|json)$/)) {
    event.respondWith(networkFirst(event.request, CACHE_NAME));
    return;
  }

  // Navigation → Network-first, fallback vers la page hors-ligne selon le rôle
  if (event.request.mode === 'navigate') {
    event.respondWith(
      fetch(event.request).catch(() => {
        const path = url.pathname;
        // Fallback vers la dernière page du bon module mise en cache
        if (path.startsWith('/etudiant')) {
          return caches.match('/etudiant/dashboard')
            .then(cached => cached || new Response('Hors ligne', { status: 503 }));
        }
        return caches.match('/enseignant/dashboard')
          .then(cached => cached || new Response('Hors ligne', { status: 503 }));
      })
    );
    return;
  }

  // Default: passthrough
});

// ── Background Sync ──
self.addEventListener('sync', (event) => {
  if (event.tag === 'sync-attendance') {
    event.waitUntil(replayOfflineQueue());
  }
});

async function replayOfflineQueue() {
  const db = await openOfflineDB();
  const tx = db.transaction('queue', 'readwrite');
  const store = tx.objectStore('queue');
  const all = await storeGetAll(store);
  for (const entry of all) {
    try {
      const response = await fetch(entry.url, {
        method: entry.method, headers: entry.headers, body: entry.body
      });
      if (response.ok) store.delete(entry.id);
    } catch { break; }
  }
}

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

function storeGetAll(store) {
  return new Promise((resolve, reject) => {
    const req = store.getAll();
    req.onsuccess = () => resolve(req.result);
    req.onerror = () => reject(req.error);
  });
}
