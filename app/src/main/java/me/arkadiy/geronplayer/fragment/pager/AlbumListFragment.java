package me.arkadiy.geronplayer.fragment.pager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.nostra13.universalimageloader.core.assist.ImageSize;

import org.jaudiotagger.tag.FieldKey;

import java.util.List;

import me.arkadiy.geronplayer.MainActivity;
import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.adapters.list_view.MyCategoryAdapter;
import me.arkadiy.geronplayer.adapters.list_view.MyPrefixCategoryAdapter;
import me.arkadiy.geronplayer.loader.AbstractLoader;
import me.arkadiy.geronplayer.loader.AlbumLoader;
import me.arkadiy.geronplayer.plain.Category;
import me.arkadiy.geronplayer.plain.Song;
import me.arkadiy.geronplayer.statics.Constants;
import me.arkadiy.geronplayer.statics.MusicRetriever;
import me.arkadiy.geronplayer.statics.TagManager;
import me.arkadiy.geronplayer.statics.Utils;

public class AlbumListFragment extends AbstractListFragment<Category> {
    public final static int ARTIST = 10;
    public static final int REQUEST_CODE = 111;
    private static final String ALBUM_ID = "albumId";
    private int mode;
    private long id;
    private long albumId;

    public static AlbumListFragment newInstance(int mode, long id) {
        AlbumListFragment fragment = new AlbumListFragment();
        Bundle args = new Bundle();

        args.putInt("mode", mode);
        args.putLong("id", id);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            albumId = savedInstanceState.getLong(ALBUM_ID);
        }
        if (getArguments() != null) {
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
        int value = 2;
        int screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            value = 2;
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            value = 4;
        }
        if (screenSize > Configuration.SCREENLAYOUT_SIZE_NORMAL) {
            value *= 2;
        }
        return value;
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
                R.id.third,
                R.id.item_image,
                getResources().getString(R.string.song_count),
                -1);
    }

    @Override
    protected void onRename(final Category pojo) {
        final Activity c = getActivity();
        showProgressDialog();
        new Thread() {
            @Override
            public void run() {
                TagManager tagManager = new TagManager();
                final List<Song> songs = MusicRetriever.getSongsByAlbum(c, pojo.getID());
                for (Song song : songs) {
                    tagManager.rename(c, song.getPath(), new FieldKey[]{FieldKey.ALBUM}, new String[]{pojo.getName()});
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
    protected List<Song> getSongs(Context c, Category category) {
        return MusicRetriever.getSongsByAlbum(c, category.getID());
    }

    @Override
    protected String[] menuItems() {
        return getResources().getStringArray(R.array.album_menu_items);
    }

    @Override
    protected boolean onMenuItemClick(final int position, int code) {
        boolean isHandled = super.onMenuItemClick(position, code);
        if (!isHandled) {
            if (code == Constants.MENU.SET_ARTWORK) {
                setAlbumId(getItem(position).getID());
                chooseImage();
            }
        }
        return isHandled;
    }

    @Override
    protected int[] menuCodes() {
        return new int[]{
                Constants.MENU.PLAY,
                Constants.MENU.PLAY_NEXT,
                Constants.MENU.ADD_TO_QUEUE,
                Constants.MENU.ADD_TO_PLAYLIST,
                Constants.MENU.SET_ARTWORK,
                Constants.MENU.RENAME,
                Constants.MENU.DELETE
        };
    }

    private void chooseImage() {
        Fragment fragment = getParentFragment();
        if (fragment != null) {
            Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT, null);
            galleryIntent.setType("image/*");
            galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);

            Intent chooser = new Intent(Intent.ACTION_CHOOSER);
            chooser.putExtra(Intent.EXTRA_INTENT, galleryIntent);
            chooser.putExtra(Intent.EXTRA_TITLE, getString(R.string.choose_picture));

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
            final Context c = getActivity();
            new Thread() {
                final Uri uri = data.getData();

                @Override
                public void run() {
//                    Bitmap bitmap = Utils.getBitmap(uri);
                    float density = c.getResources().getDisplayMetrics().density;
                    int size = (int) (density * 250);
                    Bitmap bitmap = Utils.getBitmap(getContext(), uri, new ImageSize(size, size));

//                    String path = Utils.saveImage(c, bitmap, String.valueOf(System.currentTimeMillis()));
                    String path = Utils.saveImage(c, bitmap, String.valueOf(System.currentTimeMillis()));
                    Utils.setArtwork(c, albumId, path);
                    Utils.getLoader(c).clearMemoryCache();
                }
            }.start();

            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }
}
