package me.arkadiy.geronplayer.adapters.view_pager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

import me.arkadiy.geronplayer.MusicService;
import me.arkadiy.geronplayer.fragment.ImageFragment;
import me.arkadiy.geronplayer.plain.Song;

/**
 * Created by Arkadiy on 11.12.2015.
 */
public class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
    private List<Song> songs;
    private MusicService service;

    public ScreenSlidePagerAdapter(FragmentManager fm) {
        super(fm);
        songs = new ArrayList<>();
    }

    @Override
    public Fragment getItem(int position) {
        if (service != null) {
            return ImageFragment.newInstance(songs.get(service.getIndexByPosition(position)).getAlbumID());
        } else {
            return ImageFragment.newInstance(songs.get(position).getAlbumID());
        }
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    public void setService(MusicService service) {
        this.service = service;
    }
}
