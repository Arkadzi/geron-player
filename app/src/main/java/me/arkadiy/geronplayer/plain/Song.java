package me.arkadiy.geronplayer.plain;

import android.net.Uri;

public class Song implements Nameable {
    private long albumID;
    private long artistID;
    private int track;
    private long id;
    private String title;
    private String album;
    private Uri uri;
    private String artist;
    private String path;
    private long length;
    private String fileName;

    public Song(int track, long id, String title, String album, long albumID, String artist, long artistID, Uri uri) {
        this.track = track;
        this.id = id;
        this.title = title;
        this.uri = uri;
        this.album = album;
        this.albumID = albumID;
        this.artist = artist;
        this.artistID = artistID;
    }
    public long getAlbumID() {
        return albumID;
    }

    public long getArtistID() {
        return artistID;
    }

    public long getID() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getTrack() {
        return track;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Song && getID() == ((Song) o).getID();
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    public Uri getUri() {
        return uri;
    }

    public String getAlbum() {
        return album;
    }

    public String getArtist() {
        return artist;
    }

    @Override
    public String getName() {
        return title;
    }

    @Override
    public void setName(String name) {
        this.title = name;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public long getLength() {
        return length;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
