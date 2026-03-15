async function test() {
    try {
        console.log("Checking Niveaux...");
        const niveauxRes = await fetch("http://localhost:8081/api/niveaux");
        const niveaux = await niveauxRes.json();
        console.log("Found Niveaux:", niveaux.map(n => n.id));

        if (niveaux.length === 0) {
            console.log("No niveaux found to link to.");
            return;
        }

        const idsToAssign = niveaux.map(n => n.id).slice(0, 2); // Take up to 2
        console.log("Will attempt to create Filiere with niveauIds:", idsToAssign);

        const payload = {
            idFiliere: "TEST-FIL",
            nomFiliere: "Test Filiere Multiple",
            description: "Testing array attach",
            niveauIds: idsToAssign
        };

        const createRes = await fetch("http://localhost:8081/api/filieres", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });

        const created = await createRes.json();
        console.log("Created Filiere response:", created);

        console.log("Fetching all Filieres back...");
        const fetchRes = await fetch("http://localhost:8081/api/filieres");
        const allFilieres = await fetchRes.json();
        const found = allFilieres.find(f => f.id === created.id);
        
        console.log("Verified in DB:", found);

    } catch(e) {
        console.error("Error:", e);
    }
}
test();
