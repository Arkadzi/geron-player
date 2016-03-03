package me.arkadiy.geronplayer.audio;

import java.util.List;

import me.arkadiy.geronplayer.plain.Song;

/**
 * Created by Arkadiy on 28.11.2015.
 */
public interface SongControlListener {
    void onPreparedPlaying(Song song, int position, int index);
    void onStartPlaying(Song song);
    void onStopPlaying(Song song);
    void onError(Song song);
    void onRepeatStateChange(int state);
    void onShuffleStateChange(int state);
    void onQueueChanged(List<Song> songs, int position, int index);
}
