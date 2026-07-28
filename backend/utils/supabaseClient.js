// ============================================================
//  BAOU Finance — Supabase Client
//  Utilise les variables d'environnement (pas de secrets hardcodés)
//  Supabase est OPTIONNEL — miroir cloud uniquement
// ============================================================

require('dotenv').config();

let supabase = null;

const SUPABASE_URL = process.env.SUPABASE_URL;
const SUPABASE_KEY = process.env.SUPABASE_KEY;

if (SUPABASE_URL && SUPABASE_KEY) {
  try {
    const { createClient } = require('@supabase/supabase-js');
    supabase = createClient(SUPABASE_URL, SUPABASE_KEY, {
      auth: { persistSession: false },
      // Désactiver le realtime — ce backend n'utilise que les opérations REST
      realtime: { enabled: false },
    });
    console.log('[Supabase] ✅ Client cloud initialisé (miroir optionnel).');
  } catch (e) {
    console.warn('[Supabase] ⚠️  Client désactivé (compatibilité Node 20):', e.message.split('\n')[0]);
    supabase = null;
  }
} else {
  console.warn('[Supabase] ⚠️  Variables SUPABASE_URL/KEY manquantes — miroir cloud désactivé.');
}

module.exports = { supabase };
