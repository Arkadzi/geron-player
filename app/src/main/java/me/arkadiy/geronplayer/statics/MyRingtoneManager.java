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
//        File f=new File(song.getPath());
//        // Insert the ring tone to the content provider
        ContentValues value=new ContentValues();
//        value.put(MediaStore.MediaColumns.DATA, f.getAbsolutePath());
//        value.put(MediaStore.MediaColumns.TITLE, f.getName());
//        value.put(MediaStore.MediaColumns.SIZE, f.length());
//        value.put(MediaStore.MediaColumns.MIME_TYPE,"audio/*");
//        value.put(MediaStore.Audio.Media.ARTIST, "artist");
//        value.put(MediaStore.Audio.Media.DURATION, 500);
//        value.put(MediaStore.Audio.Media.IS_ALARM, false);
//        value.put(MediaStore.Audio.Media.IS_MUSIC, false);
//        value.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
        value.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        ContentResolver cr=context.getContentResolver();
        Uri uri= MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        int updated = cr.update(uri, value, MediaStore.Audio.Media._ID + " = ?", new String[] {String.valueOf(song.getID())});
//        // Set default ring tone
        Uri songUri = ContentUris.withAppendedId(
                song.getUri(),
                song.getID());
        Log.e("uri", songUri.toString() + " " + updated);
        RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, songUri);
    }
}
