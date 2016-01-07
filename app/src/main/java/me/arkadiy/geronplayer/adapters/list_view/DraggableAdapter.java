package me.arkadiy.geronplayer.adapters.list_view;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.plain.Song;
import me.arkadiy.geronplayer.statics.Utils;

/**
 * Created by Arkadiy on 13.07.2015.
 */
public class DraggableAdapter extends BaseAdapter {
    private List<Song> songs;
    private LayoutInflater songInf;

    public DraggableAdapter(Context c, List<Song> songs) {
        this.songs = songs;
        songInf = LayoutInflater.from(c);
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
        songLay.setTag(i);
        return songLay;
    }

    public void changeList(List<Song> songList) {
        songs = songList;
        notifyDataSetChanged();
    }
}
