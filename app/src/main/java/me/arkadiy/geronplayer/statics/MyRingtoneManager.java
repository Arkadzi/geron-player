package me.arkadiy.geronplayer.statics;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;

import java.io.File;

import me.arkadiy.geronplayer.plain.Song;

/**
 * Created by Arkadiy on 27.12.2015.
 */
public class MyRingtoneManager {
    public static void setRingtone(Context context, Song song) {
        ContentValues value=new ContentValues();

        value.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        ContentResolver cr=context.getContentResolver();
        Uri uri= MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        int updated = cr.update(uri, value, MediaStore.Audio.Media._ID + " = ?", new String[] {String.valueOf(song.getID())});

        Uri songUri = ContentUris.withAppendedId(
                song.getUri(),
                song.getID());
        RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, songUri);
    }
}
