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
import me.arkadiy.geronplayer.statics.MusicRetriever;

public class AlbumSongLoader extends AbstractLoader<Song> {

    private final long id;

    public AlbumSongLoader(Context context, String param, long id) {
        super(context, param);
        this.id = id;
    }

    @Override
    protected Uri getUri() {
        return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    }

    @Override
    public List<Song> getList() {
        return MusicRetriever.getSongsByAlbum(getContext(), id);
    }
}
