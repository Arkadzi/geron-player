package me.arkadiy.geronplayer.statics;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;

import me.arkadiy.geronplayer.plain.Song;

/**
 * Created by Arkadiy on 27.12.2015.
 */
public class MyRingtoneManager {
    public static void setRingtone(Context context, Song song) {
        long currSong = song.getID();
        Uri trackUri = ContentUris.withAppendedId(
                song.getUri(),
                currSong);
        RingtoneManager.setActualDefaultRingtoneUri(context.getApplicationContext(), RingtoneManager.TYPE_RINGTONE, trackUri);
    }
}
