package me.arkadiy.geronplayer.adapters.list_view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.plain.Song;
import me.arkadiy.geronplayer.views.RecyclerViewFastScroller;

/**
 * Created by Arkadiy on 28.10.2015.
 */
public class SongAdapter extends BaseAdapter  implements RecyclerViewFastScroller.BubbleTextGetter {
    private final LayoutInflater inflater;
    private List<Song> songs;

    public SongAdapter(Context c, List<Song> songs) {
        inflater = LayoutInflater.from(c);
        this.songs = songs;
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
        View v = inflater.inflate(R.layout.list_item, viewGroup, false);
        TextView songView = (TextView) v.findViewById(R.id.main);
        TextView artistView = (TextView) v.findViewById(R.id.secondary);
        Song currSong = songs.get(i);
        //get title and artist strings
        songView.setText(currSong.getTitle());
        artistView.setText(currSong.getArtist());
        //set position as tag
        v.setTag(i);
        return v;
    }

    public void setData(List<Song> data) {
        if (songs != null) {
            songs.clear();
        } else {
            songs = new ArrayList<Song>();
        }
        if (data != null) {
            songs.addAll(data);
        }
        notifyDataSetChanged();
    }


    @Override
    public String getTextToShowInBubble(int pos) {
        return Character.toString(songs.get(pos).getTitle().charAt(0));
    }
}
