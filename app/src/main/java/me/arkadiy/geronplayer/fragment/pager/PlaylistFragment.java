package me.arkadiy.geronplayer.fragment.pager;

import android.content.Context;
import android.os.Bundle;

import java.util.List;

import me.arkadiy.geronplayer.MusicService;
import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.adapters.list_view.MyCategoryAdapter;
import me.arkadiy.geronplayer.adapters.list_view.MyPrefixCategoryAdapter;
import me.arkadiy.geronplayer.loader.AbstractLoader;
import me.arkadiy.geronplayer.loader.PlaylistLoader;
import me.arkadiy.geronplayer.plain.Category;
import me.arkadiy.geronplayer.plain.Song;
import me.arkadiy.geronplayer.statics.Constants;
import me.arkadiy.geronplayer.statics.DeleteUtils;
import me.arkadiy.geronplayer.statics.MusicRetriever;
import me.arkadiy.geronplayer.statics.TagManager;

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
                        .setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_in, R.anim.pop_out)
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
    protected MyCategoryAdapter<Category> getNewAdapter(List<Category> data) {
        return new MyPrefixCategoryAdapter(null,
                data,
                R.layout.icon_list_item,
                R.id.main,
                R.id.secondary,
                R.id.third,
                R.id.icon,
                getResources().getString(R.string.song_count),
                R.drawable.ic_grade_white_36dp);
    }

    @Override
    protected String[] menuItems() {
        return getResources().getStringArray(R.array.playlist_menu_items);
    }

    @Override
    protected boolean onMenuItemClick(final int position, int code) {
        boolean isHandled = super.onMenuItemClick(position, code);
        if (!isHandled) {
            if (code == Constants.MENU.EDIT) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_in, R.anim.pop_out)
                        .replace(R.id.fragment_container,
                                ToolbarFragment.newInstance(ToolbarFragment.PLAYLIST_EDIT,
                                        getItem(position).getID(),
                                        getItem(position).getName(),
                                        null))
                        .addToBackStack(null)
                        .commit();
            }
        }
        return isHandled;
    }

    @Override
    protected void onRename(Category pojo) {
        TagManager tagManager = new TagManager();
        tagManager.renamePlaylist(getActivity(), pojo);
    }

    @Override
    protected boolean delete(MusicService c, Category item) {
        DeleteUtils utils = new DeleteUtils();
        utils.deletePlaylist(c, item.getID());
        return false;
    }

    @Override
    protected int[] menuCodes() {
        return new int[]{
                Constants.MENU.PLAY,
                Constants.MENU.PLAY_NEXT,
                Constants.MENU.ADD_TO_QUEUE,
                Constants.MENU.ADD_TO_PLAYLIST,
                Constants.MENU.EDIT,
                Constants.MENU.RENAME,
                Constants.MENU.DELETE
        };
    }

    @Override
    protected List<Song> getSongs(Context c, Category item) {
        return MusicRetriever.getSongsByPlaylist(c, item.getID());
    }
}
