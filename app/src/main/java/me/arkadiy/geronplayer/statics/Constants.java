package me.arkadiy.geronplayer.statics;

import android.content.ContentUris;
import android.net.Uri;
import android.util.Log;

public class Constants {
    public static final String STACK = "me.gumenniy.stack";

    public interface MENU {
        int PLAY = 1;
        int PLAY_NEXT = 2;
        int ADD_TO_QUEUE = 3;
        int ADD_TO_PLAYLIST = 4;
        int DELETE = 5;
        int RENAME = 6;
        int EDIT = 7;
        int SET_RINGTONE = 8;
        int SET_ARTWORK = 9;

    }

    public interface ACTION {
        String PREV_ACTION = "me.gumenniy.foregroundservice.action.prev";
        String PAUSE_PLAY_ACTION = "me.gumenniy.foregroundservice.action.pause_play";
        String NEXT_ACTION = "me.gumenniy.foregroundservice.action.next";
        String START_SERVICE_ACTION = "me.gumenniy.foregroundservice.action.start";
        String STOP_SERVICE_ACTION = "me.gumenniy.foregroundservice.action.stop";
        String BEGIN_FOREGROUND_ACTION = "me.gumenniy.foregroundservice.action.begin_foreground";
        String STOP_FOREGROUND_ACTION = "me.gumenniy.foregroundservice.action.stop_foreground";
        String PAUSE_ACTION = "me.gumenniy.foregroundservice.action.pause";
    }

    public interface WIDGET {
        String PREV_ACTION = "me.gumenniy.widget.action.prev";
        String NEXT_ACTION = "me.gumenniy.widget.action.next";
        String PLAY_PAUSE_ACTION = "me.gumenniy.widget.action.start";
        String UPDATE_ACTION = "me.gumenniy.widget.action.update";
    }

    public interface NOTIFICATION_ID {
        int FOREGROUND_SERVICE = 101;
    }

    public interface PREFERENCES {
        String PREF_NAME = "GeronPlayerPrefs";
        String SONG_NAME = "songPosition";
        String SONG_PROGRESS = "songProgress";
        String SHUFFLE_STATE = "shuffleState";
        String REPEAT_STATE = "repeatState";
        String EQUALIZER_ENABLED = "equlizerEnable";
        String BASS_STATE = "bassState";
        String VIRT_STATE = "virtState";
        String EQUALIZER_PRESET = "preset";
        String EQUALIZER_BAND = "band";

    }

    public static Uri getArtworks(long album_id) {
        Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri uri = ContentUris.withAppendedId(artworkUri, album_id);
        Log.e("uri", uri.toString());
        return uri;
    }

    public interface THEME {
        String THEME = "theme";
        int LIGHT = 0;
        int DARK = 1;
    }
}
