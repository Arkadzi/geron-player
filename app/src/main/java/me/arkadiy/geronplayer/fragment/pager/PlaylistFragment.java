package me.arkadiy.geronplayer.fragment.pager;

import android.os.Bundle;

import java.util.List;

import me.arkadiy.geronplayer.MainActivity;
import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.adapters.list_view.MyCategoryAdapter;
import me.arkadiy.geronplayer.adapters.list_view.MyPrefixCategoryAdapter;
import me.arkadiy.geronplayer.loader.AbstractLoader;
import me.arkadiy.geronplayer.loader.PlaylistLoader;
import me.arkadiy.geronplayer.plain.Category;
import me.arkadiy.geronplayer.plain.Song;
import me.arkadiy.geronplayer.statics.DeleteUtils;
import me.arkadiy.geronplayer.statics.MusicRetriever;
import me.arkadiy.geronplayer.statics.TagManager;

/**
 * Created by Arkadiy on 06.11.2015.
 */
public class PlaylistFragment extends AbstractListFragment<Category> {

    private String param;

    public static PlaylistFragment newInstance(String param1) {
        PlaylistFragment fragment = new PlaylistFragment();
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
        return new PlaylistLoader(getActivity(), param);
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
                                ToolbarFragment.newInstance(ToolbarFragment.PLAYLIST,
                                        getItem(position).getID(),
                                        getItem(position).getName(),
                                        null))
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    @Override
    protected MyCategoryAdapter getNewAdapter(List<Category> data) {
        return new MyPrefixCategoryAdapter(null,
                data,
                R.layout.icon_list_item,
                R.id.main,
                R.id.secondary,
                R.id.icon,
                getResources().getString(R.string.song_count),
                R.drawable.ic_grade_white_36dp);
    }

    @Override
    protected String[] menuItems() {
        return getResources().getStringArray(R.array.playlist_menu_items);
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
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container,
                                ToolbarFragment.newInstance(ToolbarFragment.PLAYLIST_EDIT,
                                        getItem(position).getID(),
                                        getItem(position).getName(),
                                        null))
                        .addToBackStack(null)
                        .commit();
                break;
            case 5:
                showRenameDialog(getItem(position));
                break;
            case 6:
                showProgressDialog();
                new Thread() {
                    @Override
                    public void run() {
                        DeleteUtils deleteUtils = new DeleteUtils();
                        deleteUtils.deletePlaylist(getActivity(), data.get(position).getID());
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dismissDialog();
                            }
                        });
                    }
                }.start();
                break;
        }
    }

    @Override
    protected void onRename(Category pojo) {
        TagManager tagManager = new TagManager();
        tagManager.renamePlaylist(getActivity(), pojo);
    }

    @Override
    protected List<Song> getSongs(int position) {
        return MusicRetriever.getSongsByPlaylist(getActivity(), data.get(position).getID());
    }
}
