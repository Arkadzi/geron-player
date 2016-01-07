package me.arkadiy.geronplayer.audio;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataEditor;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.os.Build;
import android.util.Log;

import com.nostra13.universalimageloader.core.assist.ImageSize;

import me.arkadiy.geronplayer.MainActivity;
import me.arkadiy.geronplayer.RemoteControlReceiver;
import me.arkadiy.geronplayer.audio.AudioFocusListener;
import me.arkadiy.geronplayer.plain.Song;
import me.arkadiy.geronplayer.statics.Utils;

/**
 * Created by Arkadiy on 11.12.2015.
 */
public class AudioFocusHelper implements AudioManager.OnAudioFocusChangeListener {
    private final AudioFocusListener listener;
    private AudioManager am;
    private RemoteControlClient remoteControlClient;

    private ComponentName remoteControlReceiver;

    private boolean isFocused;
    private ImageSize imageSize;

    public AudioFocusHelper(Context c, AudioFocusListener listener) {
        imageSize = new ImageSize(50, 50);
        am = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
        remoteControlReceiver = new ComponentName(c.getPackageName(), RemoteControlReceiver.class.getName());
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {

            Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            mediaButtonIntent.setComponent(remoteControlReceiver);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(c.getApplicationContext(), 0, mediaButtonIntent, 0);
            remoteControlClient = new RemoteControlClient(pendingIntent);
            remoteControlClient.setTransportControlFlags(
                    RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
                            | RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS
                            | RemoteControlClient.FLAG_KEY_MEDIA_NEXT);
        }

        this.listener = listener;
    }

    public void requestFocusIfNecessary(Song song) {
//        if (!isFocused) {
            int result = am.requestAudioFocus(this,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                isFocused = true;
                Log.e("audio focus", "requested");
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    am.registerMediaButtonEventReceiver(remoteControlReceiver);
                    am.registerRemoteControlClient(remoteControlClient);
                }
                String title = song.getTitle();
                int length = 30;
                if (title.length() > length) {
                    title = title.substring(0, length);
                }
                String artist = song.getArtist();
                if (artist.length() > length) {
                    artist = artist.substring(0, length);
                }
                updateRemoteControl(song);
            }
//        }
    }

    public void updateRemoteControl(Song song) {
        if (isFocused && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            remoteControlClient.editMetadata(false)
                    .putString(MediaMetadataRetriever.METADATA_KEY_TITLE, song.getTitle())
                    .putString(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST, song.getArtist())
                    .apply();
        }
    }

    public void refuseFocusIfNecessary() {
        if (isFocused) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                am.unregisterRemoteControlClient(remoteControlClient);
                am.unregisterMediaButtonEventReceiver(remoteControlReceiver);
            }
            am.abandonAudioFocus(this);
            isFocused = false;
        }
    }



    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
                isFocused = false;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                Log.e("audio focus", String.valueOf(focusChange));
                listener.onFocusLoss();
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                isFocused = true;
                listener.onFocusGained();
                break;
        }
    }

    public void playbackPaused() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
        }
    }

    public void playbackStarted() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
        }
    }
}
