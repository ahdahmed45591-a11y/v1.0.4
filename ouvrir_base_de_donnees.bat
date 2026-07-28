@echo off
title BAOU Finance - Ouvreur de Base de Donnees SQLite
cls

echo ========================================================
echo       BAOU FINANCE - EXPLORATEUR SQLITE VISUEL
echo ========================================================
echo.
echo 1. Ouvrir la base de l'Application (utilisateurs, transactions, kyc...)
echo 2. Ouvrir la base Boursiere BRVM (cours des 66 actions, historique...)
echo.
set /p choix="Faites votre choix (1 ou 2) puis appuyez sur Entree: "

if "%choix%"=="2" (
    echo Opening BRVM Database...
    start "" "C:\Users\ABOU CISSE\sqlitebrowser\DB Browser for SQLite.exe" "%~dp0backend\brvm_data\brvm_database.db"
) else (
    echo Opening Application Database...
    start "" "C:\Users\ABOU CISSE\sqlitebrowser\DB Browser for SQLite.exe" "%~dp0backend\data\baou_finance.db"
)

exit
