package me.arkadiy.geronplayer.adapters.list_view;

import android.widget.ImageView;

import java.util.List;

import me.arkadiy.geronplayer.adapters.list_view.MyCategoryAdapter;
import me.arkadiy.geronplayer.plain.Song;
import me.arkadiy.geronplayer.statics.Utils;

/**
 * Created by Arkadiy on 06.11.2015.
 */
public class MySongAdapter extends MyCategoryAdapter<Song> {
    public MySongAdapter(List<Song> categories, int viewId, int mainId, int secondaryId, int thirdId) {
        super(categories, viewId, mainId, secondaryId, thirdId, 0);
    }

    @Override
    protected void setImage(Song element, ImageView image) {

    }

    @Override
    protected String getMainText(Song element) {
        return element.getTitle();
    }

    @Override
    protected String getSecondaryText(Song element) {
        return element.getArtist();
    }

    @Override
    protected String getThirdText(Song element) {
        return Utils.formatMillis(element.getLength());
    }

}
