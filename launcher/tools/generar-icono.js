/*
 * Convierte el favicon SVG de marca (marco + corazón, índigo) en un .ico
 * multi-resolución para usarlo como icono del instalador, del .exe y del
 * acceso directo de escritorio.
 */
const path = require('path');
const fs = require('fs');
const sharp = require('sharp');
const pngToIcoModule = require('png-to-ico');
const pngToIco = pngToIcoModule.default || pngToIcoModule;

const SVG_PATH = path.join(__dirname, '..', '..', 'frontend', 'src', 'assets', 'favicon.svg');
const TAMANOS = [16, 32, 48, 256];

async function generarIcono() {
  if (!fs.existsSync(SVG_PATH)) {
    throw new Error(`No se encontró el SVG de origen: ${SVG_PATH}`);
  }

  const buffersPng = [];
  for (const tam of TAMANOS) {
    const buf = await sharp(SVG_PATH, { density: 384 })
      .resize(tam, tam)
      .png()
      .toBuffer();
    buffersPng.push(buf);
  }

  const icoBuffer = await pngToIco(buffersPng);

  const buildDir = path.join(__dirname, '..', 'build');
  const resourcesDir = path.join(__dirname, '..', 'resources');
  fs.mkdirSync(buildDir, { recursive: true });
  fs.mkdirSync(resourcesDir, { recursive: true });

  fs.writeFileSync(path.join(buildDir, 'icon.ico'), icoBuffer);
  fs.writeFileSync(path.join(resourcesDir, 'icon.ico'), icoBuffer);

  console.log('Icono generado en launcher/build/icon.ico y launcher/resources/icon.ico');
}

generarIcono().catch((err) => {
  console.error('Error generando el icono:', err);
  process.exit(1);
});
