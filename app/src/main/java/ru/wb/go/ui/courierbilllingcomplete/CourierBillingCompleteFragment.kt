package ru.wb.go.ui.courierbilllingcomplete

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.wb.go.databinding.CourierBillingCompleteFragmentBinding
import ru.wb.go.ui.app.NavDrawerListener
import ru.wb.go.ui.app.NavToolbarListener

class CourierBillingCompleteFragment : Fragment() {

    companion object {
        const val COURIER_BILLING_COMPLETE_KEY = "courier_billing_complete_key"
    }

    private val viewModel by viewModel<CourierBillingCompleteViewModel> {
        parametersOf(
            requireArguments().getParcelable<CourierBillingCompleteParameters>(
                COURIER_BILLING_COMPLETE_KEY
            )
        )
    }
    private var _binding: CourierBillingCompleteFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = CourierBillingCompleteFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initObserver()
        initListener()
    }

    private fun initView() {
        (activity as NavToolbarListener).hideToolbar()
        (activity as NavDrawerListener).lockNavDrawer()
    }

    private fun initObserver() {

        viewModel.titleState.observe(viewLifecycleOwner) {
            when (it) {
                is CourierBillingCompleteState.InfoDelivery -> binding.title.text = it.title
            }
        }

        viewModel.navigateToBack.observe(viewLifecycleOwner) {
            findNavController().navigate(CourierBillingCompleteFragmentDirections.actionCourierBillingCompleteFragmentToCourierBalanceFragment())
        }

    }

    private fun initListener() {
        binding.completeDeliveryButton.setOnClickListener {
            viewModel.onCompleteClick()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

@Parcelize
data class CourierBillingCompleteParameters(val amount: Int) : Parcelable