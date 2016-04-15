package me.arkadiy.geronplayer.adapters.list_view;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arkadiy on 10.11.2015.
 */
public abstract class MyCategoryAdapter<T>
        extends RecyclerView.Adapter<MyCategoryAdapter.ViewHolder> {

    private int imageId;
    private int viewId;
    private int mainId;
    private int secondaryId;
    private int thirdId;
    private List<T> categories;
    private ItemClickListener listener;
    private ItemLongClickListener longListener;
    private DataChangedListener dataChangedListener;
    public MyCategoryAdapter(List<T> categories, int viewId, int mainId, int secondaryId, int thirdId, int imageId) {
        this.viewId = viewId;
        this.mainId = mainId;
        this.secondaryId = secondaryId;
        this.thirdId = thirdId;
        this.imageId = imageId;
        this.categories = new ArrayList<>(categories);
    }

    public T getItem(int position) {
        if (position >= 0 && position < categories.size()) {
            return categories.get(position);
        }
        return null;
    }

    public void setDataChangedListener(DataChangedListener listener) {
        dataChangedListener = listener;
        dataChangedListener.onDataChanged(true);
    }

    public void setClickListener(ItemClickListener listener) {
        this.listener = listener;
    }

    public void setLongClickListener(ItemLongClickListener listener) {
        this.longListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(viewId, parent, false);
        return new ViewHolder(view, mainId, secondaryId, thirdId, imageId);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.main.setText(getMainText(categories.get(position)));
        holder.secondary.setText(getSecondaryText(categories.get(position)));
        holder.third.setText(getThirdText(categories.get(position)));
        setImage(categories.get(position), holder.image);
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onClick(position);
                }
            }
        });
        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (longListener != null) {
                    longListener.onLongClick(position);
                }
                return false;
            }
        });
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        holder.itemView.setOnLongClickListener(null);
        holder.itemView.setOnClickListener(null);
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public List<T> getData() {
        return categories;
    }

    public void setData(List<T> data) {
        if (categories != null) {
            categories.clear();
        } else {
            categories = new ArrayList<>();
        }
        if (data != null) {
            categories.addAll(data);
        }
        notifyDataSetChanged();
        notifyDataChangeListener();
    }

    public T removeItem(int position) {
        final T model = categories.remove(position);
        notifyItemRemoved(position);
        notifyDataChangeListener();
        return model;
    }

    public void addItem(int position, T model) {
        categories.add(position, model);
        notifyItemInserted(position);
        notifyDataChangeListener();

    }

    public void moveItem(int fromPosition, int toPosition) {
        final T model = categories.remove(fromPosition);
        categories.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
        notifyDataChangeListener();
    }

    public void animateTo(List<T> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
        notifyDataSetChanged();
        notifyDataChangeListener();
    }

    private void notifyDataChangeListener() {
        if (dataChangedListener != null) {
            dataChangedListener.onDataChanged(categories.size() > 0);
        }
    }

    private void applyAndAnimateRemovals(List<T> newModels) {
        for (int i = categories.size() - 1; i >= 0; i--) {
            final T model = categories.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<T> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final T model = newModels.get(i);
            if (!categories.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<T> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final T model = newModels.get(toPosition);
            final int fromPosition = categories.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    protected abstract String getMainText(T element);

    protected abstract String getSecondaryText(T element);

    protected abstract String getThirdText(T element);

    protected abstract void setImage(T element, ImageView image);

    public interface ItemClickListener {
        void onClick(int position);
    }

    public interface ItemLongClickListener {
        void onLongClick(int position);
    }

    public interface DataChangedListener {
        void onDataChanged(boolean hasData);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final View mView;

        public final TextView main;
        public final TextView secondary;
        public final TextView third;
        public final ImageView image;

        public ViewHolder(View view, int mainId, int secondaryId, int thirdId, int imageId) {
            super(view);
            mView = view;
            main = (TextView) view.findViewById(mainId);
            secondary = (TextView) view.findViewById(secondaryId);
            third = (TextView) view.findViewById(thirdId);
            image = (ImageView) view.findViewById(imageId);
        }
    }
}
