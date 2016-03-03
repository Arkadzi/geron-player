package me.arkadiy.geronplayer.statics;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.plain.Song;

public class MusicRetriever {
    private final static Comparator<Song> TRACK =  new Comparator<Song>() {
        public int compare(Song a, Song b) {
            return a.getTrack() - b.getTrack();
        }
    };
    private final static Comparator<Song> TITLE = new Comparator<Song>() {
        public int compare(Song a, Song b) {
            return a.getTitle().compareToIgnoreCase(b.getTitle());
        }
    };
    private final static Comparator<Song> FILE_NAME = new Comparator<Song>() {
        public int compare(Song a, Song b) {
            return a.getFileName().compareToIgnoreCase(b.getFileName());
        }
    };

    private static List<Song> getSongs(Context c, String selection, String[] args, Uri uri, String path, boolean forPlaylist, Comparator<Song> order) {
        String unknownArtist = c.getResources().getString(R.string.unknown_artist);
        ContentResolver musicResolver = c.getContentResolver();
        Cursor musicCursor = musicResolver.query(uri, null, selection, args, null);
        ArrayList<Song> songs = new ArrayList<>();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        if (musicCursor != null && musicCursor.moveToFirst()) {
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
            int pathColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int fileNameColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
            int lengthColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
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
                long thisLength = musicCursor.getLong(lengthColumn);
                String thisFileName = musicCursor.getString(fileNameColumn);
                if (thisArtist.equals(MediaStore.UNKNOWN_STRING)) {
                    thisArtist = unknownArtist;
                }
                String thisAlbum = musicCursor.getString(albumColumn);
                int thisTrack = musicCursor.getInt(songNumberColumn);
                Log.e("song", String.format("%s %s %d", thisTitle, data, thisLength));
                String folderPath = thisPath.substring(0, thisPath.indexOf(thisFileName));
                if (!data.startsWith("application") && ((path == null) || folderPath.equals(path))) {
                    Song newSong = new Song(thisTrack, thisId, thisTitle, thisAlbum, thisAlbumID, thisArtist, thisArtistID, musicUri);
                    newSong.setPath(thisPath);
                    newSong.setLength(thisLength);
                    newSong.setFileName(thisFileName);
                    songs.add(newSong);
                }
            }
            while (musicCursor.moveToNext());
            if (order != null) {
                Collections.sort(songs, order);
            }
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
        return getSongs(c, selection, args, uri, null, false, TRACK);
    }

    public static List<Song> getSongsByArtist(Context c, long id) {
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0 AND " + MediaStore.Audio.Media.ARTIST_ID + " = ?";
        String[] args = new String[]{Long.toString(id)};
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        return getSongs(c, selection, args, uri, null, false, TRACK);
    }

    public static List<Song> getSongsByGenre(Context context, long id) {
        List<Song> songs = new ArrayList<>();
        List<Long> albumIds = getAlbumIdsByGenre(context, id);
        for (Long albumId : albumIds) {
            songs.addAll(getSongsByAlbum(context, albumId));
        }
        return songs;
    }

    public static List<Song> getSongsByPlaylist(Context c, long id) {
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", id);
        return getSongs(c, selection, null, uri, null, true, TRACK);
    }


    public static List<Song> getSongsByFolder(Context context, String path) {
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        Log.e("path", path);
        Uri uri = MediaStore.Audio.Media.getContentUriForPath(path);
        return getSongs(context, selection, null, uri, path, false, FILE_NAME);
    }

    @NonNull
    public static List<Long> getAlbumIdsByGenre(Context context, long id) {
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

    public static List<Song> getAllSongs(Context context) {
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        return getSongs(context, selection, null, uri, null, false, TITLE);
    }

    public static long getLengthByAlbum(Context context, long id) {
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0 AND " + MediaStore.Audio.Media.ALBUM_ID + " = ?";
        String[] args = new String[]{Long.toString(id)};
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        return getLength(context, uri, selection, args);
    }

    public static long getLengthByArtist(Context context, long id) {
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0 AND " + MediaStore.Audio.Media.ARTIST_ID + " = ?";
        String[] args = new String[]{Long.toString(id)};
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        return getLength(context, uri, selection, args);
    }

    public static long getLengthByPlaylist(Context context, long id) {
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", id);
        return getLength(context, uri, selection, null);
    }

    public static long getLengthByGenre(Context context, long id) {
        List<Long> albumIds = getAlbumIdsByGenre(context, id);
        long length = 0;
        for (Long albumId : albumIds) {
            length += getLengthByAlbum(context, albumId);
        }
        return length;
    }

    private static long getLength(Context context, Uri uri, String selection, String[] args) {
        ContentResolver musicResolver = context.getContentResolver();

        String[] projection = new String[]{String.format("sum(%s)", MediaStore.Audio.Media.DURATION)};

        Cursor musicCursor = musicResolver.query(uri, projection, selection, args, null);
        long length = 0;
        if (musicCursor != null) {
            if (musicCursor.moveToFirst()) {
                length = musicCursor.getLong(0);
            }
            musicCursor.close();
        }
        return length;
    }

    public static long getLengthByFolder(Context context, String path) {
        ContentResolver musicResolver = context.getContentResolver();
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] projection = new String[]{
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME
        };

        Cursor musicCursor = musicResolver.query(uri, projection, selection, null, null);
        long length = 0;
        if (musicCursor != null) {
            if (musicCursor.moveToFirst()) {
                do {
                    String thisPath = musicCursor.getString(1);
                    String thisFileName = musicCursor.getString(2);
                    String folderPath = thisPath.substring(0, thisPath.indexOf(thisFileName));

                    if (folderPath.equals(path)) {
                        length += musicCursor.getLong(0);
                    }
                } while (musicCursor.moveToNext());
            }
            musicCursor.close();
        }
        return length;
    }

    public static List<Song> getSong(Context c, Uri uri) {
        return getSongs(c, null, null, uri, null, false, null);
    }
}
