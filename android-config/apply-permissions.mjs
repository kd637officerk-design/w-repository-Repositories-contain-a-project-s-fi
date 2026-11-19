import fs from 'node:fs';
const p='android/app/src/main/AndroidManifest.xml';
if (!fs.existsSync(p)) throw new Error('Android project missing; run npx cap add android first.');
let s=fs.readFileSync(p,'utf8');
const perms=[
'<uses-permission android:name="android.permission.INTERNET" />',
'<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />',
'<uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />',
'<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />',
'<uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />',
'<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:maxSdkVersion="32" />',
'<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />',
'<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />',
'<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />',
'<uses-permission android:name="android.permission.WAKE_LOCK" />',
'<uses-permission android:name="android.permission.RECORD_AUDIO" />',
'<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />',
'<uses-permission android:name="android.permission.BLUETOOTH" android:maxSdkVersion="30" />',
'<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />'
];
for (const tag of perms) {
 const m=tag.match(/android:name="([^"]+)"/);
 if (m && !s.includes(`android:name="${m[1]}"`)) s=s.replace('</manifest>',`    ${tag}\n</manifest>`);
}
fs.writeFileSync(p,s);
console.log('Android permission declarations applied.');
