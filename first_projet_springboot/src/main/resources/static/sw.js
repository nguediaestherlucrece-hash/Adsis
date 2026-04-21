// ═══ ADSIS – Service Worker ═══
// Stratégie : Cache-first pour les assets statiques, Network-first pour les API

const CACHE_NAME = 'adsis-v5';
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

// ── Install : pré-cache des assets statiques ──
self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => {
        // Cache each asset individually — don't fail the whole install if one 404s
        return Promise.allSettled(
          STATIC_ASSETS.map(url =>
            cache.add(url).catch(err => console.warn('[SW] Skip cache:', url, err.message))
          )
        );
      })
      .then(() => self.skipWaiting())
  );
});

// ── Activate : nettoyage des anciens caches ──
self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then(keys =>
      Promise.all(keys
        .filter(k => k !== CACHE_NAME && k !== CACHE_NAME + '-api')
        .map(k => caches.delete(k))
      )
    ).then(() => self.clients.claim())
  );
});

// ── Fetch : stratégie duale ──
self.addEventListener('fetch', (event) => {
  const url = new URL(event.request.url);

  // API calls → Network-first avec fallback cache
  if (url.pathname.startsWith('/api/')) {
    event.respondWith(
      fetch(event.request)
        .then(response => {
          // Cache les réponses GET API pour le mode offline
          if (event.request.method === 'GET' && response.ok) {
            const cloned = response.clone();
            caches.open(CACHE_NAME + '-api')
              .then(cache => cache.put(event.request, cloned));
          }
          return response;
        })
        .catch(() => {
          // Fallback : réponse en cache
          return caches.match(event.request)
            .then(cached => cached || new Response(
              JSON.stringify({ offline: true, error: 'Pas de connexion' }),
              { status: 503, headers: { 'Content-Type': 'application/json' } }
            ));
        })
    );
    return;
  }

  // Static assets (CSS/JS/images) → Cache-first
  if (url.pathname.match(/\.(css|js|png|jpg|svg|woff2?|ico|json)$/)) {
    event.respondWith(
      caches.match(event.request)
        .then(cached => {
          if (cached) return cached;
          return fetch(event.request).then(response => {
            if (response.ok && event.request.method === 'GET') {
              const cloned = response.clone();
              caches.open(CACHE_NAME)
                .then(cache => cache.put(event.request, cloned));
            }
            return response;
          });
        })
        .catch(() => new Response('', { status: 503 }))
    );
    return;
  }

  // Page navigation → Network-first (let Thymeleaf render)
  if (event.request.mode === 'navigate') {
    event.respondWith(
      fetch(event.request)
        .catch(() => caches.match('/enseignant/dashboard')
          .then(cached => cached || new Response('Hors ligne', { status: 503 }))
        )
    );
    return;
  }

  // Default: passthrough
  event.respondWith(fetch(event.request));
});

// ── Background Sync : rejeu des appels API hors-ligne ──
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
        method: entry.method,
        headers: entry.headers,
        body: entry.body
      });
      if (response.ok) {
        store.delete(entry.id);
      }
    } catch {
      // Encore hors-ligne, on réessaiera au prochain sync
      break;
    }
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
