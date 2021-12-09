package ru.wb.go.ui.flightdeliveries.delegates;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ru.wb.go.R;
import ru.wb.go.adapters.BaseAdapterDelegate;
import ru.wb.go.databinding.FlightDeliveriesNotUnloadLayoutBinding;
import ru.wb.go.mvvm.model.base.BaseItem;
import ru.wb.go.ui.flightdeliveries.delegates.items.FlightDeliveriesNotUnloadItem;

public class FlightDeliveriesNotUnloadDelegate extends BaseAdapterDelegate<FlightDeliveriesNotUnloadItem, FlightDeliveriesNotUnloadDelegate.NotUnloadViewHolder> {

    @NonNull
    private final OnFlightDeliveriesCallback onFlightDeliveriesCallback;

    public FlightDeliveriesNotUnloadDelegate(@NonNull Context context, @NonNull OnFlightDeliveriesCallback onFlightDeliveriesCallback) {
        super(context);
        this.onFlightDeliveriesCallback = onFlightDeliveriesCallback;
    }

    @Override
    protected boolean isForViewType(@NonNull BaseItem item) {
        return item instanceof FlightDeliveriesNotUnloadItem;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.flight_deliveries_not_unload_layout;
    }

    @NonNull
    @Override
    protected NotUnloadViewHolder createViewHolder(@NonNull View view) {
        return new NotUnloadViewHolder(view);
    }

    @Override
    protected void onBind(@NonNull FlightDeliveriesNotUnloadItem item, @NonNull NotUnloadViewHolder holder) {
        holder.itemView.setTag(item);
        FlightDeliveriesNotUnloadLayoutBinding binding = holder.binding;

        binding.address.setText(item.getAddress());
        binding.deliveryCount.setText(item.getUnloadedCount());

        binding.tookCount.setText(item.getReturnCount());
        binding.tookCount.setVisibility(item.getReturnCount().isEmpty() ? View.INVISIBLE : View.VISIBLE);
    }

    class NotUnloadViewHolder extends RecyclerView.ViewHolder {

        FlightDeliveriesNotUnloadLayoutBinding binding;

        private NotUnloadViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = FlightDeliveriesNotUnloadLayoutBinding.bind(itemView);
            binding.main.setOnClickListener(view -> {
                FlightDeliveriesNotUnloadItem item = getFlightTag(itemView);
                onFlightDeliveriesCallback.onPickToPointClick(item.getIdView());
            });
        }

        private FlightDeliveriesNotUnloadItem getFlightTag(@NonNull View itemView) {
            return (FlightDeliveriesNotUnloadItem) itemView.getTag();
        }

    }

}