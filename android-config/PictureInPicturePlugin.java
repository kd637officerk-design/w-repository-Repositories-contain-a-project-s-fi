package com.primeone.musicplayer;

import android.app.PictureInPictureParams;
import android.os.Build;
import android.util.Rational;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "PictureInPicture")
public class PictureInPicturePlugin extends Plugin {
    @PluginMethod
    public void enter(PluginCall call) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            call.reject("Picture-in-Picture requires Android 8.0 or later");
            return;
        }
        getActivity().runOnUiThread(() -> {
            try {
                PictureInPictureParams params = new PictureInPictureParams.Builder()
                    .setAspectRatio(new Rational(16, 9))
                    .build();
                boolean entered = getActivity().enterPictureInPictureMode(params);
                if (entered) call.resolve();
                else call.reject("Android could not enter Picture-in-Picture mode");
            } catch (Exception e) {
                call.reject("Unable to start Picture-in-Picture", e);
            }
        });
    }
}
