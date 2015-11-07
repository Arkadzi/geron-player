package me.arkadiy.geronplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import me.arkadiy.geronplayer.adapters.ViewAdapter;
import me.arkadiy.geronplayer.fragment.PagerFragment;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ViewAdapter adapter;
    private ViewPager viewPager;
    private boolean musicBound;
    public MusicService mService;

    private SlidingUpPanelLayout panel;
    private ServiceConnection musicConnection = new ServiceConnection() {


        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e("onServiceConnected()", "begin");
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            mService = binder.getService();
            musicBound = true;
            initLayoutViews();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
            Log.e("onServiceDisconnected()", "musicSrv == null" + (mService == null));
        }
    };


    @Override
    protected void onDestroy() {
        if (mService != null) {
            Intent playIntent = new Intent(this, MusicService.class);
            stopService(playIntent);
            getApplicationContext().unbindService(musicConnection);
            mService = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment == null) {
            android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragment = new PagerFragment();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
        }

        Intent playIntent = new Intent(getApplicationContext(), MusicService.class);
        playIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
        if (!MusicService.isStarted()) {
            startService(playIntent);
        }
        getApplicationContext().bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
//        getWindow().getDecorView().setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    private void initLayoutViews() {
//        toolbar = (Toolbar) findViewById(R.id.tool_bar);
//        setSupportActionBar(toolbar);
//        adapter = new ViewAdapter(getSupportFragmentManager());
        panel = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
//        viewPager = (ViewPager) findViewById(R.id.pager);
//        viewPager.setOffscreenPageLimit(1);
//        viewPager.setAdapter(adapter);
        View slider = findViewById(R.id.slider);
        panel.setDragView(slider);

//        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
//        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onBackPressed() {
        if (panel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            panel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else
            super.onBackPressed();
    }
}

