// ============================================================
//  BAOU Finance — Middleware d'authentification centralisé
//  Remplace les doublons dans auth.js, transactions.js, portfolio.js, users.js
// ============================================================

const jwt = require('jsonwebtoken');

const JWT_SECRET = process.env.JWT_SECRET || 'baou_finance_secret_jwt_2026_changez_cette_valeur';

/**
 * Génère un token JWT signé pour un utilisateur
 */
function generateToken(user) {
  return jwt.sign(
    { userId: user.id, role: user.role },
    JWT_SECRET,
    { expiresIn: process.env.JWT_EXPIRES_IN || '24h' }
  );
}

/**
 * Vérifie et décode un token JWT depuis le header Authorization
 */
function verifyToken(token) {
  return jwt.verify(token, JWT_SECRET);
}

/**
 * Middleware : vérifie que l'utilisateur est authentifié (JWT valide)
 */
function requireAuth(req, res, next) {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.replace('Bearer ', '').trim();

  if (!token) {
    return res.status(401).json({ error: 'Token manquant. Veuillez vous connecter.' });
  }

  try {
    const decoded = verifyToken(token);
    req.session = { userId: decoded.userId, role: decoded.role };
    next();
  } catch (err) {
    if (err.name === 'TokenExpiredError') {
      return res.status(401).json({ error: 'Session expirée. Veuillez vous reconnecter.' });
    }
    return res.status(401).json({ error: 'Token invalide.' });
  }
}

/**
 * Middleware : vérifie que l'utilisateur est admin
 * À utiliser après requireAuth
 */
function requireAdmin(req, res, next) {
  if (req.session?.role !== 'admin') {
    return res.status(403).json({ error: 'Accès réservé aux administrateurs.' });
  }
  next();
}

module.exports = { generateToken, verifyToken, requireAuth, requireAdmin };
