package me.arkadiy.geronplayer.loader;

import android.content.Context;

import me.arkadiy.geronplayer.fragment.pager.AbstractListFragment;
import me.arkadiy.geronplayer.fragment.pager.AlbumListFragment;
import me.arkadiy.geronplayer.fragment.pager.SongListFragment;
import me.arkadiy.geronplayer.plain.Category;
import me.arkadiy.geronplayer.plain.Song;

/**
 * Created by Arkadiy on 10.11.2015.
 */
public class AlbumLoader {

    public static AbstractLoader<Category> getLoader(Context c, String param, int what, long id) {
        switch (what) {
            case AlbumListFragment.ARTIST:
                return new ArtistAlbumLoader(c, param, id);
            case AlbumListFragment.GENRE:
                return new GenreAlbumLoader(c, param, id);
        }
        return null;
    }
}
