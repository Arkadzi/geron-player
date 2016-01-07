package me.arkadiy.geronplayer.database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.plain.Song;

/**
 * Created by Arkadiy on 21.12.2015.
 */
public class SQLiteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "geron_database";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_SONGS = "songs";
    private static final String TABLE_POSITIONS = "positions";

    private static final String KEY_ID = "_id";
    private static final String SONG_ID = "song_id";
    private static final String POSITION = "position";

    private static SQLiteHelper mInstance;
    private String unknownArtist;

    public static SQLiteHelper getInstance(Context ctx) {
        if (mInstance == null) {
            mInstance = new SQLiteHelper(ctx.getApplicationContext());
        }
        return mInstance;
    }

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        try {
            unknownArtist = context.getResources().getString(R.string.unknown_artist);
        } catch (Exception e) {

        }
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "
                + TABLE_SONGS + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SONG_ID + " INTEGER)");
        db.execSQL("CREATE TABLE "
                + TABLE_POSITIONS + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + POSITION + " INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (DATABASE_VERSION < newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SONGS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_POSITIONS);
            onCreate(db);
        }
    }

    public void writeSongs(List<Song> songs) {
        SQLiteDatabase db = getWritableDatabase();

        db.execSQL("DELETE FROM " + TABLE_SONGS);
        if (songs != null) {
            ContentValues cv = new ContentValues();
            for (Song song : songs) {
                cv.put(SONG_ID, song.getID());
                db.insert(TABLE_SONGS, null, cv);
            }
        }
        db.close();
    }

    public void writePositions(List<Integer> positions) {
        SQLiteDatabase db = getWritableDatabase();

        db.execSQL("DELETE FROM " + TABLE_POSITIONS);
        if (positions != null) {
            ContentValues cv = new ContentValues();
            for (Integer position : positions) {
                cv.put(POSITION, position);
                db.insert(TABLE_POSITIONS, null, cv);
            }
        }
        db.close();
    }

    public List<Song> readSongs(Context ctx) {
        List<Long> songIds = new ArrayList<>();
        List<Song> songs = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery(String.format("SELECT %s FROM %s", SONG_ID, TABLE_SONGS), null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    songIds.add(c.getLong(0));
                } while (c.moveToNext());
            }
            c.close();
        }
        db.close();

        List<Song> allSongs = getSongs(ctx);
        int size = allSongs.size();
        for (Long id : songIds) {
            for (int i = 0; i < size; i++) {
                if (allSongs.get(i).getID() == id) {
                    songs.add(allSongs.get(i));
                    break;
                }
            }
        }

        return songs;
    }


    public List<Integer> readPositions() {
        List<Integer> positions = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery(String.format("SELECT %s FROM %s", POSITION, TABLE_POSITIONS), null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    positions.add(c.getInt(0));
                } while (c.moveToNext());
            }
            c.close();
        }
        db.close();

        return positions;
    }

    protected List<Song> getSongs(Context ctx) {
        ContentResolver resolver = ctx.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        Cursor musicCursor = resolver.query(
                uri, null, selection, null, null);
        ArrayList<Song> songs = new ArrayList<>();
        if (musicCursor != null && musicCursor.moveToFirst()) {
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int albumIdColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM_ID);
            int artistIdColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST_ID);
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int albumColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int songNumberColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.TRACK);
            do {
                long thisId = musicCursor.getLong(idColumn);
                long thisAlbumID = musicCursor.getLong(albumIdColumn);
                long thisArtistID = musicCursor.getLong(artistIdColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                if (thisArtist.equals("<unknown>")) {
                    thisArtist = unknownArtist;
                }
                String thisAlbum = musicCursor.getString(albumColumn);
                int thisTrack = musicCursor.getInt(songNumberColumn);
                String data = musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE));
                if (!data.startsWith("application")) {
                    Song newSong = new Song(thisTrack, thisId, thisTitle, thisAlbum, thisAlbumID, thisArtist, thisArtistID, uri);
                    songs.add(newSong);
                }
            }
            while (musicCursor.moveToNext());
            Collections.sort(songs, new Comparator<Song>() {
                public int compare(Song a, Song b) {
                    return a.getTitle().compareToIgnoreCase(b.getTitle());
                }
            });
        }
        if (musicCursor != null) {
            musicCursor.close();
        }
        return songs;
    }
}
