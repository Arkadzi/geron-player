package me.arkadiy.geronplayer.fragment.pager;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.List;

import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.adapters.MyCategoryAdapter;
import me.arkadiy.geronplayer.adapters.MySongAdapter;
import me.arkadiy.geronplayer.loader.AbstractLoader;
import me.arkadiy.geronplayer.loader.SongLoader;
import me.arkadiy.geronplayer.plain.Song;

public class SongListFragment extends AbstractListFragment<Song> {

    private String param;

    public static SongListFragment newInstance(String param1) {
        SongListFragment fragment = new SongListFragment();
        Bundle args = new Bundle();
        args.putString("asd", param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        param = getArguments().getString("asd");
    }

    @Override
    public AbstractLoader<Song> getNewLoader() {
        return new SongLoader(getActivity(), param);
    }


    @Override
    protected void setListener(MyCategoryAdapter adapter) {
        adapter.setListener(new MyCategoryAdapter.ItemListener() {
            @Override
            public void onClick(int position) {
                Log.e("SongListFragment", "onClick");
            }
        });
    }

    @Override
    protected MyCategoryAdapter<Song> getNewAdapter(List<Song> data) {
        return new MySongAdapter(data,
                R.layout.song_item,
                R.id.main,
                R.id.secondary);
    }
}
