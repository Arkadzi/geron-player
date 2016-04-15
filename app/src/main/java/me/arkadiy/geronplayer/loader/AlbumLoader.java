package me.arkadiy.geronplayer.loader;

import android.content.Context;

import me.arkadiy.geronplayer.fragment.pager.AlbumListFragment;
import me.arkadiy.geronplayer.plain.Category;

/**
 * Created by Arkadiy on 10.11.2015.
 */
public class AlbumLoader {

    public static AbstractLoader<Category> getLoader(Context c, String param, int what, long id) {
        switch (what) {
            case AlbumListFragment.ARTIST:
                return new ArtistAlbumLoader(c, param, id);
        }
        return null;
    }
}
