package com.example.sos;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PowerButton extends BroadcastReceiver {

    public static boolean wasScreenOn = true;
    public static int count = 0;
    public static long firstTime = 0;
    public static long lastTime = 0;
    MainActivity main = new MainActivity();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            count++;
            if (count >= 4) {
                count = 4;
            }
            wasScreenOn = false;

        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            count++;

            Log.e("LOB", "wasScreenOn" + wasScreenOn);


        }
        if (count == 1) {
            firstTime = System.currentTimeMillis();
        } else if (count <= 3) {
            long currentTime = System.currentTimeMillis();
            if   (currentTime - firstTime >= 3000) {
                count = 0;
            }
        }
        if (count == 4) {
            lastTime = System.currentTimeMillis();
            if (lastTime - firstTime <= 4000) {
                Log.e("Sent msgs", String.valueOf(count));
                main.onSend(null);
            }
        }
    }
}
