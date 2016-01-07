package me.arkadiy.geronplayer.statics;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import me.arkadiy.geronplayer.plain.Song;

/**
 * Created by Arkadiy on 27.12.2015.
 */
public class DeleteUtils {
    private final Uri URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    public void deleteSong(Context context, long id) {
        String selection = MediaStore.Audio.Media._ID + " = ?";
        String[] args = new String[]{String.valueOf(id)};
        delete(context, selection, args);
    }


    public void deleteAlbum(Context context, long id) {
        String selection = MediaStore.Audio.Media.ALBUM_ID + " = ?";
        String[] args = new String[]{String.valueOf(id)};
        delete(context, selection, args);
    }

    private void delete(Context context, String selection, String[] args) {
        ContentResolver contentResolver = context.getContentResolver();
        Cursor c = contentResolver.query(
                URI,
                new String[]{MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA},
                selection,
                args,
                null
        );
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    File file = new File(c.getString(1));
                    if (file.delete()) {
                        Uri uri = ContentUris.withAppendedId(URI,
                                c.getLong(0));
                        contentResolver.delete(uri, null, null);
                    }
                } while (c.moveToNext());
            }
            c.close();
        }
    }

    public void deleteArtist(Context context, long id) {
        String selection = MediaStore.Audio.Media.ARTIST_ID + " = ?";
        String[] args = new String[]{String.valueOf(id)};
        delete(context, selection, args);
    }

    public void deleteGenre(Context context, long id) {
        List<Long> albumIds = MusicRetriever.getAlbumIds(context, id);

        for (Long albumId : albumIds) {
            deleteAlbum(context, albumId);
        }

    }

    public void deleteFolder(Context context, String path) {
        List<Song> songs = MusicRetriever.getSongsByFolder(context, path);
        for (Song song : songs) {
            deleteSong(context, song.getID());
        }
    }

    public void deletePlaylist(Context context, long id) {
        ContentResolver resolver = context.getContentResolver();
        String where = MediaStore.Audio.Playlists._ID + "=?";
        String[] whereVal = new String[]{String.valueOf(id)};
        resolver.delete(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, where, whereVal);
    }
}