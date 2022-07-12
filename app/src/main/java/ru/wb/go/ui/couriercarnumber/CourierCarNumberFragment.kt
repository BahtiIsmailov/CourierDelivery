package ru.wb.go.ui.couriercarnumber

import android.os.Bundle
import android.os.Parcelable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.wb.go.R
import ru.wb.go.databinding.CourierCarNumberFragmentBinding
import ru.wb.go.ui.app.NavDrawerListener
import ru.wb.go.ui.app.NavToolbarListener

class CourierCarNumberFragment : Fragment(R.layout.courier_car_number_fragment) {

    companion object {
        const val COURIER_CAR_NUMBER_ID_KEY = "courier_car_number_id_key"
        const val COURIER_CAR_NUMBER_ID_EDIT_KEY = "courier_car_number_is_edit_key"
    }

    private val viewModel by viewModel<CourierCarNumberViewModel> {
        parametersOf(
            requireArguments().getParcelable<CourierCarNumberParameters>(
                COURIER_CAR_NUMBER_ID_KEY
            )
        )
    }

    private var _binding: CourierCarNumberFragmentBinding? = null
    private val binding get() = _binding!!

    private val carTypeAdapter: CourierCarTypeAdapter
        get() = binding.types.adapter as CourierCarTypeAdapter


    private val bottomSheetCarTypes: BottomSheetBehavior<ConstraintLayout>
    get() = BottomSheetBehavior.from(binding.carTypes)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = CourierCarNumberFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
        initStateObserve()
        initBottomSheet()
        initRecyclerViewDetails()
    }

    private val bottomSheetDetailsCallback = object :
        BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                binding.carTypesLayout.visibility = INVISIBLE
                binding.confirm.visibility = VISIBLE
                binding.cancel.visibility = VISIBLE
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
    }

    private fun initBottomSheet() {
        val bottomSheetCarTypes = bottomSheetCarTypes
        bottomSheetCarTypes.skipCollapsed = true
        bottomSheetCarTypes.addBottomSheetCallback(bottomSheetDetailsCallback)
        bottomSheetCarTypes.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun initRecyclerViewDetails() {
        binding.types.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.types.setHasFixedSize(true)
        initSmoothScrollerAddress()
    }

    private fun initSmoothScrollerAddress() {
        object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference() = SNAP_TO_START
        }
    }

    private fun initViews() {
        (activity as NavToolbarListener).hideToolbar()
        (activity as NavDrawerListener).lockNavDrawer()
    }

    private fun initListeners() {
        binding.carClose.setOnClickListener { viewModel.onCancelCarNumberClick() }
        binding.confirm.setOnClickListener { viewModel.onCheckCarNumberClick() }
        binding.cancel.setOnClickListener { viewModel.onCancelCarNumberClick() }
        viewModel.onNumberObservableClicked(binding.viewKeyboard.observableListener)
        binding.iconCarTypeSelect.setOnClickListener { viewModel.onCarTypeSelectClick() }
        binding.iconCarTypeChange.setOnClickListener { viewModel.onCarTypeSelectClick() }
        binding.typesClose.setOnClickListener { viewModel.onCarTypeCloseClick() }
    }

    private val callback = object : CourierCarTypeAdapter.OnItemClickCallBack {
        override fun onItemClick(index: Int) {
            viewModel.onAddressItemClick(index)
        }
    }

    private fun initStateObserve() {
        viewModel.navigationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CourierCarNumberNavigationState.NavigateToOrderDetails -> {
                    findNavController().previousBackStackEntry?.savedStateHandle?.set(
                        COURIER_CAR_NUMBER_ID_EDIT_KEY, state.result
                    )
                    findNavController().popBackStack()
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
                    binding.confirm.isEnabled = true
                    binding.viewKeyboard.unlock()
                    binding.viewKeyboard.active()
                }
                CourierCarNumberUIState.NumberNotFilled -> {
                    binding.confirm.isEnabled = false
                }
                is CourierCarNumberUIState.NumberSpanFormat -> {
                    binding.carNumber.setText(
                        carNumberSpannable(state.numberFormat, state.numberSpanLength),
                        TextView.BufferType.SPANNABLE
                    )
                    binding.region.setText(
                        regionSpannable(state.regionFormat, state.regionSpanLength),
                        TextView.BufferType.SPANNABLE
                    )
                    binding.viewKeyboard.setKeyboardMode(state.mode)
                }
                is CourierCarNumberUIState.InitTypeItems -> {
                    binding.types.adapter = CourierCarTypeAdapter(requireContext(), state.items, callback)
                    showCarTypes()
                }
                CourierCarNumberUIState.CloseTypeItems -> closeCarTypes()
                is CourierCarNumberUIState.SelectedCarType -> {
                    closeCarTypes()

                    binding.iconCarTypeSelected.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            state.item.icon
                        )
                    )
                    binding.carTypeSelectedName.text = state.item.name
                    binding.iconCarTypeSelect.visibility = INVISIBLE
                    //выключено до реализации на сервере
//                    binding.iconCarTypeChange.visibility = VISIBLE
                }
            }

        }

        viewModel.progressState.observe(viewLifecycleOwner) {
            when (it) {
                CourierCarNumberProgressState.Progress -> {}
                CourierCarNumberProgressState.ProgressComplete -> {}
            }
        }
    }

    private fun showCarTypes() {
        binding.confirm.visibility = INVISIBLE
        binding.cancel.visibility = INVISIBLE
        binding.carTypesLayout.visibility = VISIBLE
        bottomSheetCarTypes.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun closeCarTypes() {
        bottomSheetCarTypes.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun carNumberSpannable(number: String, lenghtSpan: Int): Spannable {
        val spannable: Spannable = SpannableString(number)
        val first = 0
        val span = ForegroundColorSpan(
            ResourcesCompat.getColor(resources, R.color.primary, null)
        )
        spannable.setSpan(span, first, lenghtSpan, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(RelativeSizeSpan(0.8f), 0, 1, 0)
        spannable.setSpan(RelativeSizeSpan(0.8f), 6, 8, 0)
        return spannable
    }

    private fun regionSpannable(region: String, lenghtSpan: Int): Spannable {
        val spannable: Spannable = SpannableString(region)
        val first = 0
        val span = ForegroundColorSpan(ResourcesCompat.getColor(resources, R.color.primary, null))
        spannable.setSpan(span, first, lenghtSpan, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannable
    }

}

@Parcelize
data class CourierCarNumberParameters(val result: CourierCarNumberResult) : Parcelable