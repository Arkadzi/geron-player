package me.arkadiy.geronplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

import me.arkadiy.geronplayer.statics.Constants;

/**
 * Created by Arkadiy on 27.08.2015.
 */
public class NoisyAudioReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
            Intent newIntent = new Intent(context, MusicService.class);
            newIntent.setAction(Constants.ACTION.PAUSE_ACTION);
            context.startService(newIntent);
        }
    }
}