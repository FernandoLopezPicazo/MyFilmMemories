@echo off
title Series Tracker - Lanzador
cd /d "%~dp0"

echo Iniciando backend (Spring Boot)...
start "Series Tracker - Backend" cmd /k "cd backend && java -jar target\seriestracker-0.0.1-SNAPSHOT.jar"

echo Esperando a que el backend arranque...
timeout /t 8 /nobreak >nul

echo Iniciando frontend (Angular)...
start "Series Tracker - Frontend" cmd /k "cd frontend && ng serve"

echo Esperando a que el frontend compile...
timeout /t 15 /nobreak >nul

echo Abriendo navegador...
start http://localhost:4200

echo.
echo Listo. Backend y frontend corriendo en sus propias ventanas.
echo Cierra esas ventanas para detener la aplicacion.
