// ============================================================
//  🐘 Éléphant Bourse — Backend CORE Service (Moteur Métier & DB)
//  Port: 3002
// ============================================================

require('dotenv').config();
const express = require('express');
const cors = require('cors');
const { v4: uuidv4 } = require('uuid');

const app = express();
const PORT = process.env.CORE_PORT || 3002;

app.use(cors());
app.use(express.json({ limit: '15mb' }));
app.use(express.urlencoded({ limit: '15mb', extended: true }));

const {
  users,
  stocks,
  transactions,
  tickets,
  chatMessages,
  portfolios,
  adminStats,
  saveUserToSupabase,
  saveTransactionToSupabase,
} = require('../data/store');

// Logger silencieux
app.use((req, res, next) => {
  const ts = new Date().toLocaleTimeString('fr-FR');
  console.log(`[CORE:3002 - ${ts}] ${req.method} ${req.path}`);
  next();
});

// ── Healthcheck ─────────────────────────────────────────────
app.get('/health', (req, res) => {
  res.json({ status: 'ok', service: 'backend-core', port: PORT, timestamp: new Date().toISOString() });
});

const bcrypt = require('bcryptjs');

// ── AUTH CORE ────────────────────────────────────────────────
app.post('/internal/auth/login', async (req, res) => {
  const { email, password } = req.body;
  const user = users.find(u => u.email.toLowerCase() === (email || '').toLowerCase());
  if (!user) {
    return res.status(401).json({ success: false, message: 'Email ou mot de passe incorrect.' });
  }

  let passwordMatch = false;
  if (user.password && user.password.startsWith('$2')) {
    passwordMatch = await bcrypt.compare(password, user.password);
  } else {
    passwordMatch = (user.password === password);
  }

  if (!passwordMatch) {
    return res.status(401).json({ success: false, message: 'Email ou mot de passe incorrect.' });
  }

  const { password: _, ...safeUser } = user;
  res.json({ success: true, user: safeUser });
});

app.post('/internal/auth/register', async (req, res) => {
  const { email, password, name, firstName } = req.body;
  const userName = (name || firstName || '').trim();

  if (!email || !password || !userName) {
    return res.status(400).json({ error: 'Email, mot de passe et nom/prénom sont requis.' });
  }

  const existing = users.find(u => u.email.toLowerCase() === (email || '').toLowerCase().trim());
  if (existing) {
    return res.status(409).json({ error: 'Un compte existe déjà avec cet email.' });
  }

  const hashedPassword = await bcrypt.hash(password, 10);
  const newUser = {
    id: `CLI-${Date.now()}`,
    name: userName,
    email: email.toLowerCase().trim(),
    password: hashedPassword,
    role: 'client',
    kyc: 'pending',
    balance: 0.0,
    joinedAt: new Date().toISOString(),
  };

  users.push(newUser);
  saveUserToSupabase(newUser);
  const { password: _, ...safeUser } = newUser;
  res.status(201).json({ success: true, user: safeUser });
});

app.patch('/internal/auth/profile', (req, res) => {
  const { userId, firstName, lastName, kycStatus, whatsapp, birthDate, profession, residence } = req.body;
  const user = users.find(u => u.id === userId);
  if (!user) return res.status(404).json({ error: 'Utilisateur introuvable.' });

  if (firstName || lastName) user.name = `${firstName || ''} ${lastName || ''}`.trim();
  if (kycStatus) user.kyc = kycStatus;
  if (whatsapp) user.whatsapp = whatsapp;
  if (birthDate) user.birthDate = birthDate;
  if (profession) user.profession = profession;
  if (residence) user.residence = residence;

  saveUserToSupabase(user);
  const { password: _, ...safeUser } = user;
  res.json({ success: true, user: safeUser });
});

// ── STOCKS CORE ──────────────────────────────────────────────
app.get('/internal/stocks', (req, res) => {
  res.json({ success: true, count: stocks.length, data: stocks });
});

app.get('/internal/stocks/:ticker', (req, res) => {
  const stock = stocks.find(s => s.ticker === (req.params.ticker || '').toUpperCase());
  if (!stock) return res.status(404).json({ error: 'Titre introuvable.' });
  res.json({ success: true, data: stock });
});

// ── TRANSACTIONS CORE ────────────────────────────────────────
app.get('/internal/transactions', (req, res) => {
  const { userId, role } = req.query;
  let list = [...transactions];
  if (role !== 'admin' && userId) {
    list = list.filter(t => t.userId === userId);
  }
  res.json({ success: true, count: list.length, data: list });
});

app.post('/internal/transactions', (req, res) => {
  const { userId, userEmail, userName, ticker, type, quantity, price, paymentRef, paymentMethod } = req.body;
  const typeUpper = (type || 'BUY').toUpperCase();

  const user = users.find(u => u.id === userId);
  if (!user) return res.status(404).json({ error: 'Utilisateur introuvable.' });

  const qtyNum = parseInt(quantity || 1);
  const priceNum = parseFloat(price || 0);

  // Traitement DÉPÔT / RECHARGE
  if (typeUpper === 'DEPOSIT' || typeUpper === 'RECHARGE') {
    const depTotal = Math.round(priceNum * 100) / 100;
    user.balance = Math.round((user.balance + depTotal) * 100) / 100;
    saveUserToSupabase(user);

    const depTx = {
      id: uuidv4(),
      userId,
      userEmail: userEmail || user.email,
      userName: userName || user.name,
      ticker: (ticker || 'CASH').toUpperCase(),
      company: `Dépôt ${paymentMethod || 'Wave CI'}`,
      type: 'DEPOSIT',
      quantity: 1,
      price: depTotal,
      total: depTotal,
      fees: 0,
      tva: 0,
      grandTotal: depTotal,
      status: 'validated',
      paymentRef: paymentRef || `REF-${Math.floor(100000 + Math.random() * 900000)}`,
      paymentMethod: paymentMethod || 'Wave CI',
      submittedAt: new Date().toISOString(),
      processedAt: new Date().toISOString(),
      processedBy: 'SYSTEM',
    };

    transactions.unshift(depTx);
    saveTransactionToSupabase(depTx);
    return res.status(201).json({ success: true, data: depTx });
  }

  // Achat / Vente
  const stock = stocks.find(s => s.ticker === (ticker || '').toUpperCase());
  if (!stock) return res.status(404).json({ error: `Titre "${ticker}" introuvable.` });

  const total = qtyNum * priceNum;
  const fees = total * 0.005;
  const tva = fees * 0.18;
  const grandTotal = total + fees + tva;

  if (typeUpper === 'BUY' && user.balance < grandTotal) {
    return res.status(400).json({ error: `Solde insuffisant (${user.balance} FCFA dispo, ${grandTotal.toFixed(0)} FCFA requis).` });
  }

  const newTx = {
    id: uuidv4(),
    userId,
    userEmail: userEmail || user.email,
    userName: userName || user.name,
    ticker: ticker.toUpperCase(),
    company: stock.company,
    type: typeUpper,
    quantity: qtyNum,
    price: priceNum,
    total: Math.round(total * 100) / 100,
    fees: Math.round(fees * 100) / 100,
    tva: Math.round(tva * 100) / 100,
    grandTotal: Math.round(grandTotal * 100) / 100,
    status: 'pending',
    paymentRef: paymentRef || `AUTO-${Date.now()}`,
    paymentMethod: paymentMethod || 'Non spécifié',
    submittedAt: new Date().toISOString(),
    processedAt: null,
    processedBy: null,
  };

  transactions.unshift(newTx);
  saveTransactionToSupabase(newTx);
  res.status(201).json({ success: true, data: newTx });
});

app.patch('/internal/transactions/:id/validate', (req, res) => {
  const tx = transactions.find(t => t.id === req.params.id);
  if (!tx) return res.status(404).json({ error: 'Transaction introuvable.' });
  if (tx.status !== 'pending') return res.status(400).json({ error: `Transaction déjà "${tx.status}".` });

  tx.status = 'validated';
  tx.processedAt = new Date().toISOString();
  tx.processedBy = req.body.adminId || 'ADMIN';

  const user = users.find(u => u.id === tx.userId);
  if (user) {
    if (tx.type === 'BUY') user.balance -= tx.grandTotal;
    else user.balance += tx.total;
    user.balance = Math.max(0, Math.round(user.balance * 100) / 100);
    saveUserToSupabase(user);
  }

  saveTransactionToSupabase(tx);
  res.json({ success: true, data: tx });
});

app.patch('/internal/transactions/:id/reject', (req, res) => {
  const tx = transactions.find(t => t.id === req.params.id);
  if (!tx) return res.status(404).json({ error: 'Transaction introuvable.' });

  tx.status = 'rejected';
  tx.rejectionReason = req.body.reason || 'Rejeté par l\'administrateur.';
  tx.processedAt = new Date().toISOString();

  saveTransactionToSupabase(tx);
  res.json({ success: true, data: tx });
});

// ── ADMIN & USERS CORE ────────────────────────────────────────
app.get('/internal/admin/stats', (req, res) => {
  const clients = users.filter(u => u.role === 'client');
  res.json({
    success: true,
    data: {
      ...adminStats,
      totalUsers: clients.length,
      kycVerified: clients.filter(u => u.kyc === 'verified').length,
      kycPending: clients.filter(u => u.kyc === 'pending').length,
      suspended: clients.filter(u => u.kyc === 'suspended').length,
    },
  });
});

app.get('/internal/admin/users', (req, res) => {
  const safeUsers = users.filter(u => u.role === 'client').map(({ password: _, ...u }) => u);
  res.json({ success: true, count: safeUsers.length, data: safeUsers });
});

app.patch('/internal/admin/users/:id/kyc', (req, res) => {
  const { status } = req.body;
  const user = users.find(u => u.id === req.params.id);
  if (!user) return res.status(404).json({ error: 'Utilisateur introuvable.' });

  user.kyc = status;
  saveUserToSupabase(user);
  res.json({ success: true, data: { id: user.id, name: user.name, kyc: user.kyc } });
});

// ── SUPPORT & CHAT CORE ──────────────────────────────────────
app.get('/internal/support', (req, res) => {
  res.json({ success: true, count: tickets.length, data: tickets });
});

app.get('/internal/chat/:userId', (req, res) => {
  const messages = chatMessages[req.params.userId] || [];
  res.json({ success: true, data: messages });
});

app.post('/internal/chat/:userId', (req, res) => {
  const { text, sender } = req.body;
  const userId = req.params.userId;
  const user = users.find(u => u.id === userId);

  const msg = {
    id: `MSG-${Date.now()}`,
    userId,
    userName: user?.name || 'Client',
    sender: sender || 'ADMIN',
    text: (text || '').trim(),
    time: new Date().toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' }),
  };

  if (!chatMessages[userId]) chatMessages[userId] = [];
  chatMessages[userId].push(msg);
  res.status(201).json({ success: true, data: msg });
});

// ── Démarrage Serveur ────────────────────────────────────────
const server = app.listen(PORT, () => {
  console.log(`[CORE SERVICE] Server running on http://localhost:${PORT}`);
});

module.exports = { app, server };
