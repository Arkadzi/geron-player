package me.arkadiy.geronplayer.fragment;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.mobeta.android.dslv.DragSortListView;

import java.util.ArrayList;
import java.util.List;

import me.arkadiy.geronplayer.MainActivity;
import me.arkadiy.geronplayer.MusicService;
import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.adapters.list_view.QueueAdapter;
import me.arkadiy.geronplayer.audio.SongControlListener;
import me.arkadiy.geronplayer.plain.Song;
import me.arkadiy.geronplayer.statics.QueueMenuManager;


public class QueueFragment extends Fragment implements SongControlListener {

    private QueueAdapter songAdapter;
    private MusicService service;
    private DragSortListView listView;
    private List<Song> songList;
    private QueueMenuManager menuManager;
    private boolean mBound;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) serviceBinder;

            service = binder.getService();
            service.addSongListener(QueueFragment.this);

            mBound = true;
            Log.e("QueueFragment", "onServiceConnected()");
            QueueFragment.this.onServiceConnected(binder.getService());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            onUnbind();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        menuManager = new QueueMenuManager();
    }

    @Override
    public void onResume() {
        super.onResume();

        Intent intent = new Intent(getActivity(), MusicService.class);
        getActivity().getApplicationContext().bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onPause() {
        menuManager.dismissMenu();
        if (mBound) {
            getActivity().getApplicationContext().unbindService(connection);
            onUnbind();
        }
        super.onPause();
    }

    private void onUnbind() {
        service.removeSongListener(this);
        service = null;
        mBound = false;
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
        songAdapter = new QueueAdapter(getActivity());
        songAdapter.setSongs(new ArrayList<Song>());

        listView.setAdapter(songAdapter);
        listView.setRemoveListener(new DragSortListView.RemoveListener() {
            @Override
            public void remove(int i) {
                if (mBound) {
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
            }
        });
        listView.setDropListener(new DragSortListView.DropListener() {
            @Override
            public void drop(int from, int to) {
                if (mBound) {
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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            getActivity().onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPreparedPlaying(Song song, int position, int index) {
        songAdapter.setSong(index);
        songAdapter.notifyDataSetChanged();
        if (listView.getFirstVisiblePosition() > songAdapter.getSong() ||
                listView.getLastVisiblePosition() < songAdapter.getSong()) {
//            listView.setSelectionFromTop(songAdapter.getSong() - 2, 0);
            int selectionFromTop = (songAdapter.getSong() - 2);
            if (selectionFromTop < 0) selectionFromTop = 0;
            listView.setSelectionFromTop(selectionFromTop , 0);
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
    public void onQueueChanged(List<Song> queue, int position, int index) {
        songList = queue;
        songAdapter.setSongs(songList);
        songAdapter.setSong(index);
        songAdapter.notifyDataSetChanged();
    }

    public void onServiceConnected(MusicService service) {
        songList = service.getQueue();
        if (songList == null) songList = new ArrayList<>();
        songAdapter.setSongs(songList);
        songAdapter.setSong(service.getCurrentSongIndex());
        animateList();
        songAdapter.notifyDataSetChanged();
        if (songList.size() == 1) {
            listView.setDragEnabled(false);
        }
        if (listView.getFirstVisiblePosition() > songAdapter.getSong() ||
                listView.getLastVisiblePosition() < songAdapter.getSong()) {
            int selectionFromTop = (songAdapter.getSong() - 2);
            if (selectionFromTop < 0) selectionFromTop = 0;
            listView.setSelectionFromTop(selectionFromTop , 0);
        }
    }

    private void animateList() {
        listView.setAlpha(0);
        listView.animate().alpha(1);
    }
}
