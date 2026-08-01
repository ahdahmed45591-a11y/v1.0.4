// ============================================================
//  BAOU Finance — Route Transactions
//  Corrections : middleware centralisé, uuidv4, email dans réponse
// ============================================================

const express = require('express');
const router = express.Router();
const { v4: uuidv4 } = require('uuid');
const {
  transactions,
  users,
  stocks,
  saveUserToSupabase,
  saveTransactionToSupabase,
  saveHoldingToSupabase,
  removeHoldingFromSupabase
} = require('../data/store');
const { requireAuth, requireAdmin } = require('../middleware/auth');

// GET /api/transactions — Transactions de l'utilisateur connecté
router.get('/', requireAuth, (req, res) => {
  const userTx = transactions.filter(t => t.userId === req.session.userId);
  res.json({ success: true, count: userTx.length, data: userTx });
});

// GET /api/transactions/all — Admin: toutes les transactions
router.get('/all', requireAuth, requireAdmin, (req, res) => {
  const { status, type, page = 1, limit = 20 } = req.query;
  let result = [...transactions];

  if (status) result = result.filter(t => t.status === status);
  if (type) result = result.filter(t => t.type === type.toUpperCase());

  const total = result.length;
  const start = (parseInt(page) - 1) * parseInt(limit);
  const paginated = result.slice(start, start + parseInt(limit));

  res.json({
    success: true,
    total,
    page: parseInt(page),
    limit: parseInt(limit),
    data: paginated,
    summary: {
      pending: transactions.filter(t => t.status === 'pending').length,
      validated: transactions.filter(t => t.status === 'validated').length,
      rejected: transactions.filter(t => t.status === 'rejected').length,
      totalValue: transactions.reduce((sum, t) => sum + (t.grandTotal || 0), 0),
    },
  });
});

// GET /api/transactions/:id — Détail d'une transaction
router.get('/:id', requireAuth, (req, res) => {
  const tx = transactions.find(t => t.id === req.params.id);
  if (!tx) return res.status(404).json({ error: 'Transaction introuvable.' });

  if (req.session.role !== 'admin' && tx.userId !== req.session.userId) {
    return res.status(403).json({ error: 'Accès refusé.' });
  }

  res.json({ success: true, data: tx });
});

// POST /api/transactions — Passer un ordre d'achat, de vente ou effectuer un dépôt
router.post('/', requireAuth, (req, res) => {
  const { ticker, type, quantity, price, paymentRef, paymentMethod } = req.body;

  if (!type || (!price && price !== 0)) {
    return res.status(400).json({ error: 'Type et prix sont requis.' });
  }

  const typeUpper = type.toUpperCase();
  if (!['BUY', 'SELL', 'DEPOSIT', 'RECHARGE'].includes(typeUpper)) {
    return res.status(400).json({ error: 'Type doit être BUY, SELL, DEPOSIT ou RECHARGE.' });
  }

  const qtyNum = parseInt(quantity || 1);
  const priceNum = parseFloat(price);

  if (isNaN(qtyNum) || qtyNum <= 0) {
    return res.status(400).json({ error: 'La quantité doit être un entier positif.' });
  }
  if (isNaN(priceNum) || priceNum <= 0) {
    return res.status(400).json({ error: 'Le montant doit être un nombre positif.' });
  }

  const user = users.find(u => u.id === req.session.userId);
  if (!user) return res.status(404).json({ error: 'Utilisateur introuvable.' });

  // Traitement spécial pour les DÉPÔTS (Wave, Orange Money, etc.)
  if (typeUpper === 'DEPOSIT' || typeUpper === 'RECHARGE') {
    const depTotal = Math.round(priceNum * 100) / 100;
    user.balance = Math.round((user.balance + depTotal) * 100) / 100;
    saveUserToSupabase(user);

    const depTx = {
      id: uuidv4(),
      userId: req.session.userId,
      userEmail: user.email,
      userName: user.name,
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
      rejectionReason: null,
      submittedAt: new Date().toISOString(),
      processedAt: new Date().toISOString(),
      processedBy: 'SYSTEM',
    };

    transactions.unshift(depTx);
    saveTransactionToSupabase(depTx);

    return res.status(201).json({
      success: true,
      message: `Dépôt de ${depTotal} FCFA effectué avec succès !`,
      data: depTx,
    });
  }

  // Ordres d'Achat (BUY) et de Vente (SELL)
  const stock = stocks.find(s => s.ticker === (ticker || '').toUpperCase());
  if (!stock) return res.status(404).json({ error: `Titre "${ticker}" introuvable.` });

  const total = qtyNum * priceNum;
  const fees = total * 0.005;       // 0.5%
  const tva = fees * 0.18;           // 18% TVA sur frais
  const grandTotal = total + fees + tva;

  if (typeUpper === 'BUY' && user.balance < grandTotal) {
    return res.status(400).json({
      error: `Solde insuffisant. Requis: ${grandTotal.toFixed(0)} FCFA, Disponible: ${user.balance} FCFA`,
    });
  }

  const newTx = {
    id: uuidv4(), // ✅ UUID garanti unique
    userId: req.session.userId,
    userEmail: user.email, // ✅ Email inclus pour le frontend
    userName: user.name,
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
    rejectionReason: null,
    submittedAt: new Date().toISOString(),
    processedAt: null,
    processedBy: null,
  };

  transactions.unshift(newTx);
  saveTransactionToSupabase(newTx);

  res.status(201).json({
    success: true,
    message: 'Ordre soumis avec succès. En attente de validation admin.',
    data: newTx,
  });
});

// PATCH /api/transactions/:id/validate — Admin: valider
router.patch('/:id/validate', requireAuth, requireAdmin, (req, res) => {
  const tx = transactions.find(t => t.id === req.params.id);
  if (!tx) return res.status(404).json({ error: 'Transaction introuvable.' });
  if (tx.status !== 'pending') {
    return res.status(400).json({ error: `Transaction déjà "${tx.status}".` });
  }

  tx.status = 'validated';
  tx.processedAt = new Date().toISOString();
  tx.processedBy = req.session.userId;

  const user = users.find(u => u.id === tx.userId);
  if (user) {
    if (tx.type === 'BUY') user.balance -= tx.grandTotal;
    else user.balance += tx.total;
    user.balance = Math.max(0, Math.round(user.balance * 100) / 100); // éviter les négatifs
    saveUserToSupabase(user);
  }

  const { portfolios } = require('../data/store');
  let userPortfolio = portfolios[tx.userId];
  if (!userPortfolio) {
    userPortfolio = {
      userId: tx.userId,
      totalValue: 0, invested: 0, gainLoss: 0, gainLossPct: 0,
      dailyChange: 0, dailyChangePct: 0, dividendsReceived: 0,
      holdings: []
    };
    portfolios[tx.userId] = userPortfolio;
  }

  if (tx.type === 'BUY') {
    let holding = userPortfolio.holdings.find(h => h.ticker === tx.ticker);
    if (holding) {
      const oldQty = holding.quantity;
      holding.quantity += tx.quantity;
      holding.avgBuy = ((oldQty * holding.avgBuy) + (tx.quantity * tx.price)) / holding.quantity;
      holding.value = holding.quantity * holding.currentPrice;
      saveHoldingToSupabase(tx.userId, holding);
    } else {
      holding = {
        ticker: tx.ticker,
        company: tx.company,
        quantity: tx.quantity,
        avgBuy: tx.price,
        currentPrice: tx.price,
        value: tx.quantity * tx.price,
        gainLoss: 0,
        gainLossPct: 0
      };
      userPortfolio.holdings.push(holding);
      saveHoldingToSupabase(tx.userId, holding);
    }
  } else if (tx.type === 'SELL') {
    let holding = userPortfolio.holdings.find(h => h.ticker === tx.ticker);
    if (holding) {
      holding.quantity -= tx.quantity;
      if (holding.quantity <= 0) {
        userPortfolio.holdings = userPortfolio.holdings.filter(h => h.ticker !== tx.ticker);
        removeHoldingFromSupabase(tx.userId, tx.ticker);
      } else {
        holding.value = holding.quantity * holding.currentPrice;
        saveHoldingToSupabase(tx.userId, holding);
      }
    }
  }

  saveTransactionToSupabase(tx);
  res.json({ success: true, message: `Transaction #${tx.id} validée.`, data: tx });
});

// PATCH /api/transactions/:id/reject — Admin: rejeter
router.patch('/:id/reject', requireAuth, requireAdmin, (req, res) => {
  const tx = transactions.find(t => t.id === req.params.id);
  if (!tx) return res.status(404).json({ error: 'Transaction introuvable.' });
  if (tx.status !== 'pending') {
    return res.status(400).json({ error: `Transaction déjà "${tx.status}".` });
  }

  const { reason } = req.body;
  tx.status = 'rejected';
  tx.rejectionReason = reason || 'Rejeté par l\'administrateur.';
  tx.processedAt = new Date().toISOString();
  tx.processedBy = req.session.userId;

  saveTransactionToSupabase(tx);
  res.json({ success: true, message: `Transaction #${tx.id} rejetée.`, data: tx });
});

module.exports = router;
