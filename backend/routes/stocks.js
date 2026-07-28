// ============================================================
//  BAOU Finance — Route Stocks
//  Correction : fluctuations de prix calculées à intervalles fixes
// ============================================================

const express = require('express');
const router = express.Router();
const { stocks } = require('../data/store');

// ─── Fluctuations de prix à intervalle fixe (toutes les 30s) ──────────────
// Au lieu de recalculer à chaque requête, on maintient un état stable
let fluctuatedStocks = stocks.map(s => ({ ...s }));

function applyMarketFluctuations() {
  fluctuatedStocks = stocks.map(stock => {
    const delta = (Math.random() - 0.5) * 0.3; // ±0.15%
    const newPrice = Math.max(1, Math.round(stock.price * (1 + delta / 100)));
    return {
      ...stock,
      price: newPrice,
      lastUpdated: new Date().toISOString()
    };
  });
}

// Mise à jour toutes les 30 secondes (cohérent pour tous les clients)
applyMarketFluctuations();
setInterval(applyMarketFluctuations, 30000);

// GET /api/stocks — Tous les titres BRVM
router.get('/', (req, res) => {
  res.json({ success: true, count: fluctuatedStocks.length, data: fluctuatedStocks });
});

// GET /api/stocks/brvm/indices — Indices BRVM (doit être AVANT /:ticker)
router.get('/brvm/indices', (req, res) => {
  res.json({
    success: true,
    data: {
      composite: { value: 214.68 + (Math.random() - 0.5) * 2, change: 0.85 },
      brvm10: { value: 312.45 + (Math.random() - 0.5) * 3, change: 1.23 },
      volume: 482300000,
      status: 'OPEN',
      lastUpdated: new Date().toISOString(),
    },
  });
});

// GET /api/stocks/:ticker — Un titre spécifique
router.get('/:ticker', (req, res) => {
  const ticker = req.params.ticker.toUpperCase();
  const stock = fluctuatedStocks.find(s => s.ticker === ticker);

  if (!stock) {
    return res.status(404).json({ error: `Titre "${ticker}" introuvable sur la BRVM.` });
  }

  res.json({ success: true, data: stock });
});

module.exports = router;
