package me.arkadiy.geronplayer.fragment.pager;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import me.arkadiy.geronplayer.MainActivity;
import me.arkadiy.geronplayer.R;
import me.arkadiy.geronplayer.adapters.MyCategoryAdapter;
import me.arkadiy.geronplayer.loader.AbstractLoader;
import me.arkadiy.geronplayer.plain.Nameable;
import me.arkadiy.geronplayer.plain.Song;
import me.arkadiy.geronplayer.views.RecyclerViewFastScroller;

/**
 * Created by Arkadiy on 03.11.2015.
 */
public abstract class AbstractListFragment<T extends Nameable> extends Fragment
        implements LoaderManager.LoaderCallbacks<List<T>>, SearchView.OnQueryTextListener, MainActivity.BackPressListener {
    protected boolean showScroller = true;
    protected RecyclerView mRecyclerView;
    protected AbstractLoader<T> loader;
    protected RecyclerViewFastScroller fastScroller;
    protected List<T> data;

    private MyCategoryAdapter<T> adapter;



    @Override
    public boolean onBackPress() {
        if (searchView != null && !searchView.isIconified()) {
            searchView.setIconified(true);
            return true;
        }
        return false;
    }

    private SearchView searchView;

    public T getItem(int position) {
        T item =  adapter.getItem(position);
        return item;
    }

    @Override
    public void onPause() {
        super.onPause();
        ((MainActivity) getActivity()).removeListener(this);
        if (loader != null) {
            loader.unregisterObserver();
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
        ((MainActivity) getActivity()).addListener(this);
        if (loader != null) {
            loader.registerObserver();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
    }

    @Override
    public void onDestroyOptionsMenu() {
        searchView.setIconified(true);
        super.onDestroyOptionsMenu();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_menu, menu);
        final MenuItem item = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextChange(String query) {
        // Here is where we are going to implement our filter logic
//        MyCategoryAdapter mAdapter = (MyCategoryAdapter) mRecyclerView.getAdapter();
        final List<T> filteredModelList = filter(data, query);
        adapter.animateTo(filteredModelList);
        mRecyclerView.scrollToPosition(0);
        return true;
    }

    private List<T> filter(List<T> models, String query) {
        query = query.toLowerCase();

        final List<T> filteredModelList = new ArrayList<>();
        for (T model : models) {
            final String text = model.getName().toLowerCase();
            if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        GridLayoutManager manager = new RecyclerLayoutManager(getActivity(), getColumnCount(), GridLayoutManager.VERTICAL, false);
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.list);
        fastScroller = (RecyclerViewFastScroller) view.findViewById(R.id.fast_scroller);
        if (!showScroller)
        fastScroller.setVisibility(View.GONE);
        mRecyclerView.setLayoutManager(manager);
        fastScroller.setRecyclerView(mRecyclerView);
        fastScroller.setViewsToUse(R.layout.recycler_view_fast_scroller__fast_scroller, R.id.fastscroller_bubble, R.id.fastscroller_handle);
        data = new ArrayList<T>();
        adapter = getNewAdapter(data);
        mRecyclerView.setAdapter(adapter);
        setListener(adapter);
        return view;
    }

    protected abstract int getColumnCount();

    protected abstract void setListener(MyCategoryAdapter adapter);

    protected abstract MyCategoryAdapter<T> getNewAdapter(List<T> data);

    public class RecyclerLayoutManager extends GridLayoutManager {
        public RecyclerLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
            super(context, spanCount, orientation, reverseLayout);
        }
//        public RecyclerLayoutManager(Context context, int orientation, boolean reverseLayout) {
//            super(context, orientation, reverseLayout);
//        }

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
//            RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
            fastScroller.setVisibility(adapter.getItemCount() > itemsShown ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<T>> loader) {
        if (mRecyclerView != null && mRecyclerView.getAdapter() != null) {
            data = new ArrayList<T>();
            adapter.setData(data);
        }
    }


    @Override
    public void onLoadFinished(Loader<List<T>> loader, List<T> data) {
        this.data = data;
        if (mRecyclerView != null)
            if (mRecyclerView.getAdapter() == null) {
                adapter = getNewAdapter(data);
                mRecyclerView.setAdapter(adapter);
                setListener(adapter);
            } else {
                adapter.setData(data);
            }
    }

}
