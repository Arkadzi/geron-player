package me.arkadiy.geronplayer;

/**
 * Created by Arkadiy on 25.07.2015.
 */
public class Constants {
    public interface ACTION {
        String PREV_ACTION = "me.gumenniy.foregroundservice.action.prev";
        String PAUSE_PLAY_ACTION = "me.gumenniy.foregroundservice.action.pause_play";
        String NEXT_ACTION = "me.gumenniy.foregroundservice.action.next";
        String STARTFOREGROUND_ACTION = "me.gumenniy.foregroundservice.action.start";
        String STOPFOREGROUND_ACTION = "me.gumenniy.foregroundservice.action.stop";
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
}
