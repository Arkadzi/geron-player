package me.arkadiy.geronplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Virtualizer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.List;

import me.arkadiy.geronplayer.plain.Song;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {
    private final IBinder musicBind = new MusicBinder();
    private List<Song> queue;
    private MediaPlayer player;
    private Equalizer equalizer;
    private Virtualizer virtualizer;
    private boolean isForeground;
    private BassBoost bassBoost;
    private static boolean isStarted;
    private int currentSong;
    private boolean isPrepared;
    private boolean isShouldStart;
    private SongControlListener display;


    public MusicService() {
    }


    private void initMediaPlayer() {
        setPlayerProperties();
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
    }

    public void setCurrentSong(int currentSong) {
        this.currentSong = currentSong;
    }


    /*
    Player listeners
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        playNext();
    }


    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e("onError", "service");
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        isPrepared = true;
        if (isShouldStart()) {
            start();
        } else if (isForeground) {
            updateRemoteView(false);
        }
        if (display != null) {
            display.onPreparedPlaying(getCurrentSong());
            if (isShouldStart()) {
                display.onStartPlaying(getCurrentSong());
            }
        }
    }

    /*
    Service lifecycle
     */
    @Override
    public void onCreate() {
        super.onCreate();
        initMediaPlayer();
        Log.e("MusicService", "onCreate()");
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.e("MusicService", "onBind()");

        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e("MusicService", "onUnbind()");
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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
            beginForeground();
        } else if (intent.getAction().equals(Constants.ACTION.STOP_FOREGROUND_ACTION)) {
            endForeground();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.e("MusicService", "onDestroy()");
        isStarted = false;
        setSongListener(null);
        player.stop();
        player.reset();
        player.release();
        super.onDestroy();
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

    public void start() {
        player.start();
        if (isForeground) {
            updateRemoteView(true);
        }
        if (display != null)
        display.onStartPlaying(getCurrentSong());
        setShouldStart(true);
    }

    public void pause() {
        setShouldStart(false);
        player.pause();
        if (display != null) {
            display.onStopPlaying(getCurrentSong());
        }
        if (isForeground)
        updateRemoteView(false);
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


    public void setSongListener(SongControlListener listener) {
        this.display = listener;
    }

    public void seekTo(int position) {
        player.seekTo(position);
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

    public boolean hasSongs() {
        return queue != null && queue.size() > 0;
    }

    public Song getCurrentSong() throws NullPointerException {
        return queue.get(currentSong);
    }

    public void beginForeground() {
        isForeground = true;
        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, getNotification(isPrepared() && isPlaying()));
    }

    public void endForeground() {
        stopForeground(true);
        isForeground = false;
    }

    public void updateRemoteView(boolean isPlaying) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, getNotification(isPlaying));
    }

    public Notification getNotification(boolean isPlaying) {
        final Song song = getCurrentSong();

        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, 0);

        PendingIntent prev = getPendingIntent(Constants.ACTION.PREV_ACTION);
        PendingIntent next = getPendingIntent(Constants.ACTION.NEXT_ACTION);
        PendingIntent stop = getPendingIntent(Constants.ACTION.STOP_SERVICE_ACTION);
        PendingIntent pp = getPendingIntent(Constants.ACTION.PAUSE_PLAY_ACTION);

        final RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification_widget);

        remoteViews.setOnClickPendingIntent(R.id.notification_prev, prev);
        remoteViews.setOnClickPendingIntent(R.id.notification_next, next);
        remoteViews.setOnClickPendingIntent(R.id.notification_stop, stop);
        remoteViews.setOnClickPendingIntent(R.id.notification_pp, pp);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_album_white_24dp)
                .setTicker(song.getTitle())
                .setContent(remoteViews)
                .setContentIntent(resultPendingIntent);

        if (isPlaying) {
            remoteViews.setImageViewResource(R.id.notification_pp, R.drawable.ic_pause_white_24dp);
        } else {
            remoteViews.setImageViewResource(R.id.notification_pp, R.drawable.ic_play_arrow_white_24dp);
        }
        remoteViews.setTextViewText(R.id.notification_artist, song.getArtist());
        remoteViews.setTextViewText(R.id.notification_title, song.getTitle());
        final Notification notification = builder.build();
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)) {
            final RemoteViews expandedView = new RemoteViews(getPackageName(), R.layout.notification_expanded);
            notification.bigContentView = expandedView;

            expandedView.setTextViewText(R.id.notification_artist, song.getArtist());
            expandedView.setTextViewText(R.id.notification_title, song.getTitle());

            remoteViews.setImageViewUri(R.id.album_art, Constants.getArtworks(song.getAlbumID()));
            expandedView.setImageViewUri(R.id.album_art, Constants.getArtworks(song.getAlbumID()));

            expandedView.setOnClickPendingIntent(R.id.notification_prev, prev);
            expandedView.setOnClickPendingIntent(R.id.notification_next, next);
            expandedView.setOnClickPendingIntent(R.id.notification_stop, stop);
            expandedView.setOnClickPendingIntent(R.id.notification_pp, pp);

            if (isPlaying) {
                expandedView.setImageViewResource(R.id.notification_pp, R.drawable.ic_pause_white_24dp);
            } else {
                expandedView.setImageViewResource(R.id.notification_pp, R.drawable.ic_play_arrow_white_24dp);
            }
        }
        return notification;
    }

    private PendingIntent getPendingIntent(String action) {
        Intent intent = new Intent(this, MusicService.class);
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
        return pendingIntent;
    }

}
