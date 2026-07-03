@echo off
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot
cd /d %~dp0
echo.
echo =======================================
echo Starting server on http://localhost:9090
echo =======================================
echo.
echo Server logs will appear below.
echo Close THIS window to stop the server.
echo.
start http://localhost:9090/product/list
java -jar target\online-shopping-platform-1.0.0.jar --server.port=9090
echo Server stopped.
