// ============================================================
//  🐘 Éléphant Bourse — Orchestrateur Microservices (v1.0.4)
//  Lance simultanément :
//   1. Core Service & Database Engine (Port 3002)
//   2. REST API Gateway Publique (Port 3001)
// ============================================================

require('dotenv').config();

console.log('🚀 Démarrage de l\'écosystème microservices BAOU Finance...');
console.log('─────────────────────────────────────────────────────────');

// 1. Démarrer le Moteur Core (Port 3002)
const { app: coreApp, server: coreServer } = require('./core/server_core');

// 2. Démarrer la Gateway REST API (Port 3001) après 500ms
setTimeout(() => {
  const { app: apiApp, server: apiServer } = require('./api/server_api');
  console.log('✅ Écosystème découpé (Core:3002 <---> REST API:3001) 100% Opérationnel !');
}, 500);
