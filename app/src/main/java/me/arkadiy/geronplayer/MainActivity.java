package me.arkadiy.geronplayer;

import android.animation.TimeInterpolator;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
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

import java.util.HashSet;
import java.util.List;

import me.arkadiy.geronplayer.adapters.view_pager.ScreenSlidePagerAdapter;
import me.arkadiy.geronplayer.audio.SongControlListener;
import me.arkadiy.geronplayer.fragment.PagerFragment;
import me.arkadiy.geronplayer.fragment.pager.ToolbarFragment;
import me.arkadiy.geronplayer.plain.Song;
import me.arkadiy.geronplayer.statics.Constants;
import me.arkadiy.geronplayer.statics.QueueMenuManager;
import me.arkadiy.geronplayer.statics.TagManager;
import me.arkadiy.geronplayer.statics.Utils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SongControlListener {

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
    private ImageView ppButton;
    private ImageView pPrevButton;
    private ImageView pNextButton;
    private ImageButton prevButton;
    private ImageButton nextButton;
    private MusicService mService;
    private HashSet<BackPressListener> backPressListeners;
    private SlidingUpPanelLayout panel;
    private boolean mBound;
    private Handler timeHandler;
    private QueueMenuManager menuManager;
    private View container;
    private SeekBar songDurationView;
    private boolean isFingerSlide;
    private DisplayImageOptions gaussOptions;
    private ImageSize smallImageSize;
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

                positionView.setText(Utils.formatMillis(seconds * 1000));
                songDurationView.setProgress(seconds);
            }
            timeHandler.postDelayed(this, post);
        }
    };
    private FragmentManager.OnBackStackChangedListener fragmentStackListener = new FragmentManager.OnBackStackChangedListener() {
        @Override
        public void onBackStackChanged() {
            if (panel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                panel.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        panel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    }
                }, 300);
            }
        }
    };
    private View movingPanel;
    private boolean isHighlighted;
    private Dialog dialog;
    private Button lyricsButton;
    private View lyricsBackground;
    private TextView lyricsView;
    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
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

                onPreparedPlaying(mService.getCurrentSong(), mService.getCurrentSongPosition(), mService.getCurrentSongIndex());
                if (mService.isPrepared() && mService.isPlaying()) {
                    onStartPlaying(mService.getCurrentSong());
                } else {
                    onStopPlaying(mService.getCurrentSong());
                }
            } else {
                panel.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService.removeSongListener(MainActivity.this);
            mBound = false;
            mService = null;
            mPagerAdapter.setService(null);
        }
    };
    private Button lyricsEditButton;
    private ImageSize imageSize;

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
            } else if (v == pPrevButton || v == prevButton) {
                mService.playPrev();
            } else if (v == pNextButton || v == nextButton) {
                mService.playNext();
            } else if (v == repeatView) {
                mService.nextRepeatState();
            } else if (v == shuffleView) {
                mService.nextShuffleState();
            } else if (v == equalizerButton) {
                makeEqualizerFragment();
            } else if (v == queueButton) {
                makeQueueFragment();
            } else if (v == lyricsButton) {
                showLyricsView();
            } else if (v == lyricsEditButton) {
                showLyricsEditDialog();
            }
        }
    }

    private void showLyricsEditDialog() {
        menuManager.showLyricsEditMenu(this, getService().getCurrentSong());
    }

    private void showLyricsView() {
        if (lyricsBackground.getVisibility() == View.GONE) {
            lyricsBackground.setVisibility(View.VISIBLE);
            setLyrics(getService().getCurrentSong());
        } else {
            lyricsBackground.setVisibility(View.GONE);
        }
    }

    public void setLyrics(Song song) {
        if (lyricsBackground.getVisibility() == View.VISIBLE) {
            String lyrics = new TagManager().getLyrics(song);
            if (TextUtils.isEmpty(lyrics)) {
                lyrics = getString(R.string.no_lyrics);
            }
            lyricsView.setText(lyrics);
        }
    }

    private void makeEqualizerFragment() {
        makeFragment(ToolbarFragment.newInstance(
                ToolbarFragment.EQUALIZER,
                0,
                getString(R.string.equalizer),
                null), "equalizer");
    }


    private void makeQueueFragment() {
        makeFragment(ToolbarFragment.newInstance(
                ToolbarFragment.QUEUE,
                0,
                getString(R.string.queue),
                null), "queue");
    }

    private void makeFragment(Fragment fragment, String tag) {
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStack(Constants.STACK, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_in, R.anim.pop_out);
        transaction.replace(R.id.fragment_container, fragment, tag);
        transaction.addToBackStack(Constants.STACK);
        transaction.commit();
    }

    public void updateActionBar(Toolbar toolbar) {
    }


    public MusicService getService() {
        return mService;
    }

    public void playQueue(List<Song> queue, int position) {
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

    private void setCurrentTheme() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        int theme = preferences.getInt(Constants.THEME.THEME, Constants.THEME.DARK);
        if (theme == Constants.THEME.DARK) {
            setTheme(R.style.AppTheme);
        } else if (theme == Constants.THEME.LIGHT) {
            setTheme(R.style.AppThemeLight);
        } else if (theme == Constants.THEME.BLUE) {
            setTheme(R.style.AppThemeBlue);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        playFromFileBrowser(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setCurrentTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        backPressListeners = new HashSet<>();
        timeHandler = new Handler();
        menuManager = new QueueMenuManager();
        initLayoutViews();
        setColorArray();
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
        playFromFileBrowser(getIntent());
    }

    private void playFromFileBrowser(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            Uri uri = intent.getData();
            if (Intent.ACTION_VIEW.equals(action) && uri != null) {
                Intent newIntent = new Intent(this, MusicService.class);
                newIntent.setAction(Intent.ACTION_VIEW);
                newIntent.setData(uri);
                startService(newIntent);
            }
        }
    }

    private void setColorArray() {
        if (colors == null) {
            colors = new int[2];
            colors[0] = Color.WHITE;
            colors[1] = Utils.getColorAttribute(this, R.attr.colorAccent);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        checkTrial();

        Intent playIntent = new Intent(this, MusicService.class);
        playIntent.setAction(Constants.ACTION.STOP_FOREGROUND_ACTION);
        startService(playIntent);

        Intent startIntent = new Intent(this, MusicService.class);
        getApplicationContext().bindService(startIntent, musicConnection, Context.BIND_AUTO_CREATE);

        if (panel.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
            bottomPanel.setVisibility(View.INVISIBLE);
            setClickableButtons(true);

        } else {
            setClickableButtons(false);
        }
    }

    private void setClickableButtons(boolean state) {
        int clickable = state ? View.VISIBLE : View.GONE;
        lyricsButton.setVisibility(clickable);
        equalizerButton.setVisibility(clickable);
        queueButton.setVisibility(clickable);
    }

    @Override
    protected void onPause() {

        dismissTrialDialog();
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
                    Intent playIntent = new Intent(this, MusicService.class);
                    stopService(playIntent);
                }
            }
            mBound = false;
            mService = null;
            mPagerAdapter.setService(null);
        }
        super.onPause();
    }

    @Override
    protected void onUserLeaveHint() {
        if (mBound) {
            Intent playIntent = new Intent(this, MusicService.class);
            playIntent.setAction(Constants.ACTION.BEGIN_FOREGROUND_ACTION);
            startService(playIntent);
        }
        super.onUserLeaveHint();
    }

    @Override
    protected void onDestroy() {
        menuManager.dismissMenu();
        getSupportFragmentManager().removeOnBackStackChangedListener(fragmentStackListener);
        backPressListeners.clear();
        super.onDestroy();
    }

    private void initImageLoader() {
        float density = getResources().getDisplayMetrics().density;
        int normalSize = (int) (density * 280);
        smallImageSize = new ImageSize(50, 50);

        imageSize = new ImageSize(normalSize, normalSize);
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

    public void addBackPressListener(BackPressListener newListener) {
        backPressListeners.add(newListener);
    }

    public void removeBackPressListener(BackPressListener listener) {
        backPressListeners.remove(listener);
    }

    private void initLayoutViews() {
        fadeImage = new AlphaAnimation(0, 1);
        fadeImage.setDuration(500);
        fadeImage.setInterpolator(new DecelerateInterpolator());

        panel = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        View slider = findViewById(R.id.slider);
        lyricsBackground = findViewById(R.id.lyrics_background);
        lyricsView = (TextView) lyricsBackground.findViewById(R.id.lyrics_view);
        songTitleView = (TextView) panel.findViewById(R.id.song_title);
        artistNameView = (TextView) panel.findViewById(R.id.artist_name);
        repeatView = (ImageView) panel.findViewById(R.id.repeat_view);
        shuffleView = (ImageView) panel.findViewById(R.id.shuffle_view);
        subSongTitleView = (TextView) panel.findViewById(R.id.sub_song_title);
        subArtistNameView = (TextView) panel.findViewById(R.id.sub_artist_name);
        playbackButton = (ImageButton) slider.findViewById(R.id.playback_button);
        prevButton = (ImageButton) slider.findViewById(R.id.prev_button);
        nextButton = (ImageButton) slider.findViewById(R.id.next_button);
        ppButton = (ImageView) panel.findViewById(R.id.pp_button);
        pPrevButton = (ImageView) panel.findViewById(R.id.p_prev);
        pNextButton = (ImageView) panel.findViewById(R.id.p_next);
        queueButton = (ImageButton) findViewById(R.id.queue_button);
        equalizerButton = (ImageButton) findViewById(R.id.equalizer_button);
        lyricsButton = (Button) findViewById(R.id.show_lyrics_button);
        lyricsEditButton = (Button) findViewById(R.id.edit_lyrics_button);
        backgroundCoverView = (ImageView) findViewById(R.id.background_cover);
        positionView = (TextView) panel.findViewById(R.id.position_text);
        durationView = (TextView) panel.findViewById(R.id.duration_text);
        subCoverArtView = (ImageView) panel.findViewById(R.id.sub_cover_art);
        songDurationView = (SeekBar) panel.findViewById(R.id.progress_seek_bar);
        mPager = (ViewPager) findViewById(R.id.image_pager);
        container = findViewById(R.id.fragment_container);
        bottomPanel = (ViewGroup) panel.findViewById(R.id.bottom_panel);
        movingPanel = slider.findViewById(R.id.moving_view);
        View songSettings = findViewById(R.id.song_settings);

        equalizerButton.setOnClickListener(this);
        lyricsButton.setOnClickListener(this);
        queueButton.setOnClickListener(this);
        playbackButton.setOnClickListener(this);
        prevButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);
        ppButton.setOnClickListener(this);
        pPrevButton.setOnClickListener(this);
        pNextButton.setOnClickListener(this);
        shuffleView.setOnClickListener(this);
        repeatView.setOnClickListener(this);
        lyricsEditButton.setOnClickListener(this);

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public int state;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
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
                }
                this.state = state;
            }
        });

        songSettings.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mBound && mService.hasSongs()) {
                    menuManager.showMenu(MainActivity.this, mService.getCurrentSong());
                }
                return false;
            }
        });

        songDurationView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mService != null && mService.isPrepared()) {
                    mService.seekTo(progress * 1000);
                    positionView.setText(Utils.formatMillis(progress * 1000));
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
        panel.setPanelSlideListener(new SlidingUpPanelLayout.SimplePanelSlideListener() {

            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                bottomPanel.setAlpha(1.0f - slideOffset);
                if (bottomPanel.getVisibility() == View.INVISIBLE) {
                    bottomPanel.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPanelCollapsed(View panel) {
                setClickableButtons(false);
            }

            @Override
            public void onPanelExpanded(View panel) {
                setClickableButtons(true);
                bottomPanel.setVisibility(View.INVISIBLE);
                songTitleView.setSelected(true);
            }
        });

        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        movingPanel.post(new Runnable() {
            @Override
            public void run() {
                movingPanel.setTranslationY(-movingPanel.getHeight());

            }
        });
        panel.setDragView(slider);
    }

    @Override
    public void onBackPressed() {
        if (panel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            panel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            boolean pressBack = true;
            for (BackPressListener listener : backPressListeners) {
                boolean b = listener.onBackPress();
                if (b) pressBack = false;
            }
            if (pressBack) {
                super.onBackPressed();
            }
        }
    }

    @Override
    public void onPreparedPlaying(final Song song, final int position, final int index) {
        if (mService.isPrepared()) {
            if (!isFingerSlide) {
                mPager.setCurrentItem(position, false);
                this.position = position;
            }

            isFingerSlide = false;
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                songTitleView.setText(song.getName());
                artistNameView.setText(song.getArtist());
            } else {
                songTitleView.setText(String.format("%s - %s", song.getName(), song.getArtist()));
            }
            subSongTitleView.setText(song.getName());
            subArtistNameView.setText(song.getArtist());

            int duration = mService.getDuration();
            durationView.setText(Utils.formatMillis(duration));
            songDurationView.setMax(duration / 1000);

            int songPosition = mService.getPosition();
            positionView.setText(Utils.formatMillis(songPosition));
            songDurationView.setProgress(songPosition / 1000);

            String uri = Utils.getArtworks(song.getAlbumID()).toString();
            Utils.getLoader(this).displayImage(uri, subCoverArtView);
            boolean showAnimation = uri == null || !uri.equals(backgroundCoverView.getTag());
            if (showAnimation) {
                Utils.getLoader(this).loadImage(uri, smallImageSize, gaussOptions, imageListener);
            }

            if (panel.getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN) {
                panel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
            setLyrics(song);
        }
    }

    @Override
    public void onStartPlaying(Song song) {
        ppButton.setImageResource(R.drawable.ic_pause_white_48dp);
        playbackButton.setImageResource(R.drawable.ic_pause_white_24dp);

        timeHandler.removeCallbacks(runnable);

        if (mBound) {
            int duration = mService.getDuration();
            durationView.setText(Utils.formatMillis(duration));
            songDurationView.setMax(duration / 1000);

            int position = mService.getPosition();
            positionView.setText(Utils.formatMillis(position));
            songDurationView.setProgress(position / 1000);

            timeHandler.postDelayed(runnable, 1000 - mService.getPosition() % 1000);
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
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            songTitleView.setText(song.getName());
            artistNameView.setText(song.getArtist());
        } else {
            songTitleView.setText(String.format("%s - %s", song.getName(), song.getArtist()));
        }
        subSongTitleView.setText(song.getName());
        subArtistNameView.setText(song.getArtist());
        durationView.setText("-");
        positionView.setText("-");
        subCoverArtView.setImageBitmap(null);
        songDurationView.setProgress(0);
        songDurationView.setMax(0);
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
        mPagerAdapter.notifyDataSetChanged();

        onPreparedPlaying(mService.getCurrentSong(), mService.getCurrentSongPosition(), mService.getCurrentSongIndex());
    }

    @Override
    public void onQueueChanged(List<Song> songs, int position, int index) {
        mPagerAdapter.setSongs(songs);
        mPagerAdapter.notifyDataSetChanged();
        mPager.setCurrentItem(position, false);
        this.position = position;
    }

    private void updateShuffleButton(int state) {
        if (state == MusicService.SHUFFLE_ON) {
            shuffleView.setColorFilter(colors[1]);
        } else {
            shuffleView.setColorFilter(colors[0]);
        }
    }

    public void animateHighlight() {
        if (movingPanel != null) {
            movingPanel.animate().translationY(isHighlighted ? -movingPanel.getHeight() : 0)
                    .setDuration(200)
                    .setInterpolator(getInterpolator());
            isHighlighted = !isHighlighted;
        }
    }

    private TimeInterpolator getInterpolator() {
        return isHighlighted ? new FastOutLinearInInterpolator() : new LinearOutSlowInInterpolator();
    }

    private void checkTrial() {
        try {
            if (!Utils.isShouldPlay(this)) {
                showTrialDialog();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void showTrialDialog() {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("market://details?id=ru.alexanderklimov.crib"));
                    startActivity(intent);
                }
                finish();
            }
        };
        dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.trial_dialog_title)
                .setMessage(R.string.trial_dialog_message)
                .setPositiveButton(R.string.buy, listener)
                .setCancelable(false)
                .setNegativeButton(R.string.cancel, listener)
                .show();
    }

    private void dismissTrialDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }

    public interface BackPressListener {
        boolean onBackPress();
    }
}

