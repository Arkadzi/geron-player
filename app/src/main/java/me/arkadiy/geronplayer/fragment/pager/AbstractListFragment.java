package me.arkadiy.geronplayer.fragment.pager;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.Resetable;
import me.arkadiy.geronplayer.adapters.MyCategoryAdapter;
import me.arkadiy.geronplayer.loader.AbstractLoader;
import me.arkadiy.geronplayer.loader.ArtistLoader;
import me.arkadiy.geronplayer.plain.Category;
import me.arkadiy.geronplayer.views.RecyclerViewFastScroller;

/**
 * Created by Arkadiy on 03.11.2015.
 */
public abstract class AbstractListFragment<T> extends Fragment implements LoaderManager.LoaderCallbacks<List<T>> {

    protected RecyclerView mRecyclerView;
    protected AbstractLoader<T> loader;
    protected RecyclerViewFastScroller fastScroller;

    @Override
    public void onPause() {
        super.onPause();
        if (loader != null) {
            loader.unregisterObserver();
        } else {
            Log.e("AbstractListFragment", "don't unregister");
        }
    }

    @Override
    public Loader<List<T>> onCreateLoader(int id, Bundle args) {
        loader = getNewLoader();
        return loader;
    }

    public abstract AbstractLoader<T> getNewLoader();

    @Override
    public void onDestroy() {
        loader = null;
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (loader != null) {
            loader.registerObserver();
        } else {
            Log.e("AbstractListFragment", "don't register");
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getLoaderManager().initLoader(0, null, this);

    }

    protected abstract void setListener(MyCategoryAdapter adapter);

    protected abstract MyCategoryAdapter getNewAdapter(List<T> data);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LinearLayoutManager manager = new RecyclerLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.list);
        fastScroller = (RecyclerViewFastScroller) view.findViewById(R.id.fast_scroller);
        mRecyclerView.setLayoutManager(manager);
        fastScroller.setRecyclerView(mRecyclerView);
        fastScroller.setViewsToUse(R.layout.recycler_view_fast_scroller__fast_scroller, R.id.fastscroller_bubble, R.id.fastscroller_handle);
        MyCategoryAdapter adapter = getNewAdapter(new ArrayList<T>());
        mRecyclerView.setAdapter(adapter);
        setListener(adapter);
        return view;
    }

    public class RecyclerLayoutManager extends LinearLayoutManager {
        public RecyclerLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        @Override
        public void onLayoutChildren(final RecyclerView.Recycler recycler, final RecyclerView.State state) {
            super.onLayoutChildren(recycler, state);
            final int firstVisibleItemPosition = findFirstVisibleItemPosition();
            if (firstVisibleItemPosition != 0) {
                if (firstVisibleItemPosition == -1)
                    fastScroller.setVisibility(View.GONE);
                return;
            }
            final int lastVisibleItemPosition = findLastVisibleItemPosition();
            int itemsShown = lastVisibleItemPosition - firstVisibleItemPosition + 1;
            RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
            fastScroller.setVisibility(adapter.getItemCount() > itemsShown ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<T>> loader) {
        if (mRecyclerView != null && mRecyclerView.getAdapter() != null)
            ((Resetable) mRecyclerView.getAdapter()).setData(new ArrayList<T>());
    }


    @Override
    public void onLoadFinished(Loader<List<T>> loader, List<T> data) {
        if (mRecyclerView != null)
            if (mRecyclerView.getAdapter() == null) {
                MyCategoryAdapter adapter = getNewAdapter(data);
                mRecyclerView.setAdapter(adapter);
                setListener(adapter);
            } else {
                ((Resetable) mRecyclerView.getAdapter()).setData(data);
            }
    }

}
