package me.arkadiy.geronplayer;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class MusicService extends Service {
    private final IBinder musicBind = new MusicBinder();
    private static boolean isStarted;

    public static boolean isStarted() {
        return isStarted;
    }

    public MusicService() {
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("MusicService", "onCreate()");
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e("MusicService", "onBind()");

        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e("MusicService", "onUnbind()");
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("MusicService", "onStartCommand()");
        isStarted = true;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.e("MusicService", "onDestroy()");
        isStarted = false;
        super.onDestroy();
    }
}
