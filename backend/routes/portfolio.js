// ============================================================
//  BAOU Finance — Route Portfolio
//  Correction : division par zéro, middleware centralisé
// ============================================================

const express = require('express');
const router = express.Router();
const { portfolios, stocks } = require('../data/store');
const { requireAuth } = require('../middleware/auth');

// GET /api/portfolio — Portefeuille de l'utilisateur connecté
router.get('/', requireAuth, (req, res) => {
  const portfolio = portfolios[req.session.userId];

  if (!portfolio || portfolio.holdings.length === 0) {
    return res.json({
      success: true,
      data: {
        userId: req.session.userId,
        totalValue: 0,
        invested: 0,
        gainLoss: 0,
        gainLossPct: 0,
        dailyChange: 0,
        dailyChangePct: 0,
        dividendsReceived: 0,
        holdings: [],
      },
    });
  }

  // Actualiser les prix des positions
  const updatedHoldings = portfolio.holdings.map(h => {
    const stock = stocks.find(s => s.ticker === h.ticker);
    const currentPrice = stock ? stock.price : h.currentPrice;
    const value = h.quantity * currentPrice;
    const costBasis = h.quantity * h.avgBuy;
    const gainLoss = value - costBasis;
    // ✅ Correction division par zéro
    const gainLossPct = costBasis > 0
      ? parseFloat(((gainLoss / costBasis) * 100).toFixed(2))
      : 0;
    return { ...h, currentPrice, value, gainLoss, gainLossPct };
  });

  const totalValue = updatedHoldings.reduce((s, h) => s + h.value, 0);
  const invested = updatedHoldings.reduce((s, h) => s + h.quantity * h.avgBuy, 0);
  const gainLoss = totalValue - invested;
  // ✅ Correction division par zéro au niveau du portefeuille global
  const gainLossPct = invested > 0
    ? parseFloat(((gainLoss / invested) * 100).toFixed(2))
    : 0;

  res.json({
    success: true,
    data: {
      ...portfolio,
      holdings: updatedHoldings,
      totalValue: Math.round(totalValue),
      invested: Math.round(invested),
      gainLoss: Math.round(gainLoss),
      gainLossPct,
    },
  });
});

module.exports = router;
