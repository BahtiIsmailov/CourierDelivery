package com.wb.logistics.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import androidx.constraintlayout.widget.ConstraintLayout
import com.wb.logistics.databinding.SelectorLayoutBinding
import com.wb.logistics.ui.config.KeyValueAdapter
import com.wb.logistics.ui.config.dao.KeyValueDao

class SelectorView : ConstraintLayout {
    private lateinit var binding: SelectorLayoutBinding

    private var title: String? = null
    private lateinit var items: List<KeyValueDao>

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(
        context, attrs
    ) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        init()
    }

    private fun init() {
        initUI()
    }

    private fun initUI() {
        binding = SelectorLayoutBinding.inflate(
            LayoutInflater.from(context), this, true
        )
    }

    fun initData(title: String, items: List<KeyValueDao>) {
        this.title = title
        this.items = items
        setupTitle(title)
        setupAdapter(items)
    }

    fun selectItem(value: String) {
        val position = findPosition(value, items)
        if (position != -1) {
            binding.spinnerSelector.setSelection(position)
        }
    }

    fun selectItem(value: KeyValueDao) {
        val position = findPosition(value, items)
        if (position != -1) {
            binding.spinnerSelector.setSelection(position)
        }
    }

    override fun onDetachedFromWindow() {
        title = null
        super.onDetachedFromWindow()
    }

    private fun setupTitle(title: String?) {
        binding.textTitle.text = title
    }

    private fun setupAdapter(items: List<KeyValueDao>) {
        val adapter = KeyValueAdapter(context, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSelector.adapter = adapter
    }

    private fun findPosition(value: String, items: List<KeyValueDao>): Int {
        for (i in items.indices) {
            if (items[i].value == value) {
                return i
            }
        }
        return -1
    }

    private fun findPosition(value: KeyValueDao, items: List<KeyValueDao>): Int {
        for (i in items.indices) {
            if (items[i] == value) {
                return i
            }
        }
        return -1
    }

    fun setItemListener(listener: OnItemSelectListener) {
        //this.listener = listener
        binding.spinnerSelector.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
                listener.onItemSelected(items[i])
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
    }

    interface OnItemSelectListener {
        fun onItemSelected(keyValue: KeyValueDao)
    }
}