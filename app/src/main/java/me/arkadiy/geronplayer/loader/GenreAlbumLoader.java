package me.arkadiy.geronplayer.loader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

import me.arkadiy.geronplayer.plain.Category;

public class GenreAlbumLoader extends AbstractLoader<Category> {

    private final long genreId;

    public GenreAlbumLoader(Context context, String param, long id) {
        super(context, param);
        this.genreId = id;
    }

    @Override
    protected Uri getUri() {
//        return MediaStore.Audio.Albums.getContentUri("external", genreId);
        return MediaStore.Audio.Genres.Members.getContentUri("external", genreId);
    }

    @Override
    protected List<Category> getList() {
        ArrayList<Category> albums = new ArrayList<>();
        Cursor albumCursor = albumIds();
        if (albumCursor != null && albumCursor.moveToFirst()) {
            do {
                long id = albumCursor.getLong(0);
                String name = albumCursor.getString(1);
                int count = numberOfSongs(id);
                albums.add(new Category(id, name, count));
            } while (albumCursor.moveToNext());
        }
        if (albumCursor != null) {
            albumCursor.close();
        }

        return albums;
    }

    private Cursor albumIds() {
        return musicResolver.query(
                getUri(),
                new String[]{"distinct " + MediaStore.Audio.Genres.Members.ALBUM_ID,
                        MediaStore.Audio.Genres.Members.ALBUM},
                null,
                null,
                null);
    }

    private int numberOfSongs(long albumId) {
        int number = 0;
        Cursor cursor = musicResolver.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Albums.NUMBER_OF_SONGS},
                MediaStore.Audio.Albums._ID + " = ?",
                new String[] {Long.toString(albumId)},
                null);
        if (cursor != null && cursor.moveToFirst()) {

            number = cursor.getInt(0);
        }
        if (cursor != null) {
            cursor.close();
        }

        return number;
    }
}
