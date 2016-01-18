package me.arkadiy.geronplayer.fragment.pager;

import android.os.Bundle;

import java.util.List;

import me.arkadiy.geronplayer.MainActivity;
import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.adapters.list_view.MyCategoryAdapter;
import me.arkadiy.geronplayer.adapters.list_view.MyPrefixCategoryAdapter;
import me.arkadiy.geronplayer.loader.AbstractLoader;
import me.arkadiy.geronplayer.loader.ArtistLoader;
import me.arkadiy.geronplayer.plain.Category;
import me.arkadiy.geronplayer.plain.Song;
import me.arkadiy.geronplayer.statics.DeleteUtils;
import me.arkadiy.geronplayer.statics.MusicRetriever;
import me.arkadiy.geronplayer.statics.TagManager;

/**
 * Created by Arkadiy on 03.11.2015.
 */
public class ArtistListFragment extends AbstractListFragment<Category> {

    private String param;

    public static ArtistListFragment newInstance(String param1) {
        ArtistListFragment fragment = new ArtistListFragment();
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
        return new ArtistLoader(getActivity(), param);
    }

    @Override
    protected int getColumnCount() {
        return 1;
    }

    @Override
    protected void setListener(final MyCategoryAdapter adapter) {
        adapter.setClickListener(new MyCategoryAdapter.ItemClickListener() {
            @Override
            public void onClick(int position) {
//                Log.e("category", getItem(position).toString());

                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container,
                                ToolbarFragment.newInstance(ToolbarFragment.ARTIST,
                                        getItem(position).getID(),
                                        getItem(position).getName(),
                                        null), getTag())
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    @Override
    protected MyCategoryAdapter<Category> getNewAdapter(List<Category> data) {
        return new MyPrefixCategoryAdapter(null,
                data,
                R.layout.icon_list_item,
                R.id.main,
                R.id.secondary,
                R.id.icon,
                getResources().getString(R.string.album_count),
                R.drawable.ic_action_artist);
    }


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
                showRenameDialog(getItem(position));
                break;
            case 5:
                showProgressDialog();
                new Thread() {
                    @Override
                    public void run() {
                        DeleteUtils deleteUtils = new DeleteUtils();
                        deleteUtils.deleteArtist(getActivity(), data.get(position).getID());
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
        tagManager.renameArtist(getActivity(), pojo);
        if (loader != null) {
            loader.notifyChanges();
        }
    }

    @Override
    protected List<Song> getSongs(int position) {
        return MusicRetriever.getSongsByArtist(getActivity(), data.get(position).getID());
    }
}
