// ═══ ADSIS – Service Worker ═══
// Stratégie : Network-first partout (fiabilité > performance)
// Le cache sert uniquement de fallback hors-ligne

const CACHE_NAME = 'adsis-v10';
const API_CACHE  = 'adsis-v10-api';

// Assets à pré-cacher pour le mode hors-ligne
const STATIC_ASSETS = [
  '/css/shared-ens.css',
  '/css/dashboardens.css',
  '/css/emploiens.css',
  '/css/appel.css',
  '/css/profilens.css',
  '/js/shared.js',
  '/js/auth.js',
  '/js/api.js',
  '/js/dashboardens.js',
  '/js/emploiens.js',
  '/js/appel.js',
  '/js/login.js',
  '/js/profile-unified.js',
  '/js/pwa.js',
  '/manifest.json'
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

  // API → Network-first, cache API séparé
  if (url.pathname.startsWith('/api/')) {
    if (event.request.method !== 'GET') return; // POST/PUT/DELETE = pas de cache
    event.respondWith(networkFirst(event.request, API_CACHE));
    return;
  }

  // Assets statiques (CSS/JS/images) → Network-first, fallback cache
  if (url.pathname.match(/\.(css|js|png|jpg|svg|woff2?|ico|json)$/)) {
    event.respondWith(networkFirst(event.request, CACHE_NAME));
    return;
  }

  // Navigation → Network-first
  if (event.request.mode === 'navigate') {
    event.respondWith(
      fetch(event.request).catch(() =>
        caches.match('/enseignant/dashboard')
          .then(cached => cached || new Response('Hors ligne', { status: 503 }))
      )
    );
    return;
  }

  // Default: passthrough (pas d'interception)
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
