package me.arkadiy.geronplayer.audio;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;

import me.arkadiy.geronplayer.MainActivity;
import me.arkadiy.geronplayer.MusicService;
import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.plain.Song;
import me.arkadiy.geronplayer.statics.Constants;
import me.arkadiy.geronplayer.statics.Utils;
import me.arkadiy.geronplayer.widget.PlaybackWidgetProvider;

public class ForegroundManager {
    private final Service c;
    private final ImageSize remoteImageSize;
    private final ImageSize expandedImageSize;
    private final ImageSize widgetImageSize;
    private boolean isForeground;

    public ForegroundManager(Service c) {
        this.c = c;
        float density = c.getResources().getDisplayMetrics().density;
        int remoteSize = (int) (density * 64);
        int widgetSize = (int) (density * 80);
        int expandedSize = (int) (density * 128);
        remoteImageSize = new ImageSize(remoteSize, remoteSize);
        widgetImageSize = new ImageSize(widgetSize, widgetSize);
        expandedImageSize = new ImageSize(expandedSize, expandedSize);
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
        Bitmap widgetBitmap = Utils.getBitmap(c, Utils.getArtworks(song.getAlbumID()), widgetImageSize);
        for (int id : ids) {
            PlaybackWidgetProvider.updateWidget(c, appWidgetManager, id, song, isPlaying, widgetBitmap);
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

            Bitmap remoteBitmap = Utils.getBitmap(c, Utils.getArtworks(song.getAlbumID()), remoteImageSize);
            Bitmap expandedBitmap = Utils.getBitmap(c, Utils.getArtworks(song.getAlbumID()), expandedImageSize);
            remoteViews.setImageViewBitmap(R.id.album_art,remoteBitmap);
            expandedView.setImageViewBitmap(R.id.album_art, expandedBitmap);
//            remoteViews.setImageViewUri(R.id.album_art, Constants.getArtworks(song.getAlbumID()));
//            expandedView.setImageViewUri(R.id.album_art, Constants.getArtworks(song.getAlbumID()));

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
        return PendingIntent.getService(c, 0, intent, 0);
    }

    public boolean isForeground() {
        return isForeground;
    }
}
