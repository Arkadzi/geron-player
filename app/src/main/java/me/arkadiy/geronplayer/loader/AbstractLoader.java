package me.arkadiy.geronplayer.loader;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;

import java.util.List;

public abstract class AbstractLoader<T> extends AsyncTaskLoader<List<T>> {
    protected String param;
    protected ContentResolver musicResolver;
    protected List<T> list;
    private ForceLoadContentObserver observer;

    public AbstractLoader(Context context, String param) {
        super(context);
        musicResolver = context.getContentResolver();
        observer = new ForceLoadContentObserver();
        this.param = param;
    }

    @Override
    public List<T> loadInBackground() {
        list = getList();
        return list;
    }

    protected abstract Uri getUri();

    @Override
    protected void onStartLoading() {

        if (list != null) {
            deliverResult(list);
        }
        boolean contentChanged = takeContentChanged();
        if (contentChanged || list == null) {
            forceLoad();
        }
    }

    public void registerObserver() {
        musicResolver.registerContentObserver(getUri(), true, observer);
    }

    public void unregisterObserver() {
        musicResolver.unregisterContentObserver(observer);
    }

    protected abstract List<T> getList();
}
