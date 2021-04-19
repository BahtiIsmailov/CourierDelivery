package com.wb.logistics.ui.flightdeliveries.delegates;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wb.logistics.R;
import com.wb.logistics.adapters.BaseAdapterDelegate;
import com.wb.logistics.databinding.FlightsLayoutRouteUpdateBinding;
import com.wb.logistics.mvvm.model.base.BaseItem;
import com.wb.logistics.ui.flightdeliveries.delegates.items.FlightDeliveriesRefreshItem;
import com.wb.logistics.views.ProgressButtonMode;

public class FlightDeliveriesRefreshDelegate extends BaseAdapterDelegate<FlightDeliveriesRefreshItem,
        FlightDeliveriesRefreshDelegate.EmptyViewHolder> {

    @NonNull
    private final OnFlightDeliveriesUpdateCallback onRouteEmptyCallback;

    public FlightDeliveriesRefreshDelegate(@NonNull Context context, @NonNull OnFlightDeliveriesUpdateCallback onRouteEmptyCallback) {
        super(context);
        this.onRouteEmptyCallback = onRouteEmptyCallback;
    }

    @Override
    protected boolean isForViewType(@NonNull BaseItem item) {
        return item instanceof FlightDeliveriesRefreshItem;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.flights_layout_route_update;
    }

    @NonNull
    @Override
    protected EmptyViewHolder createViewHolder(@NonNull View view) {
        return new EmptyViewHolder(view);
    }

    @Override
    protected void onBind(@NonNull FlightDeliveriesRefreshItem item,
                          @NonNull EmptyViewHolder holder) {
        holder.binding.emptyFlightMessageText.setText(item.getMessage());
        holder.binding.progressButton.setState(ProgressButtonMode.ENABLE);
    }

    class EmptyViewHolder extends RecyclerView.ViewHolder {

        FlightsLayoutRouteUpdateBinding binding;

        private EmptyViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = FlightsLayoutRouteUpdateBinding.bind(itemView);
            binding.progressButton.setOnClickListener(view -> {
                binding.progressButton.setState(ProgressButtonMode.PROGRESS);
                onRouteEmptyCallback.onUpdateRouteClick();
            });

        }

    }

}