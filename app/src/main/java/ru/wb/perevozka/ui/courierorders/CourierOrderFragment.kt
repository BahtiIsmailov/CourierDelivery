package ru.wb.perevozka.ui.courierorders

import android.app.AlertDialog
import android.os.Bundle
import android.os.Parcelable
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView.*
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.wb.perevozka.R
import ru.wb.perevozka.adapters.DefaultAdapterDelegate
import ru.wb.perevozka.databinding.CourierOrderFragmentBinding
import ru.wb.perevozka.mvvm.model.base.BaseItem
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.ui.courierorders.delegates.CourierOrderDelegate
import ru.wb.perevozka.ui.dialogs.InformationDialogFragment
import ru.wb.perevozka.ui.flights.delegates.FlightsProgressDelegate
import ru.wb.perevozka.ui.splash.NavToolbarListener
import ru.wb.perevozka.views.ProgressButtonMode


class CourierOrderFragment : Fragment() {

    companion object {
        const val COURIER_ORDER_ID_KEY = "courier_order_id_key"
    }

    private val viewModel by viewModel<CourierOrderViewModel> {
        parametersOf(
            requireArguments().getParcelable<CourierOrderParameters>(
                COURIER_ORDER_ID_KEY
            )
        )
    }

    private var _binding: CourierOrderFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: DefaultAdapterDelegate
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var smoothScroller: SmoothScroller
    private lateinit var progressDialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = CourierOrderFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as NavToolbarListener).hideToolbar()
        initRecyclerView()
        initAdapter()
        initListener()
        initStateObserve()
        initProgressDialog()
    }

    private fun initProgressDialog() {
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomProgressAlertDialog)
        val viewGroup: ViewGroup = binding.recyclerView
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.custom_progress_layout_dialog, viewGroup, false)
        builder.setView(dialogView)
        progressDialog = builder.create()
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                progressDialog.dismiss()
                viewModel.onCancelLoadClick()
            }
            true
        }
    }

    private fun initListener() {
        binding.toolbarLayout.back.setOnClickListener { findNavController().popBackStack() }
        binding.update.setOnClickListener { viewModel.onUpdateClick() }
    }

    private fun initStateObserve() {

        viewModel.navigateToMessage.observe(viewLifecycleOwner) {
            InformationDialogFragment.newInstance(it.title, it.message, it.button)
                .show(parentFragmentManager, "INFO_MESSAGE_TAG")
        }

        viewModel.toolbarNetworkState.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Failed -> {
                    binding.toolbarLayout.noInternetImage.visibility = View.VISIBLE
                }

                is NetworkState.Complete -> {
                    binding.toolbarLayout.noInternetImage.visibility = View.INVISIBLE
                }
            }
        }

        viewModel.toolbarLabelState.observe(viewLifecycleOwner) {
            binding.toolbarLayout.toolbarTitle.text = it.label
        }

        viewModel.stateUIList.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CourierOrderUIListState.ShowOrders -> {
                    binding.emptyList.visibility = GONE
                    binding.progress.visibility = GONE
                    binding.recyclerView.visibility = VISIBLE
                    binding.update.setState(ProgressButtonMode.ENABLE)
                    displayItems(state.items)
                }
                is CourierOrderUIListState.Empty -> {
                    binding.emptyList.visibility = VISIBLE
                    binding.recyclerView.visibility = GONE
                    binding.emptyTitle.text = state.info
                }
            }
        }

        viewModel.progressState.observe(viewLifecycleOwner) { state ->
            when (state) {
                CourierOrderProgressState.Progress -> showProgressDialog()
                CourierOrderProgressState.Complete -> closeProgressDialog()
            }
        }

    }

//    private fun showDialogReturnBalance() {
//        val dialog = SimpleResultDialogFragment.newInstance(
//            getString(R.string.dc_loading_return_dialog_title),
//            getString(R.string.dc_loading_return_dialog_description),
//            getString(R.string.dc_loading_return_dialog_positive_button),
//            getString(R.string.dc_loading_return_dialog_negative_button)
//        )
//        dialog.setTargetFragment(this, RETURN_BALANCE_REQUEST_CODE)
//        dialog.show(parentFragmentManager, RETURN_BALANCE_TAG)
//    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (resultCode == RESULT_OK && requestCode == RETURN_BALANCE_REQUEST_CODE) {
//            //viewModel.action(FlightsUIAction.RemoveBoxesClick)
//        }
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun displayItems(items: List<BaseItem>) {
        adapter.clear()
        adapter.addItems(items)
        adapter.notifyDataSetChanged()
    }

    private fun initRecyclerView() {
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
        )
        binding.recyclerView.setHasFixedSize(true)
        initSmoothScroller()
    }

    private fun initSmoothScroller() {
        smoothScroller = object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }
    }

    private fun initAdapter() {
        adapter = with(DefaultAdapterDelegate()) {
            addDelegate(CourierOrderDelegate(requireContext()))
//            addDelegate(
//                FlightsRefreshDelegate(
//                    requireContext(),
//                    object : OnFlightsUpdateCallback {
//                        override fun onUpdateRouteClick() {
//                            viewModel.action(FlightsUIAction.Refresh)
//                        }
//                    })
//            )
            addDelegate(FlightsProgressDelegate(requireContext()))
        }
        binding.recyclerView.adapter = adapter
    }

    private fun closeProgressDialog() {
        if (progressDialog.isShowing) progressDialog.dismiss()
    }

    private fun showProgressDialog() {
        progressDialog.show()
    }

//    private fun goneStartAddingBoxes() {
////        binding.scanBoxes.visibility = View.GONE
////        binding.returnGroup.visibility = View.GONE
//    }

}

@Parcelize
data class CourierOrderParameters(val currentWarehouseId: Int, val address: String) : Parcelable