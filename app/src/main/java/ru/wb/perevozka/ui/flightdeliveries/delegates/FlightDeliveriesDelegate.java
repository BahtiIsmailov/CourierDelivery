package ru.wb.perevozka.ui.flightdeliveries.delegates;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ru.wb.perevozka.R;
import ru.wb.perevozka.adapters.BaseAdapterDelegate;
import ru.wb.perevozka.databinding.FlightDeliveriesLayoutRouteBinding;
import ru.wb.perevozka.mvvm.model.base.BaseItem;
import ru.wb.perevozka.ui.flightdeliveries.delegates.items.FlightDeliveriesItem;

public class FlightDeliveriesDelegate extends BaseAdapterDelegate<FlightDeliveriesItem, FlightDeliveriesDelegate.RouterViewHolder> {

    @NonNull
    private final OnFlightDeliveriesCallback onFlightDeliveriesCallback;

    public FlightDeliveriesDelegate(@NonNull Context context, @NonNull OnFlightDeliveriesCallback onFlightDeliveriesCallback) {
        super(context);
        this.onFlightDeliveriesCallback = onFlightDeliveriesCallback;
    }

    @Override
    protected boolean isForViewType(@NonNull BaseItem item) {
        return item instanceof FlightDeliveriesItem;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.flight_deliveries_layout_route;
    }

    @NonNull
    @Override
    protected RouterViewHolder createViewHolder(@NonNull View view) {
        return new RouterViewHolder(view);
    }

    @Override
    protected void onBind(@NonNull FlightDeliveriesItem item, @NonNull RouterViewHolder holder) {
        holder.itemView.setTag(item);
        FlightDeliveriesLayoutRouteBinding binding = holder.binding;

        binding.address.setText(item.getAddress());
        binding.redoCount.setText(item.getDeliverCount());

        binding.undoCount.setText(item.getReturnedCount());
        binding.undoCount.setVisibility(item.getReturnedCount().isEmpty() ? View.INVISIBLE : View.VISIBLE);

        boolean isEnabled = item.isEnabled();
        binding.main.setEnabled(isEnabled);
        binding.main.setClickable(isEnabled);
    }

    class RouterViewHolder extends RecyclerView.ViewHolder {

        FlightDeliveriesLayoutRouteBinding binding;

        private RouterViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = FlightDeliveriesLayoutRouteBinding.bind(itemView);
            binding.main.setOnClickListener(view -> {
                FlightDeliveriesItem item = getFlightTag(itemView);
                onFlightDeliveriesCallback.onPickToPointClick(item.getIdView());
            });
        }

        private FlightDeliveriesItem getFlightTag(@NonNull View itemView) {
            return (FlightDeliveriesItem) itemView.getTag();
        }

    }

}