@echo off
chcp 65001 >nul
title Online Shop - Debug Start

cd /d "%~dp0"

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot

echo [DEBUG] Full startup log will be shown
echo [DEBUG] If stuck on Downloading..., it's downloading Maven dependencies (normal)
echo.

for /f "tokens=5" %%a in ('netstat -ano 2^>nul ^| findstr ":9090 " ^| findstr "LISTENING"') do (
    echo [INFO] Stopping existing process PID=%%a
    taskkill /F /PID %%a >nul 2>&1
)

if exist "target\online-shopping-platform-1.0.0.jar" (
    java -jar target\online-shopping-platform-1.0.0.jar --server.port=9090
) else (
    echo [INFO] First run, building...
    call mvnw.cmd spring-boot:run -DskipTests
)

echo.
echo Server stopped. Press any key to close.
pause >nul
