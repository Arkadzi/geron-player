package me.arkadiy.geronplayer.fragment.pager;


import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.util.List;

import me.arkadiy.geronplayer.MainActivity;
import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.audio.ShuffleButtonListener;
import me.arkadiy.geronplayer.fragment.EqualizerFragment;
import me.arkadiy.geronplayer.fragment.QueueFragment;
import me.arkadiy.geronplayer.statics.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class ToolbarFragment extends Fragment {
    public final static int ARTIST = 0;
    public final static int GENRE = 1;
    public final static int PLAYLIST = 2;
    public final static int FOLDER = 3;
    public final static int ALBUM = 4;
    public final static int PLAYLIST_EDIT = 5;
    public final static int QUEUE = 6;
    public final static int EQUALIZER = 7;
    private int what;
    private long id;
    private String toolbarText;
    private String additional;
    private DisplayImageOptions options;

    public ToolbarFragment() {
        // Required empty public constructor
    }

    public static ToolbarFragment newInstance(int what, long id, String toolbarText, String additionalParam) {
        ToolbarFragment fragment = new ToolbarFragment();
        Bundle args = new Bundle();
        args.putInt("what", what);
        args.putLong("id", id);
        args.putString("toolbarText", toolbarText);
        args.putString("additional", additionalParam);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        what = getArguments().getInt("what");
        id = getArguments().getLong("id");
        toolbarText = getArguments().getString("toolbarText");
        additional = getArguments().getString("additional");
        options = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new FadeInBitmapDisplayer(1000))
                .build();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.tool_bar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(toolbarText);
            actionBar.setDisplayHomeAsUpEnabled(true);
            ((MainActivity) getActivity()).updateActionBar(toolbar);
        }
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.child_fragment_container);
        if (fragment == null) {
            switch (what) {
                case ARTIST:
                    fragment = AlbumListFragment.newInstance(AlbumListFragment.ARTIST, id);
                    break;
                case GENRE:
                    fragment = SongListFragment.newInstance(SongListFragment.GENRE, id, true, null);
                    break;
                case PLAYLIST:
                    fragment = SongListFragment.newInstance(SongListFragment.PLAYLIST, id, false, null);
                    break;
                case FOLDER:
                    fragment = SongListFragment.newInstance(SongListFragment.FOLDER, 0, true, additional);
                    break;
                case ALBUM:
                    fragment = SongListFragment.newInstance(SongListFragment.ALBUM, id, false, null);
                    break;
                case PLAYLIST_EDIT:
                    fragment = PlaylistEditFragment.newInstance(id);
                    break;
                case QUEUE:
                    fragment = QueueFragment.newInstance();
                    break;
                case EQUALIZER:
                    fragment = EqualizerFragment.newInstance();
                    break;
            }
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.child_fragment_container, fragment).commit();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (what != ALBUM)
            return inflater.inflate(R.layout.fragment_toolbar, container, false);
        else {
            View view = inflater.inflate(R.layout.fragment_coordinator, container, false);
            FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab_shuffle);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ShuffleButtonListener fragment = (ShuffleButtonListener) getChildFragmentManager()
                            .findFragmentById(R.id.child_fragment_container);
                    fragment.onShuffleButtonClick();

                }
            });
            ImageView coverArt = (ImageView) view.findViewById(R.id.large_cover_art);
            Utils.getLoader(getActivity()).displayImage(Utils.getArtworks(id).toString(), coverArt, options);
            return view;
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        List<Fragment> fragments = getChildFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment instanceof AlbumListFragment) {
                    fragment.onActivityResult(requestCode, resultCode, data);
                }
            }
        }
    }
}
