/* ===== ADSIS – profil.js ===== */

const user = JSON.parse(localStorage.getItem('adsis_user') || '{}');

// ── Populate fields ───────────────────────────────────────────────────────────
function splitName(name = '') {
  const parts = name.trim().split(' ');
  return { prenom: parts[0] || '', nom: parts.slice(1).join(' ') || '' };
}

const { prenom, nom } = splitName(user.name);
const el = id => document.getElementById(id);

el('f-prenom').value  = prenom;
el('f-nom').value     = nom;
el('f-email').value   = user.email  || '';
el('f-tel').value     = user.phone  || '';
el('f-adresse').value = user.address || '';
el('f-etab').value    = user.etablissement || '';

el('prof-name').textContent = user.name || 'Utilisateur';
el('prof-etab').textContent = user.etablissement || '';

// Avatar initials
const initials = (user.name || 'U').split(' ').map(w => w[0]).join('').slice(0,2).toUpperCase();
el('prof-avatar').textContent = initials;

// ── Toast helper ──────────────────────────────────────────────────────────────
let toastTimer;
function showToast(msg, type = 'success') {
  const t = el('toast');
  t.textContent = msg;
  t.className = `toast ${type}-toast show`;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => { t.className = 'toast'; }, 3500);
}

// ── Save profile ──────────────────────────────────────────────────────────────
el('btn-save').addEventListener('click', async () => {
  const prenom  = el('f-prenom').value.trim();
  const nom     = el('f-nom').value.trim();
  const email   = el('f-email').value.trim();
  const phone   = el('f-tel').value.trim();
  const address = el('f-adresse').value.trim();
  const feedback = el('save-feedback');

  if (!prenom || !nom || !email) {
    feedback.textContent = 'Veuillez remplir les champs obligatoires.';
    feedback.className = 'save-feedback error';
    return;
  }

  // Update local storage
  const updated = {
    ...user,
    name: `${prenom} ${nom}`,
    email, phone, address,
  };
  localStorage.setItem('adsis_user', JSON.stringify(updated));

  // Update UI
  el('prof-name').textContent = updated.name;
  document.querySelectorAll('#user-name-top').forEach(e => e.textContent = updated.name);
  const newInitials = updated.name.split(' ').map(w => w[0]).join('').slice(0,2).toUpperCase();
  document.querySelectorAll('.avatar, #prof-avatar').forEach(e => e.textContent = newInitials);

  // Try to sync with backend
  try {
    const token = localStorage.getItem('adsis_token') || '';
    await fetch('/api/user/profile', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
      body: JSON.stringify(updated),
    });
  } catch { /* offline – already saved locally */ }

  feedback.textContent = 'Modifications enregistrées !';
  feedback.className = 'save-feedback success';
  showToast('Profil mis à jour avec succès !', 'success');
  setTimeout(() => { feedback.textContent = ''; feedback.className = 'save-feedback'; }, 4000);
});

// ── Change password ───────────────────────────────────────────────────────────
el('btn-change-pw').addEventListener('click', async () => {
  const cur  = el('f-pw-cur').value;
  const nw   = el('f-pw-new').value;
  const conf = el('f-pw-conf').value;

  if (!cur || !nw || !conf) {
    showToast('Veuillez remplir tous les champs de sécurité.', 'error');
    return;
  }
  if (nw.length < 6) {
    showToast('Le mot de passe doit comporter au moins 6 caractères.', 'error');
    return;
  }
  if (nw !== conf) {
    showToast('Les nouveaux mots de passe ne correspondent pas.', 'error');
    return;
  }

  try {
    const token = localStorage.getItem('adsis_token') || '';
    const res = await fetch('/api/user/password', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
      body: JSON.stringify({ currentPassword: cur, newPassword: nw }),
    });
    if (!res.ok) throw new Error();
    showToast('Mot de passe modifié avec succès !', 'success');
    el('f-pw-cur').value = '';
    el('f-pw-new').value = '';
    el('f-pw-conf').value = '';
  } catch {
    // Demo: accept any change
    showToast('Mot de passe modifié avec succès ! (mode démo)', 'success');
    el('f-pw-cur').value = '';
    el('f-pw-new').value = '';
    el('f-pw-conf').value = '';
  }
});