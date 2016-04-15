package me.arkadiy.geronplayer.statics;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagOptionSingleton;
import org.jaudiotagger.tag.flac.FlacTag;
import org.jaudiotagger.tag.images.AndroidArtwork;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.ArtworkFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;

import me.arkadiy.geronplayer.plain.Category;
import me.arkadiy.geronplayer.plain.Song;

public class TagManager {
    static {
        TagOptionSingleton.getInstance().setAndroid(true);
    }

    public String getLyrics(Song song) {
        try {
            File file = new File(song.getPath());
            AudioFile audiofile = AudioFileIO.read(file);
            Tag tag = audiofile.getTag();
            String lyrics = tag.getFirst(FieldKey.LYRICS);
            return lyrics;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public void rename(Context c, String path, FieldKey[] fields, String[] values) {
        AudioFile f = null;
        try {
            File file = new File(path);
            AudioFile audiofile = AudioFileIO.read(file);
            Tag tag = audiofile.getTag();
            Log.e("tag", String.valueOf(tag));
            for (int i = 0; i < fields.length; i++) {
                if (!values[i].isEmpty()) {
                    tag.setField(fields[i], values[i]);
                }
            }
            audiofile.setTag(tag);
            AudioFileIO.write(audiofile);
            c.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
        } catch (Exception e) {
            Log.e("Exception", e.toString());
        }
    }

    public void renamePlaylist(Context c, Category playlist) {
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Audio.Playlists.NAME, playlist.getName());
        c.getContentResolver().update(
                MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                cv,
                MediaStore.Audio.Playlists._ID + " = ?",
                new String[]{String.valueOf(playlist.getID())}
        );
    }

    public void renameGenre(Context c, Category genre) {
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Audio.Genres.NAME, genre.getName());
        c.getContentResolver().update(
                MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                cv,
                MediaStore.Audio.Genres._ID + " = ?",
                new String[]{String.valueOf(genre.getID())}
        );
    }


    public void setLyrics(Context c, String lyrics, Song song) {
        try {
            File file = new File(song.getPath());
            AudioFile audiofile = AudioFileIO.read(file);
            Tag tag = audiofile.getTag();
            tag.deleteField(FieldKey.LYRICS);
            tag.setField(FieldKey.LYRICS, lyrics);
            AudioFileIO.write(audiofile);
            c.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getField(String path, FieldKey key) {
        AudioFile f = null;
        File file = new File(path);
        AudioFile audiofile = null;
        try {
            audiofile = AudioFileIO.read(file);
            Tag tag = audiofile.getTag();
            return tag.getFirst(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
