import fs from 'node:fs';
import path from 'node:path';
const source='android-config/res';
const target='android/app/src/main/res';
if (!fs.existsSync(target)) throw new Error('Android project missing; run npx cap add android first.');
for (const density of ['mdpi','hdpi','xhdpi','xxhdpi','xxxhdpi']) {
  const from=path.join(source,`mipmap-${density}`);
  const to=path.join(target,`mipmap-${density}`);
  fs.mkdirSync(to,{recursive:true});
  for (const file of ['ic_launcher.png','ic_launcher_round.png','ic_launcher_foreground.png']) {
    const p=path.join(from,file); if (fs.existsSync(p)) fs.copyFileSync(p,path.join(to,file));
  }
}
console.log('PRIME ONE app launcher icons installed.');
