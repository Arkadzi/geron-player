package me.arkadiy.geronplayer.loader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import me.arkadiy.geronplayer.loader.AbstractLoader;
import me.arkadiy.geronplayer.plain.Category;
import me.arkadiy.geronplayer.statics.PlaylistUtils;

/**
 * Created by Arkadiy on 06.11.2015.
 */
public class PlaylistLoader extends AbstractLoader<Category> {

    public PlaylistLoader(Context context, String param) {
        super(context, param);
    }

    @Override
    protected Uri getUri() {
        return MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
    }

    @Override
    protected List<Category> getList() {
        return PlaylistUtils.getPlaylists(getContext());
    }
}
