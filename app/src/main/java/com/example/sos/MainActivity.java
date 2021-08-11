package com.example.sos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static com.example.sos.AddContacts.phoneNumber1;
import static com.example.sos.AddContacts.phoneNumber2;
import static com.example.sos.AddContacts.phoneNumber3;
import static com.example.sos.AddContacts.myPreferences;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;

import android.os.Bundle;
import android.os.Looper;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;


import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private float mAccel;
    private float mAccelCurrent;
    private  float mAccelLast;
    private SensorManager sensorManager;
    // normal number string

    static String[] numbers = new String[3];
    private FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    static double mLatitude,mLongitude;
    Button SOS;
    Button add_contacts;
    SharedPreferences sharedPreferences;
    static Address address;




    // main method

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Need to check permission regularly

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.SEND_SMS, Manifest.permission.READ_CONTACTS,
                    Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.FOREGROUND_SERVICE
            }, 1);
        }

        // get phone numbers from the edittext

        SOS = findViewById(R.id.send);
        add_contacts = findViewById(R.id.button3);

        //locationClient

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //sensorClient

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Objects.requireNonNull(sensorManager).registerListener(mSensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 10f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;
        add_contacts.setOnClickListener(v -> {
            Intent n = new Intent(this,AddContacts.class);
            startActivity(n);
        });
        // sharedPref
        getLocation();
    }
    //Foreground Service

    public void startService(View v){
        Intent serviceIntent = new Intent(MainActivity.this, MyService.class);
        ContextCompat.startForegroundService(this,serviceIntent);
    }
    public void stopService(View v){
        Intent serviceIntent = new Intent(this,MyService.class);
        stopService(serviceIntent);
    }

    // callback for requestLocationUpdate

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull @org.jetbrains.annotations.NotNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
        }
    };

    // SensorListener for onShake trigger feature
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
                onSend(null);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            //
        }
    };
    public static class PowerButton extends BroadcastReceiver {
        public  boolean wasScreenOn = true;
        public  int count = 0;
        public  long firstTime = 0;
        public  long lastTime = 0;

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
                    Log.e("Send Messages", String.valueOf(count)); // this was executed
                    updateAddress(context);
                    String strAddress = "I'm in danger and Here is my current address" + "\n" +
                            "Place : " + address.getLocality() + "\n" +
                            "City : " + address.getSubAdminArea() + "\n" +
                            "State : " + address.getAdminArea() + "\n" +
                            "Country : " + address.getCountryName() + "\n" +
                            "Country Code : " + address.getCountryCode() + "\n";

                    SmsManager smsManager = SmsManager.getDefault();
                    for(String num : numbers){
                        smsManager.sendTextMessage(num,null,strAddress,null,null);
                    }
                }
            }
        }
    }


    // Sending the textMsg
    public void onSend(View v) {
        // Update location everytime we need to send the text
        // Get numbers from the shared preference list

        this.sharedPreferences = getSharedPreferences(myPreferences,Context.MODE_PRIVATE);
        numbers[0] = sharedPreferences.getString(phoneNumber1,"8129636160");
        numbers[1] = sharedPreferences.getString(phoneNumber2,"8129636160");
        numbers[2] = sharedPreferences.getString(phoneNumber3,"8129636160");
        Log.e("num",numbers[0]);
        Log.e("num",numbers[1]);
        Log.e("num",numbers[2]);

        //     numbers[0] = number1.getText().toString();
   //     numbers[1] = number2.getText().toString();
   //     numbers[2] = number3.getText().toString();

        // Geocoder to change latitudes and longitudes into address

        Geocoder geocoder = new Geocoder(this);

        try{
            List<Address> list = geocoder.getFromLocation(mLatitude,mLongitude,1);
            address = list.get(0);
            String strAddress = "I'm in danger and Here is my current address" + "\n" +
                    "Place : " + address.getLocality() + "\n" +
                    "City : " + address.getSubAdminArea() + "\n" +
                    "State : " + address.getAdminArea() + "\n" +
                    "Country : " + address.getCountryName() + "\n" +
                    "Country Code : " + address.getCountryCode() + "\n";
            SmsManager smsManager = SmsManager.getDefault();
            for(String num : numbers){
                smsManager.sendTextMessage(num,null,strAddress,null,null);
            }

        }
        catch (IOException e){
            e.printStackTrace();
        }
        Toast.makeText(getApplicationContext(),"sent",Toast.LENGTH_SHORT).show();
        Intent n = new Intent(MainActivity.this,TextSent.class);
        startActivity(n);
    }
    public static void updateAddress(Context context){
        numbers[0] = context.getSharedPreferences(myPreferences,Context.MODE_PRIVATE).getString(phoneNumber1,"8129636160");
        numbers[1] = context.getSharedPreferences(myPreferences,Context.MODE_PRIVATE).getString(phoneNumber2,"8129636160");
        numbers[2] = context.getSharedPreferences(myPreferences,Context.MODE_PRIVATE).getString(phoneNumber3,"8129636160");
        Geocoder geocoder = new Geocoder(context);
        try{
            List<Address> list = geocoder.getFromLocation(mLatitude,mLongitude,1);
            address = list.get(0);
        }
        catch (IOException e){
            //
        }
    }






    // Updating the global variable latitude and longitude from getLocation
    public void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(),"Permission for location not granted",Toast.LENGTH_SHORT).show();
            return;
        }
       locationRequest = LocationRequest.create();
       locationRequest.setInterval(100000);
       locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
       fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper());

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            if(location!=null){
                  mLatitude = location.getLatitude();
                  mLongitude = location.getLongitude();
            }
            else{
                Toast.makeText(getApplicationContext(),"Cannot get location",Toast.LENGTH_SHORT).show();
            }
        });

    }



}