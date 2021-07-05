package com.wb.logistics.ui.flightdeliveriesdetails.delegates;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wb.logistics.R;
import com.wb.logistics.adapters.BaseAdapterDelegate;
import com.wb.logistics.databinding.FlightDeliveriesErrorDelegateBinding;
import com.wb.logistics.mvvm.model.base.BaseItem;
import com.wb.logistics.ui.flightdeliveriesdetails.delegates.items.FlightDeliveriesDetailsErrorItem;

public class FlightDeliveriesErrorDelegate extends BaseAdapterDelegate<FlightDeliveriesDetailsErrorItem, FlightDeliveriesErrorDelegate.RouterViewHolder> {

    public FlightDeliveriesErrorDelegate(@NonNull Context context) {
        super(context);
    }

    @Override
    protected boolean isForViewType(@NonNull BaseItem item) {
        return item instanceof FlightDeliveriesDetailsErrorItem;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.flight_deliveries_error_delegate;
    }

    @NonNull
    @Override
    protected RouterViewHolder createViewHolder(@NonNull View view) {
        return new RouterViewHolder(view);
    }

    @Override
    protected void onBind(@NonNull FlightDeliveriesDetailsErrorItem item, @NonNull RouterViewHolder holder) {
        holder.itemView.setTag(item);
        FlightDeliveriesErrorDelegateBinding binding = holder.binding;
        binding.barcode.setText(item.getBarcode());
        binding.data.setText(item.getData());
    }

    class RouterViewHolder extends RecyclerView.ViewHolder {

        FlightDeliveriesErrorDelegateBinding binding;

        private RouterViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = FlightDeliveriesErrorDelegateBinding.bind(itemView);
        }

    }

}