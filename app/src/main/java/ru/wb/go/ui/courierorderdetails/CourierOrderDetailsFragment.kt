package ru.wb.go.ui.courierorderdetails

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.ViewTreeObserver
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
import ru.wb.go.ui.dialogs.DialogInfoFragment.Companion.DIALOG_INFO_TAG
import ru.wb.go.ui.dialogs.DialogInfoStyle
import ru.wb.go.utils.WaitLoader
import ru.wb.go.utils.managers.ErrorDialogData


class CourierOrderDetailsFragment : Fragment() {

    companion object {
        const val COURIER_ORDER_DETAILS_ID_KEY = "courier_order_details_id_key"
        const val DIALOG_TASK_NOT_EXIST_RESULT_TAG = "DIALOG_TASK_NOT_EXIST_RESULT_TAG"
        const val DIALOG_CONFIRM_SCORE_RESULT_TAG = "DIALOG_CONFIRM_SCORE_RESULT_TAG"
        const val DIALOG_REGISTRATION_RESULT_TAG = "DIALOG_REGISTRATION_RESULT_TAG"
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

        }

        setFragmentResultListener(DIALOG_INFO_TAG) { _, bundle ->
            if (bundle.containsKey(DialogInfoFragment.DIALOG_INFO_BACK_KEY)) {
                viewModel.goBack()
            }
        }

        setFragmentResultListener(DIALOG_REGISTRATION_RESULT_TAG) { _, bundle ->
            if (bundle.containsKey(DialogConfirmInfoFragment.DIALOG_CONFIRM_INFO_POSITIVE_KEY)) {
                viewModel.onRegistrationConfirmClick()
            }
            if (bundle.containsKey(DialogConfirmInfoFragment.DIALOG_CONFIRM_INFO_NEGATIVE_KEY)) {
                viewModel.onRegistrationCancelClick()
            }
        }
    }

    private fun initView() {
        (activity as NavDrawerListener).lockNavDrawer()
        binding.selectedOrder.selectedBackground.visibility = INVISIBLE
        initBottomSheet()
        hideAllBottomSheet()
    }

    private fun initBottomSheet() {
        binding.orderAddresses.visibility = VISIBLE

        bottomSheetOrderDetails = BottomSheetBehavior.from(binding.orderDetails)

        bottomSheetOrderAddresses = BottomSheetBehavior.from(binding.orderAddresses)
        bottomSheetOrderAddresses.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) expandedDetails()
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initObservable() {

        viewModel.orderInfo.observe(viewLifecycleOwner) {
            when (it) {
                is CourierOrderDetailsInfoUIState.InitOrderInfo -> {
                    with(binding.selectedOrder) {
                        linerNumber.text = it.lineNumber
                        orderId.text = it.orderId
                        cost.text = it.cost
                        cargo.text = it.cargo
                        countOffice.text = it.countPvz
                        reserve.text = it.reserve
                    }


                    showDetails()
                    updateHeightInfoPixels()
                }
                is CourierOrderDetailsInfoUIState.NumberSpanFormat -> {
                    binding.carNumber.text = it.numberFormat
                }
            }
        }

        viewModel.orderDetails.observe(viewLifecycleOwner) {
            when (it) {
                is CourierOrderDetailsUIState.InitItems -> {
                    binding.emptyList.visibility = GONE
                    binding.routes.visibility = VISIBLE
                    binding.takeOrder.isEnabled = true
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
                    binding.takeOrder.isEnabled = false
                }
                is CourierOrderDetailsUIState.UpdateItems -> {
                    adapter.clear()
                    adapter.addItems(it.items)
                    adapter.notifyDataSetChanged()
                }
            }
        }

        viewModel.navigateToDialogInfo.observe(viewLifecycleOwner) {
            showDialogInfo(it)
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
                CourierOrderDetailsNavigationState.NavigateToRegistrationDialog -> {
                    showRegistrationDialogConfirmInfo(
                        DialogInfoStyle.INFO.ordinal,
                        getString(R.string.demo_registration_title_dialog),
                        getString(R.string.demo_registration_message_dialog),
                        getString(R.string.demo_registration_positive_dialog),
                        getString(R.string.demo_registration_negative_dialog)
                    )
                }
                CourierOrderDetailsNavigationState.NavigateToRegistration -> {
                    findNavController().navigate(
                        CourierOrderDetailsFragmentDirections.actionCourierOrderDetailsFragmentToAuthNavigation()
                    )
                }
            }
        }

        viewModel.waitLoader.observe(viewLifecycleOwner) { state ->
            when (state) {
                WaitLoader.Wait -> {
                    binding.holdLayout.visibility = VISIBLE
                    binding.progress.visibility = GONE

                }
                WaitLoader.Complete -> {
                    binding.holdLayout.visibility = GONE
                    binding.progress.visibility = GONE
                }
            }
        }

        viewModel.demoState.observe(viewLifecycleOwner) {
            when (it) {
                true -> {
                    binding.toRegistration.visibility = VISIBLE
                    carNumberTextColor(R.color.lvl_2)
                    binding.carChangeImage.visibility = GONE
                }
                false -> {
                    binding.toRegistration.visibility = GONE
                    carNumberTextColor(R.color.primary)
                    binding.carChangeImage.visibility = VISIBLE
                }
            }
        }

    }

    private fun carNumberTextColor(color: Int) {
        binding.carNumber.setTextColor(ContextCompat.getColor(requireContext(), color))
    }

    private fun showRegistrationDialogConfirmInfo(
        style: Int,
        title: String,
        message: String,
        positiveButtonName: String,
        negativeButtonName: String
    ) {
        DialogConfirmInfoFragment.newInstance(
            resultTag = DIALOG_REGISTRATION_RESULT_TAG,
            type = style,
            title = title,
            message = message,
            positiveButtonName = positiveButtonName,
            negativeButtonName = negativeButtonName
        ).show(parentFragmentManager, DialogConfirmInfoFragment.DIALOG_CONFIRM_INFO_TAG)
    }

    private fun updateHeightInfoPixels() {
        binding.orderDetails.viewTreeObserver.addOnGlobalLayoutListener(
            object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    binding.orderDetails.viewTreeObserver.removeOnGlobalLayoutListener(
                        this
                    )
                    viewModel.onHeightInfoBottom(binding.orderDetails.height)
                }
            })
    }

    private fun initListeners() {
        binding.backFull.setOnClickListener { findNavController().popBackStack() }
        binding.carChangeImage.setOnClickListener { viewModel.onChangeCarNumberClick() }
        binding.addresses.setOnClickListener { showAddresses() }
        binding.addressClose.setOnClickListener { showDetails() }
        binding.toRegistration.setOnClickListener { viewModel.toRegistrationClick() }
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
        negative.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        negative.text = getString(R.string.courier_orders_details_dialog_negative_button)
        positive.setOnClickListener {
            alertDialog.dismiss()
            viewModel.confirmTakeOrderClick()
        }
        positive.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
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

    private fun showDialogInfo(
        errorDialogData: ErrorDialogData
    ) {
        DialogInfoFragment.newInstance(
            resultTag = errorDialogData.dlgTag,
            type = errorDialogData.type,
            title = errorDialogData.title,
            message = errorDialogData.message,
            positiveButtonName = requireContext().getString(R.string.ok_button_title)
        ).show(parentFragmentManager, DIALOG_INFO_TAG)
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