package me.arkadiy.geronplayer.loader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

import me.arkadiy.geronplayer.plain.Category;
import me.arkadiy.geronplayer.plain.Song;
import me.arkadiy.geronplayer.statics.MusicRetriever;

public class GenreSongLoader extends AbstractLoader<Song> {

    private final long genreId;

    public GenreSongLoader(Context context, String param, long id) {
        super(context, param);
        this.genreId = id;
    }

    @Override
    protected Uri getUri() {
        return MediaStore.Audio.Genres.Members.getContentUri("external", genreId);
    }

    @Override
    protected List<Song> getList() {
        return MusicRetriever.getSongsByGenre(getContext(), genreId);
    }
}
