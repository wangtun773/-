@echo off
setlocal enabledelayedexpansion
cd /d %~dp0

set "FOUND_JAVA="
set "JAVA_BIN="

rem 1. system PATH
call :try_java "system PATH" "java"
if defined FOUND_JAVA ( set "JAVA_BIN=java" & goto :run )

rem 2. JAVA_HOME
if defined JAVA_HOME (
    call :try_java "JAVA_HOME" "%JAVA_HOME%\bin\java.exe"
    if defined FOUND_JAVA ( set "JAVA_BIN=%JAVA_HOME%\bin\java.exe" & goto :run )
)

rem 3. common JDK 17+ paths
for %%p in (
    "C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"
    "C:\Program Files\Java\jdk-17"
    "E:\jdk-17.0.11.9-hotspot"
) do (
    if exist "%%~p\bin\java.exe" (
        call :try_java "%%~p" "%%~p\bin\java.exe"
        if defined FOUND_JAVA (
            set "JAVA_BIN=%%~p\bin\java.exe"
            goto :run
        )
    )
)

rem all failed
echo [ERROR] JDK 17 or higher not found!
echo.
echo Please ensure JDK 17+ is installed and do one of the following:
echo   - Add JDK 17+ java to system PATH
echo   - Set JAVA_HOME environment variable to JDK 17+ path
echo   - Install JDK 17+ to one of these locations:
echo       C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot
echo       C:\Program Files\Java\jdk-17
echo       E:\jdk-17.0.11.9-hotspot
echo.
pause
goto :eof

rem =============================================
rem try_java <label> <java_path>
rem Directly uses %~1, %~2 (NOT %%variables%%)
rem because this func can be called from for/do blocks.
rem =============================================
:try_java
"%~2" -version >nul 2>&1
if errorlevel 1 goto :eof

set "VER="
for /f "tokens=1,2,3*" %%a in ('"%~2" -version 2^>^&1') do (
    if "%%b"=="version" if not defined VER set "VER=%%~c"
)

if not defined VER goto :eof

for /f "tokens=1 delims=." %%m in ("!VER!") do set "MAJOR=%%m"
if !MAJOR! geq 17 (
    set "FOUND_JAVA=1"
    echo [INFO] Using java from %~1 ^(version !VER!^)
) else (
    echo [SKIP]  %~1 has Java !VER! ^(needs 17+^)
)
goto :eof
rem =============================================

:run
echo.
echo =======================================
echo Starting server on http://localhost:9090
echo =======================================
echo.
echo Server logs will appear below.
echo Close THIS window to stop the server.
echo.
start http://localhost:9090/product/list
"!JAVA_BIN!" -jar target\online-shopping-platform-1.0.0.jar --server.port=9090
echo.
echo =======================================
echo Server stopped.
echo =======================================
pause
endlocal