package me.arkadiy.geronplayer.fragment.pager;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import org.jaudiotagger.tag.FieldKey;

import java.util.List;

import me.arkadiy.geronplayer.MusicService;
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


    @Override
    protected void onRename(final Category pojo) {
        final Activity c = getActivity();
        showProgressDialog();
        new Thread() {
            @Override
            public void run() {
                TagManager tagManager = new TagManager();
                final List<Song> songs = MusicRetriever.getSongsByArtist(c, pojo.getID());
                for (Song song : songs) {
                    tagManager.rename(c, song.getPath(), new FieldKey[]{FieldKey.ARTIST}, new String[]{pojo.getName()});
                }
                c.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissDialog();
                    }
                });
            }
        }.start();
    }

    @Override
    protected List<Song> getSongs(Context c, int position) {
        return MusicRetriever.getSongsByArtist(c, data.get(position).getID());
    }
}
