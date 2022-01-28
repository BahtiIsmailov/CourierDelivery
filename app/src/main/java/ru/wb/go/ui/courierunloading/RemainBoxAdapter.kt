package ru.wb.go.ui.courierunloading

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.wb.go.R
import ru.wb.go.databinding.RemainBoxItemBinding


class RemainBoxAdapter (
        context: Context,
        private val items: MutableList<String>,
    ) : RecyclerView.Adapter<RemainBoxAdapter.ViewHolder>() {
        private val inflater: LayoutInflater = LayoutInflater.from(context)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = inflater.inflate(R.layout.remain_box_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.binding.remainBoxCode.text = items[position]
        }

        override fun getItemCount(): Int {
            return items.size
        }

        inner class ViewHolder(rootView: View) :
            RecyclerView.ViewHolder(rootView) {

            var binding = RemainBoxItemBinding.bind(rootView)

        }

        fun clear() {
            items.clear()
        }

        fun addItems(items: List<String>) {
            this.items.addAll(items)
        }

    }