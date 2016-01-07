package me.arkadiy.geronplayer.adapters.list_view;

import android.widget.ImageView;

import java.util.List;

import me.arkadiy.geronplayer.plain.Folder;

/**
 * Created by Arkadiy on 06.11.2015.
 */
public class FolderAdapter extends MyCategoryAdapter<Folder> {

    private final int resourceId;

    public FolderAdapter(List<Folder> categories, int viewId, int mainId, int secondaryId, int imageId, int resourceId) {
        super(categories, viewId, mainId, secondaryId, imageId);
        this.resourceId = resourceId;
    }


    @Override
    protected void setImage(Folder element, ImageView image) {
        image.setImageResource(resourceId);
    }

    @Override
    protected String getMainText(Folder element) {
        return element.getName();
    }

    @Override
    protected String getSecondaryText(Folder element) {
        return element.getPath();
    }
}
