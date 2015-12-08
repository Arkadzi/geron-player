package me.arkadiy.geronplayer;

import android.content.ContentUris;
import android.net.Uri;
import android.util.Log;

/**
 * Created by Arkadiy on 25.07.2015.
 */
public class Constants {
    public interface ACTION {
        String PREV_ACTION = "me.gumenniy.foregroundservice.action.prev";
        String PAUSE_PLAY_ACTION = "me.gumenniy.foregroundservice.action.pause_play";
        String NEXT_ACTION = "me.gumenniy.foregroundservice.action.next";
        String START_SERVICE_ACTION = "me.gumenniy.foregroundservice.action.start";
        String STOP_SERVICE_ACTION = "me.gumenniy.foregroundservice.action.stop";
        String BEGIN_FOREGROUND_ACTION = "me.gumenniy.foregroundservice.action.begin_foreground";
        String STOP_FOREGROUND_ACTION = "me.gumenniy.foregroundservice.action.stop_foreground";
    }

    public interface WIDGET {
        String PREV_ACTION = "me.gumenniy.widget.action.prev";
        String NEXT_ACTION = "me.gumenniy.widget.action.next";
        String STARTFOREGROUND_ACTION = "me.gumenniy.widget.action.start";
        String UPDATE_ACTION = "me.gumenniy.widget.action.update";
    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }

    public static Uri getArtworks(long album_id) {
        Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri uri = ContentUris.withAppendedId(artworkUri, album_id);
        Log.e("uri", uri.toString());
        return uri;
    }
}
