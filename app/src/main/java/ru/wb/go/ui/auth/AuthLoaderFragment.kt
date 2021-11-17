package ru.wb.go.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.go.R
import ru.wb.go.databinding.AuthLoaderFragmentBinding
import ru.wb.go.ui.splash.NavToolbarListener

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
        viewModel.navigationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                AuthLoaderNavigationState.NavigateToNumberPhone -> findNavController().navigate(
                    AuthLoaderFragmentDirections.actionAuthLoaderFragmentToAuthNumberPhoneFragment()
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
