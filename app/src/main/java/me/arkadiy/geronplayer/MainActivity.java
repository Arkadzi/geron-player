package me.arkadiy.geronplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.HashSet;
import java.util.List;

import me.arkadiy.geronplayer.fragment.PagerFragment;
import me.arkadiy.geronplayer.plain.Song;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SongControlListener {

    private ImageButton playbackButton;
    private ImageButton queueButton;
    private ViewGroup bottomPanel;
    private TextView songTitleView;
    private TextView artistNameView;
    private TextView subSongTitleView;
    private TextView subArtistNameView;
    private ImageView coverArtView;
    private ImageView subCoverArtView;
    private ImageView backgroundCoverView;
    private View hideArrow;
    private ImageButton ppButton;
    private ImageButton prevButton;
    private ImageButton nextButton;
    private TextView positionView;
    private TextView durationView;
    private MusicService mService;
    private HashSet<BackPressListener> listeners;
    private SlidingUpPanelLayout panel;
    private boolean mBound;
    private Handler handler;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private AppCompatSeekBar songDurationView;
    private ImageRunnable imageRunnable;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (mService != null && mService.isPrepared()) {
                int seconds = mService.getPosition() / 1000;
                positionView.setText(String.format("%02d:%02d", seconds / 60, seconds % 60));
                songDurationView.setProgress(seconds);
            }
            handler.postDelayed(this, 1000);
        }
    };
    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            mService = binder.getService();
            mService.setSongListener(MainActivity.this);
            if (mService.hasSongs()) {
                if (mService.isPrepared()) {
                    onPreparedPlaying(mService.getCurrentSong());
                    if (mService.isPlaying()) {
                        onStartPlaying(mService.getCurrentSong());
                    }
                }
            } else {
                panel.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
            }
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService.setSongListener(null);
            mService = null;
            mBound = false;
        }
    };

    @Override
    public void onClick(View v) {
        if (mService != null) {
            if (v == playbackButton || v == ppButton) {
                if (!mService.isPrepared()) {
                    mService.setShouldStart(true);
                    try {
                        mService.playSong();
                    } catch (Exception e) {
                        mService.playNext();
                    }
                } else if (mService.isPlaying()) {
                    mService.pause();
                } else {
                    mService.start();
                }
            } else if (v == prevButton) {
                mService.playPrev();
            } else if (v == nextButton) {
                mService.playNext();
            }
        }
    }


    public void playQueue(List<Song> queue, int position) {
        if (mService != null) {
            mService.setQueue(queue);
            mService.setCurrentSong(position);
            try {
                mService.setShouldStart(true);
                mService.playSong();
                panel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            } catch (Exception e) {
                onError(mService.getCurrentSong());
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            getApplicationContext().unbindService(musicConnection);
            mBound = false;
        }
        handler.removeCallbacks(runnable);
        if (mService != null) {
            mService.setSongListener(null);
            if (isFinishing()) {
                if (!(mService.isPrepared() && mService.isPlaying())) {
                    Intent playIntent = new Intent(this, MusicService.class);
                    stopService(playIntent);
                }
            }
            if (!isChangingConfigurations() && mService.isPrepared() && mService.isPlaying()) {
                Intent playIntent = new Intent(this, MusicService.class);
                playIntent.setAction(Constants.ACTION.BEGIN_FOREGROUND_ACTION);
                startService(playIntent);
            }
            mService = null;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("panel", panel.getPanelState().toString());
        if (panel.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
            bottomPanel.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent startIntent = new Intent(this, MusicService.class);
        getApplicationContext().bindService(startIntent, musicConnection, Context.BIND_AUTO_CREATE);
        Intent playIntent = new Intent(this, MusicService.class);
        playIntent.setAction(Constants.ACTION.STOP_FOREGROUND_ACTION);
        startService(playIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        initLayoutViews();
//        panel.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        handler = new Handler();
        if (!MusicService.isStarted()) {
            Intent startIntent = new Intent(this, MusicService.class);
            startIntent.setAction(Constants.ACTION.START_SERVICE_ACTION);
            startService(startIntent);
        }
        initImageLoader();
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment == null) {
            android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragment = new PagerFragment();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
        }

//        getWindow().getDecorView().setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    private void initImageLoader() {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).threadPriority(Thread.NORM_PRIORITY - 2)

                .build();
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);
        options = new DisplayImageOptions.Builder()
                .showImageOnFail(R.drawable.default_album_art)
                .build();
        imageRunnable = new ImageRunnable(backgroundCoverView, ContextCompat.getDrawable(this, R.drawable.default_background), imageLoader);
    }

    @Override
    protected void onDestroy() {
        listeners.clear();
        listeners = null;
        super.onDestroy();
    }

    public void addListener(BackPressListener newListener) {
        if (listeners == null) listeners = new HashSet<>();
        listeners.add(newListener);
    }

    public void removeListener(BackPressListener listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    private void initLayoutViews() {
        panel = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        View slider = findViewById(R.id.slider);
        bottomPanel = (ViewGroup) panel.findViewById(R.id.bottom_panel);
        panel.setDragView(slider);
        songTitleView = (TextView) panel.findViewById(R.id.song_title);
        artistNameView = (TextView) panel.findViewById(R.id.artist_name);
        subSongTitleView = (TextView) panel.findViewById(R.id.sub_song_title);
        subArtistNameView = (TextView) panel.findViewById(R.id.sub_artist_name);
        playbackButton = (ImageButton) slider.findViewById(R.id.playback_button);
        ppButton = (ImageButton) panel.findViewById(R.id.pp_button);
        prevButton = (ImageButton) panel.findViewById(R.id.p_prev);
        nextButton = (ImageButton) panel.findViewById(R.id.p_next);
        hideArrow = slider.findViewById(R.id.hide_arrow);
        queueButton = (ImageButton) slider.findViewById(R.id.queue_button);
        coverArtView = (ImageView) panel.findViewById(R.id.cover_art);
        backgroundCoverView = (ImageView) panel.findViewById(R.id.background_cover);
        subCoverArtView = (ImageView) panel.findViewById(R.id.sub_cover_art);
        positionView = (TextView) panel.findViewById(R.id.position_text);
        durationView = (TextView) panel.findViewById(R.id.duration_text);
        songDurationView = (AppCompatSeekBar) panel.findViewById(R.id.progress_seek_bar);
        songDurationView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mService != null && mService.isPrepared()) {
                    mService.seekTo(progress * 1000);
                    positionView.setText(String.format("%02d:%02d", progress / 60, progress % 60));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(runnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                handler.post(runnable);
            }
        });
        playbackButton.setOnClickListener(this);
        ppButton.setOnClickListener(this);
        prevButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);
        panel.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {

            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                bottomPanel.setAlpha(1.0f - slideOffset);
                if (bottomPanel.getVisibility() == View.GONE)
                    bottomPanel.setVisibility(View.VISIBLE);
                hideArrow.setRotation((1.0f - slideOffset) * 180);
            }

            @Override
            public void onPanelCollapsed(View panel) {

            }

            @Override
            public void onPanelExpanded(View panel) {
                bottomPanel.setVisibility(View.GONE);
                songTitleView.setSelected(true);
            }

            @Override
            public void onPanelAnchored(View panel) {

            }

            @Override
            public void onPanelHidden(View panel) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (panel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            panel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            boolean pressBack = true;
            for (BackPressListener listener : listeners) {
                boolean b = listener.onBackPress();
                if (b) pressBack = false;
            }
            if (pressBack) {
                super.onBackPressed();
            }
        }
    }

    @Override
    public void onPreparedPlaying(Song song) {
        songTitleView.setText(song.getName());
        subSongTitleView.setText(song.getName());
        artistNameView.setText(song.getArtist());
        subArtistNameView.setText(song.getArtist());

        int seconds = mService.getDuration() / 1000;
        durationView.setText(String.format("%02d:%02d", seconds / 60, seconds % 60));
        songDurationView.setMax(seconds);
        seconds = mService.getPosition() / 1000;
        positionView.setText(String.format("%02d:%02d", seconds / 60, seconds % 60));
        songDurationView.setProgress(seconds);
        final String uri = Utils.getArtworks(song.getAlbumID()).toString();
        handler.removeCallbacks(imageRunnable);
        imageRunnable.setUri(uri);
        handler.post(imageRunnable);
        imageLoader.displayImage(uri, subCoverArtView, options);
        imageLoader.displayImage(uri, coverArtView, options);
    }

    @Override
    public void onStartPlaying(Song song) {
        ppButton.setImageResource(R.drawable.ic_pause_white_48dp);
        playbackButton.setImageResource(R.drawable.ic_pause_white_24dp);
        if (mService != null) {
            int seconds = mService.getDuration() / 1000;
            durationView.setText(String.format("%02d:%02d", seconds / 60, seconds % 60));
            songDurationView.setMax(seconds);
            seconds = mService.getPosition() / 1000;
            positionView.setText(String.format("%02d:%02d", seconds / 60, seconds % 60));
            songDurationView.setProgress(seconds);
            handler.postDelayed(runnable, 1000 - mService.getPosition() % 1000);
        }
    }

    @Override
    public void onStopPlaying(Song song) {
        ppButton.setImageResource(R.drawable.ic_play_arrow_white_48dp);
        playbackButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
        handler.removeCallbacks(runnable);
    }

    @Override
    public void onError(Song song) {

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast,
                (ViewGroup) findViewById(R.id.custom_toast_layout_id));
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();

        onStopPlaying(song);
    }

    public interface BackPressListener {
        boolean onBackPress();
    }
}

