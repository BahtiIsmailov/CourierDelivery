package ru.wb.go.ui.flightdeliveriesdetails.delegates;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ru.wb.go.R;
import ru.wb.go.adapters.BaseAdapterDelegate;
import ru.wb.go.databinding.FlightDeliveriesDetailsDelegateBinding;
import ru.wb.go.mvvm.model.base.BaseItem;
import ru.wb.go.ui.flightdeliveriesdetails.delegates.items.FlightDeliveriesDetailsItem;

public class FlightDeliveriesDetailsDelegate extends BaseAdapterDelegate<FlightDeliveriesDetailsItem, FlightDeliveriesDetailsDelegate.RouterViewHolder> {

    public FlightDeliveriesDetailsDelegate(@NonNull Context context) {
        super(context);
    }

    @Override
    protected boolean isForViewType(@NonNull BaseItem item) {
        return item instanceof FlightDeliveriesDetailsItem;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.flight_deliveries_details_delegate;
    }

    @NonNull
    @Override
    protected RouterViewHolder createViewHolder(@NonNull View view) {
        return new RouterViewHolder(view);
    }

    @Override
    protected void onBind(@NonNull FlightDeliveriesDetailsItem item, @NonNull RouterViewHolder holder) {
        holder.itemView.setTag(item);
        FlightDeliveriesDetailsDelegateBinding binding = holder.binding;
        binding.barcode.setText(item.getBarcode());
        binding.data.setText(item.getData());
    }

    class RouterViewHolder extends RecyclerView.ViewHolder {

        FlightDeliveriesDetailsDelegateBinding binding;

        private RouterViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = FlightDeliveriesDetailsDelegateBinding.bind(itemView);
        }

    }

}