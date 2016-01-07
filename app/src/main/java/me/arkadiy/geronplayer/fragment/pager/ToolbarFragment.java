package me.arkadiy.geronplayer.fragment.pager;


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

import com.squareup.picasso.Picasso;

import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.audio.ShuffleButtonListener;
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
    private int what;
    private long id;
    private String toolbarText;
    private String additional;
    private FloatingActionButton fab;

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

    public ToolbarFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        what = getArguments().getInt("what");
        id = getArguments().getLong("id");
        toolbarText = getArguments().getString("toolbarText");
        additional = getArguments().getString("additional");
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.tool_bar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(toolbarText);
        }
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.child_fragment_container);
        if (fragment == null) {
            switch (what) {
                case ARTIST:
                    fragment = AlbumListFragment.newInstance(AlbumListFragment.ARTIST, id);
                    break;
                case GENRE:
                    fragment = AlbumListFragment.newInstance(AlbumListFragment.GENRE, id);
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
            fab = (FloatingActionButton) view.findViewById(R.id.fab_shuffle);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ShuffleButtonListener fragment = (ShuffleButtonListener) getChildFragmentManager()
                            .findFragmentById(R.id.child_fragment_container);
                    fragment.onShuffleButtonClick();

                }
            });
            ImageView coverArt = (ImageView) view.findViewById(R.id.large_cover_art);
            Picasso.with(getActivity())
                    .load(Utils.getArtworks(id)).placeholder(R.drawable.default_album_art).into(coverArt);
            return view;
        }

    }


}
