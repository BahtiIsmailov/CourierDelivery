package com.wb.logistics.ui.unloading;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wb.logistics.R;
import com.wb.logistics.databinding.ReceptionBoxesItemLayoutBinding;

import java.util.List;

public class UnloadingReturnBoxesAdapter extends ArrayAdapter<String> {

    @NonNull
    private final List<UnloadingReturnBoxesItem> items;
    @NonNull
    private final OnItemClickCallBack onItemClickCallBack;

    public interface OnItemClickCallBack {

        void onItemClick(int index, boolean isChecked);

    }

    public UnloadingReturnBoxesAdapter(@NonNull Context context,
                                       @NonNull List<UnloadingReturnBoxesItem> items,
                                       @NonNull OnItemClickCallBack onItemClickCallBack) {
        super(context, R.layout.unloading_boxes_item_layout);
        this.items = items;
        this.onItemClickCallBack = onItemClickCallBack;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.unloading_boxes_item_layout, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        UnloadingReturnBoxesItem item = items.get(position);
        holder.binding.number.setText(item.getNumber());
        holder.binding.box.setText(item.getBarcode());
        holder.binding.address.setText(item.getData());
        holder.binding.checked.setOnCheckedChangeListener(null);
        holder.binding.checked.setChecked(item.isChecked());
        holder.binding.checked.setOnCheckedChangeListener((buttonView, isChecked) -> onItemClickCallBack.onItemClick(position, isChecked));
        return convertView;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    protected class ViewHolder {

        ReceptionBoxesItemLayoutBinding binding;

        protected ViewHolder(View rootView) {
            binding = ReceptionBoxesItemLayoutBinding.bind(rootView);
        }

    }

}
