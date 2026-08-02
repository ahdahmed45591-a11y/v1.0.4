/**
 * TEST COMPLET — Flux dépôt Wave + vérification solde dans l'admin
 * 
 * Vérifie :
 * 1. Connexion et récupération du solde initial
 * 2. Dépôt Wave de 50 000 FCFA
 * 3. Le solde backend est bien incrémenté (+50 000)
 * 4. La transaction apparaît dans la liste admin avec type=DEPOSIT
 * 5. Le statut est "validated" (= APPROVED dans l'admin)
 */

const BASE_URL = 'http://localhost:3001/api';

async function runTests() {
    let passed = 0;
    let failed = 0;

    const check = (label, condition, detail = '') => {
        if (condition) {
            console.log(`  ✅ [PASS] ${label}`);
            passed++;
        } else {
            console.log(`  ❌ [FAIL] ${label}${detail ? ' — ' + detail : ''}`);
            failed++;
        }
    };

    console.log('\n🧪 TEST DÉPÔT WAVE + CRÉDIT PORTEFEUILLE\n' + '═'.repeat(60));

    // ── Étape 0 : Inscription + Connexion ────────────────────────
    console.log('\n[ÉTAPE 0] Inscription + connexion...');
    const email = `wave_test_${Date.now()}@test.ci`;
    const password = 'test1234';

    const regRes = await fetch(`${BASE_URL}/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password, firstName: 'WaveTest', name: 'WaveTest' })
    });
    check('Inscription réussie', regRes.ok || regRes.status === 201, `status: ${regRes.status}`);

    const loginRes = await fetch(`${BASE_URL}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
    });
    check('Connexion réussie', loginRes.ok, `status: ${loginRes.status}`);
    const loginData = await loginRes.json();
    const token = loginData.token;
    const soldeInitial = loginData.user?.balance ?? 0;
    console.log(`  → Solde initial : ${soldeInitial} FCFA`);
    check('Token JWT reçu', !!token);

    // ── Étape 1 : Simulation clic "J'ai payé" (confirmWaveDeposit) ──
    console.log('\n[ÉTAPE 1] Dépôt Wave de 50 000 FCFA...');
    const montant = 50000;
    const depositRes = await fetch(`${BASE_URL}/transactions`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
            ticker: 'CASH',
            type: 'DEPOSIT',
            quantity: 1,
            price: montant,
            paymentRef: `WAVE-TEST-${Date.now()}`,
            paymentMethod: 'Wave CI'
        })
    });
    check(`Dépôt enregistré (status ${depositRes.status})`, depositRes.status === 201, `status: ${depositRes.status}`);
    const depositData = await depositRes.json();
    check('Transaction de type DEPOSIT créée', depositData.data?.type === 'DEPOSIT', `type: ${depositData.data?.type}`);
    check('Statut "validated"', depositData.data?.status === 'validated', `status: ${depositData.data?.status}`);
    check('Montant correct', depositData.data?.total === montant, `montant: ${depositData.data?.total}`);
    check('Méthode "Wave CI"', depositData.data?.paymentMethod === 'Wave CI', `méthode: ${depositData.data?.paymentMethod}`);

    // ── Étape 2 : Vérification que le solde a été mis à jour ─────
    console.log('\n[ÉTAPE 2] Vérification solde après dépôt...');
    const loginRes2 = await fetch(`${BASE_URL}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
    });
    const loginData2 = await loginRes2.json();
    const soldeApres = loginData2.user?.balance ?? 0;
    console.log(`  → Solde après : ${soldeApres} FCFA (initial: ${soldeInitial}, +${montant})`);
    check('Solde incrémenté de 50 000 FCFA', soldeApres === soldeInitial + montant, `attendu: ${soldeInitial + montant}, obtenu: ${soldeApres}`);

    // ── Étape 3 : Vérification dans la liste admin ───────────────
    console.log('\n[ÉTAPE 3] Vérification visibilité dans l\'admin...');

    // Se connecter en admin
    const adminLogin = await fetch(`${BASE_URL}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: 'admin@elephantbourse.ci', password: 'admin2024' })
    });
    const adminData = await adminLogin.json();
    const adminToken = adminData.token;
    check('Connexion admin réussie', !!adminToken);

    const allTxRes = await fetch(`${BASE_URL}/transactions/all`, {
        headers: { 'Authorization': `Bearer ${adminToken}` }
    });
    check('Liste transactions admin accessible', allTxRes.ok);
    const allTxData = await allTxRes.json();
    const waveDeposit = (allTxData.data || []).find(t =>
        t.paymentMethod === 'Wave CI' && t.type === 'DEPOSIT' && t.total === montant
    );
    check('Dépôt Wave visible dans l\'admin', !!waveDeposit, waveDeposit ? '' : 'introuvable');
    if (waveDeposit) {
        check('Badge type = DEPOSIT', waveDeposit.type === 'DEPOSIT');
        check('Badge méthode = Wave CI', waveDeposit.paymentMethod === 'Wave CI');
        check('Statut "validated" (APPROVED dans admin)', waveDeposit.status === 'validated');
    }

    // ── Résumé ─────────────────────────────────────────────────
    console.log('\n' + '═'.repeat(60));
    console.log(`📋 RÉSULTATS : ${passed} PASSÉS ✅  /  ${failed} ÉCHOUÉS ❌`);
    if (failed === 0) {
        console.log('🎉 TOUS LES TESTS PASSÉS — Dépôt Wave crédite bien LIQUIDITES CASH !');
    } else {
        console.log('⚠️  Des tests ont échoué — voir les détails ci-dessus.');
        process.exit(1);
    }
    console.log('═'.repeat(60) + '\n');
}

runTests().catch(e => {
    console.error('❌ Erreur test:', e.message);
    process.exit(1);
});
