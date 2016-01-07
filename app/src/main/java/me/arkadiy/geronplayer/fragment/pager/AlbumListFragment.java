package me.arkadiy.geronplayer.fragment.pager;

import android.content.res.Configuration;
import android.os.Bundle;

import java.util.List;

import me.arkadiy.geronplayer.MainActivity;
import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.adapters.list_view.MyCategoryAdapter;
import me.arkadiy.geronplayer.adapters.list_view.MyPrefixCategoryAdapter;
import me.arkadiy.geronplayer.loader.AbstractLoader;
import me.arkadiy.geronplayer.loader.AlbumLoader;
import me.arkadiy.geronplayer.plain.Category;
import me.arkadiy.geronplayer.plain.Song;
import me.arkadiy.geronplayer.statics.DeleteUtils;
import me.arkadiy.geronplayer.statics.MusicRetriever;
import me.arkadiy.geronplayer.statics.TagManager;

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
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            return 2;
        else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            return 4;
        return 2;
    }

    @Override
    protected void setListener(MyCategoryAdapter adapter) {
        adapter.setClickListener(new MyCategoryAdapter.ItemClickListener() {
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
    protected MyCategoryAdapter<Category> getNewAdapter(List<Category> data) {
        return new MyPrefixCategoryAdapter(getActivity(),
                data,
                R.layout.album_item,
                R.id.main,
                R.id.secondary,
                R.id.item_image,
                getResources().getString(R.string.song_count),
                -1);
    }

    @Override
    protected void onRename(Category pojo) {
        TagManager tagManager = new TagManager();
        tagManager.renameAlbum(getActivity(), pojo);
    }

    @Override
    protected List<Song> getSongs(int position) {
        return MusicRetriever.getSongsByAlbum(getActivity(), data.get(position).getID());
    }

    @Override
    protected String[] menuItems() {
        return getResources().getStringArray(R.array.album_menu_items);
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
                        deleteUtils.deleteAlbum(getActivity(), data.get(position).getID());
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
}
