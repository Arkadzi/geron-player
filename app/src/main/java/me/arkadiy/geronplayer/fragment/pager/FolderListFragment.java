package me.arkadiy.geronplayer.fragment.pager;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.List;

import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.adapters.FolderAdapter;
import me.arkadiy.geronplayer.adapters.MyCategoryAdapter;
import me.arkadiy.geronplayer.loader.AbstractLoader;
import me.arkadiy.geronplayer.loader.FolderLoader;
import me.arkadiy.geronplayer.plain.Folder;

/**
 * Created by Arkadiy on 06.11.2015.
 */
public class FolderListFragment extends AbstractListFragment<Folder> {
    private String param;

    public static FolderListFragment newInstance(String param1) {
        FolderListFragment fragment = new FolderListFragment();
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
    public AbstractLoader<Folder> getNewLoader() {
        return new FolderLoader(getActivity(), param);
    }

    @Override
    protected void setListener(MyCategoryAdapter adapter) {
        adapter.setListener(new MyCategoryAdapter.ItemListener() {
            @Override
            public void onClick(int position) {
                Log.e("FolderListFragment", "onClick");
            }
        });
    }

    @Override
    protected MyCategoryAdapter<Folder> getNewAdapter(List<Folder> data) {
        return new FolderAdapter(data, R.layout.folder_item, R.id.main, R.id.secondary);
    }
}
