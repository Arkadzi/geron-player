package me.arkadiy.geronplayer.fragment.pager;

import android.content.Context;
import android.os.Bundle;

import java.util.List;

import me.arkadiy.geronplayer.MusicService;
import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.adapters.list_view.MyCategoryAdapter;
import me.arkadiy.geronplayer.adapters.list_view.MyPrefixCategoryAdapter;
import me.arkadiy.geronplayer.loader.AbstractLoader;
import me.arkadiy.geronplayer.loader.GenreLoader;
import me.arkadiy.geronplayer.plain.Category;
import me.arkadiy.geronplayer.plain.Song;
import me.arkadiy.geronplayer.statics.DeleteUtils;
import me.arkadiy.geronplayer.statics.MusicRetriever;
import me.arkadiy.geronplayer.statics.TagManager;

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
                 ToolbarFragment.newInstance(ToolbarFragment.GENRE,
                         getItem(position).getID(),
                         getItem(position).getName(),
                         null))
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
                R.id.third,
                R.id.icon,
                getResources().getString(R.string.album_count),
                R.drawable.ic_music_note_white_36dp);
    }

    @Override
    protected void onRename(Category pojo) {
        TagManager tagManager = new TagManager();
        tagManager.renameGenre(getActivity(), pojo);
    }

    @Override
    protected List<Song> getSongs(Context c, Category category) {
        return MusicRetriever.getSongsByGenre(c, category.getID());
    }
}
