package com.wb.logistics.ui.flights.delegates;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wb.logistics.R;
import com.wb.logistics.adapters.BaseAdapterDelegate;
import com.wb.logistics.mvvm.model.base.BaseItem;
import com.wb.logistics.ui.flights.delegates.items.FlightProgressItem;

public class FlightsProgressDelegate extends BaseAdapterDelegate<FlightProgressItem,
        FlightsProgressDelegate.ProgressViewHolder> {

    public FlightsProgressDelegate(@NonNull Context context) {
        super(context);
    }

    @Override
    protected boolean isForViewType(@NonNull BaseItem item) {
        return item instanceof FlightProgressItem;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.flights_layout_route_progress;
    }

    @NonNull
    @Override
    protected ProgressViewHolder createViewHolder(@NonNull View view) {
        return new ProgressViewHolder(view);
    }

    @Override
    protected void onBind(@NonNull FlightProgressItem item, @NonNull ProgressViewHolder holder) {

    }

    class ProgressViewHolder extends RecyclerView.ViewHolder {

        private ProgressViewHolder(@NonNull View itemView) {
            super(itemView);
        }

    }

}