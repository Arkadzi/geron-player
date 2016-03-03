package me.arkadiy.geronplayer.loader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.plain.Folder;
import me.arkadiy.geronplayer.plain.Song;
import me.arkadiy.geronplayer.statics.MusicRetriever;

/**
 * Created by Arkadiy on 06.11.2015.
 */
public class FolderLoader extends AbstractLoader<Folder> {

    public FolderLoader(Context context, String param) {
        super(context, param);
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

            int pathColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int filenameColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);

            do {
                String thisPath = musicCursor.getString(pathColumn);
                String thisFilename = musicCursor.getString(filenameColumn);
                thisPath = thisPath.substring(0, thisPath.indexOf(thisFilename) - 1);
                String folderPath = thisPath + File.separator;

                String data = musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE));
                if (!data.startsWith("application")) {
                    Folder currentFolder = null;
                    for (Folder folder : folders) {
                        if (folder.getPath().equals(folderPath)) {
                            currentFolder = folder;
                            break;
                        }
                    }
                    if (currentFolder == null) {
                        String thisFolderName = thisPath.substring(thisPath.lastIndexOf(File.separatorChar) + 1);
                        currentFolder = new Folder(thisFolderName, folderPath);
                        long length = MusicRetriever.getLengthByFolder(getContext(), currentFolder.getPath());
                        currentFolder.setLength(length);

                        folders.add(currentFolder);
                    }
                }
            }
            while (musicCursor.moveToNext());
            Collections.sort(folders, new Comparator<Folder>() {
                public int compare(Folder a, Folder b) {
                    return a.getName().compareToIgnoreCase(b.getName());
                }
            });
        }
        if (musicCursor != null) {
            musicCursor.close();
        }
        return folders;
    }
}
