package me.arkadiy.geronplayer.adapters.list_view;

import android.content.Context;
import android.widget.ImageView;

import java.util.List;

import me.arkadiy.geronplayer.MainActivity;
import me.arkadiy.geronplayer.plain.Category;
import me.arkadiy.geronplayer.statics.Utils;

public class MyPrefixCategoryAdapter extends MyCategoryAdapter<Category> {
    private final String prefix;
    private final Context c;
    private final int resourceId;

    public MyPrefixCategoryAdapter(Context c, List<Category> categories, int viewId, int mainId,
                                   int secondaryId, int thirdId,  int imageId, String prefix, int resourceId) {
        super(categories, viewId, mainId, secondaryId, thirdId, imageId);
        this.prefix = prefix;
        this.resourceId = resourceId;
        this.c = c;
    }

    @Override
    protected void setImage(Category element, ImageView image) {
        if (c != null)
            Utils.getLoader(c).displayImage(Utils.getArtworks(element.getID()).toString(), image);
        else {
            image.setImageResource(resourceId);
        }
    }

    @Override
    protected String getMainText(Category element) {
        return element.getName();
    }

    @Override
    protected String getSecondaryText(Category element) {
        return prefix + element.getCount();
    }

    @Override
    protected String getThirdText(Category element) {
        return Utils.formatMillis(element.getLength());
    }
}
