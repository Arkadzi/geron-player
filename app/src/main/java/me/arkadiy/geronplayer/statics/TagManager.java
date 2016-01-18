package me.arkadiy.geronplayer.statics;

import android.content.ContentValues;
import android.content.Context;
import android.provider.MediaStore;
import android.util.Log;

import me.arkadiy.geronplayer.plain.Category;
import me.arkadiy.geronplayer.plain.Song;

/**
 * Created by Arkadiy on 07.01.2016.
 */
public class TagManager {
    public void renameSong(Context c, Song song) {
        ContentValues cv = new ContentValues();
        Log.e("Song id", String.valueOf(song.getID()));
        cv.put(MediaStore.Audio.Media.TITLE, song.getTitle());
        c.getContentResolver().update(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                cv,
                MediaStore.Audio.Media._ID + " = ?",
                new String[]{String.valueOf(song.getID())}
        );
    }

    public void renameSongArtist(Context c, Song song) {
        ContentValues cv = new ContentValues();
        Log.e("Song id", String.valueOf(song.getID()));
        cv.put(MediaStore.Audio.Media.ARTIST, song.getArtist());
        c.getContentResolver().update(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                cv,
                MediaStore.Audio.Media._ID + " = ?",
                new String[]{String.valueOf(song.getID())}
        );
    }

    public void renameAlbumArtist(Context c, String newName, long albumId) {
        ContentValues cv = new ContentValues();
        Log.e("Song id", String.valueOf(albumId));
        cv.put(MediaStore.Audio.Media.ARTIST, newName);
        c.getContentResolver().update(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                cv,
                MediaStore.Audio.Media.ALBUM_ID + " = ?",
                new String[]{String.valueOf(albumId)}
        );
    }

    public void renameSongAlbum(Context c, Song song) {
        ContentValues cv = new ContentValues();
        Log.e("Song id", String.valueOf(song.getID()));
        cv.put(MediaStore.Audio.Media.ALBUM, song.getAlbum());
        c.getContentResolver().update(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                cv,
                MediaStore.Audio.Media._ID + " = ?",
                new String[]{String.valueOf(song.getID())}
        );
    }

    public void renameAlbum(Context c, Category album) {
        ContentValues cv = new ContentValues();
        Log.e("Album id", String.valueOf(album.getID()));
        cv.put(MediaStore.Audio.Albums.ALBUM, album.getName());
        c.getContentResolver().update(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                cv,
                MediaStore.Audio.Albums.ALBUM_ID + " = ?",
                new String[]{String.valueOf(album.getID())}
        );
    }

    public void renameArtist(Context c, Category artist) {
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Audio.ArtistColumns.ARTIST, artist.getName());
        Log.e("Artist id", String.valueOf(artist.getID()));
        c.getContentResolver().update(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                cv,
                MediaStore.Audio.AudioColumns.ARTIST_ID + " = ?",
                new String[]{String.valueOf(artist.getID())}
        );
    }

    public void renamePlaylist(Context c, Category playlist) {
        ContentValues cv = new ContentValues();
        Log.e("playlist id", String.valueOf(playlist.getID()));
        cv.put(MediaStore.Audio.Playlists.NAME, playlist.getName());
        c.getContentResolver().update(
                MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                cv,
                MediaStore.Audio.Playlists._ID + " = ?",
                new String[]{String.valueOf(playlist.getID())}
        );
    }

    public void renameGenre(Context c, Category genre) {
        ContentValues cv = new ContentValues();
        Log.e("genre id", String.valueOf(genre.getID()));
        cv.put(MediaStore.Audio.Genres.NAME, genre.getName());
        c.getContentResolver().update(
                MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                cv,
                MediaStore.Audio.Genres._ID + " = ?",
                new String[]{String.valueOf(genre.getID())}
        );
    }
}
