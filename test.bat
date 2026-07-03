@echo off
chcp 65001 >nul
title Online Shop - Unit Tests

cd /d "%~dp0"

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot

echo.
echo    ========================================
echo    Running Unit Tests
echo    ========================================
echo.

echo [INFO] Close running server first to avoid DB conflict
echo [INFO] Running JUnit tests...
echo.

call mvnw.cmd test

echo.
echo [INFO] Reports: target\surefire-reports\
echo [INFO] Press any key to exit...
pause >nul
