package me.arkadiy.geronplayer.fragment.pager;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.NumberPicker;

import com.pluscubed.recyclerfastscroll.RecyclerFastScroller;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import me.arkadiy.geronplayer.MainActivity;
import me.arkadiy.geronplayer.MusicService;
import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.adapters.list_view.MyCategoryAdapter;
import me.arkadiy.geronplayer.loader.AbstractLoader;
import me.arkadiy.geronplayer.plain.Category;
import me.arkadiy.geronplayer.plain.Nameable;
import me.arkadiy.geronplayer.plain.Song;
import me.arkadiy.geronplayer.statics.Constants;
import me.arkadiy.geronplayer.statics.DeleteUtils;
import me.arkadiy.geronplayer.statics.MusicRetriever;
import me.arkadiy.geronplayer.statics.PlaylistUtils;
import me.arkadiy.geronplayer.statics.QueueMenuManager;

public abstract class AbstractListFragment<T extends Nameable> extends Fragment
        implements LoaderManager.LoaderCallbacks<List<T>>, SearchView.OnQueryTextListener, MainActivity.BackPressListener {
    protected boolean showScroller = true;
    protected RecyclerView mRecyclerView;
    protected AbstractLoader<T> loader;
    protected List<T> data;
    private MyCategoryAdapter<T> adapter;
    private Dialog menuDialog;
    View.OnClickListener themePickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Activity activity = getActivity();
            boolean changed = false;
            int theme = Constants.THEME.DARK;
            if (v.getId() == R.id.light) {
                theme = Constants.THEME.LIGHT;
            } else if (v.getId() == R.id.dark) {
                theme = Constants.THEME.DARK;
            }
            menuDialog.cancel();
            SharedPreferences prefs = activity.getPreferences(Context.MODE_PRIVATE);
            if (theme != prefs.getInt(Constants.THEME.THEME, Constants.THEME.DARK)) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(Constants.THEME.THEME, theme);
                editor.commit();
                activity.finish();
                Intent intent = new Intent(activity, activity.getClass());
                activity.startActivity(intent);
            }
        }
    };
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
        return adapter.getItem(position);
    }


    @Override
    public void onPause() {
        super.onPause();
        dismissDialog();
        ((MainActivity) getActivity()).removeBackPressListener(this);
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
        loader = getNewLoader();
        loader.registerObserver();
        return loader;
    }

    public abstract AbstractLoader<T> getNewLoader();

    @Override
    public void onDestroy() {
        if (loader != null) {
            loader.unregisterObserver();
        }
        loader = null;
        super.onDestroy();
    }


    @Override
    public void onResume() {
        super.onResume();

        ((MainActivity) getActivity()).addBackPressListener(this);
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
        if (id == android.R.id.home) {
            getActivity().onBackPressed();
        } else if (id == R.id.action_theme) {
            showThemeDialog();
        } else if (id == R.id.action_timer) {
            showTimePickDialog();
        } else if (id == R.id.action_license) {
            showLicenseDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLicenseDialog() {
        WebView view = (WebView) View.inflate(getActivity(), R.layout.licenses_dialog, null);
        view.loadUrl("file:///android_asset/open_source_licenses.html");
        menuDialog = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.license))
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .create();
        menuDialog.show();
    }

    private void showTimePickDialog() {
        final MusicService service = ((MainActivity) getActivity()).getService();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = View.inflate(getActivity(), R.layout.timer_picker_dialog, null);
        builder.setView(view);

        final NumberPicker picker = (NumberPicker) view.findViewById(R.id.number_picker);
        picker.setMinValue(1);
        picker.setMaxValue(30);
        picker.setValue(10);

        final SwitchCompat switchButton = (SwitchCompat) view.findViewById(R.id.switch_button);
        Log.e("switch", String.valueOf(service.isTimerEnabled()));
        switchButton.setChecked(service.isTimerEnabled());

        builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                service.setTimer(switchButton.isChecked(), picker.getValue());
            }
        });
        menuDialog = builder.create();
        menuDialog.show();
    }

    private void showThemeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = View.inflate(getActivity(), R.layout.theme_picker_dialog, null);

        view.findViewById(R.id.dark).setOnClickListener(themePickListener);
        view.findViewById(R.id.light).setOnClickListener(themePickListener);

        builder.setView(view);

        menuDialog = builder.create();
        menuDialog.show();
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
        if (mRecyclerView != null && mRecyclerView.getAdapter() != null) {
            data = new ArrayList<>();
            adapter.setData(data);
        }
    }


    @Override
    public void onLoadFinished(Loader<List<T>> loader, List<T> data) {
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()/*, R.style.AlertDialogCustom*/);
        builder.setItems(menuItems(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.e("Dialog", String.valueOf(which));
                onMenuItemClick(position, menuCodes()[which]);
            }
        });
        builder.setTitle(menuTitle(position));
        return builder.create();
    }

    protected void showPlaylistDialog(Context c, final int position) {

        new AsyncTask<Context, Void, Context>() {
            List<Song> songs;
            List<Category> playlists;

            @Override
            protected Context doInBackground(Context... params) {
                playlists = PlaylistUtils.getPlaylists(getActivity());
                songs = getSongs(params[0], position);
                return params[0];
            }

            @Override
            protected void onPostExecute(final Context context) {
                String[] items = new String[playlists.size() + 1];
                items[0] = getResources().getString(R.string.create_playlist);
                for (int i = 0; i < playlists.size(); i++) {
                    items[i + 1] = playlists.get(i).getName();
                }

                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(menuTitle(position));
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, final int which) {
                        if (which == 0) {
                            showCreateDialog(songs, playlists);
                        } else {
                            new Thread() {
                                @Override
                                public void run() {
                                    PlaylistUtils.addSongs(context, playlists.get(which - 1).getID(), songs);
                                }
                            }.start();
                        }
                    }
                });
                menuDialog = builder.create();
                menuDialog.show();
            }
        }.execute(c);
    }

    private void showCreateDialog(final List<Song> songs, final List<Category> playlists) {
        final Activity activity = getActivity();
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.create_playlist);
        final View view = activity.getLayoutInflater().inflate(R.layout.create_dialog, null);
        builder.setView(view);
        builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText mPlaylistName = (EditText) view.findViewById(R.id.playlist_name);
                String playlistName = mPlaylistName.getText().toString();
                if (!playlistName.isEmpty()) {
                    final String newName = QueueMenuManager.handleName(playlistName, playlists);
                    new Thread() {
                        @Override
                        public void run() {
                            long id = PlaylistUtils.createPlaylist(activity, newName);
                            if (id > -1) {
                                PlaylistUtils.addSongs(activity, id, songs);
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
        menuDialog = dialog;
        menuDialog.show();
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
        builder.setTitle(R.string.rename);
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

    protected int[] menuCodes() {
        return new int[]{
                Constants.MENU.PLAY,
                Constants.MENU.PLAY_NEXT,
                Constants.MENU.ADD_TO_QUEUE,
                Constants.MENU.ADD_TO_PLAYLIST,
                Constants.MENU.RENAME,
                Constants.MENU.DELETE
        };
    }

    protected String menuTitle(int position) {
        return getItem(position).getName();
    }

    protected boolean onMenuItemClick(final int position, final int code) {
        final MainActivity activity = (MainActivity) getActivity();
        final int shuffle = activity.getService().getShuffleState();
        switch (code) {
            case Constants.MENU.PLAY:
                playQueue(position, activity, shuffle);
                break;
            case Constants.MENU.PLAY_NEXT:
                playNext(position, activity);
                break;
            case Constants.MENU.ADD_TO_QUEUE:
                addToQueue(position, activity);
                break;
            case Constants.MENU.ADD_TO_PLAYLIST:
                showPlaylistDialog(activity, position);
                break;
            case Constants.MENU.DELETE:
                delete(position, activity);
                break;
            case Constants.MENU.RENAME:
                showRenameDialog(getItem(position));
                break;
            default:
                return false;
        }
        return true;
    }

    private void delete(final int position, final MainActivity activity) {
        showProgressDialog();
        new Thread() {
            @Override
            public void run() {
//                List<Song> songs = activity.getService().getQueue();
//                int currentSong = activity.getService().getCurrentSongPosition();
                final boolean changed = delete(activity.getService(), position);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (changed) {
                            activity.getService().notifyQueueChanges();
                        }
                        dismissDialog();
                    }
                });
            }
        }.start();
    }

    private void addToQueue(final int position, final MainActivity activity) {
        new Thread() {
            @Override
            public void run() {
                final List<Song> songs = getSongs(activity, position);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.addToQueue(songs);
                    }
                });
            }
        }.start();
    }

    private void playNext(final int position, final MainActivity activity) {
        new Thread() {
            @Override
            public void run() {
                final List<Song> songs = getSongs(activity, position);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.addNext(songs);
                    }
                });
            }
        }.start();
    }

    private void playQueue(final int position, final MainActivity activity, final int shuffle) {
        new Thread() {
            @Override
            public void run() {
                final List<Song> songs = getSongs(activity, position);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int song = 0;
                        if (shuffle == MusicService.SHUFFLE_ON) {
                            song = new Random().nextInt(songs.size());
                        }
                        activity.playQueue(songs, song);
                    }
                });
            }
        }.start();
    }

    protected boolean delete(MusicService c, int pos) {
        List<Song> songsForDelete = getSongs(c, pos);
        List<Song> songs = c.getQueue();
        List<Song> absoluteDelete = new ArrayList<>(songsForDelete);
        absoluteDelete.removeAll(songs);
        for (Song song : absoluteDelete) {
            DeleteUtils.deleteSong(c, song.getID());
        }
        return DeleteUtils.deleteSongs(c, songsForDelete, songs);
    }

    protected abstract List<Song> getSongs(Context c, int position);
}
