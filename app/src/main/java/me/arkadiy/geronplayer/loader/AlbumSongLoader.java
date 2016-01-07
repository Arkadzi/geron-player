package me.arkadiy.geronplayer.loader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.plain.Song;

/**
 * Created by Arkadiy on 11.11.2015.
 */
public class AlbumSongLoader extends AbstractLoader<Song> {

    private final long id;
    private String unknownArtist;

    public AlbumSongLoader(Context context, String param, long id) {
        super(context, param);
        this.id = id;
        try {
            this.unknownArtist = context.getResources().getString(R.string.unknown_artist);
        } catch (Exception e) { }
    }

    @Override
    protected Uri getUri() {
        return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    }

    @Override
    public List<Song> getList() {
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0 AND " + MediaStore.Audio.Media.ALBUM_ID + " = ?";
        String[] args = new String[] {Long.toString(id)};
        Cursor musicCursor = musicResolver.query(getUri(), null, selection, args, null);
        ArrayList<Song> songs = new ArrayList<>();
        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
//            int playlist = musicCursor.getColumnIndex(MediaStore.Audio.Playlists.NAME);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
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
                    (MediaStore.Audio.Media.TRACK);
            int pathColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int filenameColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
            //add songList to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                long thisAlbumID = musicCursor.getLong(albumIdColumn);
                long thisArtistID = musicCursor.getLong(artistIdColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                if (thisArtist.equals(MediaStore.UNKNOWN_STRING)) {
                    thisArtist = unknownArtist;
                }
                String thisAlbum = musicCursor.getString(albumColumn);
                int thisTrack = musicCursor.getInt(songNumberColumn);
//                int key = musicCursor.getInt(playlist);
                String data = musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE));
                if (!data.startsWith("application")) {
                    Song newSong = new Song(thisTrack, thisId, thisTitle, thisAlbum, thisAlbumID, thisArtist, thisArtistID, getUri());
                    songs.add(newSong);
                }
            }
            while (musicCursor.moveToNext());
            Collections.sort(songs, new Comparator<Song>() {
                public int compare(Song a, Song b) {
                    return a.getTrack() - b.getTrack();
                }
            });
        }
        if (musicCursor != null) {
            musicCursor.close();
        }
        return songs;
    }
}
