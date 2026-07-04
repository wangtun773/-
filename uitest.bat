@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul
title Online Shop - UI Tests
cd /d "%~dp0"

set "JDK="

rem 1. Check system JAVA_HOME
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        call :check_ver "%JAVA_HOME%\bin\java.exe" "JAVA_HOME" "%JAVA_HOME%"
        if defined JDK goto :run
    )
)

rem 2. Check known JDK paths
for %%p in (
    "E:\jdk-17.0.11.9-hotspot"
    "C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"
    "C:\Program Files\Java\jdk-17"
) do (
    if exist "%%~p\bin\java.exe" (
        call :check_ver "%%~p\bin\java.exe" "%%~p" "%%~p"
        if defined JDK goto :run
    )
)

echo [ERROR] JDK 17 or higher not found!
pause
goto :eof

:check_ver
"%~1" -version >nul 2>&1
if errorlevel 1 goto :eof
for /f "tokens=1,2,3*" %%a in ('"%~1" -version 2^>^&1') do (
    if "%%b"=="version" if not defined VER set "VER=%%~c"
)
if not defined VER goto :eof
for /f "tokens=1 delims=." %%m in ("%VER%") do set "MAJOR=%%m"
if %MAJOR% geq 17 (
    set "JDK=%~3"
    echo [INFO] Using JDK %VER% from %~2
) else (
    echo [SKIP]  %~2 has Java %VER% ^(needs 17+^)
)
goto :eof

:run
set "JAVA_HOME=%JDK%"

echo.
echo    ========================================
echo    Running Selenium UI Tests
echo    ========================================
echo.
echo [INFO] Using HtmlUnit headless browser - no Chrome/chromedriver required
echo [INFO] Please start the server first (run.bat)
echo.

call mvnw.cmd test -Dtest="SeleniumUITest,AdminSeleniumUITest"

echo.
echo [INFO] Press any key to exit...
pause >nul
endlocal
