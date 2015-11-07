package me.arkadiy.geronplayer.loader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.plain.Category;

/**
 * Created by Arkadiy on 06.11.2015.
 */
public class GenreLoader extends AbstractLoader<Category> {
    private String unknown;

    public GenreLoader(Context context, String param) {
        super(context, param);
        try {
            this.unknown = context.getResources().getString(R.string.unknown_artist);
        } catch (Exception e) {
        }
    }

    @Override
    protected Uri getUri() {
        return MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI;
    }

    @Override
    protected List<Category> getList() {
        ArrayList<Category> categories = new ArrayList<>();

        String[] columns = {MediaStore.Audio.Genres._ID,
                MediaStore.Audio.Genres.NAME};

        Cursor musicCursor = musicResolver.query(getUri(), columns, null, null, null);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            do {
                long thisId = musicCursor.getLong(0);
                String thisName = musicCursor.getString(1);
//                int thisAlbumCount = musicCursor.getInt(2);
                if (thisName.equals("<unknown>")) {
                    thisName = unknown;
                }
                int numberOfAlbums = numberOfAlbums(thisId, thisName);
                if (numberOfAlbums > 0)
                    categories.add(new Category(thisId, thisName, numberOfAlbums));

            } while (musicCursor.moveToNext());
            musicCursor.close();
            Collections.sort(categories, new Comparator<Category>() {
                public int compare(Category a, Category b) {
                    return a.getName().compareToIgnoreCase(b.getName());
                }
            });
        }
        return categories;
    }

    private int numberOfAlbums(long genreId, String genreName) {
        Cursor cursor = musicResolver.query(
                MediaStore.Audio.Genres.Members.getContentUri("external", Long.valueOf(genreId)),
                new String[]{"distinct " + MediaStore.Audio.Genres.Members.ALBUM_ID,
                        MediaStore.Audio.Genres.Members.ALBUM}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            return cursor.getCount();
        }
        return 0;
    }
}
