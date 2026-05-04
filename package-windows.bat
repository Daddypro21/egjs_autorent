@echo off
REM ============================================================
REM  EGJS AutoRent — Packaging Windows (.exe)
REM  Prérequis : Java 17+, jpackage dans PATH
REM ============================================================

echo === EGJS AutoRent — Création installeur Windows ===
echo.

REM Étape 1 : Compilation et JAR
echo [1/3] Compilation du projet...
call gradlew.bat jar
if %errorlevel% neq 0 (
    echo ERREUR : Compilation échouée. Vérifiez les erreurs ci-dessus.
    exit /b 1
)
echo ✓ JAR créé dans build/libs/

REM Étape 2 : jpackage — installeur .exe avec Java embarqué
echo.
echo [2/3] Création de l'installeur Windows...
jpackage ^
  --input build/libs ^
  --name "EGJS AutoRent" ^
  --app-version "1.0.0" ^
  --vendor "EGJS" ^
  --description "Système de gestion d'agence de location de voitures - Brazzaville" ^
  --main-jar egjs-autorent-1.0.0.jar ^
  --main-class cg.egjs.autorent.app.MainApp ^
  --type exe ^
  --win-dir-chooser ^
  --win-menu ^
  --win-shortcut ^
  --win-shortcut-prompt ^
  --dest build/installer ^
  --java-options "--add-opens java.base/java.lang=ALL-UNNAMED"

if %errorlevel% neq 0 (
    echo ERREUR : jpackage échoué.
    exit /b 1
)

echo.
echo [3/3] Terminé !
echo ✓ Installeur créé : build/installer/EGJS AutoRent-1.0.0.exe
echo.
echo Double-cliquez sur le .exe pour installer l'application.
echo Java est inclus dans l'installeur — aucune installation préalable requise.
pause
