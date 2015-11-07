package me.arkadiy.geronplayer.fragment.pager;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.List;

import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.adapters.MyCategoryAdapter;
import me.arkadiy.geronplayer.adapters.MyPrefixSecondaryTextAdapter;
import me.arkadiy.geronplayer.loader.AbstractLoader;
import me.arkadiy.geronplayer.loader.GenreLoader;
import me.arkadiy.geronplayer.plain.Category;

/**
 * Created by Arkadiy on 06.11.2015.
 */
public class GenreListFragment extends AbstractListFragment<Category> {
    private String param;

    public static GenreListFragment newInstance(String param1) {
        GenreListFragment fragment = new GenreListFragment();
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
    public AbstractLoader<Category> getNewLoader() {
        return new GenreLoader(getActivity(), param);
    }

    @Override
    protected void setListener(MyCategoryAdapter adapter) {
        adapter.setListener(new MyCategoryAdapter.ItemListener() {
            @Override
            public void onClick(int position) {
                Log.e("GenreListFragment", "onClick");
            }
        });
    }

    @Override
    protected MyCategoryAdapter<Category> getNewAdapter(List<Category> data) {
        return new MyPrefixSecondaryTextAdapter(data,
                R.layout.genre_item,
                R.id.main,
                R.id.secondary,
                getResources().getString(R.string.album_count));
    }
}
