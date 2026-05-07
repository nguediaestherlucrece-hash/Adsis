/* ===== ADSIS – login.js ===== */

const emailInput   = document.getElementById('email');
const passInput    = document.getElementById('password');
const btnLogin     = document.getElementById('btn-login');
const btnText      = document.getElementById('btn-text');
const btnSpinner   = document.getElementById('btn-spinner');
const alertBox     = document.getElementById('alert-box');
const togglePw     = document.getElementById('toggle-pw');

// ── Toggle password visibility ──────────────────────────────────────────────
togglePw.addEventListener('click', () => {
  const isHidden = passInput.type === 'password';
  passInput.type = isHidden ? 'text' : 'password';
  togglePw.querySelector('svg').innerHTML = isHidden
    ? '<path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/>'
    : '<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>';
});

// ── Show / clear alert ───────────────────────────────────────────────────────
function showAlert(msg, type = 'error') {
  alertBox.textContent = msg;
  alertBox.className = 'alert-box ' + (type === 'error' ? 'show-error' : 'show-success');
}
function clearAlert() {
  alertBox.textContent = '';
  alertBox.className = 'alert-box';
}

// ── Validation des champs ──────────────────────────────────────────────────────
function validate() {
  const email = emailInput.value.trim();
  const pass  = passInput.value;
  let ok = true;
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    emailInput.classList.add('error-field'); ok = false;
  } else emailInput.classList.remove('error-field');
  if (pass.length < 4) {
    passInput.classList.add('error-field'); ok = false;
  } else passInput.classList.remove('error-field');
  return ok;
}

// ── État du bouton ─────────────────────────────────────────────────────────────
function setLoading(state) {
  btnLogin.disabled = state;
  btnText.style.display    = state ? 'none' : 'inline';
  btnSpinner.style.display = state ? 'inline-block' : 'none';
}

// ── Routing par rôle ─────────────────────────────────────────────────────────
function routeByRole(roles = []) {
  if (roles.includes('ADMIN'))
    window.location.href = '/admin/tableaudebord';
  else if (roles.includes('ENSEIGNANT'))
    window.location.href = '/enseignant/dashboard';
  else if (roles.includes('ASSISTANT_PEDAGOGIQUE') || roles.includes('SURVEILLANT'))
    window.location.href = '/assistant/tableaudebord';
  else if (roles.includes('ETUDIANT'))
    window.location.href = '/etudiant/dashboard';
  else
    window.location.href = '/enseignant/dashboard';
}

// ── Connexion réelle ──────────────────────────────────────────────────────────
async function doLogin() {
  clearAlert();
  if (!validate()) { showAlert('Veuillez remplir correctement tous les champs.'); return; }
  setLoading(true);
  try {
    const res = await fetch('http://localhost:8081/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        email:    emailInput.value.trim(),
        password: passInput.value,
      }),
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || 'Email ou mot de passe incorrect.');
    }
    const data = await res.json();
    // Stocker token + rôles + permissions dans localStorage
    localStorage.setItem('adsis_token',         data.accessToken);
    localStorage.setItem('adsis_refresh_token', data.refreshToken);
    localStorage.setItem('adsis_user', JSON.stringify({
      id:          data.id,
      nom:         data.nom,
      prenom:      data.prenom,
      email:       data.email,
      roles:       data.roles       || [],
      permissions: data.permissions || [],
    }));
    showAlert('Connexion réussie !', 'success');
    setTimeout(() => routeByRole(data.roles || []), 600);
  } catch (err) {
    showAlert(err.message || 'Erreur de connexion. Réessayez.');
  } finally {
    setLoading(false);
  }
}

// ── Event listeners ───────────────────────────────────────────────────────────
btnLogin.addEventListener('click', doLogin);
[emailInput, passInput].forEach(el => {
  el.addEventListener('keydown', e => { if (e.key === 'Enter') doLogin(); });
  el.addEventListener('input', clearAlert);
});

// ── Auto-fill if already logged in ───────────────────────────────────────────
(function checkSession() {
  try {
    const user = JSON.parse(localStorage.getItem('adsis_user') || 'null');
    if (user && user.token) routeByRole(user.role);
  } catch { /* ignore */ }
})();