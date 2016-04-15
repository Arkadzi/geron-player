package me.arkadiy.geronplayer.statics;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.util.List;

import me.arkadiy.geronplayer.MusicService;
import me.arkadiy.geronplayer.plain.Song;

public class DeleteUtils {


    private static final Uri URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    public static boolean deleteSongs(MusicService c, List<Song> songsForDelete, List<Song> songs) {
        boolean hasChanged = false;
        for (Song item : songsForDelete) {
            for (int i = 0; i < songs.size(); i++) {
                if (item.getID() == songs.get(c.getIndexByPosition(i)).getID() && item.getID() != c.getCurrentSong().getID()) {
                    songs.remove(c.getIndexByPosition(i));

                    if (i < c.getCurrentSongPosition()) {
                        c.setCurrentSong(c.getCurrentSongPosition() - 1);
                    }
                    if (c.getShuffleState() == MusicService.SHUFFLE_ON) {
                        List<Integer> shuffle = c.getShuffleIds();
                        int min = c.getIndexByPosition(i);
                        for (int j = 0; j < shuffle.size(); j++) {
                            if (shuffle.get(j) >= min) {
                                shuffle.set(j, shuffle.get(j) - 1);
                            }
                        }
                        shuffle.remove(i);
                    }
                    deleteSong(c, item.getID());
                    hasChanged = true;
                }
            }
        }
        return hasChanged;
    }

    public static void deleteSong(Context context, long id) {
        String selection = MediaStore.Audio.Media._ID + " = ?";
        String[] args = new String[]{String.valueOf(id)};
        delete(context, selection, args);
    }

    private static void delete(Context context, String selection, String[] args) {
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

    public void deletePlaylist(Context context, long id) {
        ContentResolver resolver = context.getContentResolver();
        String where = MediaStore.Audio.Playlists._ID + "=?";
        String[] whereVal = new String[]{String.valueOf(id)};
        resolver.delete(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, where, whereVal);
    }

}