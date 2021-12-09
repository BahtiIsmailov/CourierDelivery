package ru.wb.go.ui.flightpickpoint.delegates;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ru.wb.go.R;
import ru.wb.go.adapters.BaseAdapterDelegate;
import ru.wb.go.databinding.FlightPickPointDelegteBinding;
import ru.wb.go.mvvm.model.base.BaseItem;
import ru.wb.go.ui.flightpickpoint.delegates.items.FlightPickPointItem;

public class FlightPickPointDelegate extends BaseAdapterDelegate<FlightPickPointItem, FlightPickPointDelegate.RouterViewHolder> {

    public FlightPickPointDelegate(@NonNull Context context) {
        super(context);
    }

    @Override
    protected boolean isForViewType(@NonNull BaseItem item) {
        return item instanceof FlightPickPointItem;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.flight_pick_point_delegte;
    }

    @NonNull
    @Override
    protected RouterViewHolder createViewHolder(@NonNull View view) {
        return new RouterViewHolder(view);
    }

    @Override
    protected void onBind(@NonNull FlightPickPointItem item, @NonNull RouterViewHolder holder) {
        holder.itemView.setTag(item);
        FlightPickPointDelegteBinding binding = holder.binding;
        binding.address.setText(item.getAddress());
        binding.deliverCount.setText(item.getDeliverCount());
        binding.pickupCount.setText(item.getPickupCount());
        binding.pickupCount.setVisibility(item.isPickupPoint() ? View.VISIBLE : View.GONE);
    }

    class RouterViewHolder extends RecyclerView.ViewHolder {

        FlightPickPointDelegteBinding binding;

        private RouterViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = FlightPickPointDelegteBinding.bind(itemView);
        }

    }

}