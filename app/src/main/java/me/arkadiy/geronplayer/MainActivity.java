package me.arkadiy.geronplayer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;

import me.arkadiy.geronplayer.adapters.view_pager.ScreenSlidePagerAdapter;
import me.arkadiy.geronplayer.audio.SongControlListener;
import me.arkadiy.geronplayer.fragment.EqualizerFragment;
import me.arkadiy.geronplayer.fragment.PagerFragment;
import me.arkadiy.geronplayer.fragment.QueueFragment;
import me.arkadiy.geronplayer.fragment.pager.AlbumListFragment;
import me.arkadiy.geronplayer.plain.Song;
import me.arkadiy.geronplayer.statics.Constants;
import me.arkadiy.geronplayer.statics.MenuManager;
import me.arkadiy.geronplayer.statics.Utils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SongControlListener {

    public static ImageLoader imageLoader;
    public static DisplayImageOptions options;
    public int position;
    private ViewPager mPager;
    private ScreenSlidePagerAdapter mPagerAdapter;
    private ImageButton playbackButton;
    private ImageButton queueButton;
    private ImageButton equalizerButton;
    private ViewGroup bottomPanel;
    private TextView songTitleView;
    private TextView artistNameView;
    private TextView subSongTitleView;
    private TextView subArtistNameView;
    private TextView positionView;
    private TextView durationView;
    private ImageView subCoverArtView;
    private ImageView backgroundCoverView;
    private ImageView repeatView;
    private ImageView shuffleView;
    private ImageButton ppButton;
    private ImageButton prevButton;
    private ImageButton nextButton;
    private MusicService mService;
    private View hideArrow;
    private HashSet<BackPressListener> listeners;
    private SlidingUpPanelLayout panel;
    private boolean mBound;
    private Handler timeHandler;
    private MenuManager menuManager;
    private View container;
    private SeekBar songDurationView;
    private boolean isFingerSlide;
    private boolean animateSlide;
    private DisplayImageOptions gaussOptions;
    private ImageSize imageSize;
    private AlphaAnimation fadeImage;
    private int[] colors;
    private SimpleImageLoadingListener imageListener = new SimpleImageLoadingListener() {
        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            backgroundCoverView.clearAnimation();
            if (backgroundCoverView.getTag() != null && !backgroundCoverView.getTag().equals("empty")) {
                backgroundCoverView.startAnimation(fadeImage);
            }
            backgroundCoverView.setImageBitmap(null);
            backgroundCoverView.setTag("empty");

        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            Log.e("complete", String.format("uri %s tag %s", imageUri, backgroundCoverView.getTag()));
            backgroundCoverView.clearAnimation();
            if (backgroundCoverView.getTag() != null) {
                backgroundCoverView.startAnimation(fadeImage);
            }
            backgroundCoverView.setImageBitmap(loadedImage);
            backgroundCoverView.setTag(imageUri);
        }
    };
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            int post = 1000;
            if (mBound && mService.isPrepared()) {
                int seconds = mService.getPosition() / 1000;
                post = 1000 - mService.getPosition() % 1000;
                String time = String.format("%02d:%02d", seconds / 60, seconds % 60);
                positionView.setText(time);

                songDurationView.setProgress(seconds);
            }
            timeHandler.postDelayed(this, post);
        }
    };
    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e("MainActivity", "onServiceConnected() null " + (mService == null));
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            mService = binder.getService();
            mBound = true;
            mService.addSongListener(MainActivity.this);
            updateRepeatButton(mService.getRepeatState());
            updateShuffleButton(mService.getShuffleState());
            if (mService.hasSongs()) {
                mPagerAdapter.setService(mService);
                mPagerAdapter.setSongs(mService.getQueue());
                mPagerAdapter.notifyDataSetChanged();
                if (mService.isPrepared()) {
                    onPreparedPlaying(mService.getCurrentSong(), mService.getCurrentSongPosition());
                    if (mService.isPlaying()) {
                        onStartPlaying(mService.getCurrentSong());
                    } else {
                        onStopPlaying(mService.getCurrentSong());
                    }
                }
            } else {
                panel.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e("MainActivity", "onServiceDisconnected() null " + (mService == null));
            mService.removeSongListener(MainActivity.this);
            mBound = false;
            mService = null;
            mPagerAdapter.setService(null);
        }
    };
    private FragmentManager.OnBackStackChangedListener fragmentStackListener = new FragmentManager.OnBackStackChangedListener() {
        @Override
        public void onBackStackChanged() {
            if (panel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                panel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        }
    };

    @Override
    public void onClick(View v) {
        if (mBound) {
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
            } else if (v == repeatView) {
                mService.nextRepeatState();
            } else if (v == shuffleView) {
                mService.nextShuffleState();
            } else if (v == equalizerButton) {
                FragmentManager fm = getSupportFragmentManager();
                fm.popBackStack(Constants.STACK, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.replace(R.id.fragment_container, EqualizerFragment.newInstance(), "equalizer");
                transaction.addToBackStack(Constants.STACK);
                transaction.commit();

            } else if (v == queueButton) {
                FragmentManager fm = getSupportFragmentManager();
                fm.popBackStack(Constants.STACK, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.replace(R.id.fragment_container, QueueFragment.newInstance(), "queue");
                transaction.addToBackStack(Constants.STACK);
                transaction.commit();
            }
        }
    }

    public MusicService getService() {
        return mService;
    }

    public void playQueue(List<Song> queue, int position) {
        Log.e("MainActivity", "play queue null " + (mService == null));
        if (mBound) {
            mService.setCurrentSong(position);
            mPagerAdapter.setService(mService);
            mService.setQueue(queue);
            try {
                mService.setShouldStart(true);
                mService.playSong();
            } catch (Exception e) {
                mService.setShouldStart(false);
                onError(mService.getCurrentSong());
            }
        }
    }


    public void addNext(List<Song> queue) {
        if (mBound) {
            mService.addNext(queue);
        }
    }

    public void addToQueue(List<Song> queue) {
        if (mBound) {
            mService.addToQueue(queue);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initLayoutViews();
        setColorArray();
        timeHandler = new Handler();
        menuManager = new MenuManager();
        initImageLoader();
        Intent startIntent = new Intent(this, MusicService.class);
        startIntent.setAction(Constants.ACTION.START_SERVICE_ACTION);
        startService(startIntent);
        getSupportFragmentManager().addOnBackStackChangedListener(fragmentStackListener);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment == null) {
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragment = new PagerFragment();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
        }

//        getWindow().getDecorView().setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    private void setColorArray() {
        if (colors == null) {
            colors = new int[2];
            colors[0] = Utils.getColorAttribute(this, R.attr.colorControlNormal);
            colors[1] = Utils.getColorAttribute(this, R.attr.colorAccent);
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        Log.e("MainActivity", "onResume() null " + (mService == null));
        Log.e("panel", panel.getPanelState().toString());
        Intent playIntent = new Intent(this, MusicService.class);
        playIntent.setAction(Constants.ACTION.STOP_FOREGROUND_ACTION);
        startService(playIntent);
        Intent startIntent = new Intent(this, MusicService.class);
        getApplicationContext().bindService(startIntent, musicConnection, Context.BIND_AUTO_CREATE);
        if (panel.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
            bottomPanel.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPause() {
        menuManager.dismissMenu();
        Log.e("MainActivity", "omPause() null " + (mService == null));
        timeHandler.removeCallbacks(runnable);
        if (mBound) {
            getApplicationContext().unbindService(musicConnection);
            mService.removeSongListener(this);
            if (isFinishing()) {
                if (mService.isPrepared() && mService.isPlaying()) {
                    Intent playIntent = new Intent(this, MusicService.class);
                    playIntent.setAction(Constants.ACTION.BEGIN_FOREGROUND_ACTION);
                    startService(playIntent);
                } else {
                    Log.e("MainActivity", "onPause() stopService");
                    Intent playIntent = new Intent(this, MusicService.class);
                    stopService(playIntent);
                }
            }
            Log.e("condition", String.format("%b %b %b", !isChangingConfigurations(), mService.isPrepared(), mService.isPlaying()));
            mBound = false;
            mService = null;
            mPagerAdapter.setService(null);
        }
        super.onPause();
    }

    @Override
    protected void onUserLeaveHint() {
        if (mBound && mService.isPrepared() && mService.isPlaying()) {
            Intent playIntent = new Intent(this, MusicService.class);
            playIntent.setAction(Constants.ACTION.BEGIN_FOREGROUND_ACTION);
            startService(playIntent);
        }
        super.onUserLeaveHint();
        Log.e("MainActivity", "onUserLeaveHint()");
    }

    @Override
    protected void onDestroy() {
        getSupportFragmentManager().removeOnBackStackChangedListener(fragmentStackListener);
        Log.e("MainActivity", "onDestroy()");
        if (listeners != null) {
            listeners.clear();
            listeners = null;
        }
        super.onDestroy();
    }

    private void initImageLoader() {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .resetViewBeforeLoading(true)
//                .cacheInMemory(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .memoryCacheSize(5000000)
                .defaultDisplayImageOptions(options)
//                .denyCacheImageMultipleSizesInMemory()
//                .memoryCache(new UsingFreqLimitedMemoryCache(5000000))
                .build();
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);

        MainActivity.options = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.RGB_565)
//                .resetViewBeforeLoading(true)
                .cacheInMemory(true)
                .build();
        imageSize = new ImageSize(50, 50);
        gaussOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .preProcessor(new BitmapProcessor() {
                    @Override
                    public Bitmap process(Bitmap bitmap) {
                        return Utils.fastblur(bitmap, 8);
                    }
                })
                .build();
    }

    public void addListener(BackPressListener newListener) {
        if (listeners == null) {
            listeners = new HashSet<>();
        }
        listeners.add(newListener);
    }

    public void removeListener(BackPressListener listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    private void initLayoutViews() {
        mPager = (ViewPager) findViewById(R.id.image_pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public int state;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.e("pager", "onPageSelected()" + String.valueOf(position));
                MainActivity.this.position = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (mBound && (state == 0 || (state == 1 && this.state == 2))) {
                    if (position != mService.getCurrentSongPosition()) {
                        mService.setCurrentSong(position);
                        isFingerSlide = true;
                        try {
                            mService.playSong();
                        } catch (Exception e) {
                            onError(mService.getCurrentSong());
                        }
                    }
//                    onPreparedPlaying(mService.getQueue().get(position), position);
                }
                this.state = state;
            }
        });
        container = findViewById(R.id.fragment_container);
        panel = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        View slider = findViewById(R.id.slider);
        bottomPanel = (ViewGroup) panel.findViewById(R.id.bottom_panel);
        if (Build.VERSION.SDK_INT >= 19) {
            View topPanel = slider.findViewById(R.id.top_panel);
            topPanel.setPadding(0, getStatusBarHeight(), 0, 0);
        }
        panel.setDragView(slider);
        View songSettings = findViewById(R.id.song_settings);
        songSettings.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mBound && mService.hasSongs()) {
                    menuManager.showMenu(MainActivity.this, mService.getCurrentSong());
                }
                return false;
            }
        });
        songTitleView = (TextView) panel.findViewById(R.id.song_title);
        artistNameView = (TextView) panel.findViewById(R.id.artist_name);
        repeatView = (ImageView) panel.findViewById(R.id.repeat_view);
        shuffleView = (ImageView) panel.findViewById(R.id.shuffle_view);
        subSongTitleView = (TextView) panel.findViewById(R.id.sub_song_title);
        subArtistNameView = (TextView) panel.findViewById(R.id.sub_artist_name);
        playbackButton = (ImageButton) slider.findViewById(R.id.playback_button);
        ppButton = (ImageButton) panel.findViewById(R.id.pp_button);
        prevButton = (ImageButton) panel.findViewById(R.id.p_prev);
        nextButton = (ImageButton) panel.findViewById(R.id.p_next);
        hideArrow = slider.findViewById(R.id.hide_arrow);
        queueButton = (ImageButton) slider.findViewById(R.id.queue_button);
        equalizerButton = (ImageButton) slider.findViewById(R.id.equalizer_button);
        backgroundCoverView = (ImageView) panel.findViewById(R.id.background_cover);
        positionView = (TextView) panel.findViewById(R.id.position_text);
        durationView = (TextView) panel.findViewById(R.id.duration_text);
        fadeImage = new AlphaAnimation(0, 1);
        fadeImage.setDuration(500);
        fadeImage.setInterpolator(new DecelerateInterpolator());
        subCoverArtView = (ImageView) panel.findViewById(R.id.sub_cover_art);
        songDurationView = (SeekBar) panel.findViewById(R.id.progress_seek_bar);
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
                timeHandler.removeCallbacks(runnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                timeHandler.post(runnable);
            }
        });
//        TintChanger tintChanger = new TintChanger(getResources());
//        ppButton.setOnTouchListener(tintChanger);
//        prevButton.setOnTouchListener(tintChanger);
//        nextButton.setOnTouchListener(tintChanger);
//        equalizerButton.setOnTouchListener(tintChanger);
        subCoverArtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                panel.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            }
        });
//        queueButton.setOnTouchListener(tintChanger);
        equalizerButton.setOnClickListener(this);
        queueButton.setOnClickListener(this);
        playbackButton.setOnClickListener(this);
        ppButton.setOnClickListener(this);
        prevButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);
        shuffleView.setOnClickListener(this);
        repeatView.setOnClickListener(this);
        panel.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {

            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                bottomPanel.setAlpha(1.0f - slideOffset);
                if (bottomPanel.getVisibility() == View.GONE) {
                    bottomPanel.setVisibility(View.VISIBLE);
                }
                Log.e("MainActivity", "slideOffset " + slideOffset);
//                if (slideOffset >= 0.0f && slideOffset < 0.3f) {
//                    container.setScaleY((float) Math.pow(1.0f - slideOffset, 1. / 3));
//                    container.setScaleX((float) Math.pow(1.0f - slideOffset, 1. / 3));
//                    container.setAlpha(1.0f - slideOffset);
//                }
//                hideArrow.setRotation((1.0f - slideOffset) * 180);
            }

            @Override
            public void onPanelCollapsed(View panel) {

            }

            @Override
            public void onPanelExpanded(View panel) {
                bottomPanel.setVisibility(View.GONE);
                songTitleView.setSelected(true);
//                float scale = (float) Math.pow(0.7f, 1. / 3);
//                container.setScaleY(scale);
//                container.setScaleX(scale);
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
            if (listeners != null)
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
    public void onPreparedPlaying(final Song song, final int position) {
        Log.e("MainActivity", "onPreparedPlaying() " + String.valueOf(position) + " " + animateSlide);
        if (mService.isPrepared()) {
            if (!isFingerSlide) {
                mPager.setCurrentItem(position, false);
                this.position = position;
            }
            isFingerSlide = false;
            animateSlide = false;
            songTitleView.setText(song.getName());
            artistNameView.setText(song.getArtist());
            subSongTitleView.setText(song.getName());
            subArtistNameView.setText(song.getArtist());
            int seconds = mService.getDuration() / 1000;
            durationView.setText(String.format("%02d:%02d", seconds / 60, seconds % 60));
            songDurationView.setMax(seconds);
            seconds = mService.getPosition() / 1000;
            positionView.setText(String.format("%02d:%02d", seconds / 60, seconds % 60));
            songDurationView.setProgress(seconds);
            String uri = Utils.getArtworks(song.getAlbumID()).toString();
            imageLoader.displayImage(uri, subCoverArtView);
            boolean showAnimation = uri == null || !uri.equals(backgroundCoverView.getTag());
            if (showAnimation) {
                imageLoader.loadImage(uri, imageSize, gaussOptions, imageListener);
            }
            if (panel.getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN) {
                panel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        }
    }

    @Override
    public void onStartPlaying(Song song) {
        ppButton.setImageResource(R.drawable.ic_pause_white_48dp);
        playbackButton.setImageResource(R.drawable.ic_pause_white_24dp);
        timeHandler.removeCallbacks(runnable);
        if (mBound) {
            int seconds = mService.getDuration() / 1000;
            durationView.setText(String.format("%02d:%02d", seconds / 60, seconds % 60));
            songDurationView.setMax(seconds);

            seconds = mService.getPosition() / 1000;
            positionView.setText(String.format("%02d:%02d", seconds / 60, seconds % 60));
            songDurationView.setProgress(seconds);

            timeHandler.postDelayed(runnable, 1000 - mService.getPosition() % 1000);
            Log.e("MainActivity", String.format("runnable %d", 1000 - mService.getPosition() % 1000));
        }
    }

    @Override
    public void onStopPlaying(Song song) {
        ppButton.setImageResource(R.drawable.ic_play_arrow_white_48dp);
        playbackButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
        timeHandler.removeCallbacks(runnable);
    }

    @Override
    public void onError(Song song) {
        Snackbar.make(container, R.string.load_error, Snackbar.LENGTH_SHORT).show();
        isFingerSlide = false;
        onStopPlaying(song);
    }

    @Override
    public void onRepeatStateChange(int state) {
        updateRepeatButton(state);
    }

    private void updateRepeatButton(int state) {
        if (state == MusicService.REPEAT_ON) {
            repeatView.setImageResource(R.drawable.ic_repeat_white_24dp);
            repeatView.setColorFilter(colors[1]);
        } else if (state == MusicService.REPEAT_SINGLE) {
            repeatView.setImageResource(R.drawable.ic_repeat_one_white_24dp);
            repeatView.setColorFilter(colors[1]);
        } else if (state == MusicService.REPEAT_OFF) {
            repeatView.setImageResource(R.drawable.ic_repeat_white_24dp);
            repeatView.setColorFilter(colors[0]);
        }
    }

    @Override
    public void onShuffleStateChange(int state) {
        updateShuffleButton(state);
        mPager.setAdapter(mPagerAdapter);
        mPagerAdapter.notifyDataSetChanged();
        onPreparedPlaying(mService.getCurrentSong(), mService.getCurrentSongPosition());
    }

    @Override
    public void onQueueChanged(List<Song> songs, int i) {
        mPagerAdapter.setSongs(songs);
        mPager.setAdapter(mPagerAdapter);
        mPagerAdapter.notifyDataSetChanged();
        mPager.setCurrentItem(i, false);
        position = i;
    }

    private void updateShuffleButton(int state) {
        if (state == MusicService.SHUFFLE_ON) {
            shuffleView.setColorFilter(colors[1]);
        } else {
            shuffleView.setColorFilter(colors[0]);
        }
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public interface BackPressListener {
        boolean onBackPress();
    }
}

