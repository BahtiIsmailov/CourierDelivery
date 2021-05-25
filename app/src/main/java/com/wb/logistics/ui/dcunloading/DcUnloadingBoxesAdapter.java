package com.wb.logistics.ui.dcunloading;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wb.logistics.R;
import com.wb.logistics.databinding.UnloadingBoxesLegendBinding;

import java.util.List;

public class DcUnloadingBoxesAdapter extends ArrayAdapter<String> {

    @NonNull
    private final List<String> items;

    public DcUnloadingBoxesAdapter(@NonNull Context context, @NonNull List<String> items) {
        super(context, R.layout.dc_unloading_boxes_legend);
        this.items = items;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.dc_unloading_boxes_legend, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String item = items.get(position);
        holder.binding.barcode.setText(item);
        return convertView;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    private class ViewHolder {

        UnloadingBoxesLegendBinding binding;

        protected ViewHolder(View rootView) {
            binding = UnloadingBoxesLegendBinding.bind(rootView);
        }

    }

}
