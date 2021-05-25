package com.wb.logistics.ui.dcforcedtermination;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wb.logistics.R;
import com.wb.logistics.databinding.DcForcedTerminationDetailsLegendBinding;
import com.wb.logistics.ui.forcedtermination.ForcedTerminationItem;

import java.util.List;

public class DcForcedTerminationDetailsAdapter extends ArrayAdapter<ForcedTerminationItem> {

    @NonNull
    private final List<DcForcedTerminationDetailsItem> items;

    public DcForcedTerminationDetailsAdapter(@NonNull Context context, @NonNull List<DcForcedTerminationDetailsItem> items) {
        super(context, R.layout.dc_forced_termination_details_legend);
        this.items = items;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.dc_forced_termination_details_legend, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        DcForcedTerminationDetailsItem item = items.get(position);
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

        DcForcedTerminationDetailsLegendBinding binding;

        protected ViewHolder(View rootView) {
            binding = DcForcedTerminationDetailsLegendBinding.bind(rootView);
        }

    }

}
