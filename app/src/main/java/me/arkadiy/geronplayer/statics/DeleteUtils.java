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

import me.arkadiy.geronplayer.MainActivity;
import me.arkadiy.geronplayer.MusicService;
import me.arkadiy.geronplayer.plain.Song;

public class DeleteUtils {


    private static final Uri URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    public static boolean deleteSongs(MusicService c, List<Song> songsForDelete, List<Song> songs) {
//        DeleteUtils utils = new DeleteUtils();
        boolean hasChanged = false;
        Log.e("songs", String.valueOf(c.getShuffleIds()) + c.getCurrentSongIndex() + " " + c.getCurrentSongPosition());
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
                        Log.e("songs", String.valueOf(c.getShuffleIds()) + c.getCurrentSongIndex() + " " + c.getCurrentSongPosition());
                        int k = shuffle.remove(i);
                    }
                    deleteSong(c, item.getID());
                    Log.e("songs", String.valueOf(c.getShuffleIds()) + c.getCurrentSongIndex() + " " + c.getCurrentSongPosition());
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


//    public void deleteAlbum(Context context, long id) {
//        String selection = MediaStore.Audio.Media.ALBUM_ID + " = ?";
//        String[] args = new String[]{String.valueOf(id)};
//        delete(context, selection, args);
//    }

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

//    public void deleteArtist(Context context, long id) {
//        String selection = MediaStore.Audio.Media.ARTIST_ID + " = ?";
//        String[] args = new String[]{String.valueOf(id)};
//        delete(context, selection, args);
//    }
//
//    public void deleteGenre(Context context, long id) {
//        List<Long> albumIds = MusicRetriever.getAlbumIdsByGenre(context, id);
//
//        for (Long albumId : albumIds) {
//            deleteAlbum(context, albumId);
//        }
//
//    }
//
//    public void deleteFolder(Context context, String path) {
//        List<Song> songs = MusicRetriever.getSongsByFolder(context, path);
//        for (Song song : songs) {
//            deleteSong(context, song.getID());
//        }
//    }

    public void deletePlaylist(Context context, long id) {
        ContentResolver resolver = context.getContentResolver();
        String where = MediaStore.Audio.Playlists._ID + "=?";
        String[] whereVal = new String[]{String.valueOf(id)};
        resolver.delete(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, where, whereVal);
    }

}