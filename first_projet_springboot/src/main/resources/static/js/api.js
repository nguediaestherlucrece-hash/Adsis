import { authFetch } from './auth.js';

// authFetch préfixe déjà avec http://localhost:8081
// Les endpoints sont donc des chemins relatifs /api/...

const ROLE_ENDPOINTS = {
    "ETUDIANT":              "/api/etudiants",
    "ENSEIGNANT":            "/api/enseignants",
    "SURVEILLANT":           "/api/surveillants",
    "ASSISTANT_PEDAGOGIQUE": "/api/assistants-pedagogiques",
    "ADMIN":                 "/api/administrateurs",
    "Étudiant":              "/api/etudiants",
    "Enseignant":            "/api/enseignants",
    "Surveillant":           "/api/surveillants",
    "Assistant pédagogique": "/api/assistants-pedagogiques",
    "Administrateur":        "/api/administrateurs",
};

const ROLE_MAPPING = {
    "Étudiant":              "ETUDIANT",
    "Enseignant":            "ENSEIGNANT",
    "Surveillant":           "SURVEILLANT",
    "Assistant pédagogique": "ASSISTANT_PEDAGOGIQUE",
    "Administrateur":        "ADMIN",
};

function getEndpoint(role) {
    if (Array.isArray(role)) role = role[0];
    return ROLE_ENDPOINTS[role] || "/api/etudiants";
}

export const Api = {

    // Récupère la liste globale de tous les utilisateurs
    async getUsers() {
        const endpoints = [...new Set(Object.values(ROLE_ENDPOINTS))];
        const results = await Promise.all(
            endpoints.map(ep =>
                authFetch(ep).then(r => r && r.ok ? r.json() : []).catch(() => [])
            )
        );
        return results.flat();
    },

    // Récupère uniquement le staff (enseignants, surveillants, assistants)
    async getStaff() {
        const staffEndpoints = ['/api/enseignants', '/api/surveillants', '/api/assistants-pedagogiques'];
        const results = await Promise.all(
            staffEndpoints.map(ep =>
                authFetch(ep).then(r => r && r.ok ? r.json() : []).catch(() => [])
            )
        );
        return results.flat();
    },

    // Récupère un utilisateur selon son ID et son rôle
    async getUserById(id, role) {
        const ep = getEndpoint(role);
        const r = await authFetch(`${ep}/${id}`);
        if (!r || !r.ok) throw new Error(`Erreur récupération utilisateur ${id}`);
        return r.json();
    },

    // Crée un nouvel utilisateur
    async createUser(data) {
        const role = data.roleNames ? data.roleNames[0] : (ROLE_MAPPING[data.role] || "ETUDIANT");
        if (!data.roleNames) data.roleNames = [ROLE_MAPPING[data.role]];
        const ep = getEndpoint(role);
        const r = await authFetch(ep, {
            method: "POST",
            body: JSON.stringify(data),
        });
        if (!r || !r.ok) throw new Error(`Erreur création: ${await r.text()}`);
        return r.json();
    },

    // Édite les informations d'un utilisateur existant
    async updateUser(id, data, role) {
        const ep = getEndpoint(role);
        const r = await authFetch(`${ep}/${id}`, {
            method: "PUT",
            body: JSON.stringify(data),
        });
        if (!r || !r.ok) throw new Error(`Erreur modification: ${await r.text()}`);
        return r.json();
    },

    // Détruit définitivement un utilisateur
    async deleteUser(id, role) {
        const ep = getEndpoint(role);
        const r = await authFetch(`${ep}/${id}`, { method: "DELETE" });
        if (!r || !r.ok) throw new Error("Erreur suppression");
        return true;
    },

    async blockUser(id, role) {
        return this.updateUser(id, { statut: "INACTIF" }, role);
    },

    async unblockUser(id, role) {
        return this.updateUser(id, { statut: "ACTIF" }, role);
    },

    // Récupère les rôles globaux et leurs permissions
    async getRoles() {
        const r = await authFetch('/api/roles');
        if (!r || !r.ok) throw new Error("Erreur récupération rôles");
        return r.json();
    },

    // Met à jour les permissions d'un rôle
    async updateRolePermissions(id, permissions) {
        const r = await authFetch(`/api/roles/${id}/permissions`, {
            method: "PUT",
            body: JSON.stringify({ permissions }),
        });
        if (!r || !r.ok) throw new Error("Erreur mise à jour permissions: " + await r.text());
        return r.json();
    },

    // Récupère toutes les permissions disponibles
    async getAllPermissions() {
        const r = await authFetch('/api/permissions');
        if (!r || !r.ok) throw new Error("Erreur récupération permissions");
        return r.json();
    },
};
