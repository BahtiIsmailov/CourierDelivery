package ru.wb.perevozka.ui.unloadingboxes

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.wb.perevozka.R
import ru.wb.perevozka.databinding.UnloadingBoxesItemLayoutBinding

class UnloadingBoxesAdapter(context: Context, private val items: MutableList<UnloadingBoxesItem>) :
    RecyclerView.Adapter<UnloadingBoxesAdapter.ViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.unloading_boxes_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.barcode.text = items[position].barcode
        holder.binding.time.text = items[position].time
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
        var binding = UnloadingBoxesItemLayoutBinding.bind(rootView)
    }

}