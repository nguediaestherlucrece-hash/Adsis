document.addEventListener('DOMContentLoaded', () => {
    // Affiche l'image choisie par l'utilisateur
    window.previewImage = function (input) {
        const display = document.getElementById('profileDisplay');
        if (input.files && input.files[0]) {
            const reader = new FileReader();
            reader.onload = function (e) {
                display.innerHTML = `<img src="${e.target.result}" style="width: 100%; height: 100%; object-fit: cover;">`;
            };
            reader.readAsDataURL(input.files[0]);
        }
    };

    // Retire la photo et remet l'initiale
    window.removePhoto = function () {
        const display = document.getElementById('profileDisplay');
        const input = document.getElementById('photoInput');
        display.innerHTML = "A";
        input.value = "";
    };

    // Gère la sauvegarde du profil
    const profileForm = document.querySelector('form');
    if (profileForm) {
        profileForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const btn = profileForm.querySelector('button[type="submit"]');
            const originalText = btn.innerText;
            btn.innerText = "Enregistrement...";
            btn.disabled = true;
            try {
                await new Promise(r => setTimeout(r, 1000));
                alert("Profil mis à jour avec succès !");
            } catch (error) {
                console.error(error);
                alert("Erreur lors de la mise à jour");
            } finally {
                btn.innerText = originalText;
                btn.disabled = false;
            }
        });
    }
});
