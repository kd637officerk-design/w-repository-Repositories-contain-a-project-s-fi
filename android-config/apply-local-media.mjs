import fs from 'node:fs';
import path from 'node:path';
const javaRoot=path.resolve('android/app/src/main/java');
function findFile(dir,name){for(const e of fs.readdirSync(dir,{withFileTypes:true})){const p=path.join(dir,e.name);if(e.isDirectory()){const x=findFile(p,name);if(x)return x}else if(e.name===name)return p}return null}
const main=findFile(javaRoot,'MainActivity.java');
if(!main)throw new Error('MainActivity.java not found');
let src=fs.readFileSync(main,'utf8');
if(!src.includes('LocalMusicPlugin.class')){
 src=src.replace(/(public class MainActivity extends BridgeActivity\s*\{)/,'$1\n @Override public void onCreate(android.os.Bundle state){ registerPlugin(LocalMusicPlugin.class); super.onCreate(state); }');
 if(!src.includes('import com.primeone.musicplayer.LocalMusicPlugin;'))src=src.replace(/(package [^;]+;)/,'$1\nimport com.primeone.musicplayer.LocalMusicPlugin;');
 fs.writeFileSync(main,src);
}
const dir=path.join(javaRoot,'com/primeone/musicplayer');fs.mkdirSync(dir,{recursive:true});
for(const f of ['LocalMusicPlugin.java','MediaActionReceiver.java'])fs.copyFileSync(path.resolve('android-config',f),path.join(dir,f));
const manifest=path.resolve('android/app/src/main/AndroidManifest.xml');
let xml=fs.readFileSync(manifest,'utf8');
if(!xml.includes('MediaActionReceiver'))xml=xml.replace('</application>','<receiver android:name="com.primeone.musicplayer.MediaActionReceiver" android:exported="false"><intent-filter><action android:name="com.primeone.musicplayer.prev"/><action android:name="com.primeone.musicplayer.next"/><action android:name="com.primeone.musicplayer.playpause"/></intent-filter></receiver>\n</application>');
fs.writeFileSync(manifest,xml);
const gradle=path.resolve('android/app/build.gradle');
if(fs.existsSync(gradle)){let g=fs.readFileSync(gradle,'utf8');if(!g.includes('androidx.media:media'))g=g.replace(/dependencies\s*\{/,'dependencies {\n    implementation "androidx.media:media:1.7.0"');fs.writeFileSync(gradle,g);}
console.log('Native MediaStore scanner and media notification plugin installed.');
