package com.primeone.musicplayer;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
public class MediaActionReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context c,Intent i){String a=i.getAction();if(a==null)return;
  if(a.endsWith(".prev"))LocalMusicPlugin.dispatch("prev");
  else if(a.endsWith(".next"))LocalMusicPlugin.dispatch("next");
  else LocalMusicPlugin.dispatch("playpause");
 }
}
