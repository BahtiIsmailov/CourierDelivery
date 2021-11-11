package ru.wb.go.ui.flightdeliveries.delegates;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ru.wb.go.R;
import ru.wb.go.adapters.BaseAdapterDelegate;
import ru.wb.go.databinding.FlightDeliveriesUnloadLayoutBinding;
import ru.wb.go.mvvm.model.base.BaseItem;
import ru.wb.go.ui.flightdeliveries.delegates.items.FlightDeliveriesUnloadItem;

public class FlightDeliveriesUnloadDelegate extends BaseAdapterDelegate<FlightDeliveriesUnloadItem, FlightDeliveriesUnloadDelegate.UnloadViewHolder> {

    @NonNull
    private final OnFlightDeliveriesCallback onFlightDeliveriesCallback;

    public FlightDeliveriesUnloadDelegate(@NonNull Context context, @NonNull OnFlightDeliveriesCallback onFlightDeliveriesCallback) {
        super(context);
        this.onFlightDeliveriesCallback = onFlightDeliveriesCallback;
    }

    @Override
    protected boolean isForViewType(@NonNull BaseItem item) {
        return item instanceof FlightDeliveriesUnloadItem;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.flight_deliveries_unload_layout;
    }

    @NonNull
    @Override
    protected UnloadViewHolder createViewHolder(@NonNull View view) {
        return new UnloadViewHolder(view);
    }

    @Override
    protected void onBind(@NonNull FlightDeliveriesUnloadItem item, @NonNull UnloadViewHolder holder) {
        holder.itemView.setTag(item);
        FlightDeliveriesUnloadLayoutBinding binding = holder.binding;

        binding.address.setText(item.getAddress());
        binding.deliveryCount.setText(item.getUnloadedCount());

        binding.tookCount.setText(item.getReturnCount());
        binding.tookCount.setVisibility(item.getReturnCount().isEmpty() ? View.INVISIBLE : View.VISIBLE);
    }

    class UnloadViewHolder extends RecyclerView.ViewHolder {

        FlightDeliveriesUnloadLayoutBinding binding;

        private UnloadViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = FlightDeliveriesUnloadLayoutBinding.bind(itemView);
            binding.main.setOnClickListener(view -> {
                FlightDeliveriesUnloadItem item = getFlightTag(itemView);
                onFlightDeliveriesCallback.onPickToPointClick(item.getIdView());
            });
        }

        private FlightDeliveriesUnloadItem getFlightTag(@NonNull View itemView) {
            return (FlightDeliveriesUnloadItem) itemView.getTag();
        }

    }

}