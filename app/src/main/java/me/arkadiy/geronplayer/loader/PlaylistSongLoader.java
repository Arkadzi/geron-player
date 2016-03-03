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
import me.arkadiy.geronplayer.statics.MusicRetriever;

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
        return MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
    }

    @Override
    protected List<Song> getList() {
        return MusicRetriever.getSongsByPlaylist(getContext(), playlistId);
    }
}
