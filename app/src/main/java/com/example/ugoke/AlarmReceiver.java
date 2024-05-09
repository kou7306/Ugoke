package com.example.ugoke;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int hour = intent.getIntExtra("ALARM_HOUR", 0);
        int minute = intent.getIntExtra("ALARM_MINUTE", 0);
        Log.d("tag", "onReceive: ");
        Intent nextActivity = new Intent(context, NotificationActivity.class);
        nextActivity.putExtra("ALARM_TRIGGERED", true);
        nextActivity.putExtra("ALARM_HOUR", hour);
        nextActivity.putExtra("ALARM_MINUTE", minute);
        nextActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);


        // PendingIntentを使わずに直接アクティビティを起動する
        context.startActivity(nextActivity);
    }
}
