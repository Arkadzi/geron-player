package me.arkadiy.geronplayer.adapters.list_view;

import android.content.Context;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

import me.arkadiy.geronplayer.MainActivity;
import me.arkadiy.geronplayer.statics.Utils;
import me.arkadiy.geronplayer.plain.Category;

/**
 * Created by Arkadiy on 10.11.2015.
 */
public class MyPrefixCategoryAdapter extends MyCategoryAdapter<Category> {
    private final String prefix;
    private final Context c;
    private final int resourceId;

    public MyPrefixCategoryAdapter(Context c, List<Category> categories, int viewId, int mainId,
                                   int secondaryId, int imageId, String prefix, int resourceId) {
        super(categories, viewId, mainId, secondaryId, imageId);
        this.prefix = prefix;
        this.resourceId = resourceId;
        this.c = c;
    }

    @Override
    protected void setImage(Category element, ImageView image) {
        if (c != null)
//            Picasso.with(c)
//                    .load(Utils.getArtworks(element.getID()))
//                    .fit()
//                    .into(image);
            MainActivity.imageLoader.displayImage(Utils.getArtworks(element.getID()).toString(), image);
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
}
