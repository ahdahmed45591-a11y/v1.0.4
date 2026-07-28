// ============================================================
//  🐘 BAOU Finance — Persistent SQLite Database Module
//  Database path: backend/data/baou_finance.db
// ============================================================

const sqlite3 = require('sqlite3').verbose();
const path = require('path');
const fs = require('fs');

const dbPath = path.join(__dirname, 'baou_finance.db');
const db = new sqlite3.Database(dbPath, (err) => {
  if (err) {
    console.error('[SQLite] Error opening database:', err.message);
  } else {
    console.log('[SQLite] Connected to local database:', dbPath);
  }
});

// Initialize database schema synchronously/in order
db.serialize(() => {
  // 1. Users Table
  db.run(`CREATE TABLE IF NOT EXISTS users (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    role TEXT DEFAULT 'client',
    level INTEGER DEFAULT 1,
    avatar TEXT,
    phone TEXT,
    whatsapp TEXT,
    birthDate TEXT,
    profession TEXT,
    residence TEXT,
    kyc TEXT DEFAULT 'pending',
    balance REAL DEFAULT 0.0,
    portfolioValue REAL DEFAULT 0.0,
    identityDocStatus TEXT,
    proofOfAddressStatus TEXT,
    signatureStatus TEXT,
    cni_recto_url TEXT,
    cni_verso_url TEXT,
    selfie_url TEXT,
    proof_address_url TEXT,
    contract_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
  )`, (err) => {
    if (!err) {
      // Auto-migrate missing columns for existing SQLite database
      const cols = ['cni_recto_url', 'cni_verso_url', 'selfie_url', 'proof_address_url', 'contract_url'];
      cols.forEach(col => {
        db.run(`ALTER TABLE users ADD COLUMN ${col} TEXT`, () => {});
      });
    }
  });

  // 2. Transactions Table
  db.run(`CREATE TABLE IF NOT EXISTS transactions (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    user_name TEXT,
    ticker TEXT,
    company TEXT,
    type TEXT NOT NULL,
    quantity INTEGER DEFAULT 0,
    price REAL DEFAULT 0.0,
    total REAL DEFAULT 0.0,
    fees REAL DEFAULT 0.0,
    tva REAL DEFAULT 0.0,
    grand_total REAL DEFAULT 0.0,
    status TEXT DEFAULT 'PENDING',
    payment_ref TEXT,
    payment_method TEXT,
    rejection_reason TEXT,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
  )`);

  // 3. Holdings Table
  db.run(`CREATE TABLE IF NOT EXISTS holdings (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    ticker TEXT NOT NULL,
    company TEXT,
    quantity INTEGER DEFAULT 0,
    avg_price REAL DEFAULT 0.0,
    current_price REAL DEFAULT 0.0,
    total_value REAL DEFAULT 0.0,
    gain_loss REAL DEFAULT 0.0,
    gain_loss_percent REAL DEFAULT 0.0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
  )`);

  // 4. Chat Messages Table
  db.run(`CREATE TABLE IF NOT EXISTS chat_messages (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    sender TEXT NOT NULL,
    sender_name TEXT,
    text TEXT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
  )`);

  // 5. Documents Table
  db.run(`CREATE TABLE IF NOT EXISTS documents (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    category TEXT,
    description TEXT,
    date TEXT,
    url TEXT
  )`);

  // Seed default Admin User if not existing
  db.get(`SELECT id FROM users WHERE email = ?`, ['admin@elephantbourse.ci'], (err, row) => {
    if (!row) {
      db.run(`INSERT INTO users (id, name, email, password, role, level, avatar, kyc) VALUES (?, ?, ?, ?, ?, ?, ?, ?)`,
        ['ADMIN-001', 'M. Cissé', 'admin@elephantbourse.ci', 'admin2024', 'admin', 4, 'MK', 'verified']
      );
      console.log('[SQLite] Seeded default Admin user (M. Cissé)');
    }
  });

  // Seed default Documents if table is empty
  db.get(`SELECT COUNT(*) as count FROM documents`, [], (err, row) => {
    if (row && row.count === 0) {
      const defaultDocs = [
        ['DOC-101', "Contrat d'Ouverture de Compte SGI.pdf", 'Contrat', "Contrat officiel d'ouverture de compte titres SGI BRVM à distance.", '19/07/2026', '/downloads/contrat_ouverture_sgi.pdf'],
        ['DOC-102', "Fiche d'Inscription KYC & Pièces.pdf", 'KYC', 'Formulaire de conformité et liste des pièces requises.', '19/07/2026', '/downloads/fiche_kyc_sgi.pdf'],
        ['DOC-103', 'Reglement General BRVM AMF-UMOA.pdf', 'Règlement', 'Règlement général des opérations boursières sur la BRVM.', '19/07/2026', '/downloads/reglement_general_brvm.pdf']
      ];
      defaultDocs.forEach(doc => {
        db.run(`INSERT INTO documents (id, name, category, description, date, url) VALUES (?, ?, ?, ?, ?, ?)`, doc);
      });
      console.log('[SQLite] Seeded default BRVM SGI documents');
    }
  });
});

module.exports = db;
