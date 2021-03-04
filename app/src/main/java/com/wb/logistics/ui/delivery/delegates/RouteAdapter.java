package com.wb.logistics.ui.delivery.delegates;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wb.logistics.R;
import com.wb.logistics.databinding.DeliveryLayoutRouteLegendBinding;

import java.util.List;

public class RouteAdapter extends ArrayAdapter<String> {

    @NonNull
    private List<String> items;
    @NonNull
    private final OnItemClickCallBack onItemClickCallBack;

    public interface OnItemClickCallBack {

        void onItemClick(int idItem);

    }

    public RouteAdapter(@NonNull Context context, @NonNull List<String> items,
                        @NonNull OnItemClickCallBack onItemClickCallBack) {
        super(context, R.layout.delivery_layout_route_legend);
        this.items = items;
        this.onItemClickCallBack = onItemClickCallBack;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.delivery_layout_route_legend, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String item = items.get(position);
        holder.binding.routeLegendDescriptionText.setText(item);
        return convertView;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    protected class ViewHolder {

        DeliveryLayoutRouteLegendBinding binding;

        protected ViewHolder(View rootView) {
            binding = DeliveryLayoutRouteLegendBinding.bind(rootView);
        }

    }

}
