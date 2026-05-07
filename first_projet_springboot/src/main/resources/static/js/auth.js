/* ===== ADSIS – auth.js — Module central de sécurité ===== */
/* Importé par TOUS les scripts. Ne pas modifier sans coordination. */

const API_BASE_URL = 'http://localhost:8081';

const KEYS = {
    ACCESS_TOKEN:  'adsis_token',
    REFRESH_TOKEN: 'adsis_refresh_token',
    USER:          'adsis_user',
};

// ══════════════════════════════════════════════════════════════
// 1. Gestion de session (localStorage)
// ══════════════════════════════════════════════════════════════

export function saveSession(data) {
    localStorage.setItem(KEYS.ACCESS_TOKEN, data.accessToken);
    localStorage.setItem(KEYS.REFRESH_TOKEN, data.refreshToken);
    localStorage.setItem(KEYS.USER, JSON.stringify({
        id:          data.id,
        nom:         data.nom,
        prenom:      data.prenom,
        email:       data.email,
        roles:       data.roles       || [],
        permissions: data.permissions || [],
    }));
}

export function getToken()        { return localStorage.getItem(KEYS.ACCESS_TOKEN); }
export function getRefreshToken() { return localStorage.getItem(KEYS.REFRESH_TOKEN); }

export function getUser() {
    try { return JSON.parse(localStorage.getItem(KEYS.USER) || 'null'); }
    catch { return null; }
}

export function clearSession() {
    Object.values(KEYS).forEach(k => localStorage.removeItem(k));
}

// ══════════════════════════════════════════════════════════════
// 2. Helpers rôles & permissions
// ══════════════════════════════════════════════════════════════

export function hasRole(allowedRoles = []) {
    const user = getUser();
    if (!user) return false;
    return allowedRoles.some(r => (user.roles || []).includes(r));
}

export function hasPermission(requiredPermissions = []) {
    const user = getUser();
    if (!user) return false;
    if ((user.roles || []).includes('ADMIN')) return true;
    return requiredPermissions.some(p => (user.permissions || []).includes(p));
}

export function requirePermission(permission) {
    const user = getUser();
    if (!user) return false;
    if ((user.roles || []).includes('ADMIN')) return true;
    return (user.permissions || []).includes(permission);
}

export function applyPermissionVisibility() {
    const user = getUser();
    if (!user) return;
    const isAdmin = (user.roles || []).includes('ADMIN');
    document.querySelectorAll('[data-requires-permission]').forEach(el => {
        const perm = el.dataset.requiresPermission;
        const hasIt = isAdmin || (user.permissions || []).includes(perm);
        if (!hasIt) {
            el.style.display = 'none';
            el.setAttribute('aria-hidden', 'true');
        }
    });
}

// ══════════════════════════════════════════════════════════════
// 3. Refresh token silencieux
// ══════════════════════════════════════════════════════════════

let _refreshPromise = null;

async function _tryRefreshToken() {
    if (_refreshPromise) return _refreshPromise;
    _refreshPromise = (async () => {
        const rt = getRefreshToken();
        if (!rt) return false;
        try {
            const res = await _nativeFetch(`${API_BASE_URL}/api/auth/refresh`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ refreshToken: rt }),
            });
            if (!res.ok) return false;
            const data = await res.json();
            saveSession(data);
            return true;
        } catch { return false; }
        finally   { _refreshPromise = null; }
    })();
    return _refreshPromise;
}

// Référence au vrai fetch natif, avant patch
const _nativeFetch = window.fetch.bind(window);

// ══════════════════════════════════════════════════════════════
// 4. authFetch — fetch authentifié (usage explicite dans du code refactorisé)
// ══════════════════════════════════════════════════════════════

export async function authFetch(url, options = {}) {
    // Si l'URL est relative (/api/...) on préfixe avec le base URL
    const fullUrl = (typeof url === 'string' && url.startsWith('/'))
        ? `${API_BASE_URL}${url}`
        : url;

    const token = getToken();
    const isFormData = options.body instanceof FormData;
    const headers = {
        ...(isFormData ? {} : { 'Content-Type': 'application/json' }),
        ...(options.headers || {}),
        ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
    };

    let response = await _nativeFetch(fullUrl, { ...options, headers });

    if (response.status === 401) {
        const refreshed = await _tryRefreshToken();
        if (refreshed) {
            headers['Authorization'] = `Bearer ${getToken()}`;
            response = await _nativeFetch(fullUrl, { ...options, headers });
        } else {
            clearSession();
            window.location.href = getLoginPath();
            return null;
        }
    }
    return response;
}

// ══════════════════════════════════════════════════════════════
// 5. Intercepteur global window.fetch
// Intercepte TOUS les fetch() natifs vers localhost:8081
// (admin.js, cycles.js, matiere.js, emploi.js, etc.)
// ══════════════════════════════════════════════════════════════

window.fetch = async function interceptedFetch(url, options = {}) {
    const urlStr = (url instanceof Request) ? url.url : String(url || '');

    // Détecter si c'est une requête vers notre backend
    const isBackend = urlStr.includes('localhost:8081') || urlStr.startsWith('/api');

    if (!isBackend) {
        // Requête externe (CDN Bootstrap, etc.) → pass-through
        return _nativeFetch(url, options);
    }

    // URL relative → absolue
    const finalUrl = (typeof url === 'string' && url.startsWith('/'))
        ? `${API_BASE_URL}${url}`
        : url;

    // Injecter le token
    const token = getToken();
    const isFormData = options.body instanceof FormData;
    const existingHeaders = options.headers
        ? (options.headers instanceof Headers
            ? Object.fromEntries(options.headers.entries())
            : { ...options.headers })
        : {};

    const headers = {
        ...(isFormData ? {} : { 'Content-Type': 'application/json' }),
        ...existingHeaders,
        ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
    };

    let response = await _nativeFetch(finalUrl, { ...options, headers });

    // Refresh silencieux si 401
    if (response.status === 401) {
        const refreshed = await _tryRefreshToken();
        if (refreshed) {
            headers['Authorization'] = `Bearer ${getToken()}`;
            return _nativeFetch(finalUrl, { ...options, headers });
        } else {
            clearSession();
            window.location.href = getLoginPath();
            return response;
        }
    }
    return response;
};

// ══════════════════════════════════════════════════════════════
// 6. Guards de rôle
// ══════════════════════════════════════════════════════════════

export function requireRole(allowedRoles = []) {
    const user = getUser();
    if (!user) {
        clearSession();
        window.location.href = getLoginPath();
        return false;
    }
    if (!allowedRoles.some(r => (user.roles || []).includes(r))) {
        _redirectToDashboard(user.roles || []);
        return false;
    }
    return true;
}

// ══════════════════════════════════════════════════════════════
// 7. Logout
// ══════════════════════════════════════════════════════════════

export function logout() {
    // Appel backend en best-effort (sans bloquer)
    authFetch('/api/auth/logout', { method: 'POST' }).catch(() => {});
    clearSession();
    window.location.href = getLoginPath();
}

// ══════════════════════════════════════════════════════════════
// 8. Helpers privés
// ══════════════════════════════════════════════════════════════

function _redirectToDashboard(roles) {
    if (roles.includes('ADMIN'))
        window.location.href = '/admin/tableaudebord';
    else if (roles.includes('ENSEIGNANT'))
        window.location.href = '/enseignant/dashboard';
    else if (roles.includes('ASSISTANT_PEDAGOGIQUE') || roles.includes('SURVEILLANT'))
        window.location.href = '/assistant/tableaudebord';
    else if (roles.includes('ETUDIANT'))
    window.location.href = '/etudiant/dashboard';
    else
        window.location.href = getLoginPath();
}

export function getLoginPath() {
    return '/login';
}
