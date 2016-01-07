package me.arkadiy.geronplayer.statics;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.plain.Song;

/**
 * Created by Arkadiy on 27.12.2015.
 */
public class MusicRetriever {

    private static List<Song> getSongs(Context c, String selection, String[] args, Uri uri, String path, boolean forPlaylist) {
        String unknownArtist = c.getResources().getString(R.string.unknown_artist);
        ContentResolver musicResolver = c.getContentResolver();
        Cursor musicCursor = musicResolver.query(uri, null, selection, args, null);
        ArrayList<Song> songs = new ArrayList<>();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
//            int playlist = musicCursor.getColumnIndex(MediaStore.Audio.Playlists.NAME);
            int idColumn = musicCursor.getColumnIndex
                    (forPlaylist ? MediaStore.Audio.Playlists.Members.AUDIO_ID : android.provider.MediaStore.Audio.Media._ID);
            int albumIdColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM_ID);
            int artistIdColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST_ID);
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int albumColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int songNumberColumn = musicCursor.getColumnIndex
                    (forPlaylist ? MediaStore.Audio.Playlists.Members.PLAY_ORDER : MediaStore.Audio.Media.TRACK);
            int pathColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATA);
            int typeColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE);
            //add songList to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                long thisAlbumID = musicCursor.getLong(albumIdColumn);
                long thisArtistID = musicCursor.getLong(artistIdColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisPath = musicCursor.getString(pathColumn);
                String data = musicCursor.getString(typeColumn);
                if (thisArtist.equals("<unknown>")) {
                    thisArtist = unknownArtist;
                }
                String thisAlbum = musicCursor.getString(albumColumn);
                int thisTrack = musicCursor.getInt(songNumberColumn);
//                int key = musicCursor.getInt(playlist);
                if (!data.startsWith("application") && ((path == null) || thisPath.startsWith(path))) {
                    Log.e("artist", thisTitle + " " + thisAlbum + " " + thisTrack);
                    Song newSong = new Song(thisTrack, thisId, thisTitle, thisAlbum, thisAlbumID, thisArtist, thisArtistID, musicUri);
                    songs.add(newSong);
                }
            }
            while (musicCursor.moveToNext());
            if (path == null)
                Collections.sort(songs, new Comparator<Song>() {
                    public int compare(Song a, Song b) {
                        return a.getTrack() - b.getTrack();
                    }
                });
            else
                Collections.sort(songs, new Comparator<Song>() {
                    public int compare(Song a, Song b) {
                        return a.getTitle().compareTo(b.getTitle());
                    }
                });
        }
        if (musicCursor != null) {
            musicCursor.close();
        }
        return songs;
    }

    public static List<Song> getSongsByAlbum(Context c, long id) {
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0 AND " + MediaStore.Audio.Media.ALBUM_ID + " = ?";
        String[] args = new String[]{Long.toString(id)};
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        return getSongs(c, selection, args, uri, null, false);
    }

    public static List<Song> getSongsByArtist(Context c, long id) {
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0 AND " + MediaStore.Audio.Media.ARTIST_ID + " = ?";
        String[] args = new String[]{Long.toString(id)};
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        return getSongs(c, selection, args, uri, null, false);
    }

    public static List<Song> getSongsByGenre(Context context, long id) {
        List<Song> songs = new ArrayList<>();
        List<Long> albumIds = getAlbumIds(context, id);
        for (Long albumId : albumIds) {
            songs.addAll(getSongsByAlbum(context, albumId));
        }
        return songs;
    }

    public static List<Song> getSongsByPlaylist(Context c, long id) {
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
         Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", id);
        return getSongs(c, selection, null, uri, null, true);
    }


    public static List<Song> getSongsByFolder(Context context, String path) {
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        Log.e("path", path);
        path += File.separator;
        Uri uri = MediaStore.Audio.Media.getContentUriForPath(path);
        return getSongs(context, selection, null, uri, path, false);
    }

    @NonNull
    public static List<Long> getAlbumIds(Context context, long id) {
        List<Long> albumIds = new ArrayList<>();
        Uri uri = MediaStore.Audio.Genres.Members.getContentUri("external", id);
        ContentResolver musicResolver = context.getContentResolver();
        Cursor albumCursor = musicResolver.query(
                uri,
                new String[]{"distinct " + MediaStore.Audio.Genres.Members.ALBUM_ID},
                null,
                null,
                null);
        if (albumCursor != null && albumCursor.moveToFirst()) {
            do {
                long albumId = albumCursor.getLong(0);
                albumIds.add(albumId);
            } while (albumCursor.moveToNext());
        }
        if (albumCursor != null) {
            albumCursor.close();
        }
        return albumIds;
    }
}
