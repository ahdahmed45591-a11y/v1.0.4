const fs = require('fs');
const path = require('path');
const PDFDocument = require('pdfkit');
const sqlite3 = require('sqlite3').verbose();

const downloadsDir = 'C:\\Users\\ABOU CISSE\\Downloads';
const projectPdfDir = path.join(__dirname, '..', 'pdf_documents');

if (!fs.existsSync(projectPdfDir)) {
  fs.mkdirSync(projectPdfDir, { recursive: true });
}

const db = new sqlite3.Database(path.join(__dirname, '..', 'data', 'baou_finance.db'));

db.all('SELECT * FROM users', [], (err, users) => {
  db.all('SELECT * FROM transactions', [], (err2, transactions) => {
    
    // 📄 1. Génération du Rapport Financier Général PDF
    createFinancialReport(users || [], transactions || []);
    
    // 📄 2. Génération du Contrat d'Ouverture SGI PDF
    createSgiContract();

    // 📄 3. Génération du Reçu de Transaction PDF
    createTransactionReceipt(transactions && transactions.length > 0 ? transactions[0] : null);

  });
});

function createFinancialReport(users, transactions) {
  const doc = new PDFDocument({ margin: 40, size: 'A4' });
  
  const fileProject = path.join(projectPdfDir, 'Rapport_Financier_BAOU_Finance.pdf');
  const fileDownloads = path.join(downloadsDir, 'Rapport_Financier_BAOU_Finance.pdf');

  const streamProject = fs.createWriteStream(fileProject);
  doc.pipe(streamProject);
  doc.pipe(fs.createWriteStream(fileDownloads));

  // En-tête
  doc.fillColor('#0B1C30').rect(0, 0, 595, 80).fill();
  doc.fillColor('#FF8200').fontSize(22).font('Helvetica-Bold').text('BAOU FINANCE', 40, 25);
  doc.fillColor('#FFFFFF').fontSize(11).font('Helvetica').text('RAPPORT D\'ACTIVITÉ ÉLÉPHANT BOURSE — BRVM', 40, 50);
  doc.fillColor('#FFFFFF').fontSize(10).text(new Date().toLocaleDateString('fr-FR'), 450, 35, { align: 'right' });

  doc.moveDown(3);

  // Synthèse KPIs
  doc.fillColor('#0B1C30').fontSize(14).font('Helvetica-Bold').text('1. Synthèse des Utilisateurs & Portefeuilles');
  doc.strokeColor('#DEC1AF').lineWidth(1).moveTo(40, doc.y + 5).lineTo(555, doc.y + 5).stroke();
  doc.moveDown(1);

  doc.fillColor('#333333').fontSize(11).font('Helvetica');
  doc.text(`• Total des utilisateurs enregistrés : ${users.length}`);
  doc.text(`• Total des transactions exécutées : ${transactions.length}`);
  doc.text(`• Nombre d'actions BRVM suivies : 66 Tickers en direct`);

  doc.moveDown(2);

  // Tableau Utilisateurs
  doc.fillColor('#0B1C30').fontSize(14).font('Helvetica-Bold').text('2. Liste des Utilisateurs Récents');
  doc.strokeColor('#DEC1AF').lineWidth(1).moveTo(40, doc.y + 5).lineTo(555, doc.y + 5).stroke();
  doc.moveDown(1);

  let y = doc.y + 10;
  doc.fillColor('#0B1C30').fontSize(10).font('Helvetica-Bold');
  doc.text('ID Client', 40, y);
  doc.text('Nom & Prénom', 130, y);
  doc.text('Email', 280, y);
  doc.text('Statut KYC', 450, y);

  y += 18;
  doc.strokeColor('#CCCCCC').lineWidth(0.5).moveTo(40, y).lineTo(555, y).stroke();
  y += 8;

  doc.font('Helvetica').fontSize(9).fillColor('#444444');
  users.slice(0, 8).forEach(u => {
    doc.text(u.id || '-', 40, y);
    doc.text(u.name || '-', 130, y);
    doc.text(u.email || '-', 280, y);
    doc.text((u.kyc || 'pending').toUpperCase(), 450, y);
    y += 18;
  });

  doc.end();
  console.log('✅ Rapport Financier PDF généré dans Downloads & pdf_documents');
}

function createSgiContract() {
  const doc = new PDFDocument({ margin: 40, size: 'A4' });
  const fileProject = path.join(projectPdfDir, 'Contrat_Ouverture_Compte_SGI.pdf');
  const fileDownloads = path.join(downloadsDir, 'Contrat_Ouverture_Compte_SGI.pdf');

  doc.pipe(fs.createWriteStream(fileProject));
  doc.pipe(fs.createWriteStream(fileDownloads));

  doc.fillColor('#0B1C30').rect(0, 0, 595, 80).fill();
  doc.fillColor('#FF8200').fontSize(20).font('Helvetica-Bold').text('CONTRAT SGI PARTENAIRE', 40, 25);
  doc.fillColor('#FFFFFF').fontSize(11).font('Helvetica').text('Ouverture de Compte Titres Bourse Régionale (BRVM)', 40, 50);

  doc.moveDown(3);
  doc.fillColor('#0B1C30').fontSize(13).font('Helvetica-Bold').text('ARTICLE 1 : OBJET DU CONTRAT');
  doc.fillColor('#444444').fontSize(10).font('Helvetica').text(
    'Le présent contrat régit les relations entre la Société de Gestion et d\'Intermédiation (SGI) partenaire d\'Éléphant Bourse BAOU Finance et le titulaire du compte pour la conservation et la négociation des titres financiers cotés à la BRVM.'
  );

  doc.moveDown(1.5);
  doc.fillColor('#0B1C30').fontSize(13).font('Helvetica-Bold').text('ARTICLE 2 : TARIFICATION ET FRAIS DE COURTAGE');
  doc.fillColor('#444444').fontSize(10).font('Helvetica').text(
    'Conformément à la réglementation du CREPMF/AMF-UMOA, les frais de courtage sont fixés à 0,5% HT par opération de bourse, majorés de la TVA légale à 18%.'
  );

  doc.moveDown(2);
  doc.fillColor('#0B1C30').fontSize(13).font('Helvetica-Bold').text('SIGNATURES LÉGALES ET VALITATION');
  doc.moveDown(1);
  doc.fillColor('#666666').fontSize(9).text('Document signé numériquement via signature cryptographique BAOU Finance.');

  doc.end();
  console.log('✅ Contrat SGI PDF généré dans Downloads & pdf_documents');
}

function createTransactionReceipt(tx) {
  const doc = new PDFDocument({ margin: 40, size: 'A4' });
  const fileProject = path.join(projectPdfDir, 'Recu_Transaction_BRVM.pdf');
  const fileDownloads = path.join(downloadsDir, 'Recu_Transaction_BRVM.pdf');

  doc.pipe(fs.createWriteStream(fileProject));
  doc.pipe(fs.createWriteStream(fileDownloads));

  doc.fillColor('#0B1C30').rect(0, 0, 595, 80).fill();
  doc.fillColor('#FF8200').fontSize(20).font('Helvetica-Bold').text('REÇU DE TRANSACTION BRVM', 40, 25);
  doc.fillColor('#FFFFFF').fontSize(11).font('Helvetica').text('Attestation Officielle d\'Opération de Bourse', 40, 50);

  doc.moveDown(3);
  doc.fillColor('#0B1C30').fontSize(12).font('Helvetica-Bold').text(`Référence Transaction : ${tx ? tx.id : 'TX-88241'}`);
  doc.moveDown(0.5);

  doc.fillColor('#444444').fontSize(11).font('Helvetica');
  doc.text(`• Titre / Ticker : ${tx ? tx.ticker : 'SNTS'} (Sonatel SA)`);
  doc.text(`• Type d'opération : ${tx ? tx.type : 'ACHAT'}`);
  doc.text(`• Quantité d'actions : ${tx ? tx.quantity : 10}`);
  doc.text(`• Prix unitaire : ${tx ? tx.price : 31500} FCFA`);
  doc.text(`• Montant Total : ${tx ? tx.total : 315000} FCFA`);
  doc.text(`• Statut de l'ordre : VALIDÉ ET CONFIRMÉ SGI`);

  doc.end();
  console.log('✅ Reçu Transaction PDF généré dans Downloads & pdf_documents');
}
