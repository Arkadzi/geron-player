package me.arkadiy.geronplayer.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.RemoteViews;

import me.arkadiy.geronplayer.MainActivity;
import me.arkadiy.geronplayer.MusicService;
import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.plain.Song;
import me.arkadiy.geronplayer.statics.Constants;
import me.arkadiy.geronplayer.statics.Utils;

public class PlaybackWidgetProvider extends AppWidgetProvider {
    private static PendingIntent getPendingIntent(Context context, String action) {
        Intent intent = new Intent(context, MusicService.class);
        intent.setAction(action);
        return PendingIntent.getService(context, 0, intent, 0);
    }

    public static void updateWidget(Context context, AppWidgetManager appWidgetManager,
                                    int widgetID, Song song, boolean isPlaying, Bitmap bitmap) {

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
        views.setOnClickPendingIntent(R.id.album_art, pendingIntent);

        if (MusicService.isStarted() && song == null) {
            Intent playIntent = new Intent(context, MusicService.class);
            playIntent.setAction(Constants.WIDGET.UPDATE_ACTION);
            context.startService(playIntent);
        }
        views.setOnClickPendingIntent(R.id.widget_prev, getPendingIntent(context, Constants.WIDGET.PREV_ACTION));
        views.setOnClickPendingIntent(R.id.widget_next, getPendingIntent(context, Constants.WIDGET.NEXT_ACTION));
        views.setOnClickPendingIntent(R.id.widget_pp, getPendingIntent(context, Constants.WIDGET.PLAY_PAUSE_ACTION));

        if (song == null) {
            views.setTextViewText(R.id.widget_title, "-");
            views.setTextViewText(R.id.widget_artist, "-");
        } else {
            views.setTextViewText(R.id.widget_title, song.getTitle());
            views.setTextViewText(R.id.widget_artist, song.getArtist());
            if (bitmap != null)
            views.setImageViewBitmap(R.id.album_art, bitmap);
            else
            views.setImageViewUri(R.id.album_art, Utils.getArtworks(song.getAlbumID()));
        }

        if (isPlaying) {
            views.setImageViewResource(R.id.widget_pp, R.drawable.ic_pause_white_24dp);
        } else {
            views.setImageViewResource(R.id.widget_pp, R.drawable.ic_play_arrow_white_24dp);
        }
        appWidgetManager.updateAppWidget(widgetID, views);
    }

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];
            updateWidget(context, appWidgetManager, appWidgetId, null, false, null);
        }

    }

}
