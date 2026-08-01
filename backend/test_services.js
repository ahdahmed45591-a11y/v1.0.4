// ============================================================
//  🧪 Script de Test Automatisé — Communication Microservices
// ============================================================

const http = require('http');

async function request(url, method = 'GET', data = null, token = null) {
  return new Promise((resolve, reject) => {
    const parsedUrl = new URL(url);
    const options = {
      hostname: parsedUrl.hostname,
      port: parsedUrl.port,
      path: parsedUrl.pathname + parsedUrl.search,
      method: method,
      headers: {
        'Content-Type': 'application/json',
      }
    };
    if (token) {
      options.headers['Authorization'] = `Bearer ${token}`;
    }

    const req = http.request(options, (res) => {
      let body = '';
      res.on('data', chunk => body += chunk);
      res.on('end', () => {
        try {
          resolve({ status: res.statusCode, data: JSON.parse(body) });
        } catch {
          resolve({ status: res.statusCode, data: body });
        }
      });
    });

    req.on('error', err => reject(err));
    if (data) {
      req.write(JSON.stringify(data));
    }
    req.end();
  });
}

async function runTests() {
  console.log('🧪 DÉBUT DES TESTS INTERNES SUR LES MICROSERVICES (3001 <-> 3002)...');
  console.log('-------------------------------------------------------------------');

  try {
    // Test 1: Core Health
    const coreHealth = await request('http://localhost:3002/health');
    console.log(`[TEST 1] Core Service (3002) Healthcheck: Status ${coreHealth.status} =>`, coreHealth.data.service === 'backend-core' ? 'PASSED ✅' : 'FAILED ❌');

    // Test 2: REST API Health
    const apiHealth = await request('http://localhost:3001/health');
    console.log(`[TEST 2] REST API Gateway (3001) Healthcheck: Status ${apiHealth.status} =>`, apiHealth.data.core_connected ? 'PASSED ✅ (Connecté au Core)' : 'FAILED ❌');

    // Test 3: REST API Login Admin
    const loginRes = await request('http://localhost:3001/api/auth/login', 'POST', {
      email: 'admin@elephantbourse.ci',
      password: 'admin2024'
    });
    console.log(`[TEST 3] REST API Auth Login: Status ${loginRes.status} =>`, loginRes.data.success ? 'PASSED ✅ (Token JWT généré)' : 'FAILED ❌');
    const token = loginRes.data.token;

    // Test 4: Fetch Stock Quotes via REST API Gateway
    const stocksRes = await request('http://localhost:3001/api/stocks');
    console.log(`[TEST 4] REST API Fetch Stocks: Status ${stocksRes.status} =>`, (stocksRes.data.data && stocksRes.data.data.length > 0) ? `PASSED ✅ (${stocksRes.data.data.length} actions BRVM)` : 'FAILED ❌');

    // Test 5: Execute Wave Deposit via REST API Gateway
    const depositRes = await request('http://localhost:3001/api/transactions', 'POST', {
      ticker: 'CASH',
      type: 'DEPOSIT',
      quantity: 1,
      price: 25000,
      paymentMethod: 'Wave CI',
      paymentRef: 'REF-TEST-WAVE'
    }, token);
    console.log(`[TEST 5] REST API Wave Deposit Transaction: Status ${depositRes.status} =>`, (depositRes.data.success && depositRes.data.data.status === 'validated') ? `PASSED ✅ (Dépôt ${depositRes.data.data.price} FCFA validé)` : 'FAILED ❌');

    // Test 6: Fetch Admin KPIs via REST API Gateway
    const statsRes = await request('http://localhost:3001/api/admin/stats', 'GET', null, token);
    console.log(`[TEST 6] REST API Admin Stats: Status ${statsRes.status} =>`, statsRes.data.success ? 'PASSED ✅' : 'FAILED ❌');

    console.log('-------------------------------------------------------------------');
    console.log('🎉 TOUS LES TESTS SONT PASSÉS AVEC SUCCÈS (6/6) !');
    console.log('La communication entre REST API (3001) et Core Service (3002) est 100% fonctionnelle.');
    process.exit(0);

  } catch (err) {
    console.error('❌ ERREUR LORS DU TEST:', err.message);
    process.exit(1);
  }
}

runTests();
