@echo off
chcp 65001 >nul
title Online Shop - UI Tests

cd /d "%~dp0"

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot

echo.
echo    ========================================
echo    Running Selenium UI Tests
echo    ========================================
echo.

echo [INFO] Requirements: Chrome browser + chromedriver in PATH
echo [INFO] Please start the server first (start.bat)
echo.

call mvnw.cmd test -Dtest=SeleniumUITest

echo.
echo [INFO] Press any key to exit...
pause >nul
