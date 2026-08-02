@echo off
title BAOU Finance - Lanceur Automatique Microservices v1.0.4
cls

set "ROOT_DIR=%~dp0"
set "PATH=C:\Users\ABOU CISSE\nodejs;C:\Users\ABOU CISSE\ngrok;C:\Users\ABOU CISSE\git\cmd;C:\Users\ABOU CISSE\gh\bin;%PATH%"

echo ========================================================
echo       BAOU FINANCE - ECOSYSTEME MICROSERVICES v1.0.4
echo ========================================================
echo.

echo Nettoyage des anciens processus Node en cours...
taskkill /F /IM node.exe >nul 2>&1

echo.
echo [1/4] Demarrage de l'Ecosysteme Microservices (Core: 3002 + REST API: 3001)...
start "BAOU Backend Microservices (Core:3002 + REST API:3001)" cmd /k "cd /d ""%ROOT_DIR%backend"" && node server.js"

echo Attente du demarrage (5 secondes)...
ping -n 6 127.0.0.1 >nul

echo.
echo [2/4] Verification et tests des microservices (23 tests au total)...
node "%ROOT_DIR%backend\test_services.js"
node "%ROOT_DIR%backend\test_wave_flow.js"

echo.
echo [3/4] Demarrage du Portail Web Administrateur (Port 3000)...
start "BAOU Admin Web" cmd /k "cd /d ""%ROOT_DIR%admin"" && npm install --silent && npm run dev"

echo.
echo [4/4] Demarrage du Tunnel NGROK pour l'application Mobile (Port 3001)...
start "BAOU Ngrok Tunnel" cmd /k "ngrok http 3001"

echo.
echo Ouverture du Portail Admin dans votre navigateur...
ping -n 5 127.0.0.1 >nul
start http://localhost:3000

echo.
echo ========================================================
echo  ACCES PORTAIL ADMIN WEB : http://localhost:3000
echo    Email       : admin@elephantbourse.ci
echo    Mot de passe: admin2024
echo.
echo  ARCHITECTURE MICROSERVICES :
echo    - Core Service (Moteur & DB) : http://localhost:3002
echo    - REST API Gateway           : http://localhost:3001
echo.
echo  PAIEMENT MARCHAND WAVE CI :
echo    - Lien marchand : https://pay.wave.com/m/M_ci_XRkfDq_9M8GP/c/ci/?src=p
echo    - Return Deep Link : baou://payment/success
echo.
echo  BASE DE DONNEES SQLITE LOCALE :
echo    Fichier : backend\data\baou_finance.db
echo    Uploads : backend\uploads\
echo.
echo  APPLICATION MOBILE ANDROID :
echo    1. Regardez la fenetre "BAOU Ngrok Tunnel"
echo    2. Copiez l'adresse  https://xxxx.ngrok-free.app
echo    3. Allez dans Parametres (engrenage) de l'app mobile
echo    4. Collez l'adresse et ajoutez /api/ a la fin
echo       ex: https://xxxx.ngrok-free.app/api/
echo    5. Cliquez sur Enregistrer
echo.
echo  CORRECTIONS ET AMELIORATIONS v1.0.4 :
echo    - Flux depot Wave corrige : portefeuille credite APRES paiement [OK]
echo    - DepositScreen simplifie : Wave et Orange Money en 2 clics [OK]
echo    - Routes manquantes ajoutees : update-profile, upload-doc, chat [OK]
echo    - Inscription corrigee : firstName et name acceptes [OK]
echo    - Bouton "J'ai paye" + annulation depot Wave possibles [OK]
echo ========================================================
echo.
pause
