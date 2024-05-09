package com.example.ugoke;

import android.content.Intent;
import android.content.pm.PackageManager;
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


public class NotificationActivity extends AppCompatActivity implements LocationListener{

    Button stopBtn;
    private TextView alarmText;
    private TextView distanceTextView;
    private int hour;
    private int minute;
    private LocationManager mLocationManager;
    private Location initialLocation;
    private static final float DISTANCE_THRESHOLD = 5.0f; // 5メートル

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


        startService(new Intent(this, SoundService.class));

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
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

        Log.d("Location", "onLocationChanged: " + location.toString());

        if (initialLocation == null) {
            initialLocation = location;
            Log.d("initialLocation", "onLocationChanged: " + initialLocation.toString());
        }

        float distance = location.distanceTo(initialLocation);
        Log.d("distance", "onLocationChanged: " + distance);
        float remainingDistance = DISTANCE_THRESHOLD - distance;
        Log.d("remainingDistance", "onLocationChanged: " + remainingDistance);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                distanceTextView.setText(String.format("%.2f meters left", remainingDistance));
            }
        });
        if (distance >= DISTANCE_THRESHOLD) {
            Log.d("distance", "5m");
            // 初期位置から5メートル以上離れた場合の処理
            stopService(new Intent(NotificationActivity.this, SoundService.class));
            finish();
            if (mLocationManager != null) {
                mLocationManager.removeUpdates(this);
            }

        } else {
            // 継続して位置情報を取得する
            // 残り距離を表示する

        }
    }
    }