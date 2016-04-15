package me.arkadiy.geronplayer.fragment.pager;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mobeta.android.dslv.DragSortListView;

import java.util.ArrayList;
import java.util.List;

import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.adapters.list_view.DraggableAdapter;
import me.arkadiy.geronplayer.loader.PlaylistSongLoader;
import me.arkadiy.geronplayer.plain.Song;
import me.arkadiy.geronplayer.statics.PlaylistUtils;
import me.arkadiy.geronplayer.statics.Utils;

/**
 * Created by Arkadiy on 02.01.2016.
 */
public class PlaylistEditFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Song>> {
    private DragSortListView listView;
    private PlaylistSongLoader loader;
    private List<Song> songList;
    private boolean wasChanged;
    private DraggableAdapter adapter;
    private long playlistId;


    public static PlaylistEditFragment newInstance(long id) {
        PlaylistEditFragment fragment = new PlaylistEditFragment();
        Bundle args = new Bundle();
        args.putLong("id", id);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        playlistId = getArguments().getLong("id");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.accept_menu, menu);
        MenuItem item = menu.getItem(0);
        if (item != null) {
            Drawable drawable = item.getIcon();
            if (drawable != null) {
                // If we don't mutate the drawable, then all drawable's with this id will have a color
                // filter applied to it.
                drawable.mutate();
                drawable.setColorFilter(Utils.getColorAttribute(getActivity(), R.attr.colorControlNormal), PorterDuff.Mode.SRC_ATOP);
            }
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_ok) {
            final Context c = getActivity();
            final List<Song> songs = songList;
            final long playlistId = this.playlistId;
            if (wasChanged) {
                new Thread() {
                    @Override
                    public void run() {
                        PlaylistUtils.rewritePlaylist(c, playlistId, songs);
                    }
                }.start();
            }
            getActivity().onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        loader = null;
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_draggable_list, container, false);
        songList = new ArrayList<>();
        listView = (DragSortListView) view.findViewById(R.id.list);
        adapter = new DraggableAdapter(getActivity(), songList);
        listView.setDropListener(new DragSortListView.DropListener() {
            @Override
            public void drop(int from, int to) {
                Song song = songList.remove(from);
                songList.add(to, song);
                wasChanged = true;
                adapter.notifyDataSetChanged();
            }
        });
        listView.setRemoveListener(new DragSortListView.RemoveListener() {
            @Override
            public void remove(int i) {
                songList.remove(i);
                adapter.notifyDataSetChanged();
                wasChanged = true;
            }
        });
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
        loader = new PlaylistSongLoader(getActivity(), "asd", playlistId);
        return loader;
    }

    @Override
    public void onPause() {
        if (loader != null) {
            loader.unregisterObserver();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (loader != null) {
            loader.registerObserver();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onLoadFinished(Loader<List<Song>> loader, List<Song> data) {
        this.songList = data;
        if (listView != null) {
            listView.setAlpha(0);
            listView.animate().alpha(1);
            if (listView.getAdapter() == null) {
                adapter = new DraggableAdapter(getActivity(), songList);
                listView.setAdapter(adapter);

            } else {
                adapter.changeList(songList);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Song>> loader) {
        if (listView != null && listView.getAdapter() != null) {
            songList = new ArrayList<>();
            adapter.changeList(songList);
        }
    }
}
