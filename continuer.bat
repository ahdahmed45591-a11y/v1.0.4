@echo off
title BAOU Finance - Script de Continuation v1.0.4
chcp 65001 >nul 2>&1
cls

set "ROOT_DIR=%~dp0"
set "PATH=C:\Users\ABOU CISSE\nodejs;C:\Users\ABOU CISSE\ngrok;C:\Users\ABOU CISSE\git\cmd;C:\Users\ABOU CISSE\gh\bin;%PATH%"

echo.
echo  ╔══════════════════════════════════════════════════════╗
echo  ║       BAOU FINANCE — SCRIPT DE CONTINUATION          ║
echo  ║              Microservices v1.0.4                     ║
echo  ╚══════════════════════════════════════════════════════╝
echo.

REM ── ÉTAPE 1 : PUSH GITHUB ────────────────────────────────────
echo [ETAPE 1/5]  Push sur GitHub...
echo.
cd /d "%ROOT_DIR%"
git push origin main
if %ERRORLEVEL% neq 0 (
    echo  [!] Push echoue. Verification de la connexion internet...
    echo  [!] Essayez manuellement : git push origin main
) else (
    echo  [OK] Code mis a jour sur GitHub !
)
echo.

REM ── ÉTAPE 2 : NETTOYAGE DES ANCIENS PROCESSUS ────────────────
echo [ETAPE 2/5]  Arret des anciens processus Node...
taskkill /F /IM node.exe >nul 2>&1
echo  [OK] Ports liberes (3001, 3002).
echo.

REM ── ÉTAPE 3 : DEMARRAGE DES MICROSERVICES ────────────────────
echo [ETAPE 3/5]  Demarrage des microservices backend...
echo.
echo  - Core Service   : Port 3002
echo  - REST API       : Port 3001
echo.
start "BAOU Core (3002) + API (3001)" cmd /k "cd /d ""%ROOT_DIR%backend"" && node server.js"
echo  [OK] Serveurs lances dans une nouvelle fenetre.
echo.

echo Attente du demarrage (5 secondes)...
timeout /t 5 >nul

REM ── ÉTAPE 4 : TESTS AUTOMATIQUES ─────────────────────────────
echo [ETAPE 4/5]  Execution des tests automatiques (8 tests)...
echo.
node "%ROOT_DIR%backend\test_services.js"
echo.

REM ── ÉTAPE 5 : NGROK + ADMIN + RECAP ──────────────────────────
echo [ETAPE 5/5]  Demarrage Admin Web et Tunnel Ngrok...
echo.
start "BAOU Admin Web (Port 3000)" cmd /k "cd /d ""%ROOT_DIR%admin"" && npm install --silent && npm run dev"
start "BAOU Ngrok Tunnel" cmd /k "ngrok http 3001"

timeout /t 4 >nul
start http://localhost:3000

echo.
echo  ╔══════════════════════════════════════════════════════╗
echo  ║                 TOUT EST PRET !                      ║
echo  ╠══════════════════════════════════════════════════════╣
echo  ║  Admin Web   : http://localhost:3000                 ║
echo  ║  REST API    : http://localhost:3001                 ║
echo  ║  Core        : http://localhost:3002                 ║
echo  ╠══════════════════════════════════════════════════════╣
echo  ║  COMPTE ADMIN :                                      ║
echo  ║    Email    : admin@elephantbourse.ci                ║
echo  ║    Password : admin2024                              ║
echo  ╠══════════════════════════════════════════════════════╣
echo  ║  APP MOBILE (Android) :                              ║
echo  ║   1. Regardez la fenetre "BAOU Ngrok Tunnel"         ║
echo  ║   2. Copiez  https://xxxx.ngrok-free.app             ║
echo  ║   3. App mobile -> Reglages (engrenage)              ║
echo  ║   4. Collez + ajoutez /api/ a la fin                 ║
echo  ║      Ex: https://xxxx.ngrok-free.app/api/            ║
echo  ╠══════════════════════════════════════════════════════╣
echo  ║  FLUX DEPOT WAVE (CORRECTED) :                       ║
echo  ║   1. Ouvrez l'app mobile                             ║
echo  ║   2. Dashboard -> Deposer -> Saisissez le montant    ║
echo  ║   3. Selectionnez "Wave CI"                          ║
echo  ║   4. Cliquez "Payer avec Wave"                       ║
echo  ║   5. Payez dans l'app Wave                           ║
echo  ║   6. Revenez sur BAOU -> cliquez "J'ai paye"         ║
echo  ║   7. Le portefeuille est credite !                   ║
echo  ╠══════════════════════════════════════════════════════╣
echo  ║  CORRECTIONS v1.0.4 :                                ║
echo  ║   [OK] DepositScreen simplifie (Wave en 1 clic)      ║
echo  ║   [OK] Inscription corrigee (firstName + name)       ║
echo  ║   [OK] Routes API manquantes ajoutees                ║
echo  ║   [OK] Upload document KYC -> Core service           ║
echo  ║   [OK] Chat client/admin via gateway                 ║
echo  ╚══════════════════════════════════════════════════════╝
echo.
pause
