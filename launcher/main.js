const { app, BrowserWindow, dialog } = require('electron');
const { spawn, execFileSync } = require('child_process');
const path = require('path');
const http = require('http');

const BACKEND_URL = 'http://localhost:8090';
const isDev = !app.isPackaged;

// En desarrollo, basePath apunta a la carpeta launcher/ (para probar con `npm start`
// antes de empaquetar). En producción, apunta a resources/ dentro del instalador.
const basePath = isDev
  ? path.join(__dirname, 'resources')
  : process.resourcesPath;

const javaExe = path.join(basePath, 'runtime', 'bin', 'javaw.exe');
const jarPath = path.join(basePath, 'app', 'seriestracker-0.0.1-SNAPSHOT.jar');
const appDir = path.join(basePath, 'app');
const iconPath = path.join(basePath, 'icon.ico');

let javaProcess = null;
let mainWindow = null;

function esperarBackend(url, intentos = 40, delayMs = 500) {
  return new Promise((resolve) => {
    let restantes = intentos;
    const intentar = () => {
      const req = http.get(url, (res) => {
        res.destroy();
        resolve(true);
      });
      req.on('error', () => {
        restantes -= 1;
        if (restantes <= 0) {
          resolve(false);
        } else {
          setTimeout(intentar, delayMs);
        }
      });
      req.setTimeout(delayMs, () => req.destroy());
    };
    intentar();
  });
}

function lanzarBackend() {
  javaProcess = spawn(javaExe, ['-jar', jarPath], {
    cwd: appDir,
    windowsHide: true,
    stdio: 'ignore',
    detached: false
  });

  javaProcess.on('exit', () => {
    javaProcess = null;
    // Si el backend muere solo (crash), no dejar la ventana zombie.
    if (mainWindow && !mainWindow.isDestroyed()) {
      app.quit();
    }
  });
}

function matarBackend() {
  if (javaProcess && javaProcess.pid) {
    try {
      // taskkill /T mata todo el árbol de procesos descendientes, /F fuerza.
      // Más fiable en Windows que un simple .kill() de Node sobre procesos Java.
      execFileSync('taskkill', ['/pid', String(javaProcess.pid), '/T', '/F'], {
        windowsHide: true
      });
    } catch (e) {
      // El proceso ya podría haber muerto; no es un error fatal.
    }
    javaProcess = null;
  }
}

function crearVentana() {
  mainWindow = new BrowserWindow({
    width: 1280,
    height: 800,
    icon: iconPath,
    autoHideMenuBar: true,
    webPreferences: {
      contextIsolation: true,
      nodeIntegration: false
    }
  });
  mainWindow.loadURL(BACKEND_URL);
  mainWindow.on('closed', () => {
    mainWindow = null;
  });
}

const gotLock = app.requestSingleInstanceLock();
if (!gotLock) {
  app.quit();
} else {
  app.on('second-instance', () => {
    if (mainWindow) {
      if (mainWindow.isMinimized()) mainWindow.restore();
      mainWindow.focus();
    }
  });

  app.whenReady().then(async () => {
    lanzarBackend();
    const ok = await esperarBackend(BACKEND_URL);
    if (!ok) {
      dialog.showErrorBox(
        'MyFilmMemories',
        'No se pudo iniciar el servidor de la aplicación. Inténtalo de nuevo.'
      );
      matarBackend();
      app.quit();
      return;
    }
    crearVentana();
  });
}

app.on('window-all-closed', () => {
  matarBackend();
  app.quit();
});

app.on('before-quit', () => {
  matarBackend();
});
