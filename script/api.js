const API_BASE_URL = "http://localhost:8081/api";

const ROLE_ENDPOINTS = {
    "ETUDIANT": "/etudiants",
    "ENSEIGNANT": "/enseignants",
    "SURVEILLANT": "/surveillants",
    "ASSISTANT_PEDAGOGIQUE": "/assistants-pedagogiques",
    "Étudiant": "/etudiants",
    "Enseignant": "/enseignants",
    "Surveillant": "/surveillants",
    "Assistant pédagogique": "/assistants-pedagogiques"
};

const ROLE_MAPPING = {
    "Étudiant": "ETUDIANT",
    "Enseignant": "ENSEIGNANT",
    "Surveillant": "SURVEILLANT",
    "Assistant pédagogique": "ASSISTANT_PEDAGOGIQUE"
};

// Renvoie l'URL de l'API correspondant à un rôle
function getEndpoint(role) {
    if (Array.isArray(role)) role = role[0];
    return ROLE_ENDPOINTS[role] || "/etudiants";
}

export const Api = {
    // Récupère la liste globale de tous les utilisateurs
    async getUsers() {
        try {
            const endpoints = Object.values(ROLE_ENDPOINTS).filter((v, i, a) => a.indexOf(v) === i);
            const promises = endpoints.map(endpoint =>
                fetch(`${API_BASE_URL}${endpoint}`)
                    .then(res => res.ok ? res.json() : [])
                    .catch(e => {
                        console.error(`Erreur fetch ${endpoint}`, e);
                        return [];
                    })
            );
            const results = await Promise.all(promises);
            return results.flat();
        } catch (error) {
            console.error("Erreur globale getUsers", error);
            throw error;
        }
    },

    // Récupère uniquement les surveillants et assistants
    async getStaff() {
        const staffEndpoints = ["/enseignants", "/surveillants", "/assistants-pedagogiques"];
        try {
            const promises = staffEndpoints.map(endpoint =>
                fetch(`${API_BASE_URL}${endpoint}`)
                    .then(res => res.ok ? res.json() : [])
                    .catch(e => {
                        console.error(`Erreur fetch ${endpoint}`, e);
                        return [];
                    })
            );
            const results = await Promise.all(promises);
            return results.flat();
        } catch (error) {
            console.error("Erreur globale getStaff", error);
            throw error;
        }
    },

    // Récupère un utilisateur selon son ID et son rôle
    async getUserById(id, role) {
        const endpoint = getEndpoint(role);
        const response = await fetch(`${API_BASE_URL}${endpoint}/${id}`);
        if (!response.ok) throw new Error(`Erreur récupération utilisateur ${id}`);
        return await response.json();
    },

    // Crée un nouvel utilisateur
    async createUser(data) {
        const role = data.roleNames ? data.roleNames[0] : (ROLE_MAPPING[data.role] || "ETUDIANT");
        if (!data.roleNames) data.roleNames = [ROLE_MAPPING[data.role]];
        const endpoint = getEndpoint(role);
        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(data)
        });
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Erreur création: ${errorText}`);
        }
        return await response.json();
    },

    // Édite les informations d'un utilisateur existant
    async updateUser(id, data, role) {
        const endpoint = getEndpoint(role);
        const response = await fetch(`${API_BASE_URL}${endpoint}/${id}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(data)
        });
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Erreur modification: ${errorText}`);
        }
        return await response.json();
    },

    // Détruit définitivement un utilisateur en base de données
    async deleteUser(id, role) {
        const endpoint = getEndpoint(role);
        const response = await fetch(`${API_BASE_URL}${endpoint}/${id}`, {
            method: "DELETE"
        });
        if (!response.ok) throw new Error("Erreur suppression");
        return true;
    },

    // Désactive temporairement le compte d'un utilisateur
    async blockUser(id, role) {
        return this.updateUser(id, { statut: "INACTIF" }, role);
    },

    // Réactive le compte d'un utilisateur bloqué
    async unblockUser(id, role) {
        return this.updateUser(id, { statut: "ACTIF" }, role);
    },

    // Récupère les rôles globaux et leurs permissions
    async getRoles() {
        const response = await fetch(`${API_BASE_URL}/roles`);
        if (!response.ok) throw new Error("Erreur récupération rôles");
        return await response.json();
    },

    // Met à jour les permissions affectées à un rôle donné
    async updateRolePermissions(id, permissions) {
        const payload = { permissions: permissions };
        const response = await fetch(`${API_BASE_URL}/roles/${id}/permissions`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error("Erreur mise à jour permissions du rôle: " + errorText);
        }
        return await response.json();
    },

    // Récupère toutes les permissions disponibles dans le système
    async getAllPermissions() {
        const response = await fetch(`${API_BASE_URL}/permissions`);
        if (!response.ok) throw new Error("Erreur récupération permissions");
        return await response.json();
    }
};
