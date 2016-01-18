package me.arkadiy.geronplayer.adapters.list_view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.arkadiy.geronplayer.MusicService;
import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.plain.Song;
import me.arkadiy.geronplayer.statics.Utils;

/**
 * Created by Arkadiy on 13.07.2015.
 */
public class QueueAdapter extends BaseAdapter {
    private List<Song> songs;
    private LayoutInflater songInf;
    private int song;
    private int color;

    public QueueAdapter(Context c, List<Song> songs) {
        this.songs = songs;
        songInf = LayoutInflater.from(c);
        color = Utils.getColor(c, R.color.accent);
    }

    public void setSong(Song song) {
        for (int i = 0; i < songs.size(); i++) {
            if (songs.get(i).equals(song)) {
                this.song = i;
                break;
            }
        }
    }

    public void setSong(int song) {
        this.song = song;
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int i) {
        return songs.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LinearLayout songLay = (LinearLayout) songInf.inflate
                (R.layout.draggable_song, viewGroup, false);
        TextView songView = (TextView) songLay.findViewById(R.id.song_item_name);
        TextView artistView = (TextView) songLay.findViewById(R.id.song_item_artist);
        Song currSong = songs.get(i);
        songView.setText(currSong.getTitle());
        artistView.setText(currSong.getArtist());
        if (i == song) {
            songView.setTypeface(null, Typeface.BOLD);
            artistView.setTypeface(null, Typeface.BOLD);
//            songLay.setActivated(true);
//            songLay.setSelected(true);
            songView.setTextColor(color);
//            View playArrow = songLay.findViewById(R.id.play_arrow);
//            playArrow.setVisibility(View.VISIBLE);

        }
        songLay.setTag(i);
        return songLay;
    }

    public int getSong() {
        return song;
    }

    public void setSongs(List<Song> queue) {
        songs = queue;
    }
}
