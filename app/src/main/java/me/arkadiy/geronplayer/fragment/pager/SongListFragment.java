package me.arkadiy.geronplayer.fragment.pager;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.jaudiotagger.tag.FieldKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import me.arkadiy.geronplayer.MainActivity;
import me.arkadiy.geronplayer.MusicService;
import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.adapters.list_view.FolderSongAdapter;
import me.arkadiy.geronplayer.adapters.list_view.MyCategoryAdapter;
import me.arkadiy.geronplayer.adapters.list_view.MySongAdapter;
import me.arkadiy.geronplayer.audio.ShuffleButtonListener;
import me.arkadiy.geronplayer.loader.AbstractLoader;
import me.arkadiy.geronplayer.loader.SongLoader;
import me.arkadiy.geronplayer.plain.Song;
import me.arkadiy.geronplayer.statics.Constants;
import me.arkadiy.geronplayer.statics.DeleteUtils;
import me.arkadiy.geronplayer.statics.MyRingtoneManager;
import me.arkadiy.geronplayer.statics.TagManager;

public class SongListFragment extends AbstractListFragment<Song> implements ShuffleButtonListener {
    public final static int ALL = 0;
    public final static int PLAYLIST = 1;
    public final static int ALBUM = 2;
    public final static int FOLDER = 3;
    private int mode;
    private long id;
    private String additional;

    public static SongListFragment newInstance(int mode, long id, boolean showScroller, String additional) {
        SongListFragment fragment = new SongListFragment();
        Bundle args = new Bundle();
        args.putString("add", additional);
        args.putInt("mode", mode);
        args.putLong("id", id);
        args.putBoolean("scroller", showScroller);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            additional = getArguments().getString("add");
            mode = getArguments().getInt("mode");
            id = getArguments().getLong("id");
            showScroller = getArguments().getBoolean("scroller");

        }
    }

    @Override
    public AbstractLoader<Song> getNewLoader() {
        return SongLoader.getLoader(getActivity(), additional, mode, id);
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
                Log.e("SongListFragment", "onClick");
//                int size = data.size();
//                for (int i = 0; i < size; i++) {
//                    if (data.get(i).getID() == getItem(position).getID()) {
                ((MainActivity) getActivity()).playQueue(data, position);
//                        break;
//                    }
//                }
            }
        });
    }

    @Override
    protected MyCategoryAdapter<Song> getNewAdapter(List<Song> data) {
        if (mode == FOLDER){
            return new FolderSongAdapter(data,
                    R.layout.list_item,
                    R.id.main,
                    R.id.secondary,
                    R.id.third);
        } else {
            return new MySongAdapter(data,
                    R.layout.list_item,
                    R.id.main,
                    R.id.secondary,
                    R.id.third);
        }
    }

    @Override
    public void onShuffleButtonClick() {
        MainActivity activity = ((MainActivity) getActivity());
        MusicService service = activity.getService();
        int song = new Random().nextInt(data.size());
        activity.playQueue(data, song);
        if (service.getShuffleState() == MusicService.SHUFFLE_OFF) {
            service.nextShuffleState();
        }
    }

    @Override
    protected String[] menuItems() {
        return getResources().getStringArray(R.array.song_menu_items);
    }

    @Override
    protected int[] menuCodes() {
        return new int[]{
                Constants.MENU.PLAY,
                Constants.MENU.PLAY_NEXT,
                Constants.MENU.ADD_TO_QUEUE,
                Constants.MENU.ADD_TO_PLAYLIST,
                Constants.MENU.SET_RINGTONE,
                Constants.MENU.RENAME,
                Constants.MENU.DELETE
        };
    }

    @Override
    protected String menuTitle(int position) {
        return getItem(position).getTitle();
    }

    @Override
    protected boolean onMenuItemClick(final int position, int code) {
        boolean isHandled = super.onMenuItemClick(position, code);
        if (!isHandled) {
            if (code == Constants.MENU.SET_RINGTONE) {
                MyRingtoneManager.setRingtone(getActivity(), data.get(position));
                if (getView() != null)
                    Snackbar.make(getView(), R.string.ringtone_set, Snackbar.LENGTH_SHORT).show();
            }
        }
        return isHandled;
    }

    @Override
    protected void onRename(Song pojo) {

    }

    @Override
    protected Dialog getRenameDialog(final Song pojo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.rename_dialog, null);
        final EditText mTitle = (EditText) view.findViewById(R.id.name);
        final EditText mArtist = (EditText) view.findViewById(R.id.artist);
        final EditText mAlbum = (EditText) view.findViewById(R.id.album);
        mTitle.setText(pojo.getName());
        mArtist.setText(pojo.getArtist());
        mAlbum.setText(pojo.getAlbum());
        builder.setView(view);
        builder.setTitle(R.string.rename);
        builder.setPositiveButton(R.string.action_accept, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newTitle = mTitle.getText().toString();
                String newArtist = mArtist.getText().toString();
                String newAlbum = mAlbum.getText().toString();
                TagManager tagManager = new TagManager();
                if (!newTitle.isEmpty() && !newArtist.isEmpty() && !newAlbum.isEmpty()) {
                    pojo.setName(newTitle);
                    pojo.setAlbum(newAlbum);
                    pojo.setArtist(newArtist);
                    tagManager.rename(getActivity(),
                            pojo.getPath(),
                            new FieldKey[]{FieldKey.TITLE, FieldKey.ARTIST, FieldKey.ALBUM},
                            new String[]{pojo.getTitle(), pojo.getArtist(), pojo.getAlbum()});
                }
            }
        });
        return builder.create();
    }

    @Override
    protected List<Song> getSongs(Context c, Song item) {
        List<Song> queue = new ArrayList<>();
        queue.add(item);
        return queue;
    }
}
