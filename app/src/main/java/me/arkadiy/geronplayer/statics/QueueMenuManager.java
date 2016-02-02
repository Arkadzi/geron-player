package me.arkadiy.geronplayer.statics;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import me.arkadiy.geronplayer.MainActivity;
import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.fragment.pager.ToolbarFragment;
import me.arkadiy.geronplayer.plain.Category;
import me.arkadiy.geronplayer.plain.Song;

public class QueueMenuManager {
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
            case 1: {
                FragmentManager fm = activity.getSupportFragmentManager();
                fm.popBackStack(Constants.STACK, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fm.beginTransaction()
                        .replace(R.id.fragment_container, ToolbarFragment.newInstance(ToolbarFragment.ARTIST, song.getArtistID(), song.getArtist(), null), "artist")
                        .addToBackStack(Constants.STACK)
                        .commit();
            }
            break;
            case 2: {
                FragmentManager fm = activity.getSupportFragmentManager();
                fm.popBackStack(Constants.STACK, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fm.beginTransaction()
                        .replace(R.id.fragment_container, ToolbarFragment.newInstance(ToolbarFragment.ALBUM, song.getAlbumID(), song.getAlbum(), null), "album")
                        .addToBackStack(Constants.STACK)
                        .commit();
            }
            break;
            case 3:
                showPlaylistDialog(activity, song);
                break;
        }

    }

    protected void showPlaylistDialog(final Activity c, final Song song) {

        new AsyncTask<Void, Void, Void>() {
            private Activity context = c;
            List<Song> songs;
            List<Category> playlists;
            @Override
            protected Void doInBackground(Void... params) {
                playlists = PlaylistUtils.getPlaylists(context);
                songs = new ArrayList<>();
                songs.add(song);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                String[] items = new String[playlists.size() + 1];
                items[0] = c.getResources().getString(R.string.create_playlist);
                for (int i = 0; i < playlists.size(); i++) {
                    items[i + 1] = playlists.get(i).getName();
                }

                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(song.getTitle());
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, final int which) {
                        if (which == 0) {
                            showCreateDialog(songs, c, 0, playlists);
                        } else {
                            new Thread() {
                                @Override
                                public void run() {
                                    PlaylistUtils.addSongs(c, playlists.get(which - 1).getID(), songs);
                                }
                            }.start();
                        }
                    }
                });
                menu = builder.create();
                menu.show();
            }
        }.execute();

    }

    private void showCreateDialog(final List<Song> songs, final Activity c, int position, final List<Category> playlists) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle(R.string.create_playlist);
        final View view = c.getLayoutInflater().inflate(R.layout.create_dialog, null);
        builder.setView(view);
        builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText mPlaylistName = (EditText) view.findViewById(R.id.playlist_name);
                String playlistName = mPlaylistName.getText().toString();
                if (!playlistName.isEmpty()) {
                    final String newName = handleName(playlistName, playlists);
                    new Thread() {
                        @Override
                        public void run() {
                            long id = PlaylistUtils.createPlaylist(c, newName);
                            if (id > -1) {
                                PlaylistUtils.addSongs(c, id, songs);
                            }
                        }
                    }.start();

                }
            }
        });
        menu = builder.create();
        menu.show();
    }

    public static String handleName(String playlistName, List<Category> playlists) {
        int additionalNumber = 0;
        int i;
        do {
            String name = (additionalNumber == 0) ?
                    playlistName :
                    String.format("%s %d", playlistName, additionalNumber);
            for (i = 0; i < playlists.size(); i++) {
                if (name.equals(playlists.get(i).getName())) {
                    additionalNumber++;
                    break;
                }
            }
        } while (i < playlists.size());
        if (additionalNumber != 0) {
            playlistName = String.format("%s %d", playlistName, additionalNumber);
        }
        return playlistName;
    }

}
