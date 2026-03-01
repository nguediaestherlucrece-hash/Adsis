const API_BASE_URL = "http://localhost:8081/api";

/**
 * Mappe les rôles fr aux endpoints API
 */
const ROLE_ENDPOINTS = {
    "ETUDIANT": "/etudiants",
    "ENSEIGNANT": "/enseignants",
    "SURVEILLANT": "/surveillants",
    "ASSISTANT_PEDAGOGIQUE": "/assistants-pedagogiques",
    // Mapping depuis l'interface française vers les rôles API
    "Étudiant": "/etudiants",
    "Enseignant": "/enseignants",
    "Surveillant": "/surveillants",
    "Assistant pédagogique": "/assistants-pedagogiques"
};

/**
 * Mappe les rôles fr aux Rôles API (pour l'envoi de données)
 */
const ROLE_MAPPING = {
    "Étudiant": "ETUDIANT",
    "Enseignant": "ENSEIGNANT",
    "Surveillant": "SURVEILLANT",
    "Assistant pédagogique": "ASSISTANT_PEDAGOGIQUE"
};

/**
 * Helper pour déterminer le endpoint en fonction du rôle
 * @param {string} role - Le rôle de l'utilisateur (fr ou API)
 * @returns {string} - L'endpoint correspondant
 */
function getEndpoint(role) {
    // Si le rôle est un tableau (cas API), on prend le premier
    if (Array.isArray(role)) role = role[0];
    return ROLE_ENDPOINTS[role] || "/etudiants"; // Fallback
}

export const Api = {
    /**
     * Récupère tous les utilisateurs de tous les types
     * @returns {Promise<Array>} - Liste combinée de tous les utilisateurs
     */
    async getUsers() {
        try {
            const endpoints = Object.values(ROLE_ENDPOINTS).filter((v, i, a) => a.indexOf(v) === i); // Uniques
            const promises = endpoints.map(endpoint =>
                fetch(`${API_BASE_URL}${endpoint}`)
                    .then(res => res.ok ? res.json() : [])
                    .catch(e => {
                        console.error(`Erreur fetch ${endpoint}`, e);
                        return [];
                    })
            );

            const results = await Promise.all(promises);
            // Aplatir les résultats
            return results.flat();
        } catch (error) {
            console.error("Erreur globale getUsers", error);
            throw error;
        }
    },

    /**
     * Récupère UNIQUEMENT les surveillants et assistants pédagogiques.
     * Utiliser cette méthode sur la page de gestion du personnel (gestionutilisateur.html)
     * pour garantir que les étudiants et enseignants ne s'affichent jamais ici,
     * même s'ils existent en base de données.
     * @returns {Promise<Array>}
     */
    async getStaff() {
        const staffEndpoints = ["/surveillants", "/assistants-pedagogiques"];
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

    /**
     * Récupère un utilisateur par ID et Rôle
     * Note: Comme l'ID n'est pas unique entre les tables, le rôle est nécessaire
     * @param {number} id 
     * @param {string} role 
     */
    async getUserById(id, role) {
        const endpoint = getEndpoint(role);
        const response = await fetch(`${API_BASE_URL}${endpoint}/${id}`);
        if (!response.ok) throw new Error(`Erreur récupération utilisateur ${id}`);
        return await response.json();
    },

    /**
     * Crée un nouvel utilisateur
     * @param {Object} data - Données de l'utilisateur
     */
    async createUser(data) {
        // Déterminer le endpoint basé sur le rôle sélectionné
        // Le rôle dans le formulaire est en français, on le mappe
        const role = data.roleNames ? data.roleNames[0] : (ROLE_MAPPING[data.role] || "ETUDIANT");

        // Assurer que roleNames est présent pour l'API
        if (!data.roleNames) {
            data.roleNames = [ROLE_MAPPING[data.role]];
        }

        const endpoint = getEndpoint(role);

        console.log("Creating user at", `${API_BASE_URL}${endpoint}`, data);

        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(data)
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Erreur création: ${errorText}`);
        }
        return await response.json();
    },

    /**
     * Met à jour un utilisateur existant
     * @param {number} id 
     * @param {Object} data - Données à mettre à jour
     * @param {string} role - Rôle actuel pour déterminer le endpoint
     */
    async updateUser(id, data, role) {
        const endpoint = getEndpoint(role);

        console.log("Updating user at", `${API_BASE_URL}${endpoint}/${id}`, data);

        const response = await fetch(`${API_BASE_URL}${endpoint}/${id}`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(data)
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Erreur modification: ${errorText}`);
        }
        return await response.json();
    },

    /**
     * Supprime un utilisateur
     * @param {number} id 
     * @param {string} role 
     */
    async deleteUser(id, role) {
        const endpoint = getEndpoint(role);
        const response = await fetch(`${API_BASE_URL}${endpoint}/${id}`, {
            method: "DELETE"
        });

        if (!response.ok) throw new Error("Erreur suppression");
        return true;
    },

    /**
     * Bloque un utilisateur (passe le statut à INACTIF)
     * @param {number} id 
     * @param {string} role 
     */
    async blockUser(id, role) {
        // On réutilise updateUser avec juste le statut
        return this.updateUser(id, { statut: "INACTIF" }, role);
    },

    /**
     * Débloque un utilisateur (passe le statut à ACTIF)
     * @param {number} id 
     * @param {string} role 
     */
    async unblockUser(id, role) {
        return this.updateUser(id, { statut: "ACTIF" }, role);
    },

    /**
     * Récupère tous les rôles avec leurs permissions
     */
    async getRoles() {
        const response = await fetch(`${API_BASE_URL}/roles`);
        if (!response.ok) throw new Error("Erreur récupération rôles");
        return await response.json();
    },

    /**
     * Met à jour un rôle (notamment ses permissions)
     * @param {string} name 
     * @param {Object} data 
     */
    async updateRolePermissions(id, permissions) {
        const payload = { permissions: permissions };
        console.log('Updating role permissions:', { id, payload });

        const response = await fetch(`${API_BASE_URL}/roles/${id}/permissions`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const errorText = await response.text();
            console.error('Update role permissions failed:', errorText);
            throw new Error("Erreur mise à jour permissions du rôle: " + errorText);
        }
        return await response.json();
    }
};
