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
import ru.wb.go.ui.courierdata.CourierDataParameters
import ru.wb.go.ui.courierexpects.CourierExpectsParameters
import ru.wb.go.ui.splash.NavToolbarListener
import ru.wb.go.ui.splash.OnUserInfo
import ru.wb.go.views.ProgressImageButtonMode

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
        //(activity as NavToolbarListener).hideBackButton()
    }

    private fun initListeners() {
        binding.update.setOnClickListener { viewModel.update() }
    }

    private fun initObserver() {

        viewModel.drawerHeader.observe(viewLifecycleOwner) {
            (activity as OnUserInfo).userInfo(it.name, it.company)
        }

        viewModel.navigationDrawerState.observe(viewLifecycleOwner) { state ->

            when (state) {

                is CourierLoaderNavigationState.NavigateToCouriersCompleteRegistration -> findNavController().navigate(
                    CourierLoaderFragmentDirections.actionCourierLoaderFragmentToCouriersCompleteRegistrationFragment(
                        CourierExpectsParameters(state.phone)
                    )
                )
                is CourierLoaderNavigationState.NavigateToCourierUserForm -> findNavController().navigate(
                    CourierLoaderFragmentDirections.actionCourierLoaderFragmentToUserFormFragment(
                        CourierDataParameters(state.phone, state.docs)
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
                CourierLoaderNavigationState.NavigateToAgreement -> {
                    findNavController().navigate(
                        CourierLoaderFragmentDirections.actionCourierLoaderFragmentToCourierAgreementFragment()
                    )
                }
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
                    binding.update.setState(ProgressImageButtonMode.ENABLED)
                }
                CourierLoaderUIState.Progress -> {
                    binding.progress.visibility = View.VISIBLE
                    binding.errorMessage.visibility = View.INVISIBLE
                    binding.update.visibility = View.VISIBLE
                    binding.update.setState(ProgressImageButtonMode.DISABLED)
                }
                CourierLoaderUIState.Complete -> {
                    binding.logo.visibility = View.INVISIBLE
                    binding.progress.visibility = View.INVISIBLE
                    binding.errorMessage.visibility = View.INVISIBLE
                    binding.update.visibility = View.INVISIBLE
                    binding.update.setState(ProgressImageButtonMode.ENABLED)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
