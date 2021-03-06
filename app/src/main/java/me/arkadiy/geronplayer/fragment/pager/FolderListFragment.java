package me.arkadiy.geronplayer.fragment.pager;

import android.content.Context;
import android.os.Bundle;

import java.util.List;

import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.adapters.list_view.FolderAdapter;
import me.arkadiy.geronplayer.adapters.list_view.MyCategoryAdapter;
import me.arkadiy.geronplayer.loader.AbstractLoader;
import me.arkadiy.geronplayer.loader.FolderLoader;
import me.arkadiy.geronplayer.plain.Folder;
import me.arkadiy.geronplayer.plain.Song;
import me.arkadiy.geronplayer.statics.Constants;
import me.arkadiy.geronplayer.statics.MusicRetriever;

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
    protected int getColumnCount() {
        return 1;
    }

    @Override
    protected void setListener(MyCategoryAdapter adapter) {
        adapter.setClickListener(new MyCategoryAdapter.ItemClickListener() {
            @Override
            public void onClick(int position) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_in, R.anim.pop_out)
                        .replace(R.id.fragment_container,
                                ToolbarFragment.newInstance(ToolbarFragment.FOLDER,
                                        0,
                                        getItem(position).getName(),
                                        getItem(position).getPath()))
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    @Override
    protected MyCategoryAdapter<Folder> getNewAdapter(List<Folder> data) {
        return new FolderAdapter(data,
                R.layout.three_icon_list_item,
                R.id.main,
                R.id.secondary,
                R.id.third,
                R.id.icon,
                R.drawable.ic_folder_white_36dp);
    }

    @Override
    protected String[] menuItems() {
        return getResources().getStringArray(R.array.folder_menu_items);
    }

    @Override
    protected int[] menuCodes() {
        return new int[]{
                Constants.MENU.PLAY,
                Constants.MENU.PLAY_NEXT,
                Constants.MENU.ADD_TO_QUEUE,
                Constants.MENU.ADD_TO_PLAYLIST,
                Constants.MENU.DELETE
        };
    }

    @Override
    protected List<Song> getSongs(Context c, Folder folder) {
        return MusicRetriever.getSongsByFolder(c, folder.getPath());
    }
}
