package ru.wb.go.ui.flightdeliveries.delegates;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ru.wb.go.R;
import ru.wb.go.databinding.FlightDeliveriesLayoutRouteLegendBinding;

import java.util.List;

public class FlightDeliveriesAdapter extends ArrayAdapter<String> {

    @NonNull
    private List<String> items;
    @NonNull
    private final OnItemClickCallBack onItemClickCallBack;

    public interface OnItemClickCallBack {

        void onItemClick(int idItem);

    }

    public FlightDeliveriesAdapter(@NonNull Context context, @NonNull List<String> items,
                                   @NonNull OnItemClickCallBack onItemClickCallBack) {
        super(context, R.layout.flight_deliveries_layout_route_legend);
        this.items = items;
        this.onItemClickCallBack = onItemClickCallBack;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.flight_deliveries_layout_route_legend, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String item = items.get(position);
        //holder.binding.routeLegendDescriptionText.setText(item);
        return convertView;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    private class ViewHolder {

        FlightDeliveriesLayoutRouteLegendBinding binding;

        protected ViewHolder(View rootView) {
            binding = FlightDeliveriesLayoutRouteLegendBinding.bind(rootView);
        }

    }

}
