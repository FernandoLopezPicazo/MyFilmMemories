# Script maestro: genera el instalador de escritorio de MyFilmMemories.
# Pipeline: build Angular -> copiar a static/ -> mvn package -> jlink -> icono -> electron-builder
#
# Uso: desde la raiz del repo, ejecutar  .\build-installer.ps1

$ErrorActionPreference = "Stop"
$root = $PSScriptRoot

function Paso($mensaje) {
    Write-Host ""
    Write-Host "==> $mensaje" -ForegroundColor Cyan
}

# 1. Build de Angular en modo "electron": sin login obligatorio, pero con
# las claves de Supabase configuradas para poder ofrecer la sincronizacion
# opcional con la nube (ver environment.electron.ts). Usar "production" a
# secas aqui volveria a exigir login en el escritorio (bug ya visto una vez).
Paso "1/6 Compilando frontend Angular (electron)"
Set-Location "$root\frontend"
if (-not (Test-Path "node_modules")) {
    npm install
    if (-not $?) { throw "npm install (frontend) fallo" }
}
npm run build -- --configuration electron
if (-not $?) { throw "ng build fallo" }

# 2. Copiar el build de Angular a los recursos estaticos del backend
Paso "2/6 Copiando frontend compilado a backend/src/main/resources/static"
$staticDir = "$root\backend\src\main\resources\static"
if (Test-Path $staticDir) {
    Remove-Item -Recurse -Force "$staticDir\*" -ErrorAction SilentlyContinue
} else {
    New-Item -ItemType Directory -Force -Path $staticDir | Out-Null
}
Copy-Item "$root\frontend\dist\frontend\*" $staticDir -Recurse -Force

# 3. Compilar el backend (el JAR incluye ya el frontend embebido)
Paso "3/6 Compilando backend (mvn clean package)"
Set-Location "$root\backend"
mvn clean package -DskipTests
if (-not $?) { throw "mvn package fallo" }

# 4. Generar el runtime Java portable con jlink (solo si no existe ya)
Paso "4/6 Generando runtime Java portable (jlink)"
$runtimeDir = "$root\launcher\resources\runtime"
if (Test-Path $runtimeDir) {
    Write-Host "Runtime ya existe en $runtimeDir, se omite jlink (borra la carpeta para regenerar)."
} else {
    $javaHome = $env:JAVA_HOME
    if (-not $javaHome) {
        # Derivar JAVA_HOME a partir de la ubicacion de jlink.exe en el PATH
        $jlinkPath = (Get-Command jlink -ErrorAction SilentlyContinue).Source
        if (-not $jlinkPath) { throw "No se encontro jlink en el PATH ni JAVA_HOME definido." }
        $javaHome = Split-Path (Split-Path $jlinkPath -Parent) -Parent
    }
    Write-Host "Usando JAVA_HOME: $javaHome"
    jlink `
        --module-path "$javaHome\jmods" `
        --add-modules ALL-MODULE-PATH `
        --output "$runtimeDir" `
        --strip-debug `
        --no-header-files `
        --no-man-pages `
        --compress=zip-9
    if (-not $?) { throw "jlink fallo" }
}

# 5. Copiar el JAR compilado a los recursos del launcher
Paso "5/6 Copiando JAR del backend al launcher"
$appDir = "$root\launcher\resources\app"
New-Item -ItemType Directory -Force -Path $appDir | Out-Null
Copy-Item "$root\backend\target\seriestracker-0.0.1-SNAPSHOT.jar" $appDir -Force

# 6. Generar icono, instalar dependencias del launcher y empaquetar con electron-builder
Paso "6/6 Generando icono y empaquetando instalador (electron-builder)"
Set-Location "$root\launcher"
if (-not (Test-Path "node_modules")) {
    npm install
    if (-not $?) { throw "npm install (launcher) fallo" }
}
node tools\generar-icono.js
if (-not $?) { throw "generar-icono.js fallo" }

# Evita que electron-builder intente descargar herramientas de firma de
# macOS (winCodeSign), innecesarias para un build solo-Windows y que fallan
# al extraer symlinks Unix sin privilegios elevados.
$env:CSC_IDENTITY_AUTO_DISCOVERY = "false"
npm run dist
if (-not $?) { throw "electron-builder fallo" }

Set-Location $root
Write-Host ""
Write-Host "Instalador generado en launcher\dist-installer\" -ForegroundColor Green
