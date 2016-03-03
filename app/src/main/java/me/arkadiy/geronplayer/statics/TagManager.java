package me.arkadiy.geronplayer.statics;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
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

    public String getLyrics(Song song) {
        try {
            TagOptionSingleton.getInstance().setAndroid(true);
            File file = new File(song.getPath());
            AudioFile audiofile = AudioFileIO.read(file);
            Tag tag = audiofile.getTag();
            String lyrics = tag.getFirst(FieldKey.LYRICS);
            Log.e("Lyrics", lyrics);
//            if (!TextUtils.isEmpty(lyrics)){
                return lyrics;
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public void rename(Context c, String path, FieldKey[] fields, String[] values) {
        AudioFile f = null;
        try {
            TagOptionSingleton.getInstance().setAndroid(true);
            File file = new File(path);
            AudioFile audiofile = AudioFileIO.read(file);
            Tag tag = audiofile.getTag();
            for (int i = 0; i < fields.length; i++) {
                tag.setField(fields[i], values[i]);
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
        Log.e("playlist id", String.valueOf(playlist.getID()));
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
        Log.e("genre id", String.valueOf(genre.getID()));
        cv.put(MediaStore.Audio.Genres.NAME, genre.getName());
        c.getContentResolver().update(
                MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                cv,
                MediaStore.Audio.Genres._ID + " = ?",
                new String[]{String.valueOf(genre.getID())}
        );
    }

    public void setArtwork(Context c, List<Song> songs, String artworkPath) {
        TagOptionSingleton.getInstance().setAndroid(true);
        try {
            Artwork a = ArtworkFactory.createArtworkFromFile(new File(
                    artworkPath));
            Artwork artwork = new AndroidArtwork();
            Bitmap b = Utils.getBitmap(Uri.fromFile(new File(artworkPath)));
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            artwork.setBinaryData(byteArray);
            artwork.setLinked(a.isLinked());
            artwork.setPictureType(a.getPictureType());
            artwork.setDescription(a.getDescription());
            artwork.setHeight(300);
            artwork.setWidth(300);
            artwork.setMimeType("image/jpg");
//            a.setLinked(!a.isLinked());
//            Log.e("artwork", String.valueOf(a));
            for (Song song : songs) {
                File file = new File(song.getPath());
                AudioFile f = AudioFileIO.read(file);
                Tag tag = f.getTag();
                if (!(tag instanceof FlacTag)) {
                    tag.deleteArtworkField();
                    tag.addField(a);
                    AudioFileIO.write(f);
                    c.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                }
            }
//            MediaScannerConnection.scanFile(c, new String[]{
//                    Environment.getExternalStorageDirectory().getAbsolutePath()}, null, null);

        } catch (Exception e) {
            Log.e("Exception", e.toString());
        }
    }

    public void setLyrics(Context c, String lyrics, Song song) {
        try {
            TagOptionSingleton.getInstance().setAndroid(true);
            File file = new File(song.getPath());
            AudioFile audiofile = AudioFileIO.read(file);
            Tag tag = audiofile.getTag();
            tag.deleteField(FieldKey.LYRICS);
            tag.setField(FieldKey.LYRICS, lyrics);
            AudioFileIO.write(audiofile);
            c.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
            Log.e("Lyrics", lyrics);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
