package me.arkadiy.geronplayer.statics;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import me.arkadiy.geronplayer.MainActivity;
import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.plain.Song;

/**
 * Created by Arkadiy on 06.01.2016.
 */
public class MenuManager {
    private Dialog menu;

    public void showMenu(Context c, Song currentSong) {
        menu = createMenuDialog(c, currentSong);
        menu.show();
    }

    public void dismissMenu() {
        if (menu != null) {
            if (menu.isShowing()) {
                menu.dismiss();
            }
            menu = null;
        }
    }

    private Dialog createMenuDialog(final Context c, final Song song) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setItems(R.array.single_song_menu, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onMenuItemClick((MainActivity) c, song, which);
            }
        });
        builder.setTitle(song.getTitle());
        return builder.create();
    }

    protected void onMenuItemClick(MainActivity activity, Song song, int which) {
        switch (which) {
            case 0:
                MyRingtoneManager.setRingtone(activity, song);
                Snackbar.make(activity.findViewById(R.id.song_settings), R.string.ringtone_set, Snackbar.LENGTH_SHORT).show();
                break;
            case 1:

                menu = createEditDialog(activity, song);
                menu.show();
        }
    }

    private Dialog createEditDialog(final MainActivity activity, final Song song) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final View view = activity.getLayoutInflater().inflate(R.layout.create_dialog, null);
        builder.setView(view);
        builder.setPositiveButton(R.string.action_accept, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText mTitle = (EditText) view.findViewById(R.id.playlist_name);

            }
        });

        return builder.create();
    }
}
