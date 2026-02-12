// Exemple de données
const transactions = [
  { date: '2026-01-12', desc: 'Achat transport', type: 'expense', montant: -19 },
  { date: '2026-01-12', desc: 'Paiement salaire', type: 'income', montant: 1718 },
  { date: '2026-01-11', desc: 'Achat logement', type: 'expense', montant: -56 },
  { date: '2026-01-04', desc: 'Achat logement', type: 'expense', montant: -141 },
  { date: '2025-12-23', desc: 'Payement salaire', type: 'income', montant: 14100 }
];

// Affichage transactions
function afficherTransactions(transactionsList) {
    const container = document.querySelector('.transaction-container');
    container.innerHTML = '';

    let currentDate = '';
    transactionsList.forEach(t => {
        if(t.date !== currentDate){
            currentDate = t.date;
            const dateHeader = document.createElement('div');
            dateHeader.className = 'date-header d-flex justify-content-between';
            dateHeader.innerHTML = `<span>● ${currentDate}</span><span>1 transaction</span>`;
            container.appendChild(dateHeader);
        }

        const card = document.createElement('div');
        card.className = 'transaction-card';
        card.innerHTML = `
            <div class="icon ${t.type === 'income' ? 'income' : 'expense'}">
              <i class="bi ${t.type === 'income' ? 'bi-arrow-up' : 'bi-arrow-down'}"></i>
            </div>
            <div class="details">
              <h6>${t.desc}</h6>
              <small>${t.type}</small>
            </div>
            <div class="amount ${t.montant < 0 ? 'negative' : 'positive'}">
              ${t.montant} <span>FCFA</span>
            </div>
        `;
        container.appendChild(card);
    });
}

// Filtre recherche
document.getElementById("filtre").addEventListener("input", function(){
    const value = this.value.toLowerCase();
    const filtered = transactions.filter(t =>
        t.desc.toLowerCase().includes(value)
    );
    afficherTransactions(filtered);
});

// Export CSV
document.getElementById("export-btn").addEventListener('click', function(){
    let csv = "Date,Description,Type,Montant\n";
    transactions.forEach(t=>{
        csv += `${t.date},${t.desc},${t.type},${t.montant}\n`;
    });

    const blob = new Blob([csv], {type:"text/csv"});
    const link = document.createElement("a");
    link.href = URL.createObjectURL(blob);
    link.download = "historique_transactions.csv";
    link.click();
});

// Boutons de filtre
const filterButtons = document.querySelectorAll('.filter-btn');
filterButtons.forEach(btn => {
  btn.addEventListener('click', () => {
    filterButtons.forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
  });
});

afficherTransactions(transactions);
console.log("Dashboard chargé avec succès");