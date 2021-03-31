package com.wb.logistics.ui.reception;

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

public class ReceptionBoxesAdapter extends ArrayAdapter<String> {

    @NonNull
    private final List<ReceptionBoxItem> items;
    @NonNull
    private final OnItemClickCallBack onItemClickCallBack;

    public interface OnItemClickCallBack {

        void onItemClick(int index, boolean isChecked);

    }

    public ReceptionBoxesAdapter(@NonNull Context context,
                                 @NonNull List<ReceptionBoxItem> items,
                                 @NonNull OnItemClickCallBack onItemClickCallBack) {
        super(context, R.layout.reception_boxes_item_layout);
        this.items = items;
        this.onItemClickCallBack = onItemClickCallBack;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.reception_boxes_item_layout, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ReceptionBoxItem item = items.get(position);
        holder.binding.number.setText(item.getNumber());
        holder.binding.box.setText(item.getBox());
        holder.binding.address.setText(item.getAddress());
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
