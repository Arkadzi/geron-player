package me.arkadiy.geronplayer.plain;

/**
 * Created by Arkadiy on 03.11.2015.
 */
public class Category implements Nameable {
    private long id;
    private String name;
    private int count;

    public Category(long id, String name, int count) {
        this.id = id;
        this.name = name;
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Category && getID() == ((Category) o).getID();
    }

    @Override
    public String toString() {
        return name;
    }

    public long getID() {
        return id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }
}
