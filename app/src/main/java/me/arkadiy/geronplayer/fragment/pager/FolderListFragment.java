package me.arkadiy.geronplayer.fragment.pager;

import android.os.Bundle;

import java.util.List;

import me.arkadiy.geronplayer.MainActivity;
import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.adapters.list_view.FolderAdapter;
import me.arkadiy.geronplayer.adapters.list_view.MyCategoryAdapter;
import me.arkadiy.geronplayer.loader.AbstractLoader;
import me.arkadiy.geronplayer.loader.FolderLoader;
import me.arkadiy.geronplayer.plain.Folder;
import me.arkadiy.geronplayer.plain.Song;
import me.arkadiy.geronplayer.statics.DeleteUtils;
import me.arkadiy.geronplayer.statics.MusicRetriever;

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
    protected MyCategoryAdapter getNewAdapter(List<Folder> data) {
        return new FolderAdapter(data,
                R.layout.three_icon_list_item,
                R.id.main,
                R.id.secondary,
                R.id.icon,
                R.drawable.ic_folder_white_36dp);
    }

    @Override
    protected String[] menuItems() {
        return getResources().getStringArray(R.array.folder_menu_items);
    }

    @Override
    protected void onMenuItemClick(final int position, int which) {
        switch (which) {
            case 0: {
                new Thread() {
                    @Override
                    public void run() {
                        final List<Song> songs = getSongs(position);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((MainActivity) getActivity()).playQueue(songs, 0);
                            }
                        });
                    }
                }.start();

            }
            break;
            case 1: {
                new Thread() {
                    @Override
                    public void run() {
                        final List<Song> songs = getSongs(position);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((MainActivity) getActivity()).addNext(songs);
                            }
                        });
                    }
                }.start();
            }
            break;
            case 2: {
                new Thread() {
                    @Override
                    public void run() {
                        final List<Song> songs = getSongs(position);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((MainActivity) getActivity()).addToQueue(songs);
                            }
                        });
                    }
                }.start();
            }
            break;
            case 3:
                showPlaylistDialog(position);
                break;
            case 4:
                final String path = data.get(position).getPath();
                showProgressDialog();
                new Thread() {
                    @Override
                    public void run() {
                        DeleteUtils deleteUtils = new DeleteUtils();
                        deleteUtils.deleteFolder(getActivity(), path);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dismissDialog();
                            }
                        });
                    }
                }.start();

        }
    }

    @Override
    protected List<Song> getSongs(int position) {
        return MusicRetriever.getSongsByFolder(getActivity(), data.get(position).getPath());
    }
}
