package me.arkadiy.geronplayer.adapters.list_view;

import java.util.List;

import me.arkadiy.geronplayer.plain.Song;

/**
 * Created by Arkadiy on 12.02.2016.
 */
public class FolderSongAdapter extends MySongAdapter {
    public FolderSongAdapter(List<Song> categories, int viewId, int mainId, int secondaryId, int thirdId) {
        super(categories, viewId, mainId, secondaryId, thirdId);
    }

    @Override
    protected String getMainText(Song element) {
        return element.getFileName();
    }

    @Override
    protected String getSecondaryText(Song element) {
        return element.getName();
    }
}
