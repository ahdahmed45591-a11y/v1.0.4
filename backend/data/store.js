// ============================================================
//  Éléphant Bourse — Données en mémoire (pas de base de données)
//  Reset à chaque redémarrage du serveur.
// ============================================================

const { v4: uuidv4 } = require('uuid');
const { getTickerData } = require('../utils/brvmDataReader');

// ─── UTILISATEURS ─────────────────────────────────────────
const users = [
  // Admin uniquement
  {
    id: 'ADMIN-001',
    name: 'M. Cissé',
    email: 'admin@elephantbourse.ci',
    password: 'admin2024',
    role: 'admin',
    level: 4,
    avatar: 'MK',
  },
];

// ─── ACTIONS BRVM ─────────────────────────────────────────
let stocks = [
  { ticker: 'SNTS', company: 'Sonatel CI', sector: 'Télécoms', price: 16850, prevClose: 16450, change: 2.45, volume: 124200, marketCap: '985 Mrd', high52: 18500, low52: 14200, pe: 12.4, dividend: 850, yield: 5.17 },
  { ticker: 'ORAC', company: 'Orange CI', sector: 'Télécoms', price: 10450, prevClose: 10540, change: -0.85, volume: 98700, marketCap: '512 Mrd', high52: 11200, low52: 9100, pe: 15.1, dividend: 380, yield: 4.42 },
  { ticker: 'ONTBF', company: 'Onatel BF', sector: 'Télécoms', price: 2695, prevClose: 2745, change: -1.82, volume: 32100, marketCap: '98 Mrd', high52: 3800, low52: 2500, pe: 11.2, dividend: 120, yield: 2.82 },
  { ticker: 'SGBC', company: 'Société Générale CI', sector: 'Banque', price: 37995, prevClose: 38010, change: -0.04, volume: 41200, marketCap: '284 Mrd', high52: 42500, low52: 31000, pe: 9.8, dividend: 1450, yield: 3.82 },
  { ticker: 'ETI', company: 'Ecobank Transnational', sector: 'Banque', price: 73, prevClose: 78, change: -6.41, volume: 841000, marketCap: '1.2 Trd', high52: 95, low52: 60, pe: 10.5, dividend: 6, yield: 3.80 },
  { ticker: 'BOAB', company: 'BOA Bénin', sector: 'Banque', price: 8780, prevClose: 8765, change: 0.17, volume: 58400, marketCap: '142 Mrd', high52: 9800, low52: 7200, pe: 8.2, dividend: 400, yield: 4.55 },
  { ticker: 'BOAC', company: 'BOA Côte d\'Ivoire', sector: 'Banque', price: 7200, prevClose: 7100, change: 1.41, volume: 64200, marketCap: '144 Mrd', high52: 8200, low52: 6100, pe: 7.9, dividend: 360, yield: 5.00 },
  { ticker: 'BOABF', company: 'BOA Burkina Faso', sector: 'Banque', price: 6850, prevClose: 6800, change: 0.74, volume: 22100, marketCap: '102 Mrd', high52: 7500, low52: 5800, pe: 8.4, dividend: 320, yield: 4.67 },
  { ticker: 'BOAM', company: 'BOA Mali', sector: 'Banque', price: 1850, prevClose: 1850, change: 0.0, volume: 15400, marketCap: '37 Mrd', high52: 2400, low52: 1500, pe: 6.8, dividend: 90, yield: 4.86 },
  { ticker: 'BOAN', company: 'BOA Niger', sector: 'Banque', price: 4900, prevClose: 4875, change: 0.50, volume: 11200, marketCap: '49 Mrd', high52: 5800, low52: 4100, pe: 7.5, dividend: 210, yield: 4.28 },
  { ticker: 'BOAS', company: 'BOA Sénégal', sector: 'Banque', price: 3200, prevClose: 3104, change: 3.10, volume: 38900, marketCap: '64 Mrd', high52: 3900, low52: 2700, pe: 9.1, dividend: 150, yield: 4.68 },
  { ticker: 'BICI', company: 'BICICI', sector: 'Banque', price: 7800, prevClose: 7720, change: 1.04, volume: 45300, marketCap: '165 Mrd', high52: 8900, low52: 6800, pe: 13.2, dividend: 310, yield: 3.97 },
  { ticker: 'SIB', company: 'Société Ivoirienne de Banque', sector: 'Banque', price: 5400, prevClose: 5350, change: 0.93, volume: 87100, marketCap: '270 Mrd', high52: 6200, low52: 4500, pe: 8.9, dividend: 280, yield: 5.18 },
  { ticker: 'NSBC', company: 'NSIA Banque CI', sector: 'Banque', price: 6100, prevClose: 6130, change: -0.50, volume: 32400, marketCap: '141 Mrd', high52: 7100, low52: 5200, pe: 8.1, dividend: 300, yield: 4.91 },
  { ticker: 'CBIB', company: 'Coris Bank International BF', sector: 'Banque', price: 10200, prevClose: 10078, change: 1.20, volume: 29500, marketCap: '326 Mrd', high52: 11500, low52: 8900, pe: 9.5, dividend: 520, yield: 5.09 },
  { ticker: 'PALC', company: 'Palm CI', sector: 'Agriculture', price: 7100, prevClose: 6950, change: 2.15, volume: 54200, marketCap: '113 Mrd', high52: 12500, low52: 5900, pe: 5.8, dividend: 650, yield: 9.15 },
  { ticker: 'SOGC', company: 'SOGB CI', sector: 'Agriculture', price: 4200, prevClose: 4124, change: 1.85, volume: 31200, marketCap: '91 Mrd', high52: 5900, low52: 3600, pe: 6.2, dividend: 380, yield: 9.04 },
  { ticker: 'SAPC', company: 'SAPH CI', sector: 'Agriculture', price: 3400, prevClose: 3425, change: -0.75, volume: 18900, marketCap: '53 Mrd', high52: 4800, low52: 2900, pe: 7.1, dividend: 220, yield: 6.47 },
  { ticker: 'SLBC', company: 'Solibra CI', sector: 'Industrie', price: 89000, prevClose: 89000, change: 0.0, volume: 4200, marketCap: '147 Mrd', high52: 135000, low52: 75000, pe: 14.5, dividend: 4200, yield: 4.71 },
  { ticker: 'UNXC', company: 'Uniwax CI', sector: 'Industrie', price: 780, prevClose: 790, change: -1.25, volume: 92400, marketCap: '16 Mrd', high52: 1400, low52: 680, pe: 11.0, dividend: 30, yield: 3.84 },
  { ticker: 'CABC', company: 'Sicable CI', sector: 'Industrie', price: 3625, prevClose: 3710, change: -2.29, volume: 18700, marketCap: '88 Mrd', high52: 4500, low52: 3100, pe: 9.1, dividend: 220, yield: 6.06 },
  { ticker: 'CIEC', company: 'CIE Côte d\'Ivoire', sector: 'Services Publics', price: 2150, prevClose: 2140, change: 0.45, volume: 42100, marketCap: '120 Mrd', high52: 2600, low52: 1850, pe: 10.2, dividend: 140, yield: 6.51 },
  { ticker: 'SDCC', company: 'SODECI Côte d\'Ivoire', sector: 'Services Publics', price: 5300, prevClose: 5242, change: 1.10, volume: 28400, marketCap: '47 Mrd', high52: 6100, low52: 4600, pe: 8.7, dividend: 350, yield: 6.60 },
  { ticker: 'CFAC', company: 'CFAO Motors CI', sector: 'Distribution', price: 920, prevClose: 920, change: 0.0, volume: 64100, marketCap: '17 Mrd', high52: 1300, low52: 810, pe: 12.8, dividend: 45, yield: 4.89 },
  { ticker: 'TTLS', company: 'TotalEnergies Marketing SN', sector: 'Distribution', price: 2500, prevClose: 2480, change: 0.80, volume: 21400, marketCap: '67 Mrd', high52: 2900, low52: 2100, pe: 9.9, dividend: 160, yield: 6.40 },
  { ticker: 'SDSC', company: 'AGL / Bolloré Transport CI', sector: 'Transport', price: 1650, prevClose: 1660, change: -0.60, volume: 142000, marketCap: '92 Mrd', high52: 2400, low52: 1350, pe: 8.3, dividend: 110, yield: 6.66 }
];

// Charger dynamiquement les données réelles de la BRVM si disponibles en local
stocks = stocks.map(stock => {
  const realData = getTickerData(stock.ticker);
  if (realData) {
    console.log(`[BRVM Data] Chargé ${stock.ticker}: ${realData.price} FCFA (${realData.change > 0 ? '+' : ''}${realData.change}%)`);
    return {
      ...stock,
      price: realData.price,
      prevClose: realData.prevClose,
      change: realData.change,
      volume: realData.volume
    };
  }
  return stock;
});

// ─── TRANSACTIONS ──────────────────────────────────────────
let transactions = [];

// ─── TICKETS DE SUPPORT ────────────────────────────────────
let tickets = [
  {
    id: 'TKT-1002',
    clientName: 'Mamadou Konaté',
    clientId: 'mamadou.konate@email.ci',
    subject: 'Assistance Inscription',
    message: 'Bonjour, j\'ai besoin d\'aide pour valider mon contrat.',
    status: 'OUVERT',
    dateString: 'Aujourd\'hui, 10:15'
  }
];

// ─── PORTEFEUILLES ─────────────────────────────────────────
const portfolios = {};

// ─── KPI ADMIN ─────────────────────────────────────────────
const adminStats = {
  totalUsers: 0,
  kycVerified: 0,
  kycPending: 0,
  kycUrgent: 0,
  suspended: 0,
  marketStatus: 'OPEN',
  marketCloseIn: '3h 15m',
  brvm: {
    composite: 214.68,
    composite_change: 0.85,
    brvm10: 312.45,
    brvm10_change: 1.23,
    volume: 482300000,
    volume_change: -3.2,
  },
};

// ─── SYNCHRONISATION SUPABASE ──────────────────────────────
const { supabase } = require('../utils/supabaseClient');

// ─── LOCAL SQLITE DATABASE INITIALIZATION & SYNC ─────────
const sqliteDb = require('./sqlite_db');

/**
 * Charge les données SQLite dans le store en mémoire.
 * Retourne une Promise — permet de bloquer app.listen() jusqu'à la fin.
 */
function syncFromSQLite() {
  return new Promise((resolve) => {
    console.log('[SQLite] Chargement des données locales...');

    // Charger utilisateurs puis transactions séquentiellement
    sqliteDb.all('SELECT * FROM users', [], (err, dbUsers) => {
      if (!err && dbUsers && dbUsers.length > 0) {
        dbUsers.forEach(u => {
          const existing = users.find(x => x.id === u.id || x.email === u.email);
          if (existing) {
            // Fusionner les données SQLite dans l'objet existant
            Object.assign(existing, {
              name: u.name, email: u.email, password: u.password,
              role: u.role || existing.role, level: u.level || existing.level,
              avatar: u.avatar || existing.avatar, whatsapp: u.whatsapp,
              birthDate: u.birthDate, profession: u.profession, residence: u.residence,
              kyc: u.kyc || existing.kyc, balance: u.balance || existing.balance,
              portfolioValue: u.portfolioValue || 0,
              identityDocStatus: u.identityDocStatus, proofOfAddressStatus: u.proofOfAddressStatus,
              signatureStatus: u.signatureStatus,
              cniRectoUrl: u.cni_recto_url || undefined, cniVersoUrl: u.cni_verso_url || undefined,
              selfieUrl: u.selfie_url || undefined, proofAddressUrl: u.proof_address_url || undefined,
              contractUrl: u.contract_url || undefined
            });
          } else {
            users.push({
              id: u.id, name: u.name, email: u.email, password: u.password,
              role: u.role || 'client', level: u.level || 1, avatar: u.avatar || '',
              whatsapp: u.whatsapp || '', birthDate: u.birthDate || '',
              profession: u.profession || '', residence: u.residence || '',
              kyc: u.kyc || 'pending', balance: u.balance || 0.0,
              portfolioValue: u.portfolioValue || 0.0,
              identityDocStatus: u.identityDocStatus || '',
              proofOfAddressStatus: u.proofOfAddressStatus || '',
              signatureStatus: u.signatureStatus || '',
              cniRectoUrl: u.cni_recto_url || undefined, cniVersoUrl: u.cni_verso_url || undefined,
              selfieUrl: u.selfie_url || undefined, proofAddressUrl: u.proof_address_url || undefined,
              contractUrl: u.contract_url || undefined
            });
          }
        });
        console.log(`[SQLite] ✅ ${dbUsers.length} utilisateurs chargés.`);
      }

      // Charger transactions après utilisateurs
      sqliteDb.all('SELECT * FROM transactions ORDER BY submitted_at DESC', [], (err2, dbTx) => {
        if (!err2 && dbTx && dbTx.length > 0) {
          dbTx.forEach(t => {
            if (!transactions.some(x => x.id === t.id)) {
              transactions.push({
                id: t.id, userId: t.user_id, userName: t.user_name,
                ticker: t.ticker, company: t.company, type: t.type,
                quantity: t.quantity, price: t.price, total: t.total,
                fees: t.fees, tva: t.tva, grandTotal: t.grand_total,
                status: t.status, paymentRef: t.payment_ref,
                paymentMethod: t.payment_method, rejectionReason: t.rejection_reason,
                submittedAt: t.submitted_at
              });
            }
          });
          console.log(`[SQLite] ✅ ${dbTx.length} transactions chargées.`);
        }
        resolve(); // ✅ Résoudre la Promise une fois tout chargé
      });
    });
  });
}

// ✅ Export de la fonction d'init pour que server.js puisse attendre
module.exports._syncReady = syncFromSQLite();

function saveUserToSQLite(user) {
  sqliteDb.run(`INSERT INTO users (id, name, email, password, role, level, avatar, whatsapp, birthDate, profession, residence, kyc, balance, portfolioValue, identityDocStatus, proofOfAddressStatus, signatureStatus, cni_recto_url, cni_verso_url, selfie_url, proof_address_url, contract_url)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    ON CONFLICT(id) DO UPDATE SET
      name=excluded.name,
      email=excluded.email,
      password=excluded.password,
      role=excluded.role,
      level=excluded.level,
      avatar=excluded.avatar,
      whatsapp=excluded.whatsapp,
      birthDate=excluded.birthDate,
      profession=excluded.profession,
      residence=excluded.residence,
      kyc=excluded.kyc,
      balance=excluded.balance,
      portfolioValue=excluded.portfolioValue,
      identityDocStatus=excluded.identityDocStatus,
      proofOfAddressStatus=excluded.proofOfAddressStatus,
      signatureStatus=excluded.signatureStatus,
      cni_recto_url=excluded.cni_recto_url,
      cni_verso_url=excluded.cni_verso_url,
      selfie_url=excluded.selfie_url,
      proof_address_url=excluded.proof_address_url,
      contract_url=excluded.contract_url`,
    [user.id, user.name, user.email, user.password, user.role || 'client', user.level || 1, user.avatar || '', user.whatsapp || '', user.birthDate || '', user.profession || '', user.residence || '', user.kyc || 'pending', user.balance || 0, user.portfolioValue || 0, user.identityDocStatus || '', user.proofOfAddressStatus || '', user.signatureStatus || '', user.cniRectoUrl || null, user.cniVersoUrl || null, user.selfieUrl || null, user.proofAddressUrl || null, user.contractUrl || null]
  );
}

function saveTransactionToSQLite(tx) {
  sqliteDb.run(`INSERT INTO transactions (id, user_id, user_name, ticker, company, type, quantity, price, total, fees, tva, grand_total, status, payment_ref, payment_method, rejection_reason)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    ON CONFLICT(id) DO UPDATE SET
      status=excluded.status,
      rejection_reason=excluded.rejection_reason`,
    [tx.id, tx.userId, tx.userName, tx.ticker || '', tx.company || '', tx.type, tx.quantity || 0, tx.price || 0, tx.total || 0, tx.fees || 0, tx.tva || 0, tx.grandTotal || 0, tx.status || 'PENDING', tx.paymentRef || '', tx.paymentMethod || '', tx.rejectionReason || '']
  );
}

// Utilitaires de modification et sauvegarde Supabase + SQLite
async function saveUserToSupabase(user) {
  saveUserToSQLite(user);
  if (!supabase) return; // ✅ Guard : Supabase désactivé (mode SQLite uniquement)
  try {
    const { error } = await supabase.from('users').upsert({
      id: user.id,
      name: user.name,
      email: user.email,
      password: user.password,
      role: user.role,
      type: user.type,
      kyc: user.kyc,
      balance: user.balance,
      avatar: user.avatar
    });
    if (error) console.error('[Supabase] Erreur de sauvegarde utilisateur:', error.message);
  } catch (e) {
    console.error('[Supabase] Exception sauvegarde utilisateur:', e.message);
  }
}

async function saveTransactionToSupabase(tx) {
  saveTransactionToSQLite(tx);
  if (!supabase) return; // ✅ Guard
  try {
    const { error } = await supabase.from('transactions').upsert({
      id: tx.id,
      user_id: tx.userId,
      user_name: tx.userName,
      ticker: tx.ticker,
      company: tx.company,
      type: tx.type,
      quantity: tx.quantity,
      price: tx.price,
      total: tx.total,
      fees: tx.fees,
      tva: tx.tva,
      grand_total: tx.grandTotal,
      status: tx.status,
      payment_ref: tx.paymentRef,
      payment_method: tx.paymentMethod,
      rejection_reason: tx.rejectionReason,
      submitted_at: tx.submittedAt || new Date().toISOString(),
      processed_at: tx.processedAt,
      processed_by: tx.processedBy
    });
    if (error) console.error('[Supabase] Erreur de sauvegarde transaction:', error.message);
  } catch (e) {
    console.error('[Supabase] Exception sauvegarde transaction:', e.message);
  }
}

async function saveHoldingToSupabase(userId, holding) {
  if (!supabase) return; // ✅ Guard
  try {
    const { error } = await supabase.from('holdings').upsert({
      user_id: userId,
      ticker: holding.ticker,
      company_name: holding.company,
      shares_count: holding.quantity,
      average_price: holding.avgBuy,
      current_price: holding.currentPrice,
      change_percent: holding.gainLossPct,
      sector: 'Bourse'
    }, { onConflict: 'user_id,ticker' });
    if (error) console.error('[Supabase] Erreur de sauvegarde position:', error.message);
  } catch (e) {
    console.error('[Supabase] Exception sauvegarde position:', e.message);
  }
}

async function removeHoldingFromSupabase(userId, ticker) {
  if (!supabase) return; // ✅ Guard
  try {
    const { error } = await supabase.from('holdings').delete().match({ user_id: userId, ticker });
    if (error) console.error('[Supabase] Erreur de suppression position:', error.message);
  } catch (e) {
    console.error('[Supabase] Exception suppression position:', e.message);
  }
}

const chatMessages = {};

const documents = [
  {
    id: 'DOC-101',
    name: "Contrat d'Ouverture de Compte SGI.pdf",
    category: 'Contrat',
    description: "Contrat officiel d'ouverture de compte titres SGI BRVM à distance.",
    date: '19/07/2026',
    url: '/downloads/contrat_ouverture_sgi.pdf'
  },
  {
    id: 'DOC-102',
    name: "Fiche d'Inscription KYC & Pièces.pdf",
    category: 'KYC',
    description: "Formulaire de conformité et liste des pièces requises.",
    date: '19/07/2026',
    url: '/downloads/fiche_kyc_sgi.pdf'
  },
  {
    id: 'DOC-103',
    name: 'Reglement General BRVM AMF-UMOA.pdf',
    category: 'Règlement',
    description: 'Règlement général des opérations boursières sur la BRVM.',
    date: '19/07/2026',
    url: '/downloads/reglement_general_brvm.pdf'
  }
];

module.exports = { 
  users, 
  stocks, 
  transactions, 
  tickets,
  chatMessages,
  documents,
  portfolios, 
  adminStats,
  saveUserToSupabase,
  saveTransactionToSupabase,
  saveHoldingToSupabase,
  removeHoldingFromSupabase
};
