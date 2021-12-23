package ru.wb.go.ui.courierwarehouses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.go.databinding.CourierWarehouseFragmentBinding
import ru.wb.go.ui.courierorders.CourierOrderParameters
import ru.wb.go.ui.dialogs.DialogInfoFragment
import ru.wb.go.ui.dialogs.DialogInfoFragment.Companion.DIALOG_INFO_TAG
import ru.wb.go.ui.dialogs.ProgressDialogFragment
import ru.wb.go.ui.dialogs.ProgressDialogFragment.Companion.PROGRESS_DIALOG_BACK_KEY
import ru.wb.go.ui.dialogs.ProgressDialogFragment.Companion.PROGRESS_DIALOG_RESULT
import ru.wb.go.ui.dialogs.ProgressDialogFragment.Companion.PROGRESS_DIALOG_TAG
import ru.wb.go.ui.splash.KeyboardListener
import ru.wb.go.ui.splash.NavDrawerListener
import ru.wb.go.ui.splash.NavToolbarListener
import ru.wb.go.views.ProgressButtonMode


class CourierWarehousesFragment : Fragment() {

    private val viewModel by viewModel<CourierWarehousesViewModel>()

    private var _binding: CourierWarehouseFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CourierWarehousesAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var smoothScroller: RecyclerView.SmoothScroller

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = CourierWarehouseFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initRecyclerView()
        initObservable()
        initListeners()
        initReturnResult()
        viewModel.update()
    }

    private fun initReturnResult() {
        setFragmentResultListener(PROGRESS_DIALOG_RESULT) { _, bundle ->
            if (bundle.containsKey(PROGRESS_DIALOG_BACK_KEY)) {
                viewModel.onCancelLoadClick()
            }
        }
    }

    private fun showProgressDialog() {
        val progressDialog = ProgressDialogFragment.newInstance()
        progressDialog.show(parentFragmentManager, PROGRESS_DIALOG_TAG)
    }

    private fun closeProgressDialog() {
        parentFragmentManager.findFragmentByTag(PROGRESS_DIALOG_TAG)?.let {
            if (it is ProgressDialogFragment) it.dismiss()
        }
    }

    private fun initView() {
        (activity as NavToolbarListener).showToolbar()
        (activity as NavDrawerListener).unlock()
        (activity as KeyboardListener).panMode()
    }

    private fun initObservable() {

        viewModel.navigateToDialogInfo.observe(viewLifecycleOwner) {
            showDialogInfo(it.type, it.title, it.message, it.button)
        }

        viewModel.warehouses.observe(viewLifecycleOwner) {
            when (it) {
                is CourierWarehouseItemState.InitItems -> {
                    binding.emptyList.visibility = GONE
                    binding.progress.visibility = GONE
                    binding.items.visibility = VISIBLE
                    binding.update.setState(ProgressButtonMode.ENABLE)
                    val callback = object : CourierWarehousesAdapter.OnItemClickCallBack {
                        override fun onItemClick(index: Int) {
                            viewModel.onItemClick(index)
                        }

                        override fun onDetailClick(index: Int) {
                            viewModel.onDetailClick(index)
                        }
                    }
                    adapter = CourierWarehousesAdapter(requireContext(), it.items, callback)
                    binding.items.adapter = adapter
                }
                is CourierWarehouseItemState.UpdateItems -> {
                    adapter.clear()
                    adapter.addItems(it.items)
                    adapter.notifyDataSetChanged()
                }
                is CourierWarehouseItemState.Empty -> {
                    binding.emptyList.visibility = VISIBLE
                    binding.progress.visibility = GONE
                    binding.items.visibility = GONE
                    binding.emptyTitle.text = it.info
                    binding.update.setState(ProgressButtonMode.ENABLE)
                }
            }
        }

        viewModel.progressState.observe(viewLifecycleOwner) {
            when (it) {
                CourierWarehousesProgressState.Progress -> showProgressDialog()
                CourierWarehousesProgressState.ProgressComplete -> closeProgressDialog()
            }
        }

        viewModel.holdState.observe(viewLifecycleOwner) {
            when (it) {
                true -> binding.holdLayout.visibility = VISIBLE
                false -> binding.holdLayout.visibility = GONE
            }
        }

        viewModel.navigationState.observe(viewLifecycleOwner) {
            when (it) {
                CourierWarehousesNavigationState.NavigateToBack -> findNavController().popBackStack()
                is CourierWarehousesNavigationState.NavigateToCourierOrder ->
                    findNavController().navigate(
                        CourierWarehousesFragmentDirections.actionCourierWarehouseFragmentToCourierOrderFragment(
                            CourierOrderParameters(it.officeId, it.address)
                        )
                    )
            }
        }

    }

    private fun initListeners() {
        binding.update.setOnClickListener { viewModel.onUpdateClick() }
    }

    private fun initRecyclerView() {
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.items.layoutManager = layoutManager
        binding.items.setHasFixedSize(true)
        initSmoothScroller()
    }

    private fun initSmoothScroller() {
        smoothScroller = object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showDialogInfo(
        type: Int,
        title: String,
        message: String,
        positiveButtonName: String
    ) {
        DialogInfoFragment.newInstance(
            type = type,
            title = title,
            message = message,
            positiveButtonName = positiveButtonName
        ).show(parentFragmentManager, DIALOG_INFO_TAG)
    }

}