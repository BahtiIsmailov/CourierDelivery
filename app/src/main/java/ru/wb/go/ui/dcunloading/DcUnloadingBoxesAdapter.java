package ru.wb.go.ui.dcunloading;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ru.wb.go.R;
import ru.wb.go.databinding.DcUnloadingBoxesItemLayoutBinding;

import java.util.List;

public class DcUnloadingBoxesAdapter extends ArrayAdapter<DcUnloadingBoxesItem> {

    @NonNull
    private final List<DcUnloadingBoxesItem> items;

    public DcUnloadingBoxesAdapter(@NonNull Context context, @NonNull List<DcUnloadingBoxesItem> items) {
        super(context, R.layout.dc_unloading_boxes_item_layout);
        this.items = items;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.dc_unloading_boxes_item_layout, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        DcUnloadingBoxesItem item = items.get(position);
        holder.binding.barcode.setText(item.getBarcode());
        holder.binding.time.setText(item.getTime());
        return convertView;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    private class ViewHolder {

        DcUnloadingBoxesItemLayoutBinding binding;

        protected ViewHolder(View rootView) {
            binding = DcUnloadingBoxesItemLayoutBinding.bind(rootView);
        }

    }

}
