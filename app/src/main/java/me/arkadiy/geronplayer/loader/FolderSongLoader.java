package me.arkadiy.geronplayer.loader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.plain.Song;
import me.arkadiy.geronplayer.statics.MusicRetriever;

public class FolderSongLoader extends AbstractLoader<Song> {
    private final String path;

    public FolderSongLoader(Context context, String param, String path) {
        super(context, param);
        this.path = path;
    }

    @Override
    protected Uri getUri() {
        return MediaStore.Audio.Media.getContentUriForPath(path);
    }

    @Override
    protected List<Song> getList() {
        return MusicRetriever.getSongsByFolder(getContext(), path);
    }
}
