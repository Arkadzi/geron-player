package me.arkadiy.geronplayer.audio;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import me.arkadiy.geronplayer.MainActivity;
import me.arkadiy.geronplayer.MusicService;
import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.plain.Song;
import me.arkadiy.geronplayer.statics.Constants;
import me.arkadiy.geronplayer.widget.PlaybackWidgetProvider;

/**
 * Created by Arkadiy on 11.12.2015.
 */
public class ForegroundManager {
    private final Service c;
    private boolean isForeground;

    public ForegroundManager(Service c) {
        this.c = c;
    }

    public void beginForeground(Song song, boolean isPlaying) {
        if (!isForeground()) {
            c.startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, getNotification(song, isPlaying));
            isForeground = true;
        }
    }

    public void endForeground() {
        if (isForeground()) {
            c.stopForeground(true);
            isForeground = false;
        }
    }

    public void updateRemoteView(Song song, boolean isPlaying) {
        if (isForeground()) {
            NotificationManager manager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, getNotification(song, isPlaying));
        }
        updateWidget(song, isPlaying);
    }

    public void updateWidget(Song song, boolean isPlaying) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(c);
        int ids[] = AppWidgetManager.getInstance(c.getApplication()).getAppWidgetIds(new ComponentName(c.getApplication(), PlaybackWidgetProvider.class));
        for (int id : ids) {
            PlaybackWidgetProvider.updateWidget(c, appWidgetManager, id, song, isPlaying);
        }
    }

    public Notification getNotification(Song song, boolean isPlaying) {

        Intent resultIntent = new Intent(c, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(c, 0, resultIntent, 0);

        PendingIntent prev = getPendingIntent(Constants.ACTION.PREV_ACTION);
        PendingIntent next = getPendingIntent(Constants.ACTION.NEXT_ACTION);
        PendingIntent stop = getPendingIntent(Constants.ACTION.STOP_SERVICE_ACTION);
        PendingIntent pp = getPendingIntent(Constants.ACTION.PAUSE_PLAY_ACTION);

        final RemoteViews remoteViews = new RemoteViews(c.getPackageName(), R.layout.notification_widget);

        remoteViews.setOnClickPendingIntent(R.id.notification_prev, prev);
        remoteViews.setOnClickPendingIntent(R.id.notification_next, next);
        remoteViews.setOnClickPendingIntent(R.id.notification_stop, stop);
        remoteViews.setOnClickPendingIntent(R.id.notification_pp, pp);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(c)
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
            final RemoteViews expandedView = new RemoteViews(c.getPackageName(), R.layout.notification_expanded);
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
        Intent intent = new Intent(c, MusicService.class);
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getService(c, 0, intent, 0);
        return pendingIntent;
    }

    public boolean isForeground() {
        return isForeground;
    }
}
