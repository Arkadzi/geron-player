package me.arkadiy.geronplayer.plain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arkadiy on 06.11.2015.
 */
public class Folder {
    private String name;
    private String path;
//    private List<Song> songs;

    public Folder(String name, String path) {
        this.name = name;
        this.path = path;
//        songs = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

//    public List<Song> getSongs() {
//        return songs;
//    }
}
