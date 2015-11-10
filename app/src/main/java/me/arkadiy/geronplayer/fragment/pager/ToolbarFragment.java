package me.arkadiy.geronplayer.fragment.pager;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.arkadiy.geronplayer.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ToolbarFragment extends Fragment {
    public final static int ARTIST = 0;
    public final static int GENRE = 1;
    public final static int PLAYLIST = 2;
    public final static int FOLDER = 3;
    private int what;
    private long id;
    private String additional;

    public static ToolbarFragment newInstance(int what, long id, String additionalParam) {
        ToolbarFragment fragment = new ToolbarFragment();
        Bundle args = new Bundle();
        args.putInt("what", what);
        args.putLong("id", id);
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
        additional = getArguments().getString("additional");
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.child_fragment_container);
        if (fragment == null) {
            switch (what) {
                case ARTIST:
                    fragment = AlbumListFragment.newInstance("what", AlbumListFragment.ARTIST, id);
                    break;
                case GENRE:
                    fragment = AlbumListFragment.newInstance("what", AlbumListFragment.GENRE, id);
                    break;
                case PLAYLIST:
                    fragment = SongListFragment.newInstance("what", SongListFragment.PLAYLIST, id, false);
                    break;
                case FOLDER:
                    fragment = SongListFragment.newInstance(additional, SongListFragment.FOLDER, id, true);
                    break;
            }
            Log.e("ToolbarFragment", "replace()");
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.child_fragment_container, fragment).commit();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_coordinator, container, false);
    }


}
