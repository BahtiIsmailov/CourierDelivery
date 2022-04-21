package ru.wb.go.ui.courierunloading

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.wb.go.R
import ru.wb.go.databinding.RemainBoxFragmentBinding
import ru.wb.go.ui.BaseServiceFragment
import ru.wb.go.ui.app.NavDrawerListener
import ru.wb.go.ui.app.NavToolbarListener

class RemainBoxFragment :
    BaseServiceFragment<RemainBoxViewModel, RemainBoxFragmentBinding>(
        RemainBoxFragmentBinding::inflate
    ) {

    companion object {
        const val BOX_REMAIN_KEY = "box_remain_id_key"
    }

    private lateinit var adapter: RemainBoxAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var smoothScroller: RecyclerView.SmoothScroller

    override val viewModel by viewModel<RemainBoxViewModel> {
        parametersOf(
            requireArguments().getParcelable<RemainBoxParameters>(
                BOX_REMAIN_KEY
            )
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initRecyclerView()
        initSmoothScroller()
        initObserver()
    }

    private fun initView() {
        (activity as NavToolbarListener).hideToolbar()
        (activity as NavDrawerListener).lockNavDrawer()
        binding.toolbarLayout.toolbarTitle.text = getText(R.string.remain_box_title)
        binding.toolbarLayout.back.visibility = View.VISIBLE
        binding.toolbarLayout.back.setOnClickListener { findNavController().popBackStack() }
    }

    private fun initRecyclerView() {
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.remainBoxList.layoutManager = layoutManager
        binding.remainBoxList.setHasFixedSize(true)
        initSmoothScroller()
    }

    private fun initSmoothScroller() {
        smoothScroller = object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initObserver() {

        viewModel.boxes.observe(viewLifecycleOwner) {
            when (it) {
                is RemainBoxItemState.InitItems -> {
                    binding.emptyList.visibility = View.GONE
                    binding.remainBoxList.visibility = View.VISIBLE

                    adapter = RemainBoxAdapter(requireContext(), it.items)
                    binding.remainBoxList.adapter = adapter
                }
                is RemainBoxItemState.UpdateItems -> {
                    adapter.clear()
                    adapter.addItems(it.items)
                    adapter.notifyDataSetChanged()
                }
                is RemainBoxItemState.Empty -> {
                    binding.emptyList.visibility = View.VISIBLE

                    binding.remainBoxList.visibility = View.GONE
                    binding.emptyTitle.text = it.info

                }
            }
        }

    }

}

@Parcelize
data class RemainBoxParameters(val officeId: Int) : Parcelable
