// ============================================================
//  BAOU Finance — Route Auth
//  Corrections : bcrypt, JWT, validation upload, require() centralisés
// ============================================================

require('dotenv').config();
const express = require('express');
const router = express.Router();
const bcrypt = require('bcryptjs');
const path = require('path');
const fs = require('fs');
const { users, documents } = require('../data/store');
const { generateToken, requireAuth } = require('../middleware/auth');

// Types MIME autorisés pour les uploads de documents
const ALLOWED_MIME_TYPES = [
  'image/jpeg', 'image/jpg', 'image/png', 'image/webp',
  'application/pdf'
];
// Taille max : 10 Mo en base64 (~7.5 Mo fichier réel)
const MAX_BASE64_SIZE = 10 * 1024 * 1024;

// POST /api/auth/register
router.post('/register', async (req, res) => {
  const { email, password, firstName } = req.body;

  if (!email || !password || !firstName) {
    return res.status(400).json({ error: 'Prénom, email et mot de passe requis.' });
  }
  if (password.length < 6) {
    return res.status(400).json({ error: 'Le mot de passe doit contenir au moins 6 caractères.' });
  }

  const existingUser = users.find(u => u.email === email);
  if (existingUser) {
    return res.status(400).json({ error: 'Cet e-mail est déjà utilisé.' });
  }

  // ✅ Hachage bcrypt du mot de passe
  const hashedPassword = await bcrypt.hash(password, 10);

  const newUser = {
    id: `EB-CI-${Math.floor(10000 + Math.random() * 90000)}`,
    name: firstName,
    email: email,
    password: hashedPassword,
    role: 'client',
    type: 'Standard',
    kyc: 'pending',
    balance: 0.0,
    joinedAt: new Date().toISOString().split('T')[0],
    avatar: firstName.substring(0, 2).toUpperCase()
  };

  users.push(newUser);

  const { saveUserToSupabase } = require('../data/store');
  saveUserToSupabase(newUser);

  res.status(201).json({
    success: true,
    message: 'Inscription réussie.',
    user: { id: newUser.id, name: newUser.name, email: newUser.email, kyc: newUser.kyc, balance: newUser.balance }
  });
});

// POST /api/auth/login
router.post('/login', async (req, res) => {
  const { email, password } = req.body;

  if (!email || !password) {
    return res.status(400).json({ error: 'Email et mot de passe requis.' });
  }

  const user = users.find(u => u.email === email);

  if (!user) {
    return res.status(404).json({ error: 'Utilisateur introuvable. Veuillez créer un compte.' });
  }

  // ✅ Comparaison bcrypt (compatible mot de passe en clair legacy pour l'admin seedé)
  let passwordMatch = false;
  if (user.password && user.password.startsWith('$2')) {
    // Mot de passe haché — comparer avec bcrypt
    passwordMatch = await bcrypt.compare(password, user.password);
  } else {
    // Mot de passe legacy en clair (admin initial) — comparer directement puis migrer
    passwordMatch = (user.password === password);
    if (passwordMatch) {
      // Migration automatique vers bcrypt
      user.password = await bcrypt.hash(password, 10);
      const { saveUserToSupabase } = require('../data/store');
      saveUserToSupabase(user);
    }
  }

  if (!passwordMatch) {
    return res.status(401).json({ error: 'Identifiants incorrects.' });
  }

  // ✅ Générer un vrai JWT signé
  const token = generateToken(user);

  const { password: _, ...userSafe } = user;
  res.json({
    success: true,
    token,
    user: userSafe,
    message: `Bienvenue, ${user.name} !`,
  });
});

// POST /api/auth/logout
router.post('/logout', (req, res) => {
  // Avec JWT, la révocation côté serveur n'est pas nécessaire
  // Le client doit simplement supprimer le token local
  res.json({ success: true, message: 'Déconnexion réussie.' });
});

// GET /api/auth/me — Vérifier le token courant
router.get('/me', requireAuth, (req, res) => {
  const user = users.find(u => u.id === req.session.userId);
  if (!user) return res.status(404).json({ error: 'Utilisateur introuvable.' });

  const { password: _, ...userSafe } = user;
  res.json({ success: true, user: userSafe });
});

// POST /api/auth/update-profile
router.post('/update-profile', requireAuth, (req, res) => {
  const user = users.find(u => u.id === req.session.userId);
  if (!user) return res.status(404).json({ error: 'Utilisateur introuvable.' });

  const { firstName, lastName, kycStatus, whatsapp, birthDate, profession, residence } = req.body;
  if (firstName && lastName) user.name = `${firstName} ${lastName}`;
  if (kycStatus) user.kyc = kycStatus;
  if (whatsapp) user.whatsapp = whatsapp;
  if (birthDate) user.birthDate = birthDate;
  if (profession) user.profession = profession;
  if (residence) user.residence = residence;
  user.identityDocStatus = 'Présent (CNI / Passeport)';
  user.proofOfAddressStatus = 'Présent (Facture CIE / SODECI)';
  user.signatureStatus = 'Contrat SGI Signé Numériquement';

  const { saveUserToSupabase } = require('../data/store');
  saveUserToSupabase(user);

  const { password: _, ...userSafe } = user;
  res.json({ success: true, user: userSafe });
});

// POST /api/auth/upload-document
router.post('/upload-document', requireAuth, (req, res) => {
  const user = users.find(u => u.id === req.session.userId);
  if (!user) return res.status(404).json({ error: 'Utilisateur introuvable.' });

  const { fileName, fileType, fileBase64, docType } = req.body;

  if (!fileBase64 || !fileName) {
    return res.status(400).json({ error: 'Fichier ou nom de fichier manquant.' });
  }

  // ✅ Validation du type MIME
  if (!fileType || !ALLOWED_MIME_TYPES.includes(fileType)) {
    return res.status(400).json({
      error: `Type de fichier non autorisé : "${fileType}". Formats acceptés : JPEG, PNG, WEBP, PDF.`
    });
  }

  // ✅ Validation de la taille (base64)
  if (fileBase64.length > MAX_BASE64_SIZE) {
    return res.status(400).json({ error: 'Fichier trop volumineux. Maximum : 10 Mo.' });
  }

  try {
    const uploadsDir = path.join(__dirname, '../uploads');
    if (!fs.existsSync(uploadsDir)) {
      fs.mkdirSync(uploadsDir, { recursive: true });
    }

    // ✅ Nom de fichier sécurisé
    const extension = fileType.split('/')[1].replace('jpeg', 'jpg');
    const safeFileName = `${Date.now()}_${req.session.userId}_${docType}.${extension}`;
    const filePath = path.join(uploadsDir, safeFileName);

    const base64Data = fileBase64.replace(/^data:[^;]+;base64,/, '');
    const buffer = Buffer.from(base64Data, 'base64');
    fs.writeFileSync(filePath, buffer);

    const publicUrl = `/uploads/${safeFileName}`;

    if (docType === 'cni_recto' || docType === 'cni') {
      user.cniRectoUrl = publicUrl;
      user.identityDocStatus = 'CNI (Recto & Verso) Scanné ✅';
    } else if (docType === 'cni_verso') {
      user.cniVersoUrl = publicUrl;
    } else if (docType === 'selfie') {
      user.selfieUrl = publicUrl;
      user.selfiePhotoStatus = 'Selfie Visage Validé ✅';
    } else if (docType === 'cie_sodeci' || docType === 'proof_address') {
      user.proofAddressUrl = publicUrl;
      user.proofOfAddressStatus = 'Facture CIE/SODECI Chargée ✅';
    } else if (docType === 'contract' || docType === 'contract_sgi') {
      user.contractUrl = publicUrl;
      user.signatureStatus = 'Contrat SGI Signé Numériquement ✅';
    }

    const { saveUserToSupabase } = require('../data/store');
    saveUserToSupabase(user);

    const docObj = {
      id: `DOC-${Date.now()}`,
      name: fileName,
      category: docType || 'Document Client',
      description: `Document téléversé par ${user.name}`,
      date: new Date().toLocaleDateString('fr-FR'),
      url: publicUrl
    };
    documents.push(docObj);

    res.json({ success: true, message: 'Fichier téléversé avec succès !', url: publicUrl, doc: docObj });
  } catch (err) {
    console.error('[Upload Error]', err);
    res.status(500).json({ error: 'Erreur lors de la sauvegarde du fichier.' });
  }
});

// POST /api/auth/support
router.post('/support', requireAuth, (req, res) => {
  const user = users.find(u => u.id === req.session.userId);
  if (!user) return res.status(404).json({ error: 'Utilisateur introuvable.' });

  const { subject, message } = req.body;
  if (!message) {
    return res.status(400).json({ error: 'Le contenu du message est requis.' });
  }

  const { tickets } = require('../data/store');
  const newTicket = {
    id: `TKT-${Math.floor(1000 + Math.random() * 9000)}`,
    clientName: user.name,
    clientId: user.email,
    subject: subject || 'Message de support',
    message: message,
    status: 'OUVERT',
    dateString: 'Aujourd\'hui, ' + new Date().toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })
  };

  tickets.unshift(newTicket);
  res.status(201).json({ success: true, ticket: newTicket });
});

// GET /api/auth/support
router.get('/support', requireAuth, (req, res) => {
  const user = users.find(u => u.id === req.session.userId);
  if (!user) return res.status(404).json({ error: 'Utilisateur introuvable.' });

  const { tickets } = require('../data/store');
  const userTickets = tickets.filter(t => t.clientId === user.email);
  res.json({ success: true, data: userTickets });
});

// POST /api/auth/chat
router.post('/chat', requireAuth, (req, res) => {
  const user = users.find(u => u.id === req.session.userId);
  if (!user) return res.status(404).json({ error: 'Utilisateur introuvable.' });

  const { text } = req.body;
  if (!text || !text.trim()) return res.status(400).json({ error: 'Message vide.' });

  const { chatMessages } = require('../data/store');
  const msg = {
    id: `MSG-${Date.now()}`,
    userId: user.id,
    userEmail: user.email,
    userName: user.name,
    sender: 'CLIENT',
    text: text.trim(),
    timestamp: new Date().toISOString(),
    time: new Date().toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })
  };

  if (!chatMessages[user.id]) chatMessages[user.id] = [];
  chatMessages[user.id].push(msg);
  res.status(201).json({ success: true, data: msg });
});

// GET /api/auth/chat
router.get('/chat', requireAuth, (req, res) => {
  const { chatMessages } = require('../data/store');
  const messages = chatMessages[req.session.userId] || [];
  res.json({ success: true, data: messages });
});

module.exports = { router };
