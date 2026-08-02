@echo off
title BAOU Finance - Script de Continuation v1.0.4
cls

set "ROOT_DIR=%~dp0"
set "PATH=C:\Users\ABOU CISSE\nodejs;C:\Users\ABOU CISSE\ngrok;C:\Users\ABOU CISSE\git\cmd;C:\Users\ABOU CISSE\gh\bin;%PATH%"

echo ========================================================
echo       BAOU FINANCE - SCRIPT DE CONTINUATION v1.0.4
echo ========================================================
echo.

echo [1/5] Push sur GitHub...
cd /d "%ROOT_DIR%"
git push origin main

echo.
echo [2/5] Arret des anciens processus Node...
taskkill /F /IM node.exe >nul 2>&1

echo.
echo [3/5] Demarrage des microservices backend (Core: 3002 + REST API: 3001)...
start "BAOU Core (3002) + API (3001)" cmd /k "cd /d ""%ROOT_DIR%backend"" && node server.js"

echo Attente du demarrage (5 secondes)...
ping -n 6 127.0.0.1 >nul

echo.
echo [4/5] Execution des tests automatiques (8 tests)...
node "%ROOT_DIR%backend\test_services.js"

echo.
echo [5/5] Demarrage Admin Web (3000) et Tunnel Ngrok (3001)...
start "BAOU Admin Web (Port 3000)" cmd /k "cd /d ""%ROOT_DIR%admin"" && npm install --silent && npm run dev"
start "BAOU Ngrok Tunnel" cmd /k "ngrok http 3001"

ping -n 4 127.0.0.1 >nul
start http://localhost:3000

echo.
echo ========================================================
echo  ACCES PORTAIL ADMIN WEB : http://localhost:3000
echo    Email       : admin@elephantbourse.ci
echo    Mot de passe: admin2024
echo.
echo  ARCHITECTURE MICROSERVICES :
echo    - Core Service (3002) : http://localhost:3002
echo    - REST API Gateway (3001) : http://localhost:3001
echo.
echo  PAIEMENT WAVE CI (CORRIGE) :
echo    1. Dashboard mobile -> Deposer -> Saisissez le montant
echo    2. Choisissez "Wave CI" et cliquez "Payer avec Wave"
echo    3. Effectuez le paiement dans Wave
echo    4. Revenez et cliquez "J'ai paye"
echo ========================================================
echo.
pause
