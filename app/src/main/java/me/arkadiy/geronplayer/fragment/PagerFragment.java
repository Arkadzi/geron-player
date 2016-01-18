package me.arkadiy.geronplayer.fragment;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.adapters.view_pager.ViewAdapter;

public class PagerFragment extends Fragment {
    private Toolbar toolbar;
    private ViewAdapter adapter;
    private SlidingUpPanelLayout panel;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    public PagerFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public void onDestroy() {

        super.onDestroy();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("asd", 10);
        super.onSaveInstanceState(outState);
    }

    private void initLayoutViews(View view) {
        toolbar = (Toolbar) view.findViewById(R.id.tool_bar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        adapter = new ViewAdapter(getChildFragmentManager(), getResources().getStringArray(R.array.categories));
        panel = (SlidingUpPanelLayout) getActivity().findViewById(R.id.sliding_layout);
        viewPager = (ViewPager) view.findViewById(R.id.pager);
        viewPager.setPageTransformer(false, new ViewPager.PageTransformer() {
            @Override
            public void transformPage(View page, float position) {
                /**ROATAION TRANSFORMATION**/
                page.setRotationY(position * -15);
            }
        });
        viewPager.setOffscreenPageLimit(1);
        viewPager.setAdapter(adapter);
        tabLayout = (TabLayout) view.findViewById(R.id.sliding_tabs);
        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                tabLayout.setupWithViewPager(viewPager);
            }
        });
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initLayoutViews(view);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pager, container, false);
    }
}
