package me.arkadiy.geronplayer.fragment.pager;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.pluscubed.recyclerfastscroll.RecyclerFastScroller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import me.arkadiy.geronplayer.MainActivity;
import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.adapters.list_view.MyCategoryAdapter;
import me.arkadiy.geronplayer.loader.AbstractLoader;
import me.arkadiy.geronplayer.plain.Category;
import me.arkadiy.geronplayer.plain.Nameable;
import me.arkadiy.geronplayer.plain.Song;
import me.arkadiy.geronplayer.statics.DeleteUtils;
import me.arkadiy.geronplayer.statics.PlaylistUtils;
import me.arkadiy.geronplayer.statics.Utils;

/**
 * Created by Arkadiy on 03.11.2015.
 */
public abstract class AbstractListFragment<T extends Nameable> extends Fragment
        implements LoaderManager.LoaderCallbacks<List<T>>, SearchView.OnQueryTextListener, MainActivity.BackPressListener {
    protected boolean showScroller = true;
    protected RecyclerView mRecyclerView;
    protected AbstractLoader<T> loader;
    protected List<T> data;

    private MyCategoryAdapter<T> adapter;
    private Dialog menuDialog;
    private SearchView searchView;

    @Override
    public boolean onBackPress() {
        if (searchView != null && !searchView.isIconified()) {
            searchView.setIconified(true);
            return true;
        }
        return false;
    }

    public T getItem(int position) {
        T item = adapter.getItem(position);
        return item;
    }

    @Override
    public void onPause() {
        super.onPause();
        dismissDialog();
        ((MainActivity) getActivity()).removeListener(this);
//        if (loader != null) {
//            loader.unregisterObserver();
//        }
    }

    protected void dismissDialog() {
        if (menuDialog != null && menuDialog.isShowing()) {
            menuDialog.dismiss();
        }
        menuDialog = null;
    }

    @Override
    public Loader<List<T>> onCreateLoader(int id, Bundle args) {
        Log.e("AbstractList", "onCreateLoader()");
        loader = getNewLoader();
        loader.registerObserver();
        return loader;
    }

    public abstract AbstractLoader<T> getNewLoader();

    @Override
    public void onDestroy() {
        Log.e("AbstractList", "onDestroy()");
        if (loader != null) {
            loader.unregisterObserver();
        }
        loader = null;
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).addListener(this);
        Log.e("AbstractList", "onResume() " + loader);
//        if (loader != null) {
//            loader.registerObserver();
//        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (loader == null)
        getLoaderManager().restartLoader(0, null, this);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
    }

    @Override
    public void onDestroyOptionsMenu() {
        searchView.setIconified(true);
        super.onDestroyOptionsMenu();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_menu, menu);
        final MenuItem item = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextChange(String query) {
        final List<T> filteredModelList = filter(data, query);
        adapter.animateTo(filteredModelList);
        mRecyclerView.scrollToPosition(0);
        return true;
    }

    private List<T> filter(List<T> models, String query) {
        query = query.toLowerCase();

        final List<T> filteredModelList = new ArrayList<>();
        for (T model : models) {
            final String text = model.getName().toLowerCase();
            if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.list);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), getColumnCount(), GridLayoutManager.VERTICAL, false));
        RecyclerFastScroller fastScroller1 = (RecyclerFastScroller) view.findViewById(R.id.fast_scroller);
        fastScroller1.setHandleNormalColor(Utils.getColor(getActivity(), R.color.primaryLight));
        fastScroller1.setRecyclerView(mRecyclerView);
        data = new ArrayList<>();
        adapter = getNewAdapter(data);
        mRecyclerView.setAdapter(adapter);
        setListener(adapter);
        adapter.setLongClickListener(new MyCategoryAdapter.ItemLongClickListener() {
            @Override
            public void onLongClick(int position) {
                menuDialog = createMenuDialog(position);
                menuDialog.show();
            }
        });
        return view;
    }

    protected abstract int getColumnCount();

    protected abstract void setListener(MyCategoryAdapter adapter);

    protected abstract MyCategoryAdapter<T> getNewAdapter(List<T> data);

    @Override
    public void onLoaderReset(Loader<List<T>> loader) {
        Log.e("AbstractList", "onLoaderReset() " + data.size());
        if (mRecyclerView != null && mRecyclerView.getAdapter() != null) {
            data = new ArrayList<T>();
            adapter.setData(data);
        }
    }


    @Override
    public void onLoadFinished(Loader<List<T>> loader, List<T> data) {
        Log.e("AbstractList", "onLoaderFinished() " + data.size());
        this.data = data;
        if (mRecyclerView != null)
            if (mRecyclerView.getAdapter() == null) {
                adapter = getNewAdapter(data);
                mRecyclerView.setAdapter(adapter);
                setListener(adapter);
                adapter.setLongClickListener(new MyCategoryAdapter.ItemLongClickListener() {
                    @Override
                    public void onLongClick(int position) {
                        menuDialog = createMenuDialog(position);
                        menuDialog.show();
                    }
                });
            } else {
                adapter.setData(data);
            }
    }

    private Dialog createMenuDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(menuItems(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.e("Dialog", String.valueOf(which));
                onMenuItemClick(position, which);
            }
        });
        builder.setTitle(menuTitle(position));
        return builder.create();
    }

    protected void showPlaylistDialog(final int position) {

        new AsyncTask<Void, Void, Void>() {
            List<Song> songs;
            List<Category> playlists;
            @Override
            protected Void doInBackground(Void... params) {
                playlists = PlaylistUtils.getPlaylists(getActivity());
                songs = getSongs(position);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                String[] items = new String[playlists.size() + 1];
                items[0] = getResources().getString(R.string.create_playlist);
                for (int i = 0; i < playlists.size(); i++) {
                    items[i + 1] = playlists.get(i).getName();
                }

                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(menuTitle(position));
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, final int which) {
                        final Context c = getActivity();
                        if (which == 0) {
                            showCreateDialog(songs, c, position, playlists);
                        } else {
                            new Thread() {
                                @Override
                                public void run() {
                                    PlaylistUtils.addSongs(c, playlists.get(which - 1).getID(), songs);
                                }
                            }.start();
                        }
                    }
                });
                menuDialog = builder.create();
                menuDialog.show();
            }
        }.execute();

    }

    private void showCreateDialog(final List<Song> songs, final Context c, int position, final List<Category> playlists) {
        final Activity activity = getActivity();
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(menuTitle(position));
        final View view = activity.getLayoutInflater().inflate(R.layout.create_dialog, null);
        builder.setView(view);
        builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText mPlaylistName = (EditText) view.findViewById(R.id.playlist_name);
                String playlistName = mPlaylistName.getText().toString();
                if (!playlistName.isEmpty()) {
                    final String newName = handleName(playlistName, playlists);
                    new Thread() {
                        @Override
                        public void run() {
                            long id = PlaylistUtils.createPlaylist(c, newName);
                            if (id > -1) {
                                PlaylistUtils.addSongs(c, id, songs);
                            }
                        }
                    }.start();

                }
            }
        });
        menuDialog = builder.create();
        menuDialog.show();
    }

    protected void showProgressDialog() {
        ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage(getString(R.string.please_wait));
        dialog.setCancelable(false);
        menuDialog = dialog;
        menuDialog.show();
    }


    private String handleName(String playlistName, List<Category> playlists) {
        int additionalNumber = 0;
        int i;
        do {
            String name = (additionalNumber == 0) ?
                    playlistName :
                    String.format("%s %d", playlistName, additionalNumber);
            for (i = 0; i < playlists.size(); i++) {
                if (name.equals(playlists.get(i).getName())) {
                    additionalNumber++;
                    break;
                }
            }
        } while (i < playlists.size());
        if (additionalNumber != 0) {
            playlistName = String.format("%s %d", playlistName, additionalNumber);
        }
        return playlistName;
    }

    protected void showRenameDialog(final T pojo) {
        menuDialog = getRenameDialog(pojo);
        menuDialog.show();
    }

    protected Dialog getRenameDialog(final T pojo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.create_dialog, null);
        final EditText mName = (EditText) view.findViewById(R.id.playlist_name);
        mName.setHint(R.string.new_name);
        mName.setText(pojo.getName());
        builder.setView(view);
        builder.setPositiveButton(R.string.action_accept, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = mName.getText().toString();
                if (!newName.isEmpty()) {
                    pojo.setName(newName);
                    onRename(pojo);
                }
            }
        });
        return builder.create();
    }

    protected void onRename(T pojo) {

    }

    protected String[] menuItems() {
        return getResources().getStringArray(R.array.category_menu_items);
    }

    protected String menuTitle(int position) {
        return getItem(position).getName();
    }

    protected void onMenuItemClick(final int position, int which) {

    }

    protected abstract List<Song> getSongs(int position);
}
