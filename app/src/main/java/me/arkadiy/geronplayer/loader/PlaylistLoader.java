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
        ArrayList<Category> playlists = new ArrayList<>();
        String _id = MediaStore.Audio.Playlists._ID;
        String name = MediaStore.Audio.Playlists.NAME;
        String[] columns = {_id, name};
        Cursor musicCursor = musicResolver.query(getUri(), columns, null, null, null);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            do {
                int thisId = musicCursor.getInt(0);
                String thisPlaylist = musicCursor.getString(1);
                int numberOfSongs = numberOfSongs(thisId);
                Category playlist = new Category(thisId, thisPlaylist, numberOfSongs);
                playlists.add(playlist);
            } while (musicCursor.moveToNext());
            Collections.sort(playlists, new Comparator<Category>() {
                @Override
                public int compare(Category a, Category b) {
                    return a.getName().compareTo(b.getName());
                }
            });
        }
        return playlists;
    }

    private int numberOfSongs(int playlistId) {
        String[] cols = new String[]{
                "count(*)"
        };
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        Cursor cur = musicResolver.query(uri, cols, null, null, null);
        if (cur != null && cur.moveToFirst()) {
            return cur.getInt(0);
        }
        return 0;
    }
}
