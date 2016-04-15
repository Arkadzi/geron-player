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
import me.arkadiy.geronplayer.plain.Category;
import me.arkadiy.geronplayer.plain.Song;
import me.arkadiy.geronplayer.statics.MusicRetriever;

/**
 * Created by Arkadiy on 10.11.2015.
 */
public class ArtistAlbumLoader extends AbstractLoader<Category> {

    private final long artistId;

    public ArtistAlbumLoader(Context context, String param, long id) {
        super(context, param);
        this.artistId = id;
    }

    @Override
    protected Uri getUri() {
        return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    }

    @Override
    protected List<Category> getList() {
        ArrayList<Category> albums = new ArrayList<>();

        String[] projection = {MediaStore.Audio.Albums._ID, //0
                MediaStore.Audio.Artists.Albums.ALBUM,//1
                MediaStore.Audio.Artists.Albums.NUMBER_OF_SONGS  // 2
        };

        Uri uri = MediaStore.Audio.Artists.Albums.getContentUri("external", artistId);
        final Cursor musicCursor = musicResolver.query(
                uri,
                projection, null, null, null);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            do {
                Category newAlbum = new Category(musicCursor.getLong(0),
                        musicCursor.getString(1),
                        musicCursor.getInt(2));
                long length = MusicRetriever.getLengthByAlbum(getContext(), newAlbum.getID());
                newAlbum.setLength(length);

                albums.add(newAlbum);
            } while (musicCursor.moveToNext());

        }
        if (musicCursor != null) {
            musicCursor.close();
        }
        return albums;
    }

}