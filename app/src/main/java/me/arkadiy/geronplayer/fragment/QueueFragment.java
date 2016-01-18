package me.arkadiy.geronplayer.fragment;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.mobeta.android.dslv.DragSortListView;

import java.util.List;

import me.arkadiy.geronplayer.MainActivity;
import me.arkadiy.geronplayer.MusicService;
import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.adapters.list_view.QueueAdapter;
import me.arkadiy.geronplayer.audio.SongControlListener;
import me.arkadiy.geronplayer.plain.Song;
import me.arkadiy.geronplayer.statics.MenuManager;
import me.arkadiy.geronplayer.statics.TagManager;


public class QueueFragment extends Fragment implements SongControlListener {

    private QueueAdapter songAdapter;
    private MusicService service;
    private DragSortListView listView;
    private List<Song> songList;
    private MenuManager menuManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        menuManager = new MenuManager();
        service = ((MainActivity) getActivity()).getService();
        service.addSongListener(this);
    }

    @Override
    public void onDestroy() {
        service.removeSongListener(this);
        service = null;
        super.onDestroy();
    }

    @Override
    public void onPause() {
        menuManager.dismissMenu();
        super.onPause();
    }

    public static QueueFragment newInstance() {
        QueueFragment fragment = new QueueFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_draggable_list, container, false);
        listView = (DragSortListView) view.findViewById(R.id.list);
        songList = service.getQueue();
        songAdapter = new QueueAdapter(getActivity(), songList);
        songAdapter.setSong(service.getCurrentSongIndex());
        listView.setAdapter(songAdapter);
        Log.e("QueueFragment", "onCreateView() ");
        if (songList == null || songList.size() == 1) {
            listView.setDragEnabled(false);
        }
        if (listView.getFirstVisiblePosition() > songAdapter.getSong() ||
                listView.getLastVisiblePosition() < songAdapter.getSong()) {
            listView.setSelectionFromTop(songAdapter.getSong() - 2, 0);
        }
        listView.setRemoveListener(new DragSortListView.RemoveListener() {
            @Override
            public void remove(int i) {
                if (service.getShuffleState() == MusicService.SHUFFLE_OFF) {
                    List<Song> queue = service.getQueue();
                    if (service.hasSongs() && queue.size() > 1/* && i >= 0 && i < queue.size()*/) {
                        int currentSong = service.getCurrentSongPosition();
                        queue.remove(i);
                        if (i < currentSong) {
                            currentSong--;
                            service.setCurrentSong(currentSong);
                        } else if (i == currentSong) {
                            currentSong--;
                            service.setCurrentSong(currentSong);
                            service.playNext();
                        }

                        service.notifyQueueChanges();
                    }
                    if (queue == null || queue.size() == 1) {
                        listView.setDragEnabled(false);
                    }
                } else {
                    songAdapter.notifyDataSetChanged();
                    View view = getView();
                    if (view != null) {
                        Snackbar.make(getView(), R.string.unable_to_delete, Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        });
        listView.setDropListener(new DragSortListView.DropListener() {
            @Override
            public void drop(int from, int to) {
                if (service.getShuffleState() == MusicService.SHUFFLE_OFF) {
                    int currSong = service.getCurrentSongPosition();
                    if (from > currSong && to <= currSong) {
                        currSong++;
                    } else if (from < currSong && to >= currSong) {
                        currSong--;
                    } else if (from == currSong) {
                        currSong = to;
                    }
                    Song movedItem = songList.get(from);
                    songList.remove(from);
//                if (from > to) --from;
                    songList.add(to, movedItem);
                    service.setCurrentSong(currSong);
                    service.notifyQueueChanges();
                } else {
                    songAdapter.notifyDataSetChanged();
                    View view = getView();
                    if (view != null) {
                        Snackbar.make(getView(), R.string.unable_to_move, Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ((MainActivity) getActivity()).playQueue(songList, i);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                menuManager.showMenu(getActivity(), songList.get(position));
                return false;
            }
        });
        return view;
    }

    @Override
    public void onPreparedPlaying(Song song, int position) {
        songAdapter.setSong(song);
        songAdapter.notifyDataSetChanged();
        if (listView.getFirstVisiblePosition() > songAdapter.getSong() ||
                listView.getLastVisiblePosition() < songAdapter.getSong()) {
            listView.setSelectionFromTop(songAdapter.getSong() - 2, 0);
        }
    }

    @Override
    public void onStartPlaying(Song song) {

    }

    @Override
    public void onStopPlaying(Song song) {

    }

    @Override
    public void onError(Song song) {

    }

    @Override
    public void onRepeatStateChange(int state) {

    }

    @Override
    public void onShuffleStateChange(int state) {
        songAdapter.notifyDataSetChanged();
    }

    @Override
    public void onQueueChanged(List<Song> queue, int i) {
        songAdapter.setSongs(queue);
        songList = queue;
        songAdapter.setSong(service.getCurrentSong());
        songAdapter.notifyDataSetChanged();
        Log.e("onQueueChanged()", queue.size() + " " + songAdapter.getCount());
    }
}
