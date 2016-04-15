package me.arkadiy.geronplayer.loader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.renderscript.RenderScript;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.plain.Category;
import me.arkadiy.geronplayer.statics.MusicRetriever;

public class ArtistLoader extends AbstractLoader<Category> {
    private String unknown;



    public ArtistLoader(Context context, String param) {
        super(context, param);
        try {
            this.unknown = context.getResources().getString(R.string.unknown_artist);
        } catch (Exception e) { }

    }

    @Override
    protected Uri getUri() {
        return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    }

    @Override
    protected List<Category> getList() {
        ArrayList<Category> categories = new ArrayList<>();

        String[] columns = {MediaStore.Audio.Artists._ID,
                MediaStore.Audio.Artists.ARTIST,
                MediaStore.Audio.Artists.NUMBER_OF_ALBUMS};
        Uri uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(uri, columns, null, null, null);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            do {
                long thisId = musicCursor.getLong(0);
                String thisName = musicCursor.getString(1);
                int thisAlbumCount = musicCursor.getInt(2);
                if (thisName.equals(MediaStore.UNKNOWN_STRING)) {
                    thisName = unknown;
                }
                Category newArtist = new Category(thisId, thisName, thisAlbumCount);
                long length = MusicRetriever.getLengthByArtist(getContext(), newArtist.getID());
                newArtist.setLength(length);
                categories.add(newArtist);

            } while (musicCursor.moveToNext());
            Collections.sort(categories, new Comparator<Category>() {
                public int compare(Category a, Category b) {
                    return a.getName().compareToIgnoreCase(b.getName());
                }
            });
        }
        if (musicCursor != null) {
            musicCursor.close();
        }
        return categories;
    }
}
