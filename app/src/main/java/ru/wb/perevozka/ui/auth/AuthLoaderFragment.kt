package ru.wb.perevozka.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.perevozka.R
import ru.wb.perevozka.databinding.AuthLoaderFragmentBinding
import ru.wb.perevozka.ui.splash.NavToolbarListener
import ru.wb.perevozka.ui.auth.courierexpects.CourierExpectsParameters
import ru.wb.perevozka.ui.auth.courierdata.CourierDataParameters

class AuthLoaderFragment : Fragment(R.layout.auth_loader_fragment) {

    private var _binding: AuthLoaderFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModel<AuthLoaderViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = AuthLoaderFragmentBinding.inflate(inflater, container, false)
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
        viewModel.navState.observe(viewLifecycleOwner) { state ->
            when (state) {
                AuthLoaderUINavState.NavigateToNumberPhone -> findNavController().navigate(
                    AuthLoaderFragmentDirections.actionAuthLoaderFragmentToAuthNumberPhoneFragment()
                )
                is AuthLoaderUINavState.NavigateToCouriersCompleteRegistration -> findNavController().navigate(
                    AuthLoaderFragmentDirections.actionAuthLoaderFragmentToCouriersCompleteRegistrationFragment(
                        CourierExpectsParameters(state.phone)
                    )
                )
                is AuthLoaderUINavState.NavigateToUserForm -> findNavController().navigate(
                    AuthLoaderFragmentDirections.actionAuthLoaderFragmentToUserFormFragment(
                        CourierDataParameters(state.phone)
                    )
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
