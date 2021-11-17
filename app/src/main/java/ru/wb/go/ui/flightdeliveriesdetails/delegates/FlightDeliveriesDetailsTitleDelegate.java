package ru.wb.go.ui.flightdeliveriesdetails.delegates;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ru.wb.go.R;
import ru.wb.go.adapters.BaseAdapterDelegate;
import ru.wb.go.databinding.FlightDeliveriesDetailsTitleDelegateBinding;
import ru.wb.go.mvvm.model.base.BaseItem;
import ru.wb.go.ui.flightdeliveriesdetails.delegates.items.FlightDeliveriesDetailsTitleItem;

public class FlightDeliveriesDetailsTitleDelegate extends BaseAdapterDelegate<FlightDeliveriesDetailsTitleItem, FlightDeliveriesDetailsTitleDelegate.RouterViewHolder> {

    public FlightDeliveriesDetailsTitleDelegate(@NonNull Context context) {
        super(context);
    }

    @Override
    protected boolean isForViewType(@NonNull BaseItem item) {
        return item instanceof FlightDeliveriesDetailsTitleItem;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.flight_deliveries_details_title_delegate;
    }

    @NonNull
    @Override
    protected RouterViewHolder createViewHolder(@NonNull View view) {
        return new RouterViewHolder(view);
    }

    @Override
    protected void onBind(@NonNull FlightDeliveriesDetailsTitleItem item, @NonNull RouterViewHolder holder) {
        holder.itemView.setTag(item);
        FlightDeliveriesDetailsTitleDelegateBinding binding = holder.binding;
        binding.divider.setVisibility(item.isHeader() ? View.VISIBLE : View.GONE);
        binding.deliveryTitle.setText(item.getTitle());
        binding.deliveryTitleCount.setText(item.getCount());
    }

    class RouterViewHolder extends RecyclerView.ViewHolder {

        FlightDeliveriesDetailsTitleDelegateBinding binding;

        private RouterViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = FlightDeliveriesDetailsTitleDelegateBinding.bind(itemView);
        }

    }

}