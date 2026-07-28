@echo off
title BAOU Finance - Lanceur Automatique SQLite v1.0.4
cls

set "ROOT_DIR=%~dp0"
set "PATH=C:\Users\ABOU CISSE\nodejs;C:\Users\ABOU CISSE\ngrok;C:\Users\ABOU CISSE\git\cmd;C:\Users\ABOU CISSE\gh\bin;%PATH%"


echo ========================================================
echo       BAOU FINANCE - ECOSYSTEME LOCAL SQLITE v1.0.4
echo ========================================================
echo.

echo [1/3] Demarrage du Backend Node.js et Base SQLite (Port 3001)...
start "BAOU Backend + SQLite" cmd /k "cd /d ""%ROOT_DIR%backend"" && npm install --silent && node server.js"

echo Attente du demarrage du backend...
timeout /t 4 >nul

echo [2/3] Demarrage du Portail Web Administrateur (Port 3000)...
start "BAOU Admin Web" cmd /k "cd /d ""%ROOT_DIR%admin"" && npm install --silent && npm run dev"

echo [3/3] Demarrage du Tunnel NGROK pour le telephone Android (Port 3001)...
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
echo  BACKEND API   : http://localhost:3001
echo.
echo  BASE DE DONNEES SQLITE LOCALE :
echo    Fichier : backend\data\baou_finance.db
echo    Uploads : backend\uploads\  (CNI, Selfie, CIE/SODECI)
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
echo    - Upload photos (CNI, Selfie, CIE/SODECI) vers admin [OK]
echo    - Affichage et telechargement des documents dans l'admin [OK]
echo    - Messagerie bidirectionnelle Admin <-> Client [OK]
echo    - Telechargement contrat PDF sur telephone [OK]
echo ========================================================
echo.
pause
