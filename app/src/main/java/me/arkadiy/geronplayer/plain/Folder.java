package me.arkadiy.geronplayer.plain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arkadiy on 06.11.2015.
 */
public class Folder implements Nameable {
    private String name;
    private String path;
    private long length;
//    private List<Song> songs;

    public Folder(String name, String path) {
        this.name = name;
        this.path = path;
//        songs = new ArrayList<>();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {

    }

    public void setLength(long length) {
        this.length = length;
    }

    public long getLength() {
        return length;
    }

    public String getPath() {
        return path;
    }

//    public List<Song> getSongs() {
//        return songs;
//    }
}
