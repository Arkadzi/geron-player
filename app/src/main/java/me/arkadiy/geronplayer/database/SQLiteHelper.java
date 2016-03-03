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
import me.arkadiy.geronplayer.statics.MusicRetriever;

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

        List<Song> allSongs = MusicRetriever.getAllSongs(ctx);
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

    public void clear() {
        SQLiteDatabase db = getWritableDatabase();

        db.execSQL("DELETE FROM " + TABLE_SONGS);
        db.execSQL("DELETE FROM " + TABLE_POSITIONS);
        db.close();
    }
}
