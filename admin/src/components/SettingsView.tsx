import React, { useState } from 'react';
import { 
  Settings, 
  Shield, 
  Bell, 
  Database, 
  CheckCircle2, 
  Save, 
  AlertTriangle,
  Lock,
  Smartphone
} from 'lucide-react';

interface SettingsViewProps {
  onSave: () => void;
}

export const SettingsView: React.FC<SettingsViewProps> = ({ onSave }) => {
  // Configurable thresholds & preferences
  const [doubleValidationThreshold, setDoubleValidationThreshold] = useState('50000000');
  const [premiumSla, setPremiumSla] = useState('15');
  const [standardSla, setStandardSla] = useState('60');
  const [requireKycForWithdrawal, setRequireKycForWithdrawal] = useState(true);
  const [enableNotificationEmails, setEnableNotificationEmails] = useState(true);
  const [enableMobilePush, setEnableMobilePush] = useState(false);
  const [auditLogLevel, setAuditLogLevel] = useState('DETAILED');

  const handleFormSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSave();
  };

  return (
    <div className="space-y-6">
      
      {/* View Header */}
      <div>
        <h2 className="font-sans font-extrabold text-[24px] text-[#0b1c30] tracking-tight">
          Paramètres du Système
        </h2>
        <p className="font-sans text-[14px] text-[#574235]/80 mt-0.5">
          Gérez les seuils de validation réglementaires, configurez les alertes d'audit et surveillez l'infrastructure.
        </p>
      </div>

      <form onSubmit={handleFormSubmit} className="grid grid-cols-12 gap-6">
        
        {/* Left Column: Settings panels (Span 8) */}
        <div className="col-span-12 lg:col-span-8 space-y-6">
          
          {/* Rules & Thresholds Setting Card */}
          <div className="bg-white border border-[#dec1af]/30 rounded-xl p-6 hover:shadow-xs transition-all">
            <h3 className="font-sans font-bold text-[16px] text-[#0b1c30] flex items-center gap-2 mb-6 border-b border-[#dec1af]/15 pb-3">
              <Shield className="text-[#ff8200] w-5 h-5" />
              Règles d'Audit & Seuils Réglementaires
            </h3>

            <div className="space-y-5">
              {/* Threshold inputs */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-1.5">
                  <label className="block font-sans font-bold text-[11px] text-[#574235] uppercase">
                    Seuil de Double Validation (FCFA)
                  </label>
                  <div className="relative">
                    <input
                      type="number"
                      value={doubleValidationThreshold}
                      onChange={(e) => setDoubleValidationThreshold(e.target.value)}
                      className="w-full border border-[#dec1af]/45 rounded-lg px-3 py-2 text-[14px] font-sans text-[#0b1c30] focus:ring-1 focus:ring-[#ff8200] outline-none font-bold"
                    />
                    <span className="absolute right-3 top-1/2 -translate-y-1/2 text-[11px] font-sans font-bold text-[#574235]/70">
                      FCFA
                    </span>
                  </div>
                  <p className="text-[11px] text-[#574235]/65 font-sans mt-0.5">
                    Toute transaction dépassant ce montant nécessitera la validation de deux administrateurs de niveau supérieur.
                  </p>
                </div>

                <div className="space-y-1.5">
                  <label className="block font-sans font-bold text-[11px] text-[#574235] uppercase">
                    Niveau d'audit requis (Logs)
                  </label>
                  <select
                    value={auditLogLevel}
                    onChange={(e) => setAuditLogLevel(e.target.value)}
                    className="w-full bg-white border border-[#dec1af]/45 rounded-lg px-3 py-2 text-[14px] font-sans text-[#0b1c30] focus:ring-1 focus:ring-[#ff8200] outline-none"
                  >
                    <option value="MINIMAL">Minimal (Erreurs uniquement)</option>
                    <option value="STANDARD">Standard (Activités & Validations)</option>
                    <option value="DETAILED">Détaillé (Tous les clics administrateur)</option>
                  </select>
                  <p className="text-[11px] text-[#574235]/65 font-sans mt-0.5">
                    Configure le degré de finesse de l'enregistrement des actions d'audit pour la conformité.
                  </p>
                </div>
              </div>

              {/* KYC Checkbox Switch */}
              <div className="flex items-start gap-3 pt-3 border-t border-[#dec1af]/10">
                <input
                  type="checkbox"
                  id="requireKyc"
                  checked={requireKycForWithdrawal}
                  onChange={(e) => setRequireKycForWithdrawal(e.target.checked)}
                  className="w-4.5 h-4.5 text-[#ff8200] border-[#dec1af] rounded mt-0.5 focus:ring-[#ff8200]"
                />
                <div>
                  <label htmlFor="requireKyc" className="font-sans font-bold text-[13px] text-[#0b1c30] block cursor-pointer select-none">
                    Exiger obligatoirement le statut KYC vérifié pour les transactions
                  </label>
                  <p className="text-[11px] text-[#574235]/70 font-sans mt-0.5">
                    Bloque automatiquement la soumission de tout ordre d'achat ou demande de retrait si les pièces d'identité du client ne sont pas validées.
                  </p>
                </div>
              </div>
            </div>
          </div>

          {/* Service Level Agreement (SLA) Setup */}
          <div className="bg-white border border-[#dec1af]/30 rounded-xl p-6 hover:shadow-xs transition-all">
            <h3 className="font-sans font-bold text-[16px] text-[#0b1c30] flex items-center gap-2 mb-6 border-b border-[#dec1af]/15 pb-3">
              <Lock className="text-[#006d31] w-5 h-5" />
              Objectifs de Traitement Boursier (SLA)
            </h3>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="space-y-1.5">
                <label className="block font-sans font-bold text-[11px] text-[#574235] uppercase">
                  Délai Cible Comptes Premium (Minutes)
                </label>
                <input
                  type="number"
                  value={premiumSla}
                  onChange={(e) => setPremiumSla(e.target.value)}
                  className="w-full border border-[#dec1af]/45 rounded-lg px-3 py-2 text-[14px] font-sans text-[#0b1c30] focus:ring-1 focus:ring-[#ff8200] outline-none"
                />
                <p className="text-[11px] text-[#574235]/65 font-sans mt-0.5">
                  Délai maximum suggéré pour la validation des preuves de dépôts des portefeuilles Premium.
                </p>
              </div>

              <div className="space-y-1.5">
                <label className="block font-sans font-bold text-[11px] text-[#574235] uppercase">
                  Délai Cible Comptes Standard (Minutes)
                </label>
                <input
                  type="number"
                  value={standardSla}
                  onChange={(e) => setStandardSla(e.target.value)}
                  className="w-full border border-[#dec1af]/45 rounded-lg px-3 py-2 text-[14px] font-sans text-[#0b1c30] focus:ring-1 focus:ring-[#ff8200] outline-none"
                />
                <p className="text-[11px] text-[#574235]/65 font-sans mt-0.5">
                  Délai standard recommandé pour le traitement des dossiers de versement ordinaires.
                </p>
              </div>
            </div>
          </div>

          {/* Administrative Notification Toggles */}
          <div className="bg-white border border-[#dec1af]/30 rounded-xl p-6 hover:shadow-xs transition-all">
            <h3 className="font-sans font-bold text-[16px] text-[#0b1c30] flex items-center gap-2 mb-6 border-b border-[#dec1af]/15 pb-3">
              <Bell className="text-[#005db6] w-5 h-5" />
              Alertes & Notifications Administrateur
            </h3>

            <div className="space-y-4">
              <div className="flex items-start gap-3">
                <input
                  type="checkbox"
                  id="enableEmails"
                  checked={enableNotificationEmails}
                  onChange={(e) => setEnableNotificationEmails(e.target.checked)}
                  className="w-4.5 h-4.5 text-[#ff8200] border-[#dec1af] rounded mt-0.5 focus:ring-[#ff8200]"
                />
                <div>
                  <label htmlFor="enableEmails" className="font-sans font-bold text-[13px] text-[#0b1c30] block cursor-pointer">
                    M'envoyer une notification email pour chaque nouvelle preuve soumise
                  </label>
                  <p className="text-[11px] text-[#574235]/70 font-sans mt-0.5">
                    Recevez une alerte automatisée contenant les détails du reçu sur votre adresse professionnelle.
                  </p>
                </div>
              </div>

              <div className="flex items-start gap-3 pt-3 border-t border-[#dec1af]/10">
                <input
                  type="checkbox"
                  id="enablePush"
                  checked={enableMobilePush}
                  onChange={(e) => setEnableMobilePush(e.target.checked)}
                  className="w-4.5 h-4.5 text-[#ff8200] border-[#dec1af] rounded mt-0.5 focus:ring-[#ff8200]"
                />
                <div>
                  <label htmlFor="enablePush" className="font-sans font-bold text-[13px] text-[#0b1c30] block cursor-pointer">
                    Autoriser les notifications push sur mobile pour les montants &gt; 10M FCFA
                  </label>
                  <p className="text-[11px] text-[#574235]/70 font-sans mt-0.5">
                    Alertes d'applications directes pour les dépôts à volume important nécessitant des vérifications accélérées.
                  </p>
                </div>
              </div>
            </div>
          </div>

          {/* PDF Documents & Contracts Publishing Panel */}
          <div className="bg-white border border-[#dec1af]/30 rounded-xl p-6 space-y-5 shadow-xs">
            <div className="border-b border-[#dec1af]/20 pb-3 flex justify-between items-center">
              <div>
                <h3 className="font-sans font-bold text-[16px] text-[#0b1c30]">
                  📄 Publication de Documents & Contrats PDF sur l'App
                </h3>
                <p className="font-sans text-[12px] text-[#574235]/70 mt-0.5">
                  Publiez de nouveaux formulaires, contrats et règlements directement téléchargeables par vos clients.
                </p>
              </div>
            </div>

            {/* Document Creation Form */}
            <div className="bg-[#f8f9ff] p-4 rounded-xl border border-[#dec1af]/25 space-y-3 font-sans text-[12px]">
              <h4 className="font-bold text-[#ff8200] uppercase tracking-wider">Ajouter un Nouveau Document PDF</h4>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-bold text-[#0b1c30] mb-1">Titre du Document / PDF</label>
                  <input 
                    type="text" 
                    placeholder="ex: Contrat de Courtage 2026.pdf" 
                    id="newDocName"
                    className="w-full bg-white border border-[#dec1af]/40 rounded-lg p-2 font-medium"
                  />
                </div>
                <div>
                  <label className="block font-bold text-[#0b1c30] mb-1">Catégorie</label>
                  <select id="newDocCategory" className="w-full bg-white border border-[#dec1af]/40 rounded-lg p-2 font-medium">
                    <option value="Contrat">Contrat Officiel</option>
                    <option value="KYC">Formulaire KYC</option>
                    <option value="Règlement">Règlement BRVM</option>
                    <option value="Notice">Notice d'Information</option>
                  </select>
                </div>
              </div>
              <div>
                <label className="block font-bold text-[#0b1c30] mb-1">Description courte pour les clients</label>
                <input 
                  type="text" 
                  placeholder="Formulaire officiel d'ouverture de compte et contrat d'intermédiation." 
                  id="newDocDesc"
                  className="w-full bg-white border border-[#dec1af]/40 rounded-lg p-2 font-medium"
                />
              </div>
              <button
                type="button"
                onClick={() => {
                  const nameEl = document.getElementById('newDocName') as HTMLInputElement;
                  const categoryEl = document.getElementById('newDocCategory') as HTMLSelectElement;
                  const descEl = document.getElementById('newDocDesc') as HTMLInputElement;
                  if (!nameEl?.value) {
                    alert('Veuillez saisir un nom de document PDF.');
                    return;
                  }
                  alert(`✅ Document PDF "${nameEl.value}" publié avec succès sur l'application mobile client !`);
                  nameEl.value = '';
                  descEl.value = '';
                }}
                className="w-full py-2.5 bg-[#ff8200] hover:bg-[#e67400] text-white font-bold text-[13px] rounded-lg shadow-md transition-all flex items-center justify-center gap-2"
              >
                📥 Publier le Document PDF sur l'App Mobile
              </button>
            </div>
          </div>

        </div>

        {/* Right Column: Infrastructure Status & Submission Panel (Span 4) */}
        <div className="col-span-12 lg:col-span-4 space-y-6">
          
          {/* Action Trigger Card */}
          <div className="bg-white border border-[#dec1af]/30 rounded-xl p-5 shadow-xs">
            <h4 className="font-sans font-bold text-[15px] text-[#0b1c30] mb-3">Enregistrer les Modifications</h4>
            <p className="font-sans text-[12px] text-[#574235]/80 leading-relaxed mb-6">
              Assurez-vous de vérifier les impacts de ces seuils sur la fluidité opérationnelle avant de valider. Les managers de niveau inférieur seront immédiatement informés de tout changement réglementaire.
            </p>
            <button
              type="submit"
              className="w-full bg-[#ff8200] hover:bg-[#ff8200]/95 text-white py-3 rounded-lg font-sans font-bold text-[13px] flex items-center justify-center gap-2 hover:opacity-95 active:scale-95 transition-all shadow-sm shadow-[#ff8200]/20"
            >
              <Save className="w-4.5 h-4.5" />
              Sauvegarder les Paramètres
            </button>
          </div>

          {/* Infrastructure Health Status Card */}
          <div className="bg-white border border-[#dec1af]/30 rounded-xl p-5 space-y-4">
            <h4 className="font-sans font-bold text-[14px] text-[#0b1c30] flex items-center gap-1.5 border-b border-[#dec1af]/15 pb-2">
              <Database className="text-[#006d31] w-4.5 h-4.5" />
              État de l'Infrastructure
            </h4>

            <div className="space-y-3 font-sans text-[12px]">
              <div className="flex justify-between items-center">
                <span className="text-[#574235]/70 font-medium">Instance Database</span>
                <span className="text-[#006d31] font-bold flex items-center gap-1">
                  <CheckCircle2 className="w-4 h-4" /> En ligne
                </span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-[#574235]/70 font-medium">Latence API (BRVM)</span>
                <span className="font-mono font-bold text-[#0b1c30]">24ms (Excellent)</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-[#574235]/70 font-medium">Taille de Base de Données</span>
                <span className="font-mono font-bold text-[#0b1c30]">1.24 GB / 10 GB</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-[#574235]/70 font-medium">Sertion de Certificat SSL</span>
                <span className="text-[#006d31] font-bold">Valide (342 jours restants)</span>
              </div>
            </div>

            <div className="bg-amber-50 border border-amber-200/50 p-3 rounded-xl flex items-start gap-2 text-[11px] text-amber-800 leading-normal">
              <AlertTriangle className="w-4 h-4 shrink-0 mt-0.5 text-amber-600" />
              <p>Mise à jour de sécurité système prévue pour dimanche à 02:00 GMT. Les passerelles Mobile Money risquent de présenter des micro-latences d'environ 2 minutes.</p>
            </div>
          </div>

        </div>

      </form>

    </div>
  );
};
