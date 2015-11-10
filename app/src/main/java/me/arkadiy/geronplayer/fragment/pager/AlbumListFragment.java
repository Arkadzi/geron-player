package me.arkadiy.geronplayer.fragment.pager;

import android.os.Bundle;

import java.util.List;

import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.adapters.MyCategoryAdapter;
import me.arkadiy.geronplayer.adapters.MyPrefixCategoryAdapter;
import me.arkadiy.geronplayer.loader.AbstractLoader;
import me.arkadiy.geronplayer.loader.AlbumLoader;
import me.arkadiy.geronplayer.plain.Category;

/**
 * Created by Arkadiy on 10.11.2015.
 */
public class AlbumListFragment extends AbstractListFragment<Category> {
    public final static int ARTIST = 0;
    public final static int GENRE = 1;
    private int mode;
    private String param;
    private long id;

    public static AlbumListFragment newInstance(String param1, int mode, long id) {
        AlbumListFragment fragment = new AlbumListFragment();
        Bundle args = new Bundle();
        args.putString("asd", param1);
        args.putInt("mode", mode);
        args.putLong("id", id);
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
            showScroller = false;
        }
    }

    @Override
    public AbstractLoader<Category> getNewLoader() {
        return AlbumLoader.getLoader(getActivity(), param, mode, id);
    }


    @Override
    protected int getColumnCount() {
        return 2;
    }

    @Override
    protected void setListener(MyCategoryAdapter adapter) {

    }

    @Override
    protected MyCategoryAdapter getNewAdapter(List<Category> data) {
//        return AlbumAdapter();
        return new MyPrefixCategoryAdapter(getActivity(),
                data,
                R.layout.album_item,
                R.id.main,
                R.id.secondary,
                R.id.item_image,
                getResources().getString(R.string.song_count));
    }
}
