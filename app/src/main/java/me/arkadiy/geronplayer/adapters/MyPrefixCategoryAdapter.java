package me.arkadiy.geronplayer.adapters;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

import me.arkadiy.geronplayer.plain.Category;

/**
 * Created by Arkadiy on 10.11.2015.
 */
public class MyPrefixCategoryAdapter extends MyCategoryAdapter<Category> {
    private final String prefix;
    private final Context c;

    public MyPrefixCategoryAdapter(Context c, List<Category> categories, int viewId, int mainId,
                                   int secondaryId, int imageId, String prefix) {
        super(categories, viewId, mainId, secondaryId, imageId);
        this.prefix = prefix;
        this.c = c;
    }

    @Override
    protected void setImage(Category element, ImageView image) {
        if (c != null)
            Picasso.with(c).load(getArtworks(element.getID())).into(image);
    }

    @Override
    protected String getMainText(Category element) {
        return element.getName();
    }

    @Override
    protected String getSecondaryText(Category element) {
        return prefix + element.getCount();
    }

    public static Uri getArtworks(long album_id) {
        Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri uri = ContentUris.withAppendedId(artworkUri, album_id);
        Log.e("uri", uri.toString());
        return uri;
    }
}
