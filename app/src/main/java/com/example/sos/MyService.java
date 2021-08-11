
        package com.example.sos;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.util.Objects;

public class MyService extends Service {
    private float mAccel;
    private float mAccelCurrent;
    private  float mAccelLast;
    private SensorManager sensorManager;
    MainActivity main = new MainActivity();

    @Override
    public void onCreate() {
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccel = 10f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;

        Objects.requireNonNull(sensorManager).registerListener(mSensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
        //https://stackoverflow.com/questions/31470806/register-a-broadcast-receiver-in-a-service
        // listeners to be created in onCreate cuz they will be called only once
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        this.registerReceiver(new MainActivity.PowerButton(),filter);
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        createNotificationChannel();

        // Notification panel to app

        Intent intent1 = new Intent(this,MainActivity.class);

        // Notification Tab

        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent1,0);
        Notification notification = new NotificationCompat.Builder(this,"ChannelId1")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText("MyService is Up")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1,notification); // Continuously running the app

        return START_STICKY; // returns 1
    }
    private void createNotificationChannel(){
        //SDK VERSION must be more than Oreo
        // I used API >= 26 so no problem

        NotificationChannel notificationChannel = new NotificationChannel(
                "ChannelId1", "Foreground Notification", NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(notificationChannel);
    }
    private final SensorEventListener mSensorListener =  new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            mAccelLast = mAccelCurrent;
            //type cast and update the current acceleration
            // event values are vector values to be taken magnitude

            mAccelCurrent = (float) Math.sqrt(x*x + y*y + z*z) ;
            float change = mAccelCurrent - mAccelLast;
            // determine acceleration
            mAccel = mAccel* 0.9f + change;
            if(mAccel > 12){
                Toast.makeText(getApplicationContext(),"Shake event detected",Toast.LENGTH_SHORT).show();
                main.onSend(null);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            //
        }
    };

}