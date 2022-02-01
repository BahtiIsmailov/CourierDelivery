package ru.wb.go.ui.courierorderdetails

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.wb.go.R
import ru.wb.go.databinding.CourierOrderDetailsFragmentBinding
import ru.wb.go.db.entity.courier.CourierOrderEntity
import ru.wb.go.ui.app.NavDrawerListener
import ru.wb.go.ui.couriercarnumber.CourierCarNumberParameters
import ru.wb.go.ui.dialogs.DialogConfirmInfoFragment
import ru.wb.go.ui.dialogs.DialogInfoFragment
import ru.wb.go.views.ProgressButtonMode


class CourierOrderDetailsFragment : Fragment() {

    companion object {
        const val COURIER_ORDER_DETAILS_ID_KEY = "courier_order_details_id_key"
        const val DIALOG_TASK_NOT_EXIST_RESULT_TAG = "DIALOG_TASK_NOT_EXIST_RESULT_TAG"
        const val DIALOG_CONFIRM_SCORE_RESULT_TAG = "DIALOG_CONFIRM_SCORE_RESULT_TAG"
    }

    private val viewModel by viewModel<CourierOrderDetailsViewModel> {
        parametersOf(
            requireArguments().getParcelable<CourierOrderDetailsParameters>(
                COURIER_ORDER_DETAILS_ID_KEY
            )
        )
    }

    private var _binding: CourierOrderDetailsFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CourierOrderDetailsAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var smoothScroller: RecyclerView.SmoothScroller

    private lateinit var bottomSheetOrderDetails: BottomSheetBehavior<FrameLayout>
    private lateinit var bottomSheetOrderAddresses: BottomSheetBehavior<FrameLayout>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = CourierOrderDetailsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initRecyclerView()
        initObservable()
        initListeners()
        initReturnDialogResult()
//        initProgressDialog()
        viewModel.onUpdate()
    }

    private fun initReturnDialogResult() {

        setFragmentResultListener(DIALOG_TASK_NOT_EXIST_RESULT_TAG) { _, bundle ->
            if (bundle.containsKey(DialogInfoFragment.DIALOG_INFO_BACK_KEY)) {
                viewModel.onTaskNotExistConfirmClick()
            }
        }

        setFragmentResultListener(DIALOG_CONFIRM_SCORE_RESULT_TAG) { _, bundle ->
            if (bundle.containsKey(DialogConfirmInfoFragment.DIALOG_CONFIRM_INFO_POSITIVE_KEY)) {
                viewModel.onConfirmOrderClick()
            }
            if (bundle.containsKey(DialogConfirmInfoFragment.DIALOG_CONFIRM_INFO_NEGATIVE_KEY)) {
                viewModel.onCancelOrderClick()
            }
        }
    }

    private fun initView() {
        (activity as NavDrawerListener).lockNavDrawer()
        initBottomSheet()
        hideAllBottomSheet()
    }

    private fun initBottomSheet() {
        bottomSheetOrderDetails = BottomSheetBehavior.from(binding.orderDetails)

        bottomSheetOrderAddresses = BottomSheetBehavior.from(binding.orderAddresses)
        bottomSheetOrderAddresses.setBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // TODO: 26.01.2022 закрыть список
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    expandedDetails()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initObservable() {

//        viewModel.toolbarLabelState.observe(viewLifecycleOwner) {
//            binding.toolbarLayout.toolbarTitle.text = it.label
//        }

//        viewModel.toolbarNetworkState.observe(viewLifecycleOwner) {
//            val ic = when (it) {
//                is NetworkState.Complete -> R.drawable.ic_inet_complete
//                else -> R.drawable.ic_inet_failed
//            }
//            binding.toolbarLayout.noInternetImage.setImageDrawable(
//                ContextCompat.getDrawable(requireContext(), ic)
//            )
//        }
//
//        viewModel.versionApp.observe(viewLifecycleOwner) {
//            binding.toolbarLayout.toolbarVersion.text = it
//        }

        viewModel.orderInfo.observe(viewLifecycleOwner) {
            when (it) {
                is CourierOrderDetailsInfoUIState.InitOrderInfo -> {
                    binding.carNumber.text = it.carNumber
                    binding.orderNumber.text = it.orderNumber
                    binding.order.text = it.order
                    binding.coast.text = it.coast

                    binding.countBox.text = it.countBox
                    binding.volume.text = it.volume
                    binding.countPvz.text = it.countPvz
                    binding.arrive.text = it.arrive

                    showDetails()
                }
            }
        }

        viewModel.orderDetails.observe(viewLifecycleOwner) {
            when (it) {
                is CourierOrderDetailsUIState.InitItems -> {
                    binding.emptyList.visibility = GONE
                    binding.routes.visibility = VISIBLE
                    binding.takeOrder.setState(ProgressButtonMode.ENABLE)
                    val callback = object : CourierOrderDetailsAdapter.OnItemClickCallBack {
                        override fun onItemClick(index: Int) {
                            viewModel.onItemClick(index)
                        }
                    }
                    adapter = CourierOrderDetailsAdapter(requireContext(), it.items, callback)
                    binding.routes.adapter = adapter
                }
                is CourierOrderDetailsUIState.Empty -> {
                    binding.emptyList.visibility = VISIBLE
                    binding.routes.visibility = GONE
                    binding.takeOrder.setState(ProgressButtonMode.DISABLE)
                }
                is CourierOrderDetailsUIState.UpdateItems -> {
                    adapter.clear()
                    adapter.addItems(it.items)
                    adapter.notifyDataSetChanged()
                }
            }
        }

        viewModel.progressState.observe(viewLifecycleOwner) {
            when (it) {
                CourierOrderDetailsProgressState.Progress -> {}//showProgressDialog()
                CourierOrderDetailsProgressState.ProgressComplete -> {}//closeProgressDialog()
            }
        }

        viewModel.navigateToDialogInfo.observe(viewLifecycleOwner) {
            showDialog(it.type, it.title, it.message, it.button)
        }

        viewModel.navigateToTaskIsNotExistDialog.observe(viewLifecycleOwner) {
            showTaskNotExistDialog(it.type, it.title, it.message, it.button)
        }

        viewModel.navigateToDialogConfirmScoreInfo.observe(viewLifecycleOwner) {
            showDialogConfirmScoreInfo(it.type, it.title, it.message, it.positive, it.negative)
        }

        viewModel.navigationState.observe(viewLifecycleOwner) {
            when (it) {
                is CourierOrderDetailsNavigationState.NavigateToDialogConfirm ->
                    showConfirmDialog(it.title, it.message)
                is CourierOrderDetailsNavigationState.NavigateToCarNumber ->
                    findNavController().navigate(
                        CourierOrderDetailsFragmentDirections.actionCourierOrderDetailsFragmentToCourierCarNumberFragment(
                            CourierCarNumberParameters(
                                it.title,
                                it.orderNumber,
                                it.order,
                                it.warehouseLatitude,
                                it.warehouseLongitude
                            )
                        )
                    )
                CourierOrderDetailsNavigationState.NavigateToTimer -> {
                    findNavController().navigate(
                        CourierOrderDetailsFragmentDirections.actionCourierOrderDetailsFragmentToCourierOrderTimerFragment()
                    )
                }
                CourierOrderDetailsNavigationState.NavigateToBack -> findNavController().popBackStack()
            }
        }

        viewModel.holdState.observe(viewLifecycleOwner) {
            when (it) {
                true -> binding.holdLayout.visibility = VISIBLE
                false -> binding.holdLayout.visibility = GONE
            }
        }

    }

    private fun initListeners() {
        binding.backFull.setOnClickListener { findNavController().popBackStack() }
        binding.carChangeImage.setOnClickListener { viewModel.onChangeCarNumberClick() }
        binding.addresses.setOnClickListener { showAddresses() }
        binding.addressClose.setOnClickListener { showDetails() }
        binding.takeOrder.setOnClickListener { viewModel.confirmTakeOrderClick() }
    }

    private fun expandedDetails() {
        bottomSheetOrderDetails.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun hideAllBottomSheet() {
        bottomSheetOrderDetails.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetOrderAddresses.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun showDetails() {
        bottomSheetOrderDetails.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetOrderAddresses.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun showAddresses() {
        bottomSheetOrderDetails.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetOrderAddresses.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    // TODO: 20.08.2021 переработать
//    private fun initProgressDialog() {
//        val builder = AlertDialog.Builder(requireContext(), R.style.CustomProgressAlertDialog)
//        val viewGroup: ViewGroup = binding.routes
//        val dialogView = LayoutInflater.from(requireContext())
//            .inflate(R.layout.custom_progress_layout_dialog, viewGroup, false)
//        builder.setView(dialogView)
//        progressDialog = builder.create()
//        progressDialog.setCanceledOnTouchOutside(false)
//        progressDialog.setOnKeyListener { _, keyCode, event ->
//            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == ACTION_UP) {
//                progressDialog.dismiss()
//                viewModel.onCancelLoadClick()
//            }
//            true
//        }
//    }

//    private fun closeProgressDialog() {
//        if (progressDialog.isShowing) progressDialog.dismiss()
//    }
//
//    private fun showProgressDialog() {
//        progressDialog.show()
//    }

    private fun showEmptyOrderDialog(title: String, message: String, button: String) {
        val builder: AlertDialog.Builder =
            AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val viewGroup: ViewGroup = binding.layout
        val dialogView: View =
            LayoutInflater.from(requireContext())
                .inflate(R.layout.custom_layout_dialog_info_result, viewGroup, false)
        val titleText: TextView = dialogView.findViewById(R.id.title)
        val messageText: TextView = dialogView.findViewById(R.id.message)
        val positive: Button = dialogView.findViewById(R.id.positive)

        builder.setView(dialogView)
        val alertDialog: AlertDialog = builder.create()
        titleText.text = title
        messageText.text = message
        positive.setOnClickListener { alertDialog.dismiss() }
        positive.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
        positive.text = button
        alertDialog.show()
    }

    // TODO: 27.08.2021 переработать
    private fun showConfirmDialog(title: String, message: String) {
        val builder: AlertDialog.Builder =
            AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val viewGroup: ViewGroup = binding.layout
        val dialogView: View =
            LayoutInflater.from(requireContext())
                .inflate(R.layout.custom_layout_dialog_result, viewGroup, false)
        val titleText: TextView = dialogView.findViewById(R.id.title)
        val messageText: TextView = dialogView.findViewById(R.id.message)
        val negative: Button = dialogView.findViewById(R.id.negative)
        val positive: Button = dialogView.findViewById(R.id.positive)

        builder.setView(dialogView)
        val alertDialog: AlertDialog = builder.create()
        titleText.text = title
        messageText.text = message
        negative.setOnClickListener { alertDialog.dismiss() }
        negative.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
        negative.text = getString(R.string.courier_orders_details_dialog_negative_button)
        positive.setOnClickListener {
            alertDialog.dismiss()
            viewModel.confirmTakeOrderClick()
        }
        positive.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
        positive.text = getString(R.string.courier_orders_details_dialog_positive_button)
        alertDialog.show()
    }

    private fun initRecyclerView() {
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.routes.layoutManager = layoutManager
        binding.routes.setHasFixedSize(true)
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

    private fun showDialog(
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
        ).show(parentFragmentManager, DialogInfoFragment.DIALOG_INFO_TAG)
    }

    private fun showTaskNotExistDialog(
        type: Int,
        title: String,
        message: String,
        positiveButtonName: String
    ) {
        DialogInfoFragment.newInstance(
            DIALOG_TASK_NOT_EXIST_RESULT_TAG,
            type = type,
            title = title,
            message = message,
            positiveButtonName = positiveButtonName
        ).show(parentFragmentManager, DialogInfoFragment.DIALOG_INFO_TAG)
    }

    private fun showDialogConfirmScoreInfo(
        type: Int,
        title: String,
        message: String,
        positiveButtonName: String,
        negativeButtonName: String
    ) {
        DialogConfirmInfoFragment.newInstance(
            DIALOG_CONFIRM_SCORE_RESULT_TAG,
            type,
            title,
            message,
            positiveButtonName,
            negativeButtonName
        ).show(parentFragmentManager, DialogConfirmInfoFragment.DIALOG_CONFIRM_INFO_TAG)
    }

}

@Parcelize
data class CourierOrderDetailsParameters(
    val title: String,
    val orderNumber: String,
    val order: CourierOrderEntity,
    val warehouseLatitude: Double,
    val warehouseLongitude: Double,
) : Parcelable