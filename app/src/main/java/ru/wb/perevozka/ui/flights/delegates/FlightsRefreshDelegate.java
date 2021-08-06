package ru.wb.perevozka.ui.flights.delegates;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ru.wb.perevozka.R;
import ru.wb.perevozka.adapters.BaseAdapterDelegate;
import ru.wb.perevozka.databinding.FlightsLayoutRouteUpdateBinding;
import ru.wb.perevozka.mvvm.model.base.BaseItem;
import ru.wb.perevozka.ui.flights.delegates.items.FlightRefreshItem;
import ru.wb.perevozka.views.ProgressButtonMode;

@Deprecated
public class FlightsRefreshDelegate extends BaseAdapterDelegate<FlightRefreshItem,
        FlightsRefreshDelegate.EmptyViewHolder> {

    @NonNull
    private final OnFlightsUpdateCallback onRouteEmptyCallback;

    public FlightsRefreshDelegate(@NonNull Context context, @NonNull OnFlightsUpdateCallback onRouteEmptyCallback) {
        super(context);
        this.onRouteEmptyCallback = onRouteEmptyCallback;
    }

    @Override
    protected boolean isForViewType(@NonNull BaseItem item) {
        return item instanceof FlightRefreshItem;
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
    protected void onBind(@NonNull FlightRefreshItem item,
                          @NonNull EmptyViewHolder holder) {
        holder.binding.emptyFlightMessageText.setText(item.getMessage());
        holder.binding.update.setState(ProgressButtonMode.ENABLE);
    }

    class EmptyViewHolder extends RecyclerView.ViewHolder {

        FlightsLayoutRouteUpdateBinding binding;

        private EmptyViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = FlightsLayoutRouteUpdateBinding.bind(itemView);
            binding.update.setOnClickListener(view -> {
                binding.update.setState(ProgressButtonMode.PROGRESS);
                onRouteEmptyCallback.onUpdateRouteClick();
            });

        }

    }

}