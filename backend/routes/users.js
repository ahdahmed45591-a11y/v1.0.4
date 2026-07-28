// ============================================================
//  BAOU Finance — Route Users / Admin
//  Corrections : middleware centralisé, authentification cohérente
// ============================================================

const express = require('express');
const router = express.Router();
const { users, adminStats, saveUserToSupabase } = require('../data/store');
const { requireAuth, requireAdmin } = require('../middleware/auth');

// GET /api/admin/stats — KPIs du tableau de bord
router.get('/stats', requireAuth, requireAdmin, (req, res) => {
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

// GET /api/admin/users — Liste de tous les clients
router.get('/users', requireAuth, requireAdmin, (req, res) => {
  const { kyc, search } = req.query;
  let clients = users.filter(u => u.role === 'client');

  if (kyc) clients = clients.filter(u => u.kyc === kyc);
  if (search) {
    const s = search.toLowerCase();
    clients = clients.filter(u =>
      (u.name || '').toLowerCase().includes(s) ||
      (u.email || '').toLowerCase().includes(s) ||
      (u.id || '').toLowerCase().includes(s)
    );
  }

  // Masquer les mots de passe
  const safe = clients.map(({ password: _, ...u }) => u);
  res.json({ success: true, count: safe.length, data: safe });
});

// GET /api/admin/users/:id — Profil d'un client
router.get('/users/:id', requireAuth, requireAdmin, (req, res) => {
  const user = users.find(u => u.id === req.params.id);
  if (!user) return res.status(404).json({ error: 'Utilisateur introuvable.' });
  const { password: _, ...safe } = user;
  res.json({ success: true, data: safe });
});

// PATCH /api/admin/users/:id/kyc — Mettre à jour le statut KYC
router.patch('/users/:id/kyc', requireAuth, requireAdmin, (req, res) => {
  const { status } = req.body;
  if (!['verified', 'pending', 'rejected', 'suspended'].includes(status)) {
    return res.status(400).json({ error: 'Statut KYC invalide. Valeurs: verified, pending, rejected, suspended' });
  }

  const user = users.find(u => u.id === req.params.id);
  if (!user) return res.status(404).json({ error: 'Utilisateur introuvable.' });
  if (user.role === 'admin') return res.status(400).json({ error: 'Impossible de modifier un admin.' });

  const prevStatus = user.kyc;
  user.kyc = status;
  saveUserToSupabase(user);

  res.json({
    success: true,
    message: `KYC de ${user.name} mis à jour: ${prevStatus} → ${status}`,
    data: { id: user.id, name: user.name, kyc: user.kyc },
  });
});

// PATCH /api/admin/users/:id/suspend — Suspendre / réactiver
router.patch('/users/:id/suspend', requireAuth, requireAdmin, (req, res) => {
  const user = users.find(u => u.id === req.params.id);
  if (!user) return res.status(404).json({ error: 'Utilisateur introuvable.' });
  if (user.role === 'admin') return res.status(400).json({ error: 'Impossible de suspendre un admin.' });

  user.kyc = user.kyc === 'suspended' ? 'verified' : 'suspended';
  const action = user.kyc === 'suspended' ? 'suspendu' : 'réactivé';
  saveUserToSupabase(user);

  res.json({
    success: true,
    message: `Compte de ${user.name} ${action}.`,
    data: { id: user.id, name: user.name, kyc: user.kyc },
  });
});

// GET /api/admin/support — Admin: lister tous les tickets de support
router.get('/support', requireAuth, requireAdmin, (req, res) => {
  const { tickets } = require('../data/store');
  res.json({ success: true, count: tickets.length, data: tickets });
});

// PATCH /api/admin/support/:id/status — Admin: mettre à jour le statut d'un ticket
router.patch('/support/:id/status', requireAuth, requireAdmin, (req, res) => {
  const { status } = req.body;
  if (!['OUVERT', 'EN_COURS', 'RESOLU'].includes(status)) {
    return res.status(400).json({ error: 'Statut invalide. Valeurs: OUVERT, EN_COURS, RESOLU' });
  }

  const { tickets } = require('../data/store');
  const ticket = tickets.find(t => t.id === req.params.id);
  if (!ticket) return res.status(404).json({ error: 'Ticket introuvable.' });

  ticket.status = status;
  res.json({ success: true, message: `Ticket ${ticket.id} mis à jour: ${status}`, data: ticket });
});

// GET /api/admin/chat/:userId — Admin: historique de tchat d'un client
router.get('/chat/:userId', requireAuth, requireAdmin, (req, res) => {
  const { chatMessages } = require('../data/store');
  const messages = chatMessages[req.params.userId] || [];
  res.json({ success: true, data: messages });
});

// POST /api/admin/chat/:userId — Admin: répondre à un client
router.post('/chat/:userId', requireAuth, requireAdmin, (req, res) => {
  const { text } = req.body;
  if (!text || !text.trim()) return res.status(400).json({ error: 'Message vide.' });

  const { chatMessages } = require('../data/store');
  const userId = req.params.userId;
  const user = users.find(u => u.id === userId);

  const msg = {
    id: `MSG-${Date.now()}`,
    userId: userId,
    userName: user?.name || 'Client',
    sender: 'ADMIN',
    text: text.trim(),
    timestamp: new Date().toISOString(),
    time: new Date().toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })
  };

  if (!chatMessages[userId]) chatMessages[userId] = [];
  chatMessages[userId].push(msg);
  res.status(201).json({ success: true, data: msg });
});

// GET /api/admin/documents — Lister les documents (authentification requise)
router.get('/documents', requireAuth, (req, res) => {
  const { documents } = require('../data/store');
  res.json({ success: true, data: documents });
});

// POST /api/admin/documents — Admin: publier un document
router.post('/documents', requireAuth, requireAdmin, (req, res) => {
  const { name, category, description } = req.body;
  if (!name) return res.status(400).json({ error: 'Le nom du document est requis.' });

  const { documents } = require('../data/store');
  const safeName = name.replace(/[^a-zA-Z0-9àâäéèêëîïôùûüç\s._-]/gi, '_');
  const newDoc = {
    id: `DOC-${Date.now()}`,
    name: safeName.endsWith('.pdf') ? safeName : `${safeName}.pdf`,
    category: category || 'Contrat',
    description: description || 'Document PDF officiel.',
    date: new Date().toLocaleDateString('fr-FR'),
    url: `/downloads/${safeName.toLowerCase().replace(/[^a-z0-9]/g, '_')}.pdf`
  };

  documents.unshift(newDoc);
  res.status(201).json({ success: true, message: 'Document publié avec succès !', data: newDoc });
});

// DELETE /api/admin/documents/:id — Admin: supprimer un document
router.delete('/documents/:id', requireAuth, requireAdmin, (req, res) => {
  const { documents } = require('../data/store');
  const index = documents.findIndex(d => d.id === req.params.id);
  if (index === -1) return res.status(404).json({ error: 'Document introuvable.' });

  documents.splice(index, 1);
  res.json({ success: true, message: 'Document supprimé avec succès.' });
});

module.exports = router;
