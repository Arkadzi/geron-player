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

    public Folder(String name, String path) {
        this.name = name;
        this.path = path;
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

}
