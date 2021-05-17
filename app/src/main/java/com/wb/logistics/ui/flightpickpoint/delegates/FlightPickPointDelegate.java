package com.wb.logistics.ui.flightpickpoint.delegates;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wb.logistics.R;
import com.wb.logistics.adapters.BaseAdapterDelegate;
import com.wb.logistics.databinding.FlightPickPointDelegteBinding;
import com.wb.logistics.mvvm.model.base.BaseItem;
import com.wb.logistics.ui.flightpickpoint.delegates.items.FlightPickPointItem;

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
        binding.redoCount.setText(item.getRedoCount());
    }

    class RouterViewHolder extends RecyclerView.ViewHolder {

        FlightPickPointDelegteBinding binding;

        private RouterViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = FlightPickPointDelegteBinding.bind(itemView);
        }

    }

}