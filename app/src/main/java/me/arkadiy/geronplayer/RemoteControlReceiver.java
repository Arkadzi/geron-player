package me.arkadiy.geronplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;
import android.view.KeyEvent;

import me.arkadiy.geronplayer.statics.Constants;

/**
 * Created by Arkadiy on 27.08.2015.
 */
public class RemoteControlReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                Log.e("RemoteControlReceiver", "keycode " + event.getKeyCode() + context.getClass().getName() + " " + hashCode());
                Intent newIntent = new Intent(context, MusicService.class);
                if (KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE == event.getKeyCode()) {
                    newIntent.setAction(Constants.ACTION.PAUSE_PLAY_ACTION);
                    context.startService(newIntent);
                } else if (KeyEvent.KEYCODE_MEDIA_NEXT == event.getKeyCode()) {
                    newIntent.setAction(Constants.ACTION.NEXT_ACTION);
                    context.startService(newIntent);
                } else if (KeyEvent.KEYCODE_MEDIA_PREVIOUS == event.getKeyCode()) {
                    newIntent.setAction(Constants.ACTION.PREV_ACTION);
                    context.startService(newIntent);
                }
            }
        }
    }
}