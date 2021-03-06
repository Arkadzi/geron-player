package me.arkadiy.geronplayer.adapters.view_pager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

import me.arkadiy.geronplayer.fragment.pager.ArtistListFragment;
import me.arkadiy.geronplayer.fragment.pager.FolderListFragment;
import me.arkadiy.geronplayer.fragment.pager.GenreListFragment;
import me.arkadiy.geronplayer.fragment.pager.PlaylistFragment;
import me.arkadiy.geronplayer.fragment.pager.SongListFragment;

/**
 * Created by Arkadiy on 04.08.2015.
 */
public class ViewAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT = 5;
    private String mTabTitles[];
    private ArrayList<Fragment> fragments = new ArrayList<>(PAGE_COUNT);
    public ViewAdapter(FragmentManager fm, String[] tabTitles) {
        super(fm);
        mTabTitles = tabTitles;
        fragments.add(GenreListFragment.newInstance("3"));
        fragments.add(ArtistListFragment.newInstance("1"));
        fragments.add(SongListFragment.newInstance(SongListFragment.ALL, 0, true, null));
        fragments.add(FolderListFragment.newInstance("5"));
        fragments.add(PlaylistFragment.newInstance("4"));
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        return  fragments.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTabTitles[position];
    }

}
