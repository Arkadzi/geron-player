package me.arkadiy.geronplayer.loader;

import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.List;

import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.plain.Folder;
import me.arkadiy.geronplayer.plain.Song;

/**
 * Created by Arkadiy on 08.11.2015.
 */
public class FolderSongLoader extends AbstractLoader<Song> {
    private String unknownArtist;

    public FolderSongLoader(Context context, String param) {
        super(context, param);
        try {
            this.unknownArtist = context.getResources().getString(R.string.unknown_artist);
        } catch (Exception e) { }
    }
    @Override
    protected Uri getUri() {
        return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    }

    @Override
    protected List<Song> getList() {
        return null;
    }
}
