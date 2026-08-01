@echo off
title BAOU Finance - Lanceur Automatique Microservices v1.0.4
cls

set "ROOT_DIR=%~dp0"
set "PATH=C:\Users\ABOU CISSE\nodejs;C:\Users\ABOU CISSE\ngrok;C:\Users\ABOU CISSE\git\cmd;C:\Users\ABOU CISSE\gh\bin;%PATH%"

echo ========================================================
echo       BAOU FINANCE - ECOSYSTEME MICROSERVICES v1.0.4
echo ========================================================
echo.

echo [1/3] Demarrage du Backend Microservices (Core: 3002 + REST API: 3001)...
start "BAOU Backend Microservices (Core:3002 + REST API:3001)" cmd /k "cd /d ""%ROOT_DIR%backend"" && npm install --silent && node server.js"

echo Attente de la mise en route des services Core (3002) et REST API (3001)...
timeout /t 5 >nul

echo [2/3] Demarrage du Portail Web Administrateur (Port 3000)...
start "BAOU Admin Web" cmd /k "cd /d ""%ROOT_DIR%admin"" && npm install --silent && npm run dev"

echo [3/3] Demarrage du Tunnel NGROK pour l'application Mobile (Port 3001)...
start "BAOU Ngrok Tunnel" cmd /k "ngrok http 3001"

echo.
echo Ouverture du Portail Admin dans votre navigateur...
timeout /t 5 >nul
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
echo  NOUVEAUTES v1.0.4 :
echo    - Ecosysteme decoupe Core:3002 <-> REST API:3001 [OK]
echo    - Redirection vers l'application Wave pour depot [OK]
echo    - Validation automatique deep link (baou://payment/success) [OK]
echo    - Colonne Solde Cash et Recharges dans l'Admin [OK]
echo ========================================================
echo.
pause
