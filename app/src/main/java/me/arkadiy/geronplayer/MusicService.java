package me.arkadiy.geronplayer;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Virtualizer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.arkadiy.geronplayer.audio.AudioFocusHelper;
import me.arkadiy.geronplayer.audio.AudioFocusListener;
import me.arkadiy.geronplayer.audio.ForegroundManager;
import me.arkadiy.geronplayer.audio.SongControlListener;
import me.arkadiy.geronplayer.database.SQLiteHelper;
import me.arkadiy.geronplayer.plain.Song;
import me.arkadiy.geronplayer.statics.Constants;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, AudioFocusListener {
    public final static int SHUFFLE_ON = 1;
    public final static int SHUFFLE_OFF = 0;
    public final static int REPEAT_SINGLE = 2;
    public final static int REPEAT_ON = 1;
    public final static int REPEAT_OFF = 0;

    private NoisyAudioReceiver noisyAudioReceiver;
    private int repeatState;
    private int shuffleState;
    private List<Integer> shuffleIds;
    private final IBinder musicBind = new MusicBinder();
    private List<Song> queue;
    private MediaPlayer player;
    private Equalizer equalizer;
    private Virtualizer virtualizer;
    private BassBoost bassBoost;
    private AudioFocusHelper afHelper;
    /*
        returns true if onStartCommand() has been called
     */
    private static boolean isStarted;
    /*
        returns true if audio file was asynchronously prepared
     */
    private boolean isPrepared;
    /*
        if returns true, next prepared audio file will be played after its preparation
     */
    private boolean isShouldStart;
    /*
        returns current position in play queue
     */
    private int currentSong;
    /*
        used for audio focus. Return true if playing should be resumed
     */
    private boolean resumePlaying;
    private Set<SongControlListener> displays;
    private ForegroundManager foregroundManager;
    private IntentFilter noisyFilter;
    private boolean isRegistered;

    public MusicService() {
    }

    private void initMediaPlayer() {
        afHelper = new AudioFocusHelper(this, this);
        foregroundManager = new ForegroundManager(this);
        setPlayerProperties();
    }

    public void initEqualizer() {
        SharedPreferences preferences = getSharedPreferences(Constants.PREFERENCES.PREF_NAME, MODE_PRIVATE);
        boolean enabled = preferences.getBoolean(Constants.PREFERENCES.EQUALIZER_ENABLED, false);
        equalizer = new Equalizer(0, player.getAudioSessionId());
        virtualizer = new Virtualizer(0, player.getAudioSessionId());
        bassBoost = new BassBoost(0, player.getAudioSessionId());
        equalizer.setEnabled(enabled);
        virtualizer.setEnabled(enabled);
        bassBoost.setEnabled(enabled);

        short preset = (short) preferences.getInt(Constants.PREFERENCES.EQUALIZER_PRESET, 0);
        if (preset >= 0) {
            equalizer.usePreset(preset);
        } else {
            int bandCount = equalizer.getNumberOfBands();
            for (short band = 0; band < bandCount; band++) {
                short bandLevel = (short) preferences.getInt(Constants.PREFERENCES.EQUALIZER_BAND + band, 0);
                equalizer.setBandLevel(band, bandLevel);
            }
        }

        short virtStrength = (short) preferences.getInt(Constants.PREFERENCES.VIRT_STATE, 0);
        virtualizer.setStrength(virtStrength);

        short bassStrength = (short) preferences.getInt(Constants.PREFERENCES.BASS_STATE, 0);
        bassBoost.setStrength(bassStrength);
    }

    public void setPlayerProperties() {
        player = new MediaPlayer();
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        player.reset();
    }

    public void setQueue(List<Song> queue) {
        this.queue = queue;
        if (shuffleState == SHUFFLE_ON) {
            switchToShuffleState();
        } else {
            shuffleIds = null;
        }
        for (SongControlListener listener : displays) {
            listener.onQueueChanged(getQueue(), currentSong);
        }
    }

    public void addToQueue(List<Song> queue) {
        int min = this.queue.size();
        int max = min + queue.size();
        this.queue.addAll(queue);
        if (shuffleState == SHUFFLE_ON) {
            List<Integer> additionalShufflePos = new ArrayList<>(queue.size());
            for (int i = min; i < max; i++) {
                additionalShufflePos.add(i);
            }
            Collections.shuffle(additionalShufflePos);
            shuffleIds.addAll(additionalShufflePos);
        }
        for (SongControlListener listener : displays) {
            listener.onQueueChanged(getQueue(), currentSong);
        }
    }

    public void addNext(List<Song> queue) {
        int min = getCurrentSongIndex() + 1;
        int max = min + queue.size();
        this.queue.addAll(min, queue);
        if (shuffleState == SHUFFLE_ON) {
            for (int i = 0; i < shuffleIds.size(); i++) {
                if (shuffleIds.get(i) >= min) {
                    shuffleIds.set(i, shuffleIds.get(i) + queue.size());
                }
            }
            List<Integer> additionalShufflePos = new ArrayList<>(queue.size());
            for (int i = min; i < max; i++) {
                additionalShufflePos.add(i);
            }
            shuffleIds.addAll(currentSong + 1, additionalShufflePos);
        }
        for (SongControlListener listener : displays) {
            listener.onQueueChanged(getQueue(), currentSong);
        }
    }

    public void setCurrentSong(int currentSong) {
        this.currentSong = currentSong;
    }

    /*
    Player listeners
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        if (repeatState == REPEAT_ON ||
                (repeatState == REPEAT_OFF && getCurrentSongPosition() < queue.size() - 1)) {
            playNext();
        } else if (repeatState == REPEAT_SINGLE) {
            seekTo(0);
            start();
        } else {
            pause();
            setShouldStart(false);
            playNext();
        }
    }


    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e("onError", "service");
        return false;
    }


    @Override
    public void onPrepared(MediaPlayer mp) {
        isPrepared = true;
        afHelper.updateRemoteControl(getCurrentSong());
        if (isShouldStart()) {
            start();
        } else {
            foregroundManager.updateRemoteView(getCurrentSong(), false);
        }
        for (SongControlListener display : displays) {
            display.onPreparedPlaying(getCurrentSong(), getCurrentSongPosition());
        }
    }

    /*
    Service lifecycle
     */
    @Override
    public void onCreate() {
        super.onCreate();
        displays = new HashSet<>();
        initMediaPlayer();
        initEqualizer();
        initQueue();
        noisyAudioReceiver = new NoisyAudioReceiver();
        noisyFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        Log.e("MusicService", "onCreate() " + isStarted);
    }


    private void initQueue() {
        queue = SQLiteHelper.getInstance(this).readSongs(this);
        shuffleIds = SQLiteHelper.getInstance(this).readPositions();

        int j = 0;
        while (shuffleIds.size() > queue.size()) {
            if (shuffleIds.get(j) >= queue.size()) {
                shuffleIds.remove(j);
            } else {
                j++;
            }
        }

        if (hasSongs()) {
            final SharedPreferences preferences = getSharedPreferences(Constants.PREFERENCES.PREF_NAME, MODE_PRIVATE);
            repeatState = preferences.getInt(Constants.PREFERENCES.REPEAT_STATE, 0);
            if (!shuffleIds.isEmpty())
                shuffleState = preferences.getInt(Constants.PREFERENCES.SHUFFLE_STATE, 0);
            String songName = preferences.getString(Constants.PREFERENCES.SONG_NAME, "");
            for (int i = 0; i < queue.size(); i++) {
                if (queue.get(getIndexByPosition(i)).getName().equals(songName)) {
                    setCurrentSong(i);
                    break;
                }
            }

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        playSongSync();
                    } catch (Exception e) {
                        Log.e("AsyncTask", "unable to load song");
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    int max = getDuration();
                    int progress = preferences.getInt(Constants.PREFERENCES.SONG_PROGRESS, 0);
                    if (progress < max) {
                        seekTo(progress);
                    }
                    onPrepared(player);
                }
            }.execute();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e("MusicService", "onBind() " + isStarted);

        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e("MusicService", "onUnbind() " + isStarted);
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("MusicService", "onStartCommand() isStarted " + isStarted);
        isStarted = true;
        if (intent == null) return super.onStartCommand(intent, flags, startId);
        if (intent.getAction().equals(Constants.ACTION.PREV_ACTION)) {
            playPrev();
        } else if (intent.getAction().equals(Constants.ACTION.NEXT_ACTION)) {
            playNext();
        } else if (intent.getAction().equals(Constants.ACTION.STOP_SERVICE_ACTION)) {
            pause();
            stopSelf();
        } else if (intent.getAction().equals(Constants.ACTION.PAUSE_PLAY_ACTION)) {
            if (isPlaying()) {
                pause();
            } else {
                start();
            }
        } else if (intent.getAction().equals(Constants.ACTION.BEGIN_FOREGROUND_ACTION)) {
            if (hasSongs() && isPrepared())
                foregroundManager.beginForeground(getCurrentSong(), isPlaying());
        } else if (intent.getAction().equals(Constants.ACTION.STOP_FOREGROUND_ACTION)) {
            foregroundManager.endForeground();
        } else if (intent.getAction().equals(Constants.ACTION.PAUSE_ACTION)) {
            if (isPrepared() && isPlaying()) {
                pause();
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.e("MusicService", "onDestroy() " + isStarted);
        isStarted = false;
        displays.clear();
        afHelper.refuseFocusIfNecessary();
        if (isPrepared()) {
            player.pause();
        }
        saveState();
        if (isPrepared()) {
            player.stop();
        }
        player.reset();
        player.release();

        super.onDestroy();
    }

    private void saveState() {
        SharedPreferences preferences = getSharedPreferences(Constants.PREFERENCES.PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        if (hasSongs()) {
            SQLiteHelper helper = SQLiteHelper.getInstance(this);
            helper.writeSongs(queue);
            helper.writePositions(shuffleIds);
            Log.e("MusicService", getCurrentSong().getName());
            editor.putString(Constants.PREFERENCES.SONG_NAME, getCurrentSong().getName());
            editor.putInt(Constants.PREFERENCES.SONG_PROGRESS, isPrepared() ? getPosition() : 0);
//            editor.putInt(Constants.PREFERENCES.SHUFFLE_STATE, shuffleState);
            editor.putInt(Constants.PREFERENCES.REPEAT_STATE, repeatState);
            editor.putInt(Constants.PREFERENCES.SHUFFLE_STATE, shuffleState);

        } else {
            editor.clear();
        }
        editor.putBoolean(Constants.PREFERENCES.EQUALIZER_ENABLED, equalizer.getEnabled());
        editor.putInt(Constants.PREFERENCES.VIRT_STATE, virtualizer.getRoundedStrength());
        editor.putInt(Constants.PREFERENCES.BASS_STATE, bassBoost.getRoundedStrength());
        editor.putInt(Constants.PREFERENCES.EQUALIZER_PRESET, equalizer.getCurrentPreset());
        short bandCount = equalizer.getNumberOfBands();
        for (short band = 0; band < bandCount; band++) {
            editor.putInt(Constants.PREFERENCES.EQUALIZER_BAND + band, equalizer.getBandLevel(band));
        }
        editor.apply();
    }

    public void playSong() throws Exception {
        isPrepared = false;
        player.reset();
        Song playSong = getCurrentSong();
        long currSong = playSong.getID();
        Uri trackUri = ContentUris.withAppendedId(
                playSong.getUri(),
                currSong);
        player.setDataSource(getApplicationContext(), trackUri);
        player.prepareAsync();
    }

    public void playSongSync() throws Exception {
        isPrepared = false;
        player.reset();
        Song playSong = getCurrentSong();
        long currSong = playSong.getID();
        Uri trackUri = ContentUris.withAppendedId(
                playSong.getUri(),
                currSong);
        player.setDataSource(getApplicationContext(), trackUri);
        player.prepare();
    }

    public void start() {
        player.start();
        if (!isRegistered) {
            registerReceiver(noisyAudioReceiver, noisyFilter);
            isRegistered = true;
        }
        afHelper.requestFocusIfNecessary(getCurrentSong());
        foregroundManager.updateRemoteView(getCurrentSong(), true);
        for (SongControlListener display : displays) {
            display.onStartPlaying(getCurrentSong());
        }
        afHelper.playbackStarted();
        setShouldStart(true);
    }

    public void pause() {
        setShouldStart(false);
        player.pause();
        if (isRegistered) {
            unregisterReceiver(noisyAudioReceiver);
            isRegistered = false;
        }
        for (SongControlListener display : displays) {
            display.onStopPlaying(getCurrentSong());
        }
        afHelper.playbackPaused();

        foregroundManager.updateRemoteView(getCurrentSong(), false);
    }

    public void playPrev() {
        while (true) {
            decCurrSong();
            try {
                playSong();
                break;
            } catch (Exception e) {
            }
        }
    }

    public void playNext() {
        while (true) {
            incCurrSong();
            try {
                playSong();
                break;
            } catch (Exception e) {
            }
        }
    }

    public void incCurrSong() {
        currentSong++;
        if (currentSong >= queue.size()) {
            currentSong = 0;
        }
    }

    public void decCurrSong() {
        currentSong--;
        if (currentSong < 0) {
            currentSong = queue.size() - 1;
        }
    }

    public boolean isPrepared() {
        return isPrepared;
    }

    public void addSongListener(SongControlListener listener) {
        this.displays.add(listener);
    }

    public void removeSongListener(SongControlListener listener) {
        this.displays.remove(listener);
    }

    public void seekTo(int position) {
        player.seekTo(position);
    }

    @Override
    public void onFocusGained() {
        if (isPrepared()) {
            if (resumePlaying) {
                start();
            }
        }
    }

    @Override
    public void onFocusLoss() {
        if (isPrepared()) {
            resumePlaying = isPlaying();
            if (resumePlaying) {
                pause();
            }
        }
    }

    public void nextRepeatState() {
        repeatState = (repeatState + 1) % 3;
        for (SongControlListener display : displays) {
            display.onRepeatStateChange(repeatState);
        }
    }

    public void nextShuffleState() {
        if (queue != null) {
            shuffleState = (shuffleState + 1) % 2;
            if (shuffleState == SHUFFLE_ON) {
                switchToShuffleState();
            } else {
                currentSong = shuffleIds.get(currentSong);
                shuffleIds = null;
            }
            for (SongControlListener display : displays) {
                display.onShuffleStateChange(shuffleState);
            }
        }
    }

    private void switchToShuffleState() {
        int size = queue.size();
        shuffleIds = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            if (i != currentSong) {
                shuffleIds.add(i);
            }
        }
        Collections.shuffle(shuffleIds);
        shuffleIds.add(0, currentSong);
        currentSong = 0;
    }

    public Equalizer getEqualizer() {
        return equalizer;
    }

    public Virtualizer getVirtualizer() {
        return virtualizer;
    }

    public BassBoost getBassBoost() {
        return bassBoost;
    }

    public void notifyQueueChanges() {
        for (SongControlListener l :
                displays) {
            l.onQueueChanged(getQueue(), currentSong);
        }
    }

    public class MusicBinder extends Binder {

        public MusicService getService() {
            return MusicService.this;
        }
    }

    public static boolean isStarted() {
        return isStarted;
    }

    public int getDuration() {
        return player.getDuration();
    }

    public int getPosition() {
        return player.getCurrentPosition();
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public boolean isShouldStart() {
        return isShouldStart;
    }

    public void setShouldStart(boolean should) {
        isShouldStart = should;
    }

    public int getCurrentSongPosition() {
        return currentSong;
    }

    public int getIndexByPosition(int position) {
        if (shuffleState == SHUFFLE_OFF) {
            return position;
        } else {
            return shuffleIds.get(position);
        }
    }

    public int getCurrentSongIndex() {
        return getIndexByPosition(getCurrentSongPosition());
    }

    public boolean hasSongs() {
        return queue != null && queue.size() > 0;
    }

    public Song getCurrentSong() throws NullPointerException {
        return queue.get(getCurrentSongIndex());
    }

    public int getRepeatState() {
        return repeatState;
    }

    public int getShuffleState() {
        return shuffleState;
    }

    public List<Song> getQueue() {
        return queue;
    }
}
