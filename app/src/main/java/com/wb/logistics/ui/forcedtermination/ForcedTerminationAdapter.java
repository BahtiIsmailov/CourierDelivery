package com.wb.logistics.ui.forcedtermination;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wb.logistics.R;
import com.wb.logistics.databinding.ForcedTerminationLegendBinding;

import java.util.List;

public class ForcedTerminationAdapter extends ArrayAdapter<ForcedTerminationItem> {

    @NonNull
    private final List<ForcedTerminationItem> items;

    public ForcedTerminationAdapter(@NonNull Context context, @NonNull List<ForcedTerminationItem> items) {
        super(context, R.layout.forced_termination_legend);
        this.items = items;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.forced_termination_legend, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ForcedTerminationItem item = items.get(position);
        holder.binding.number.setText(item.getNumber());
        holder.binding.barcode.setText(item.getBarcode());
        holder.binding.data.setText(item.getData());
        return convertView;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    private class ViewHolder {

        ForcedTerminationLegendBinding binding;

        protected ViewHolder(View rootView) {
            binding = ForcedTerminationLegendBinding.bind(rootView);
        }

    }

}
