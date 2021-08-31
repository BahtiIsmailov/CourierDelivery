package ru.wb.perevozka.ui.courierloader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.perevozka.R
import ru.wb.perevozka.databinding.CourierLoaderFragmentBinding
import ru.wb.perevozka.ui.courierdata.CourierDataParameters
import ru.wb.perevozka.ui.courierexpects.CourierExpectsParameters
import ru.wb.perevozka.ui.splash.NavToolbarListener

class CourierLoaderFragment : Fragment(R.layout.courier_loader_fragment) {

    private var _binding: CourierLoaderFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModel<CourierLoaderViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = CourierLoaderFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initObserver()
    }

    private fun initView() {
        (activity as NavToolbarListener).hideBackButton()
    }

    private fun initObserver() {
        viewModel.navigationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                CourierLoaderNavigationState.NavigateToCourierWarehouse -> findNavController().navigate(
                    CourierLoaderFragmentDirections.actionCourierLoaderFragmentToCourierWarehouseFragment()
                )
                is CourierLoaderNavigationState.NavigateToCouriersCompleteRegistration -> findNavController().navigate(
                    CourierLoaderFragmentDirections.actionCourierLoaderFragmentToCouriersCompleteRegistrationFragment(
                        CourierExpectsParameters(state.phone)
                    )
                )
                is CourierLoaderNavigationState.NavigateToCourierUserForm -> findNavController().navigate(
                    CourierLoaderFragmentDirections.actionCourierLoaderFragmentToUserFormFragment(
                        CourierDataParameters(state.phone)
                    )
                )
                CourierLoaderNavigationState.NavigateToCourierScanner -> {
                    findNavController().navigate(
                        CourierLoaderFragmentDirections.actionCourierLoaderFragmentToCourierScannerLoadingScanFragment()
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
