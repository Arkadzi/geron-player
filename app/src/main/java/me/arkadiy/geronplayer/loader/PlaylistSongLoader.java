package me.arkadiy.geronplayer.loader;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.plain.Category;
import me.arkadiy.geronplayer.plain.Song;

/**
 * Created by Arkadiy on 08.11.2015.
 */
public class PlaylistSongLoader extends AbstractLoader<Song>{
    private final long playlistId;
    private String unknownArtist;

    public PlaylistSongLoader(Context context, String param, long id) {
        super(context, param);
        this.playlistId = id;
        try {
            this.unknownArtist = context.getResources().getString(R.string.unknown_artist);
        } catch (Exception e) { }
    }

    @Override
    protected Uri getUri() {
        return MediaStore.Audio.Playlists.Members.getContentUri("external", Long.valueOf(playlistId));
    }

    @Override
    protected List<Song> getList() {
        ArrayList<Song> songs = new ArrayList<>();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Audio.Playlists.Members.AUDIO_ID, //0
                MediaStore.Audio.Media.TITLE,//1
                MediaStore.Audio.Media.ALBUM_ID,  // 2
                MediaStore.Audio.Media.ALBUM,//3
                MediaStore.Audio.Media.ARTIST_ID,//4
                MediaStore.Audio.Media.ARTIST,//5
                MediaStore.Audio.Playlists.Members.PLAY_ORDER//6

        };

        Uri uri = getUri();
        final Cursor musicCursor = musicResolver.query(
                uri,
                projection, null, null, null);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            do {
                String artist = musicCursor.getString(5);
                if (artist.equals("<unknown>")) {
                    artist = unknownArtist;
                }
                Song newSong = new Song(
                        musicCursor.getInt(6),
                        musicCursor.getLong(0),
                        musicCursor.getString(1),
                        musicCursor.getString(3),
                        musicCursor.getInt(2),
                        artist,
                        musicCursor.getInt(4),
                        musicUri);
                songs.add(newSong);
            } while (musicCursor.moveToNext());
            musicCursor.close();
            Collections.sort(songs, new Comparator<Song>() {
                public int compare(Song a, Song b) {
                    return a.getTrack() - b.getTrack();
                }
            });
        }
        return songs;
    }
}
