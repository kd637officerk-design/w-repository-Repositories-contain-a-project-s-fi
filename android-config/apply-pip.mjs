import fs from 'node:fs';
import path from 'node:path';

const androidRoot = path.resolve('android');
const javaRoot = path.join(androidRoot, 'app/src/main/java');
function findFile(dir, name) {
  for (const entry of fs.readdirSync(dir, {withFileTypes:true})) {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) { const found = findFile(full, name); if (found) return found; }
    else if (entry.name === name) return full;
  }
  return null;
}
const main = findFile(javaRoot, 'MainActivity.java');
if (!main) throw new Error('Capacitor MainActivity.java was not found');
let source = fs.readFileSync(main, 'utf8');
if (!source.includes('PictureInPicturePlugin')) {
  source = source.replace(/(package [^;]+;)/, '$1\n\nimport com.primeone.musicplayer.PictureInPicturePlugin;');
  source = source.replace(/(public class MainActivity extends BridgeActivity\s*\{)/,
    '$1\n    @Override\n    public void onCreate(android.os.Bundle savedInstanceState) {\n        registerPlugin(PictureInPicturePlugin.class);\n        super.onCreate(savedInstanceState);\n    }');
  fs.writeFileSync(main, source);
}
const pluginDir = path.join(javaRoot, 'com/primeone/musicplayer');
fs.mkdirSync(pluginDir, {recursive:true});
fs.copyFileSync(path.resolve('android-config/PictureInPicturePlugin.java'),
                 path.join(pluginDir, 'PictureInPicturePlugin.java'));
const manifest = path.join(androidRoot, 'app/src/main/AndroidManifest.xml');
let xml = fs.readFileSync(manifest, 'utf8');
if (!xml.includes('android:supportsPictureInPicture="true"')) {
  xml = xml.replace(/(<activity\b[^>]*android:name="[^"]*MainActivity"[^>]*)(>)/,
    '$1 android:supportsPictureInPicture="true" android:resizeableActivity="true"$2');
  fs.writeFileSync(manifest, xml);
}
console.log('Picture-in-Picture plugin and manifest configured.');
