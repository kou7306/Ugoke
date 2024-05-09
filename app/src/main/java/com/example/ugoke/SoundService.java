package com.example.ugoke;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.content.Context;

import androidx.annotation.Nullable;

import com.example.ugoke.R;

public class SoundService extends Service {
    private Ringtone ringtone;

    @Override
    public void onCreate() {
        super.onCreate();
        ringtone = RingtoneManager.getRingtone(getApplicationContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
        if (ringtone != null) {
            ringtone.play();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // サービスが強制的に停止された場合に、システムが自動的に再起動します
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop(); // 音声の再生を停止します
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
