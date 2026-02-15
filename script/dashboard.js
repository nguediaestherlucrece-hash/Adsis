import { Api } from './api.js';

document.addEventListener('DOMContentLoaded', () => {
    loadDashboardStats();
});

// Charge les statistiques du tableau de bord
async function loadDashboardStats() {
    try {
        const users = await Api.getUsers();

        // Répartition par rôle
        const students = users.filter(u => u.roleNames?.includes("ETUDIANT"));
        const teachers = users.filter(u => u.roleNames?.includes("ENSEIGNANT"));
        const surveillants = users.filter(u => u.roleNames?.includes("SURVEILLANT"));
        const assistants = users.filter(u => u.roleNames?.includes("ASSISTANT_PEDAGOGIQUE"));

        // Statut des utilisateurs
        const activeUsers = users.filter(u => u.statut === "ACTIF");
        const blockedUsers = users.filter(u => u.statut === "INACTIF");

        // Données spécifiques
        const studentsWithClass = students.filter(u => u.classeId);
        const teachersWithCourse = teachers.filter(u => u.coursIds && u.coursIds.length > 0);

        // Mise à jour de l'interface
        document.getElementById("stat-total").innerText = users.length;
        document.getElementById("stat-etudiants").innerText = students.length;
        document.getElementById("stat-enseignants").innerText = teachers.length;
        document.getElementById("stat-surveillants").innerText = surveillants.length;
        document.getElementById("stat-assistants").innerText = assistants.length;

        document.getElementById("stat-active").innerText = activeUsers.length;
        document.getElementById("stat-blocked").innerText = blockedUsers.length;
        document.getElementById("stat-students-class").innerText = studentsWithClass.length;
        document.getElementById("stat-teachers-course").innerText = teachersWithCourse.length;

    } catch (e) {
        console.error("Erreur chargement dashboard", e);
    }
}
