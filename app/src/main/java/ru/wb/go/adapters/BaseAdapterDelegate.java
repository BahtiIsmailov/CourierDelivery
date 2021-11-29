package ru.wb.go.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hannesdorfmann.adapterdelegates3.AdapterDelegate;
import ru.wb.go.mvvm.model.base.BaseItem;

import java.util.List;

public abstract class BaseAdapterDelegate<T extends BaseItem, VH extends RecyclerView.ViewHolder> extends AdapterDelegate<List<BaseItem>> {

    @NonNull
    protected final Context context;
    @NonNull
    private final LayoutInflater inflater;

    public BaseAdapterDelegate(@NonNull Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    protected boolean isForViewType(@NonNull List<BaseItem> items, int position) {
        return isForViewType(items.get(position));
    }

    protected abstract boolean isForViewType(@NonNull BaseItem item);

    @LayoutRes
    protected abstract int getLayoutId();

    @NonNull
    protected abstract VH createViewHolder(@NonNull View view);

    protected abstract void onBind(@NonNull T item, @NonNull VH holder);

    @NonNull
    @Override
    protected RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = inflater.inflate(getLayoutId(), parent, false);
        return createViewHolder(view);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onBindViewHolder(@NonNull List<BaseItem> items,
                                    int position,
                                    @NonNull RecyclerView.ViewHolder holder,
                                    @NonNull List<Object> payloads) {
        T item = (T) items.get(position);
        VH viewHolder = (VH) holder;
        onBind(item, viewHolder);
    }

}