const fs = require('fs');
const path = require('path');

/**
 * Lit le fichier daily.csv d'un ticker et retourne les dernières informations de cours.
 * @param {string} ticker 
 * @returns {object|null}
 */
function getTickerData(ticker) {
  // Mapper ETI vers ETIT
  const folderName = ticker === 'ETI' ? 'ETIT' : ticker;
  const filePath = path.join(__dirname, '..', 'brvm_data', folderName, `${folderName}.daily.csv`);
  
  try {
    if (!fs.existsSync(filePath)) {
      console.warn(`[BRVM Data] Fichier introuvable pour le ticker: ${ticker} (${filePath})`);
      return null;
    }
    
    const content = fs.readFileSync(filePath, 'utf-8');
    const lines = content.split(/\r?\n/).filter(line => line.trim() !== '');
    
    if (lines.length < 2) return null; // Seulement le header
    
    // Le fichier contient : Date,Open,High,Low,Close,Volume
    // Les lignes sont par ordre chronologique, la dernière ligne est la plus récente
    const lastLine = lines[lines.length - 1];
    const prevLine = lines[lines.length - 2];
    
    const lastParts = lastLine.split(',');
    const prevParts = prevLine.split(',');
    
    const price = parseFloat(lastParts[4]); // Close
    const prevPrice = parseFloat(prevParts[4]); // Close précédent
    
    const change = prevPrice ? ((price - prevPrice) / prevPrice) * 100 : 0.0;
    const volume = parseInt(lastParts[5]);
    
    return {
      price,
      prevClose: prevPrice,
      change: parseFloat(change.toFixed(2)),
      volume,
      date: lastParts[0]
    };
  } catch (err) {
    console.error(`Erreur lors de la lecture des données pour ${ticker}:`, err.message);
    return null;
  }
}

module.exports = { getTickerData };
