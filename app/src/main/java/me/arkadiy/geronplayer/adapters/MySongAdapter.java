package me.arkadiy.geronplayer.adapters;

import java.util.List;

import me.arkadiy.geronplayer.plain.Song;

/**
 * Created by Arkadiy on 06.11.2015.
 */
public class MySongAdapter extends MyCategoryAdapter<Song> {
    public MySongAdapter(List<Song> categories, int viewId, int mainId, int secondaryId) {
        super(categories, viewId, mainId, secondaryId);
    }

    @Override
    protected String getMainText(Song element) {
        return element.getTitle();
    }

    @Override
    protected String getSecondaryText(Song element) {
        return element.getArtist();
    }

}
