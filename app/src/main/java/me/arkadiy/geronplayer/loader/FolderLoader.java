package me.arkadiy.geronplayer.loader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.plain.Folder;
import me.arkadiy.geronplayer.plain.Song;

/**
 * Created by Arkadiy on 06.11.2015.
 */
public class FolderLoader extends AbstractLoader<Folder> {
    private String unknownArtist;

    public FolderLoader(Context context, String param) {
        super(context, param);
        try {
            this.unknownArtist = context.getResources().getString(R.string.unknown_artist);
        } catch (Exception e) { }
    }

    @Override
    protected Uri getUri() {
        return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    }

    @Override
    protected List<Folder> getList() {
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        Cursor musicCursor = musicResolver.query(getUri(), null, selection, null, null);
        ArrayList<Folder> folders = new ArrayList<>();
        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
//            int playlist = musicCursor.getColumnIndex(MediaStore.Audio.Playlists.NAME);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int albumIdColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM_ID);
            int artistIdColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST_ID);
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int albumColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int songNumberColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.TRACK);
            int pathColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int filenameColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
            //add songList to list
            do {
                String thisArtist = musicCursor.getString(artistColumn);
                String thisPath = musicCursor.getString(pathColumn);
                String thisFilename = musicCursor.getString(filenameColumn);
                thisPath = thisPath.substring(0, thisPath.lastIndexOf(thisFilename) - 1);

                String data = musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE));
                if (!data.startsWith("application")) {
                    Folder currentFolder = null;
                    for (Folder folder : folders) {
                        if (folder.getPath().equals(thisPath)) {
                            currentFolder = folder;
                            break;
                        }
                    }
                    if (currentFolder == null) {
                        String thisFolderName = thisPath.substring(thisPath.lastIndexOf("/") + 1);
                        currentFolder = new Folder(thisFolderName, thisPath);
                        folders.add(currentFolder);
                    }
                }
            }
            while (musicCursor.moveToNext());
            musicCursor.close();
            Collections.sort(folders, new Comparator<Folder>() {
                public int compare(Folder a, Folder b) {
                    return a.getName().compareToIgnoreCase(b.getName());
                }
            });
        }
        return folders;
    }
}
