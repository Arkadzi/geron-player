package me.arkadiy.geronplayer.adapters;

import java.util.List;

import me.arkadiy.geronplayer.plain.Category;

/**
 * Created by Arkadiy on 06.11.2015.
 */
public class MyPrefixSecondaryTextAdapter extends MyCategoryAdapter<Category> {

    private final String prefix;

    public MyPrefixSecondaryTextAdapter(List<Category> categories, int viewId, int mainId, int secondaryId, String prefix) {
        super(categories, viewId, mainId, secondaryId);
        this.prefix = prefix;
    }

    @Override
    protected String getMainText(Category element) {
        return element.getName();
    }

    @Override
    protected String getSecondaryText(Category element) {
        return prefix + element.getCount();
    }
}
