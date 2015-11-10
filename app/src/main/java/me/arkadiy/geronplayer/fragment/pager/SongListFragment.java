package me.arkadiy.geronplayer.fragment.pager;

import android.os.Bundle;
import android.util.Log;

import java.util.List;

import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.adapters.MyCategoryAdapter;
import me.arkadiy.geronplayer.adapters.MySongAdapter;
import me.arkadiy.geronplayer.loader.AbstractLoader;
import me.arkadiy.geronplayer.loader.SongLoader;
import me.arkadiy.geronplayer.plain.Song;

public class SongListFragment extends AbstractListFragment<Song> {
    public final static int ALL = 0;
    public final static int PLAYLIST = 1;
    public final static int ALBUM = 2;
    public final static int FOLDER = 3;
    private int mode;
    private String param;
    private long id;

    public static SongListFragment newInstance(String param, int mode, long id, boolean showScroller) {
        SongListFragment fragment = new SongListFragment();
        Bundle args = new Bundle();
        args.putString("asd", param);
        args.putInt("mode", mode);
        args.putLong("id", id);
        args.putBoolean("scroller", showScroller);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            param = getArguments().getString("asd");
            mode = getArguments().getInt("mode");
            id = getArguments().getLong("id");
            showScroller = getArguments().getBoolean("scroller");
        }
    }

    @Override
    public AbstractLoader<Song> getNewLoader() {
        return SongLoader.getLoader(getActivity(), param, mode, id);
    }

    @Override
    protected int getColumnCount() {
        return 1;
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
    protected MyCategoryAdapter getNewAdapter(List<Song> data) {
        return new MySongAdapter(data,
                R.layout.song_item,
                R.id.main,
                R.id.secondary);
    }
}
