package me.arkadiy.geronplayer.statics;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import me.arkadiy.geronplayer.plain.Category;
import me.arkadiy.geronplayer.plain.Song;

/**
 * Created by Arkadiy on 02.01.2016.
 */
public class PlaylistUtils {
    public static void rewritePlaylist(Context context, long playlistId, List<Song> songs) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        int size = contentResolver.delete(uri, null, null);
        Log.e("PlaylistUtils", "delete " + String.valueOf(size));
        insertSongs(contentResolver, playlistId, songs, 0);
    }

    public static List<Category> getPlaylists(Context context) {
        ContentResolver musicResolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        ArrayList<Category> playlists = new ArrayList<>();
        String _id = MediaStore.Audio.Playlists._ID;
        String name = MediaStore.Audio.Playlists.NAME;
        String[] columns = {_id, name};
        Cursor musicCursor = musicResolver.query(uri, columns, null, null, null);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            do {
                int thisId = musicCursor.getInt(0);
                String thisPlaylist = musicCursor.getString(1);
                int numberOfSongs = numberOfSongs(musicResolver, thisId);
                Category playlist = new Category(thisId, thisPlaylist, numberOfSongs);
                playlists.add(playlist);
            } while (musicCursor.moveToNext());
            Collections.sort(playlists, new Comparator<Category>() {
                @Override
                public int compare(Category a, Category b) {
                    return a.getName().compareToIgnoreCase(b.getName());
                }
            });
        }
        if (musicCursor != null) {
            musicCursor.close();
        }
        return playlists;
    }

    public static int numberOfSongs(ContentResolver musicResolver, int playlistId) {
        String[] cols = new String[]{
                "count(*)"
        };
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        Cursor cursor = musicResolver.query(uri, cols, null, null, null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        if (cursor != null) {
            cursor.close();
        }

        return count;
    }

    public static long createPlaylist(Context c, String playlistName) {
        ContentResolver resolver = c.getContentResolver();
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Audio.Playlists.NAME, playlistName);
        Uri uri = resolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, cv);
        if (uri != null) {
            return Long.parseLong(uri.getLastPathSegment());
        }
        else {
            return -1;
        }
    }

    public static void addSongs(Context c, long id, List<Song> songs) {
        ContentResolver resolver = c.getContentResolver();
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", id);
        String[] projection = new String[] {String.format("max(%s)",MediaStore.Audio.Playlists.Members.PLAY_ORDER)};
        Cursor cursor = resolver.query(uri, projection, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int shift = cursor.getInt(0);
                insertSongs(resolver, id, songs, shift);
            }
            cursor.close();
        }
    }

    public static void insertSongs(ContentResolver contentResolver, long id,  List<Song> songs, int shift) {
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", id);
        ContentValues[] values = new ContentValues[songs.size()];
        for (int i = 0; i < songs.size(); i++) {
            values[i] = new ContentValues();
            values[i].put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, i + shift);
            values[i].put(MediaStore.Audio.Playlists.Members.AUDIO_ID, songs.get(i).getID());
        }
        contentResolver.bulkInsert(uri, values);
    }
}
