// ============================================================
//  🐘 Éléphant Bourse — Backend API
//  Corrections : CORS restrictif, rate limiting, dotenv, logs nettoyés
// ============================================================

require('dotenv').config();
const express = require('express');
const cors = require('cors');
const rateLimit = require('express-rate-limit');

const app = express();
const PORT = process.env.PORT || 3001;

// ─── CORS — origines autorisées seulement ──────────────────
const allowedOrigins = (process.env.CORS_ORIGINS || 'http://localhost:3000,http://localhost:5173')
  .split(',')
  .map(o => o.trim());

app.use(cors({
  origin: function (origin, callback) {
    // Autoriser les requêtes sans origin (Postman, apps mobiles, etc.)
    if (!origin || allowedOrigins.includes(origin)) {
      callback(null, true);
    } else {
      callback(new Error(`CORS bloqué pour l'origine : ${origin}`));
    }
  },
  methods: ['GET', 'POST', 'PUT', 'PATCH', 'DELETE'],
  allowedHeaders: ['Content-Type', 'Authorization'],
}));

// ─── Rate Limiting sur les routes d'auth ──────────────────
const authLimiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 20, // max 20 tentatives par fenêtre par IP
  message: { error: 'Trop de tentatives. Réessayez dans 15 minutes.' },
  standardHeaders: true,
  legacyHeaders: false,
});

const globalLimiter = rateLimit({
  windowMs: 1 * 60 * 1000, // 1 minute
  max: 200, // 200 requêtes max par minute par IP
  message: { error: 'Trop de requêtes. Ralentissez.' },
});

app.use(globalLimiter);

// ─── Middleware ────────────────────────────────────────────
app.use(express.json({ limit: '15mb' }));
app.use(express.urlencoded({ limit: '15mb', extended: true }));
app.use('/uploads', express.static(require('path').join(__dirname, 'uploads')));

// ─── Logger simple ────────────────────────────────────────
app.use((req, res, next) => {
  const ts = new Date().toLocaleTimeString('fr-FR');
  console.log(`[${ts}] ${req.method} ${req.path}`);
  next();
});

// ─── Routes ───────────────────────────────────────────────
const { router: authRouter } = require('./routes/auth');
const stocksRouter        = require('./routes/stocks');
const transactionsRouter  = require('./routes/transactions');
const portfolioRouter     = require('./routes/portfolio');
const usersRouter         = require('./routes/users');

// Appliquer le rate limiter strict sur les routes d'auth
app.use('/api/auth', authLimiter, authRouter);
app.use('/api/stocks',        stocksRouter);
app.use('/api/transactions',  transactionsRouter);
app.use('/api/portfolio',     portfolioRouter);
app.use('/api/admin',         usersRouter);

// ─── Route racine ─────────────────────────────────────────
app.get('/', (req, res) => {
  res.json({
    name: '🐘 Éléphant Bourse API',
    version: '1.0.4',
    status: 'running',
    market: 'BRVM — Côte d\'Ivoire',
    endpoints: {
      auth:         '/api/auth/login',
      stocks:       '/api/stocks',
      brvm_indices: '/api/stocks/brvm/indices',
      transactions: '/api/transactions',
      portfolio:    '/api/portfolio',
      admin_users:  '/api/admin/users',
      admin_stats:  '/api/admin/stats',
    },
  });
});

// ─── Route 404 ────────────────────────────────────────────
app.use((req, res) => {
  res.status(404).json({ error: `Route "${req.method} ${req.path}" introuvable.` });
});

// ─── Gestionnaire d'erreurs global ───────────────────────
app.use((err, req, res, next) => {
  if (err.message && err.message.startsWith('CORS bloqué')) {
    return res.status(403).json({ error: err.message });
  }
  console.error('[Erreur serveur]', err);
  res.status(500).json({ error: 'Erreur interne du serveur.' });
});

// ─── Démarrage ────────────────────────────────────────────
app.listen(PORT, () => {
  console.log('');
  console.log('  🐘 ═══════════════════════════════════════════');
  console.log('       Éléphant Bourse API — v1.0.4');
  console.log('  ═══════════════════════════════════════════════');
  console.log(`  ✅  Backend:    http://localhost:${PORT}`);
  console.log(`  🖥️   Admin:      http://localhost:3000`);
  console.log('  ───────────────────────────────────────────────');
  console.log('  🔒  Auth:       JWT sécurisé (bcrypt)');
  console.log('  📦  DB:         SQLite locale + miroir Supabase');
  console.log('  ═══════════════════════════════════════════════');
  console.log('');
});

module.exports = app;
