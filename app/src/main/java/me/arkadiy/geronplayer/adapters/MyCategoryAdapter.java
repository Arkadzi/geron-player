package me.arkadiy.geronplayer.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.arkadiy.geronplayer.Resetable;
import me.arkadiy.geronplayer.views.RecyclerViewFastScroller;

/**
 * Created by Arkadiy on 10.11.2015.
 */
public abstract class MyCategoryAdapter<T>
        extends RecyclerView.Adapter<MyCategoryAdapter.ViewHolder>
        implements RecyclerViewFastScroller.BubbleTextGetter, Resetable<T> {

    private int imageId;
    private int viewId;
    private int mainId;
    private int secondaryId;

    private List<T> categories;
    private ItemListener listener;

    @Override
    public String getTextToShowInBubble(int pos) {
        return Character.toString(getMainText(categories.get(pos)).charAt(0));
    }

    public interface ItemListener {
        void onClick(int position);
    }

    public void setListener(ItemListener listener) {
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final View mView;

        public final TextView secondary;
        public final TextView main;
        public final ImageView image;

        public ViewHolder(View view, int mainId, int secondaryId, int imageId) {
            super(view);
            mView = view;
            main = (TextView) view.findViewById(mainId);
            secondary = (TextView) view.findViewById(secondaryId);
            image = (ImageView) view.findViewById(imageId);
        }
    }

    public MyCategoryAdapter(List<T> categories, int viewId, int mainId, int secondaryId, int imageId) {
        this.viewId = viewId;
        this.mainId = mainId;
        this.secondaryId = secondaryId;
        this.categories = categories;
        this.imageId = imageId;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(viewId, parent, false);

        return new ViewHolder(view, mainId, secondaryId, imageId);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.main.setText(getMainText(categories.get(position)));
        holder.secondary.setText(getSecondaryText(categories.get(position)));
        setImage(categories.get(position), holder.image);
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onClick(position);
                }
            }
        });
    }

    protected abstract void setImage(T element, ImageView image);

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void setData(List<T> data) {
        if (categories != null) {
            categories.clear();
        } else {
            categories = new ArrayList<T>();
        }
        if (data != null) {
            categories.addAll(data);
        }
        notifyDataSetChanged();
    }

    protected abstract String getMainText(T element);
    protected abstract String getSecondaryText(T element);
}
