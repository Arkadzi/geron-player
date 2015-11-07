package me.arkadiy.geronplayer.loader;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import java.util.List;

/**
 * Created by Arkadiy on 03.11.2015.
 */
public abstract class AbstractLoader<T>  extends AsyncTaskLoader<List<T>> {
    protected String param;
    protected ContentResolver musicResolver;
    private ForceLoadContentObserver observer;
    protected List<T> list;

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
        if (takeContentChanged() || list == null) {
            forceLoad();
            Log.e("AbstractLoader", "forceLoad()");
        }
    }

    public void registerObserver() {
        Log.e("AbstractLoader", "registerObserver() " + param);
        musicResolver.registerContentObserver(getUri(), true, observer);
    }

    public void unregisterObserver() {
        Log.e("AbstractLoader", "unregisterObserver() " + param);
        musicResolver.unregisterContentObserver(observer);
    }

    protected abstract List<T> getList();
}
