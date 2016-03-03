package me.arkadiy.geronplayer.loader;

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
import me.arkadiy.geronplayer.plain.Song;
import me.arkadiy.geronplayer.statics.MusicRetriever;

public class AllSongLoader extends AbstractLoader<Song> {

    public AllSongLoader(Context context, String param) {
        super(context, param);
    }

    @Override
    protected Uri getUri() {
        return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    }


    @Override
    public List<Song> getList() {
        Log.e("myloader", "AllSongLoader");
        return MusicRetriever.getAllSongs(getContext());
    }
}