package com.wb.logistics.adapters;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hannesdorfmann.adapterdelegates3.AdapterDelegate;
import com.hannesdorfmann.adapterdelegates3.AdapterDelegatesManager;
import com.wb.logistics.mvp.model.base.BaseItem;

import java.util.ArrayList;
import java.util.List;

public class DefaultAdapter extends RecyclerView.Adapter {

    @NonNull
    private final AdapterDelegatesManager<List<BaseItem>> delegatesManager;
    @NonNull
    private final List<BaseItem> items;

    public DefaultAdapter() {
        this.delegatesManager = new AdapterDelegatesManager<>();
        items = new ArrayList<>();
    }

    @Override
    public int getItemViewType(int position) {
        return delegatesManager.getItemViewType(items, position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return delegatesManager.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        delegatesManager.onBindViewHolder(items, position, holder);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void clear() {
        items.clear();
    }

    public void addItems(@NonNull List<BaseItem> items) {
        this.items.addAll(items);
    }

    public void addItem(int index, BaseItem baseItem) {
        if (index >= 0 && index <= items.size()) {
            items.add(index, baseItem);
        }
    }

    @NonNull
    public List<BaseItem> getItems() {
        return items;
    }

    public void setItem(int index, @NonNull BaseItem item) {
        if (items.size() > index) items.set(index, item);
    }

    public void removeItem(int position) {
        if (items.size() > position) {
            items.remove(position);
        }
    }

    public DefaultAdapter addDelegate(@NonNull AdapterDelegate<List<BaseItem>> delegate) {
        delegatesManager.addDelegate(delegate);
        return this;
    }

}