package ru.wb.go.ui.couriercarnumber

import android.os.Bundle
import android.os.Parcelable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.wb.go.R
import ru.wb.go.databinding.CourierCarNumberFragmentBinding
import ru.wb.go.db.entity.courier.CourierOrderEntity
import ru.wb.go.ui.app.NavDrawerListener
import ru.wb.go.ui.app.NavToolbarListener
import ru.wb.go.ui.courierorderdetails.CourierOrderDetailsParameters
import ru.wb.go.views.ProgressButtonMode

class CourierCarNumberFragment : Fragment(R.layout.courier_car_number_fragment) {

    companion object {
        const val COURIER_CAR_NUMBER_ID_KEY = "courier_car_number_id_key"
    }

    private val viewModel by viewModel<CourierCarNumberViewModel> {
        parametersOf(
            requireArguments().getParcelable<CourierOrderDetailsParameters>(
                COURIER_CAR_NUMBER_ID_KEY
            )
        )
    }

    private lateinit var _binding: CourierCarNumberFragmentBinding
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = CourierCarNumberFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
        initStateObserve()
    }

    private fun initViews() {
        (activity as NavToolbarListener).hideToolbar()
        (activity as NavDrawerListener).lockNavDrawer()
    }

    private fun initListeners() {
        binding.confirm.setOnClickListener { viewModel.onCheckCarNumberClick() }
        binding.cancel.setOnClickListener { findNavController().popBackStack() }
        viewModel.onNumberObservableClicked(binding.viewKeyboard.observableListener)
    }

    private fun initStateObserve() {
        viewModel.navigationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CourierCarNumberNavigationState.NavigateToOrderDetails -> {
                    findNavController().navigate(
                        CourierCarNumberFragmentDirections.actionCourierCarNumberFragmentToCourierOrderDetailsFragment(
                            CourierOrderDetailsParameters(
                                state.title,
                                state.orderNumber,
                                state.order,
                                state.warehouseLatitude,
                                state.warehouseLongitude
                            )
                        )
                    )
                }
            }
        }

        viewModel.stateBackspaceUI.observe(viewLifecycleOwner) {
            when (it) {
                CourierCarNumberBackspaceUIState.Active -> binding.viewKeyboard.active()
                CourierCarNumberBackspaceUIState.Inactive -> binding.viewKeyboard.inactive()
            }
        }

        viewModel.stateUI.observe(viewLifecycleOwner) { state ->
            when (state) {
                CourierCarNumberUIState.NumberFormatComplete -> {
                    binding.confirm.setState(ProgressButtonMode.ENABLE)
                    binding.viewKeyboard.unlock()
                    binding.viewKeyboard.active()
                }
                CourierCarNumberUIState.NumberNotFilled -> {
                    binding.confirm.setState(ProgressButtonMode.DISABLE)
                    binding.numberNotFound.visibility = INVISIBLE
                }
                is CourierCarNumberUIState.NumberSpanFormat -> {
                    binding.carNumber.setText(
                        phoneSpannable(state),
                        TextView.BufferType.SPANNABLE
                    )
                    binding.viewKeyboard.setKeyboardMode(state.mode)
                }
            }

        }

        viewModel.progressState.observe(viewLifecycleOwner) {
            when (it) {
                CourierCarNumberProgressState.Progress -> {} //showProgressDialog()
                CourierCarNumberProgressState.ProgressComplete -> {}// closeProgressDialog()
            }
        }
    }

    private fun phoneSpannable(state: CourierCarNumberUIState.NumberSpanFormat): Spannable {
        val phone = state.numberFormat
        val spannable: Spannable = SpannableString(phone)
        val first = 0
        val last = state.count
        val span = ForegroundColorSpan(
            ResourcesCompat.getColor(resources, R.color.text_spannable_phone, null)
        )
        spannable.setSpan(span, first, last, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannable
    }
}

@Parcelize
data class CourierCarNumberParameters(
    val title: String,
    val orderNumber: String,
    val order: CourierOrderEntity,
    val warehouseLatitude: Double,
    val warehouseLongitude: Double,
) : Parcelable