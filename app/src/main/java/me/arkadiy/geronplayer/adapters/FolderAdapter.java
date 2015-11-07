package me.arkadiy.geronplayer.adapters;

import java.util.List;

import me.arkadiy.geronplayer.plain.Folder;

/**
 * Created by Arkadiy on 06.11.2015.
 */
public class FolderAdapter extends MyCategoryAdapter<Folder> {

    public FolderAdapter(List<Folder> categories, int viewId, int mainId, int secondaryId) {
        super(categories, viewId, mainId, secondaryId);
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
