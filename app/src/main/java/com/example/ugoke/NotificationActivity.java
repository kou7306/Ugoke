package com.example.ugoke;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;


import com.example.ugoke.R;
import com.example.ugoke.SoundService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.location.LocationListener;
import android.hardware.Sensor;
import android.hardware.SensorManager;


public class NotificationActivity extends AppCompatActivity implements LocationListener, SensorEventListener{

    Button stopBtn;
    private TextView alarmText;
    private TextView distanceTextView;
    private int hour;
    private int minute;
    private LocationManager mLocationManager;
    private Location initialLocation;
    private static final float DISTANCE_THRESHOLD = 5.0f; // 5メートル
    private float remainingDistance1 = 5.0f;

    private float remainingDistance2 = 5.0f;

    private float remainingDistance = 5.0f;
    private static final float ACC_THRESHOLD = 0.5f; // 移動を検知するための加速度の閾値
    private float lastAcc = 0.0f; // 前回の加速度の値

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private float lastVelocity = 0; // 直前の速度
    private float lastPosition = 0; // 直前の位置

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // アラームの時間情報を受け取る
        Intent intent = getIntent();
        if (intent != null) {
            hour = intent.getIntExtra("ALARM_HOUR", 0);
            minute = intent.getIntExtra("ALARM_MINUTE", 0);
            // 受け取った情報を使って処理を行う
        }

        setContentView(R.layout.activity_notification);
        Toolbar toolbar = findViewById(R.id.toolbarWakeUp);
        setSupportActionBar(toolbar);
        alarmText = findViewById(R.id.textView);
        distanceTextView = findViewById(R.id.distanceTextView);

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // センサーマネージャーを取得
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // 加速度センサーを取得
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        startService(new Intent(this, SoundService.class));
        distanceTextView.setText(String.format("残り%.2fメートル", remainingDistance));
        stopBtn = (Button) findViewById(R.id.stopBtn);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService(new Intent(NotificationActivity.this, SoundService.class));
                finish();
            }
        });



    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hour > 12){
            alarmText.setText(
                    String.format("%02d",(hour)) +":"+ String.format("%02d",minute)+"PM"
            );
        } else  {
            alarmText.setText(hour+":" + minute+ "AM");
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 位置情報アクセスの権限がない場合の処理
            return;
        }

        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        }

        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 0, this);
        // 加速度センサーのリスナーを登録
        sensorManager.registerListener( this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mLocationManager != null){
            if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                return;
            }
        }
        // アプリが非アクティブになったときにセンサーリスナーを解除
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (initialLocation == null) {
            initialLocation = location;
            Log.d("initialLocation", "onLocationChanged: " + initialLocation.toString());
        }
        wifiDistanceCheck(location, initialLocation);

        updateDistance();

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        sensorDistanceCheck(event);
        updateDistance();

    }


    private void wifiDistanceCheck(Location location, Location initialLocation) {
        // Wifiを使用して5メートル進んだかどうかを判断する処理


        float distance = location.distanceTo(initialLocation);
        remainingDistance1 = DISTANCE_THRESHOLD - distance;
        Log.d("remainingDistance1", "onLocationChanged1: " + remainingDistance1);

    }


    private void sensorDistanceCheck(SensorEvent event) {
        // 加速度センサーの値が変化したときの処理
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float deltaTime = 0.1f;
            // 加速度の値を取得
            float acc = event.values[0];
            // 加速度の変化量を計算
            float deltaAcc = Math.abs(acc - lastAcc);
            // 加速度が閾値以上変化した場合は移動と判断
            if (deltaAcc > ACC_THRESHOLD) {
                // 加速度を積分して速度を計算
                float velocity = lastVelocity + acc * deltaTime;

                // 速度を積分して位置を計算
                float position = lastPosition + velocity * deltaTime;

                // 移動距離を計算
                float distance = position - lastPosition;

                // 残りの距離を更新
                remainingDistance2 = DISTANCE_THRESHOLD - Math.abs(distance);

                // 位置と速度を更新
                lastPosition = position;
                lastVelocity = velocity;

            }

            // 現在の加速度を保存
            lastAcc = acc;

        }

        Log.d("remainingDistance2", "onLocationChanged2: " + remainingDistance2);
    }






    public void updateDistance(){
        remainingDistance = Math.min(remainingDistance1, remainingDistance2);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                distanceTextView.setText(String.format("残り%.2fメートル", remainingDistance));
            }
        });


        if(remainingDistance <= 0){
            Log.d("distance", "5m");
            // 初期位置から5メートル以上離れた場合の処理
            stopService(new Intent(NotificationActivity.this, SoundService.class));
            finish();
            if (mLocationManager != null) {
                mLocationManager.removeUpdates(this);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}