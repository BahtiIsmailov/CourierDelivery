package ru.wb.perevozka.ui.couriercompletedelivery

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
import ru.wb.perevozka.databinding.CourierCompleteDeliveryFragmentBinding
import ru.wb.perevozka.ui.splash.NavDrawerListener
import ru.wb.perevozka.ui.splash.NavToolbarListener

class CourierCompleteDeliveryFragment : Fragment() {

    companion object {
        const val COURIER_COMPLETE_DELIVERY_KEY = "courier_complete_delivery_key"
    }

    private val viewModel by viewModel<CourierCompleteDeliveryViewModel> {
        parametersOf(
            requireArguments().getParcelable<CourierCompleteDeliveryParameters>(
                COURIER_COMPLETE_DELIVERY_KEY
            )
        )
    }

    private var _binding: CourierCompleteDeliveryFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        _binding = CourierCompleteDeliveryFragmentBinding.inflate(inflater, container, false)
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
        (activity as NavDrawerListener).lock()
    }

    private fun initObserver() {

        viewModel.infoState.observe(viewLifecycleOwner) {
            when (it) {
                is CourierCompleteDeliveryState.InfoDelivery -> {
                    binding.earnedCount.text = it.amount
                    binding.deliveredCount.text = it.deliveredCount
                }
            }
        }

        viewModel.navigateToBack.observe(viewLifecycleOwner) {
            findNavController().navigate(CourierCompleteDeliveryFragmentDirections.actionCourierCompleteDeliveryFragmentToCourierLoaderFragment())
        }

    }

    private fun initListener() {
        binding.completeDelivery.setOnClickListener {
            viewModel.onCompleteDeliveryClick()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

@Parcelize
data class CourierCompleteDeliveryParameters(
    val amount: Int,
    val unloadedCount: Int,
    val fromCount: Int
) : Parcelable