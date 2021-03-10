package com.wb.logistics.ui.delivery.delegates;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wb.logistics.R;
import com.wb.logistics.adapters.BaseAdapterDelegate;
import com.wb.logistics.databinding.DeliveryLayoutRouteEmptyBinding;
import com.wb.logistics.mvvm.model.base.BaseItem;
import com.wb.logistics.ui.delivery.delegates.items.RouteEmptyItem;

public class RouteEmptyDelegate extends BaseAdapterDelegate<RouteEmptyItem,
        RouteEmptyDelegate.EmptyViewHolder> {

    @NonNull
    private final OnRouteEmptyCallback onRouteEmptyCallback;

    public RouteEmptyDelegate(@NonNull Context context, @NonNull OnRouteEmptyCallback onRouteEmptyCallback) {
        super(context);
        this.onRouteEmptyCallback = onRouteEmptyCallback;
    }

    @Override
    protected boolean isForViewType(@NonNull BaseItem item) {
        return item instanceof RouteEmptyItem;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.delivery_layout_route_empty;
    }

    @NonNull
    @Override
    protected EmptyViewHolder createViewHolder(@NonNull View view) {
        return new EmptyViewHolder(view);
    }

    @Override
    protected void onBind(@NonNull RouteEmptyItem item,
                          @NonNull EmptyViewHolder holder) {
        holder.binding.emptyFlightMessageText.setText(item.getMessage());
    }

    class EmptyViewHolder extends RecyclerView.ViewHolder {

        DeliveryLayoutRouteEmptyBinding binding;

        private EmptyViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = DeliveryLayoutRouteEmptyBinding.bind(itemView);
            binding.progressButton.setOnClickListener(view -> onRouteEmptyCallback.onUpdateRouteClick());
        }

    }

}