package me.arkadiy.geronplayer;

import me.arkadiy.geronplayer.plain.Song;

/**
 * Created by Arkadiy on 28.11.2015.
 */
public interface SongControlListener {
    void onPreparedPlaying(Song song);
    void onStartPlaying(Song song);
    void onStopPlaying(Song song);
    void onError(Song song);
}
