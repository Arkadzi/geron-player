package me.arkadiy.geronplayer;

import java.util.List;

/**
 * Created by Arkadiy on 03.11.2015.
 */
public interface Resetable<T> {
    void setData(List<T> data);
}
