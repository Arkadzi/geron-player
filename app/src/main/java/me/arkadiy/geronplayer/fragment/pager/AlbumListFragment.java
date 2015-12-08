package me.arkadiy.geronplayer.fragment.pager;

import android.content.res.Configuration;
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
    public final static int ARTIST = 10;
    public final static int GENRE = 11;
    private int mode;
//    private String param;
    private long id;

    public static AlbumListFragment newInstance(int mode, long id) {
        AlbumListFragment fragment = new AlbumListFragment();
        Bundle args = new Bundle();
//        args.putString("asd", param1);
        args.putInt("mode", mode);
        args.putLong("id", id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            param = getArguments().getString("asd");
            mode = getArguments().getInt("mode");
            id = getArguments().getLong("id");
            showScroller = false;
        }
    }

    @Override
    public AbstractLoader<Category> getNewLoader() {
        return AlbumLoader.getLoader(getActivity(), "", mode, id);
    }


    @Override
    protected int getColumnCount() {
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            return 2;
        else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            return 4;
        return 2;
    }

    @Override
    protected void setListener(MyCategoryAdapter adapter) {
        adapter.setListener(new MyCategoryAdapter.ItemListener() {
            @Override
            public void onClick(int position) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container,
                                ToolbarFragment.newInstance(ToolbarFragment.ALBUM,
                                        getItem(position).getID(),
                                        getItem(position).getName(), null))
                                        .addToBackStack(null)
                                        .commit();
            }
        });
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
                getResources().getString(R.string.song_count),
                -1);
    }
}
