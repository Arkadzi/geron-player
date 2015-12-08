package me.arkadiy.geronplayer.loader;

import android.content.Context;

import me.arkadiy.geronplayer.fragment.pager.SongListFragment;
import me.arkadiy.geronplayer.plain.Song;

/**
 * Created by Arkadiy on 08.11.2015.
 */
public class SongLoader {

    public static AbstractLoader<Song> getLoader(Context c, String param, int what, long id) {
        switch (what) {
            case SongListFragment.ALL:
                return new AllSongLoader(c, param);
            case SongListFragment.PLAYLIST:
                return new PlaylistSongLoader(c, param, id);
            case SongListFragment.FOLDER:
                return new FolderSongLoader(c, param, param);
            case SongListFragment.ALBUM:
                return new AlbumSongLoader(c, param, id);
        }
        return null;
    }
}
