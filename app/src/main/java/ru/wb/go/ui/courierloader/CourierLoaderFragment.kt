package ru.wb.go.ui.courierloader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.go.R
import ru.wb.go.databinding.CourierLoaderFragmentBinding
import ru.wb.go.ui.app.NavToolbarListener
import ru.wb.go.ui.app.OnUserInfo
import ru.wb.go.ui.courierdataexpects.CourierDataExpectsParameters

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
        initListeners()
    }

    private fun initView() {
        (activity as NavToolbarListener).hideToolbar()
    }

    private fun initListeners() {
        binding.update.setOnClickListener { viewModel.initVersion() }
    }

    private fun initObserver() {

        viewModel.drawerHeader.observe(viewLifecycleOwner) {
            (activity as OnUserInfo).userInfo(it.name, it.company)
        }

        viewModel.navigationDrawerState.observe(viewLifecycleOwner) { state ->

            (activity as NavToolbarListener).showStatusBar()

            when (state) {

                is CourierLoaderNavigationState.NavigateToCouriersCompleteRegistration -> findNavController().navigate(
                    CourierLoaderFragmentDirections.actionCourierLoaderFragmentToCouriersCompleteRegistrationFragment(
                        CourierDataExpectsParameters(state.phone)
                    )
                )
                is CourierLoaderNavigationState.NavigateToCourierDataType -> findNavController().navigate(
                    CourierLoaderFragmentDirections.actionCourierLoaderFragmentToCourierDataTypeFragment(
                        state.courierDataParameters
                    )
                )
                CourierLoaderNavigationState.NavigateToCourierWarehouse -> findNavController().navigate(
                    CourierLoaderFragmentDirections.actionCourierLoaderFragmentToCourierWarehouseFragment()
                )
                CourierLoaderNavigationState.NavigateToTimer -> {
                    findNavController().navigate(
                        CourierLoaderFragmentDirections.actionCourierLoaderFragmentToCourierOrderTimerFragment()
                    )
                }
                CourierLoaderNavigationState.NavigateToScanner -> {
                    findNavController().navigate(
                        CourierLoaderFragmentDirections.actionCourierLoaderFragmentToCourierScannerLoadingScanFragment()
                    )
                }
                CourierLoaderNavigationState.NavigateToIntransit -> {
                    findNavController().navigate(
                        CourierLoaderFragmentDirections.actionCourierLoaderFragmentToCourierIntransitFragment()
                    )
                }
                CourierLoaderNavigationState.NavigateToPhone -> findNavController().navigate(
                    CourierLoaderFragmentDirections.actionCourierLoaderFragmentToAuthNavigation()
                )

                CourierLoaderNavigationState.NavigateToAppUpdate -> {
                    findNavController().navigate(
                        CourierLoaderFragmentDirections.actionCourierLoaderFragmentToCourierVersionControlFragment()
                    )
                }
            }
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CourierLoaderUIState.Error -> {
                    binding.progress.visibility = View.INVISIBLE
                    binding.errorMessage.visibility = View.VISIBLE
                    binding.errorMessage.text = state.message
                    binding.update.visibility = View.VISIBLE
                    binding.update.isEnabled = true
                }
                CourierLoaderUIState.Progress -> {
                    binding.progress.visibility = View.VISIBLE
                    binding.errorMessage.visibility = View.GONE
                    binding.update.visibility = View.GONE
                    binding.update.isEnabled = false
                }
                CourierLoaderUIState.Complete -> {
                    binding.logo.visibility = View.INVISIBLE
                    binding.progress.visibility = View.INVISIBLE
                    binding.errorMessage.visibility = View.INVISIBLE
                    binding.update.visibility = View.INVISIBLE
                    binding.update.isEnabled = true
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
