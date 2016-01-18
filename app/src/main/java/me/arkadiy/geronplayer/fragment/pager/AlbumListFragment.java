package me.arkadiy.geronplayer.fragment.pager;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.util.List;

import me.arkadiy.geronplayer.MainActivity;
import me.arkadiy.geronplayer.MusicService;
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
import me.arkadiy.geronplayer.statics.Utils;

/**
 * Created by Arkadiy on 10.11.2015.
 */
public class AlbumListFragment extends AbstractListFragment<Category> {
    public final static int ARTIST = 10;
    public final static int GENRE = 11;
    public static final int REQUEST_CODE = 111;
    private static final String ALBUM_ID = "albumId";
    private int mode;
    //    private String param;
    private long id;
    private long albumId;

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
        Log.e("Utils", "onCreate()");
        if (savedInstanceState != null) {
            albumId = savedInstanceState.getLong(ALBUM_ID);
        }
        if (getArguments() != null) {
//            param = getArguments().getString("asd");
            mode = getArguments().getInt("mode");
            id = getArguments().getLong("id");
            showScroller = false;
        }
    }

    @Override
    public AbstractLoader<Category> getNewLoader() {
        Log.e("Utils", "getNewLoader()");
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
        Log.e("Utils", "getNewAdapter()");
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
        if (loader != null) {
            loader.notifyChanges();
        }
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
                setAlbumId(getItem(position).getID());
                Log.e("Utils", "setAlbumId() " + albumId);
                chooseImage();
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
                        deleteUtils.deleteAlbum(getActivity(), getItem(position).getID());
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

    private void chooseImage() {
        Fragment fragment = getParentFragment();
        if (fragment != null) {
            Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT, null);
            galleryIntent.setType("image/*");
            galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);

//                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            Intent chooser = new Intent(Intent.ACTION_CHOOSER);
            chooser.putExtra(Intent.EXTRA_INTENT, galleryIntent);
            chooser.putExtra(Intent.EXTRA_TITLE, getString(R.string.choose_picture));

//                Intent[] intentArray =  {cameraIntent};
//                chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
            fragment.startActivityForResult(chooser, REQUEST_CODE);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong(ALBUM_ID, albumId);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == AlbumListFragment.REQUEST_CODE) {
            Log.e("Utils", "onActivityResult OK" + data.getData());
            new Thread() {
                final Uri uri = data.getData();

                @Override
                public void run() {
                    Log.e("Utils", "Thread " + albumId);
                    Utils.setArtwork(getActivity(), uri, albumId);
                    MainActivity.imageLoader.clearMemoryCache();
                }
            }.start();

            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }
}
