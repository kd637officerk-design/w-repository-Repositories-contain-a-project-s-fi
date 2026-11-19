package com.primeone.musicplayer;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.media.MediaMetadata;
import com.getcapacitor.*;
import com.getcapacitor.annotation.*;
import org.json.JSONArray;
import org.json.JSONObject;

@CapacitorPlugin(name="LocalMusic", permissions={
 @Permission(alias="audio", strings={Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE}),
 @Permission(alias="video", strings={Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_EXTERNAL_STORAGE}),
 @Permission(alias="notifications", strings={Manifest.permission.POST_NOTIFICATIONS})
})
public class LocalMusicPlugin extends Plugin {
 private static final String CHANNEL="prime_one_playback";
 private static final int NOTIFICATION_ID=7124;
 private MediaSession session;
 private String title="", artist="", album="PRIME ONE", artwork="";
 private boolean playing=false;
 private long duration=0, position=0;

 @PluginMethod public void list(PluginCall call) {
  if (!hasMediaPermission()) { requestMediaPermissions(call); return; }
  JSObject out=new JSObject();
  out.put("songs", query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, false));
  out.put("videos", query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, true));
  call.resolve(out);
 }
 private boolean hasMediaPermission() {
  if(Build.VERSION.SDK_INT>=33)
   return getContext().checkSelfPermission(Manifest.permission.READ_MEDIA_AUDIO)==PackageManager.PERMISSION_GRANTED
    || getContext().checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO)==PackageManager.PERMISSION_GRANTED;
  return getContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED;
 }
 private void requestMediaPermissions(PluginCall call) {
  if(Build.VERSION.SDK_INT>=33) requestPermissionForAliases(new String[]{"audio","video"},call,"mediaPerms");
  else requestPermissionForAlias("audio",call,"mediaPerms");
 }
 @PermissionCallback private void mediaPerms(PluginCall call) {
  if(!hasMediaPermission()){ call.reject("Media permission denied"); return; }
  list(call);
 }
 private JSArray query(Uri collection, boolean video) {
  JSArray result=new JSArray();
  String[] projection=video
   ? new String[]{MediaStore.Video.Media._ID,MediaStore.Video.Media.TITLE,MediaStore.Video.Media.DISPLAY_NAME,MediaStore.Video.Media.DURATION,MediaStore.Video.Media.DATA,MediaStore.Video.Media.SIZE}
   : new String[]{MediaStore.Audio.Media._ID,MediaStore.Audio.Media.TITLE,MediaStore.Audio.Media.DISPLAY_NAME,MediaStore.Audio.Media.DURATION,MediaStore.Audio.Media.DATA,MediaStore.Audio.Media.ARTIST,MediaStore.Audio.Media.ALBUM,MediaStore.Audio.Media.SIZE};
  String sort=video?MediaStore.Video.Media.DATE_ADDED+" DESC":MediaStore.Audio.Media.TITLE+" COLLATE NOCASE ASC";
  try(Cursor c=getContext().getContentResolver().query(collection,projection,null,null,sort)){
   if(c!=null) while(c.moveToNext()){
    String path=get(c,video?MediaStore.Video.Media.DATA:MediaStore.Audio.Media.DATA);
    if(path==null||path.isEmpty()) continue;
    JSObject item=new JSObject();
    item.put("id",get(c,video?MediaStore.Video.Media._ID:MediaStore.Audio.Media._ID));
    item.put("path",path);
    item.put("title",get(c,video?MediaStore.Video.Media.TITLE:MediaStore.Audio.Media.TITLE));
    item.put("name",get(c,video?MediaStore.Video.Media.DISPLAY_NAME:MediaStore.Audio.Media.DISPLAY_NAME));
    item.put("duration",number(c,video?MediaStore.Video.Media.DURATION:MediaStore.Audio.Media.DURATION)/1000.0);
    item.put("size",number(c,MediaStore.MediaColumns.SIZE));
    item.put("kind",video?"video":"audio");
    if(!video){item.put("artist",get(c,MediaStore.Audio.Media.ARTIST));item.put("album",get(c,MediaStore.Audio.Media.ALBUM));}
    int slash=path.lastIndexOf('/'); item.put("folder",slash>0?path.substring(0,slash):"");
    result.put(item);
   }
  } catch(Exception e){ android.util.Log.e("PRIMEONE","MediaStore query failed",e); }
  return result;
 }
 private String get(Cursor c,String col){int i=c.getColumnIndex(col);return i<0?"":c.getString(i);}
 private long number(Cursor c,String col){int i=c.getColumnIndex(col);return i<0?0:c.getLong(i);}

 @PluginMethod public void setNowPlaying(PluginCall call){
  title=call.getString("title",""); artist=call.getString("artist",""); album=call.getString("album","PRIME ONE");
  artwork=call.getString("artwork",""); playing=call.getBoolean("playing",false);
  duration=(long)(call.getDouble("duration",0.0)*1000); position=(long)(call.getDouble("position",0.0)*1000);
  showNotification(); call.resolve();
 }
 @PluginMethod public void state(PluginCall call){playing="playing".equals(call.getString("playbackState","paused"));showNotification();call.resolve();}
 @PluginMethod public void setPlaybackState(PluginCall call){playing="playing".equals(call.getString("playbackState","paused"));showNotification();call.resolve();}
 @PluginMethod public void setPositionState(PluginCall call){duration=(long)(call.getDouble("duration",0.0)*1000);position=(long)(call.getDouble("position",0.0)*1000);showNotification();call.resolve();}
 @PluginMethod public void setMetadata(PluginCall call){title=call.getString("title","");artist=call.getString("artist","");album=call.getString("album","PRIME ONE");showNotification();call.resolve();}
 @PluginMethod public void setActionHandler(PluginCall call){call.resolve();}
 @PluginMethod public void setLockEnabled(PluginCall call){call.resolve();}
 @PluginMethod public void canOverlay(PluginCall call){JSObject o=new JSObject();o.put("value",Build.VERSION.SDK_INT<23||android.provider.Settings.canDrawOverlays(getContext()));call.resolve(o);}
 @PluginMethod public void hasAllFiles(PluginCall call){JSObject o=new JSObject();o.put("value",Build.VERSION.SDK_INT<30||android.os.Environment.isExternalStorageManager());call.resolve(o);}
 @PluginMethod public void requestAllFiles(PluginCall call){if(Build.VERSION.SDK_INT>=30){Intent i=new Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,Uri.parse("package:"+getContext().getPackageName()));getActivity().startActivity(i);}call.resolve();}
 @PluginMethod public void requestOverlay(PluginCall call){if(Build.VERSION.SDK_INT>=23){Intent i=new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getContext().getPackageName()));getActivity().startActivity(i);}call.resolve();}
 @PluginMethod public void openAppSettings(PluginCall call){Intent i=new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getContext().getPackageName()));getActivity().startActivity(i);call.resolve();}
 @PluginMethod public void openUrl(PluginCall call){String u=call.getString("url","");try{Intent i=new Intent(Intent.ACTION_VIEW,Uri.parse(u));i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);getContext().startActivity(i);call.resolve();}catch(Exception e){call.reject("Cannot open file",e);}}
 @PluginMethod public void deleteFile(PluginCall call){call.reject("File deletion is not enabled by this media scanner");}
 @PluginMethod public void writeFile(PluginCall call){call.reject("File writing is not enabled by this media scanner");}
 @PluginMethod public void listStorage(PluginCall call){call.reject("Storage browsing is not included in this media scanner");}

 private void showNotification(){
  Context c=getContext(); NotificationManager nm=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
  if(Build.VERSION.SDK_INT>=26) nm.createNotificationChannel(new NotificationChannel(CHANNEL,"Music playback",NotificationManager.IMPORTANCE_LOW));
  ensureSession();
  if(session!=null){
   session.setMetadata(new MediaMetadata.Builder().putString(MediaMetadata.METADATA_KEY_TITLE,title).putString(MediaMetadata.METADATA_KEY_ARTIST,artist).putString(MediaMetadata.METADATA_KEY_ALBUM,album).putLong(MediaMetadata.METADATA_KEY_DURATION,duration).build());
   long actions=PlaybackState.ACTION_PLAY|PlaybackState.ACTION_PAUSE|PlaybackState.ACTION_PLAY_PAUSE|PlaybackState.ACTION_SKIP_TO_NEXT|PlaybackState.ACTION_SKIP_TO_PREVIOUS|PlaybackState.ACTION_SEEK_TO;
   session.setPlaybackState(new PlaybackState.Builder().setActions(actions).setState(playing?PlaybackState.STATE_PLAYING:PlaybackState.STATE_PAUSED,position,1f).build());
   session.setActive(!title.isEmpty());
  }
  Intent launch=c.getPackageManager().getLaunchIntentForPackage(c.getPackageName());
  PendingIntent content=launch==null?null:PendingIntent.getActivity(c,1,launch,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  NotificationCompat.Builder b=new NotificationCompat.Builder(c,CHANNEL).setSmallIcon(c.getApplicationInfo().icon)
   .setContentTitle(title.isEmpty()?"PRIME ONE":title).setContentText(artist).setSubText(album)
   .setContentIntent(content).setOnlyAlertOnce(true).setOngoing(playing)
   .setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setShowWhen(false)
   .addAction(android.R.drawable.ic_media_previous,"Previous",action("prev",2))
   .addAction(playing?android.R.drawable.ic_media_pause:android.R.drawable.ic_media_play,playing?"Pause":"Play",action("playpause",3))
   .addAction(android.R.drawable.ic_media_next,"Next",action("next",4));
  if(session!=null)b.setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(session.getSessionToken()).setShowActionsInCompactView(0,1,2));
  try{nm.notify(NOTIFICATION_ID,b.build());}catch(SecurityException ignored){}
 }
 private PendingIntent action(String name,int id){Intent i=new Intent(getContext(),MediaActionReceiver.class).setAction("com.primeone.musicplayer."+name);return PendingIntent.getBroadcast(getContext(),id,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
 private void ensureSession(){if(session!=null)return;session=new MediaSession(getContext(),"PRIME ONE");session.setCallback(new MediaSession.Callback(){
  @Override public void onPlay(){emit("playpause");}
  @Override public void onPause(){emit("playpause");}
  @Override public void onSkipToNext(){emit("next");}
  @Override public void onSkipToPrevious(){emit("prev");}
  @Override public void onSeekTo(long p){JSObject o=new JSObject();o.put("action","seek");o.put("position",p/1000.0);notifyListeners("lockAction",o);}
 });session.setActive(true); }
 private void emit(String a){JSObject o=new JSObject();o.put("action",a);notifyListeners("lockAction",o);}
 public static void dispatch(String a){LocalMusicPlugin p=instance;if(p!=null)p.emit(a);}
 private static LocalMusicPlugin instance;
 @Override public void load(){super.load();instance=this;ensureSession();}
 @Override protected void handleOnDestroy(){if(session!=null){session.release();session=null;}instance=null;super.handleOnDestroy();}
}
