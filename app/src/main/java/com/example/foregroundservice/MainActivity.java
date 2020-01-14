package com.example.foregroundservice;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements LocationListener {


    private TextView textView;
    LocationManager locationManager;

    double lattitude;
    double longitude;

    private SensorManager sm;
    private float acelVal;
    private float acelLast;
    private float shake;

    private EditText editTextInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextInput = findViewById(R.id.edit_text_input);

        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sm.registerListener(sensorListener, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) , SensorManager.SENSOR_DELAY_NORMAL);

        acelVal = SensorManager.GRAVITY_EARTH;
        acelLast = SensorManager.GRAVITY_EARTH;
        shake = 0.00f;

        textView = (TextView) findViewById(R.id.id_textView);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(MainActivity.this , Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this , Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        Location location = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);

        onLocationChanged(location);
    }

    private final SensorEventListener sensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {

            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            acelLast = acelVal;
            acelVal = (float) Math.sqrt((double)(x*x + y*y + z*z));
            float delta = acelVal - acelLast;
            shake = shake * 0.9f + delta;

            if(shake>12){
                Toast toast = Toast.makeText(getApplicationContext() , "DO NOT SHAKE ME" , Toast.LENGTH_LONG);
                toast.show();
                //btn_send();
            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    public void startService(View v){

        String input = editTextInput.getText().toString();
        Intent serviceIntent = new Intent(this , ExampleService.class);
        serviceIntent.putExtra("inputExtra" , input);

        startService(serviceIntent);

    }

    public void stopService(View v){

        Intent serviceIntent = new Intent(this , ExampleService.class);
        stopService(serviceIntent);
    }

    @Override
    public void onLocationChanged(Location location) {

        lattitude = location.getLatitude();
        longitude = location.getLongitude();

        textView.setText("Lattitude::" + " "+ lattitude+"\n"+"Longitude::"+" "+ longitude);


    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public void btn_send(){

        int permissionCheck = ContextCompat.checkSelfPermission(this , Manifest.permission.SEND_SMS);

        if(permissionCheck == PackageManager.PERMISSION_GRANTED){
            myMessage();
        }

        else{
            ActivityCompat.requestPermissions(this , new String[]{Manifest.permission.SEND_SMS} , 0);
        }
    }

    private void myMessage() {

        String message = "Please help me. \n My location is::: lattitude:"+" "+ lattitude+" "+"longitude:"+" "+longitude;
        String phone = "9694625490";

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phone , null , message , null , null );

        Toast.makeText(this , "Message Sent" , Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){
            case 0:

                if(grantResults.length>=0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                    myMessage();

                }
                else{

                    Toast.makeText(this , "you dont have permission" , Toast.LENGTH_SHORT).show();
                }

        }
    }
}
