// ============================================================
//  🐘 Éléphant Bourse — REST API GATEWAY (Public Microservice)
//  Port: 3001 (Communique avec Core Service sur Port 3002)
// ============================================================

require('dotenv').config();
const express = require('express');
const cors = require('cors');
const rateLimit = require('express-rate-limit');
const jwt = require('jsonwebtoken');
const http = require('http');
const path = require('path');

const app = express();
const PORT = process.env.PORT || 3001;
const CORE_URL = process.env.CORE_SERVICE_URL || 'http://localhost:3002';
const JWT_SECRET = process.env.JWT_SECRET || 'elephant_secret_key_2024_brvm';

// ── CORS ──────────────────────────────────────────────────
const allowedOrigins = (process.env.CORS_ORIGINS || 'http://localhost:3000,http://localhost:5173')
  .split(',')
  .map(o => o.trim());

app.use(cors({
  origin: function (origin, callback) {
    if (!origin || allowedOrigins.includes(origin)) {
      callback(null, true);
    } else {
      callback(null, true); // Autoriser tout en dev local
    }
  },
  methods: ['GET', 'POST', 'PUT', 'PATCH', 'DELETE'],
  allowedHeaders: ['Content-Type', 'Authorization'],
}));

// ── Rate Limiting ──────────────────────────────────────────
const globalLimiter = rateLimit({
  windowMs: 1 * 60 * 1000,
  max: 300,
  message: { error: 'Trop de requêtes. Ralentissez.' },
});
app.use(globalLimiter);

app.use(express.json({ limit: '15mb' }));
app.use(express.urlencoded({ limit: '15mb', extended: true }));
app.use('/uploads', express.static(path.join(__dirname, '../uploads')));

// ── Helper HTTP pour appeler backend-core ────────────────────
async function callCore(method, endpoint, body = null, headers = {}) {
  const url = `${CORE_URL}${endpoint}`;
  try {
    const response = await fetch(url, {
      method,
      headers: {
        'Content-Type': 'application/json',
        ...headers
      },
      body: body ? JSON.stringify(body) : null
    });
    const data = await response.json();
    return { status: response.status, data };
  } catch (err) {
    console.error(`[API GATEWAY -> CORE ERROR] ${method} ${endpoint}:`, err.message);
    return { status: 503, data: { error: 'Service Core temporairement indisponible.' } };
  }
}

// ── Middleware Auth JWT ──────────────────────────────────────
function requireAuth(req, res, next) {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ error: 'Accès refusé. Jeton d\'authentification manquant.' });
  }

  const token = authHeader.split(' ')[1];
  try {
    const decoded = jwt.verify(token, JWT_SECRET);
    req.session = decoded;
    next();
  } catch (err) {
    return res.status(401).json({ error: 'Jeton invalide ou expiré.' });
  }
}

function requireAdmin(req, res, next) {
  if (req.session?.role !== 'admin') {
    return res.status(403).json({ error: 'Accès réservé aux administrateurs.' });
  }
  next();
}

// ── Logger ────────────────────────────────────────────────
app.use((req, res, next) => {
  const ts = new Date().toLocaleTimeString('fr-FR');
  console.log(`[REST API:3001 - ${ts}] ${req.method} ${req.path}`);
  next();
});

// ── Healthcheck ───────────────────────────────────────────
app.get('/health', async (req, res) => {
  const coreHealth = await callCore('GET', '/health');
  res.json({
    status: 'ok',
    service: 'backend-api-gateway',
    port: PORT,
    core_connected: coreHealth.status === 200,
    core_response: coreHealth.data,
  });
});

// ── 1. ROUTE ROOT ─────────────────────────────────────────
app.get('/', (req, res) => {
  res.json({
    name: '🐘 Éléphant Bourse REST API Gateway',
    architecture: 'Microservices découplés (REST API Gateway : 3001 <-> Core Service : 3002)',
    version: '1.0.4',
    status: 'running',
    market: 'BRVM — Côte d\'Ivoire',
  });
});

// ── 2. AUTH ROUTES ────────────────────────────────────────
app.post('/api/auth/login', async (req, res) => {
  const coreRes = await callCore('POST', '/internal/auth/login', req.body);
  if (coreRes.status !== 200) {
    return res.status(coreRes.status).json(coreRes.data);
  }

  const user = coreRes.data.user;
  const token = jwt.sign(
    { userId: user.id, email: user.email, role: user.role, name: user.name },
    JWT_SECRET,
    { expiresIn: '24h' }
  );

  res.json({ success: true, token, user });
});

app.post('/api/auth/register', async (req, res) => {
  const coreRes = await callCore('POST', '/internal/auth/register', req.body);
  res.status(coreRes.status).json(coreRes.data);
});

app.post('/api/auth/logout', requireAuth, (req, res) => {
  res.json({ success: true, message: 'Déconnexion réussie.' });
});

app.patch('/api/auth/profile', requireAuth, async (req, res) => {
  const coreRes = await callCore('PATCH', '/internal/auth/profile', {
    userId: req.session.userId,
    ...req.body
  });
  res.status(coreRes.status).json(coreRes.data);
});

// ── 3. STOCKS ROUTES ──────────────────────────────────────
app.get('/api/stocks', async (req, res) => {
  const coreRes = await callCore('GET', '/internal/stocks');
  res.status(coreRes.status).json(coreRes.data);
});

app.get('/api/stocks/:ticker', async (req, res) => {
  const coreRes = await callCore('GET', `/internal/stocks/${req.params.ticker}`);
  res.status(coreRes.status).json(coreRes.data);
});

// ── 4. TRANSACTIONS ROUTES ────────────────────────────────
app.get('/api/transactions', requireAuth, async (req, res) => {
  const coreRes = await callCore('GET', `/internal/transactions?userId=${req.session.userId}&role=${req.session.role}`);
  res.status(coreRes.status).json(coreRes.data);
});

app.get('/api/transactions/all', requireAuth, requireAdmin, async (req, res) => {
  const coreRes = await callCore('GET', `/internal/transactions?role=admin`);
  res.status(coreRes.status).json(coreRes.data);
});

app.post('/api/transactions', requireAuth, async (req, res) => {
  const payload = {
    userId: req.session.userId,
    userEmail: req.session.email,
    userName: req.session.name,
    ...req.body
  };
  const coreRes = await callCore('POST', '/internal/transactions', payload);
  res.status(coreRes.status).json(coreRes.data);
});

app.patch('/api/transactions/:id/validate', requireAuth, requireAdmin, async (req, res) => {
  const coreRes = await callCore('PATCH', `/internal/transactions/${req.params.id}/validate`, {
    adminId: req.session.userId
  });
  res.status(coreRes.status).json(coreRes.data);
});

app.patch('/api/transactions/:id/reject', requireAuth, requireAdmin, async (req, res) => {
  const coreRes = await callCore('PATCH', `/internal/transactions/${req.params.id}/reject`, req.body);
  res.status(coreRes.status).json(coreRes.data);
});

// ── 5. ADMIN ROUTES ───────────────────────────────────────
app.get('/api/admin/stats', requireAuth, requireAdmin, async (req, res) => {
  const coreRes = await callCore('GET', '/internal/admin/stats');
  res.status(coreRes.status).json(coreRes.data);
});

app.get('/api/admin/users', requireAuth, requireAdmin, async (req, res) => {
  const coreRes = await callCore('GET', '/internal/admin/users');
  res.status(coreRes.status).json(coreRes.data);
});

app.patch('/api/admin/users/:id/kyc', requireAuth, requireAdmin, async (req, res) => {
  const coreRes = await callCore('PATCH', `/internal/admin/users/${req.params.id}/kyc`, req.body);
  res.status(coreRes.status).json(coreRes.data);
});

// ── 6. SUPPORT & CHAT ROUTES ──────────────────────────────
app.get('/api/admin/support', requireAuth, requireAdmin, async (req, res) => {
  const coreRes = await callCore('GET', '/internal/support');
  res.status(coreRes.status).json(coreRes.data);
});

app.get('/api/admin/chat/:userId', requireAuth, requireAdmin, async (req, res) => {
  const coreRes = await callCore('GET', `/internal/chat/${req.params.userId}`);
  res.status(coreRes.status).json(coreRes.data);
});

app.post('/api/admin/chat/:userId', requireAuth, requireAdmin, async (req, res) => {
  const coreRes = await callCore('POST', `/internal/chat/${req.params.userId}`, req.body);
  res.status(coreRes.status).json(coreRes.data);
});

// ── Démarrage Serveur ─────────────────────────────────────
const server = app.listen(PORT, () => {
  console.log('');
  console.log('  🐘 ═══════════════════════════════════════════');
  console.log('       REST API GATEWAY — Port 3001');
  console.log(`       Connecté à Core Service: ${CORE_URL}`);
  console.log('  ═══════════════════════════════════════════════');
  console.log('');
});

module.exports = { app, server };
