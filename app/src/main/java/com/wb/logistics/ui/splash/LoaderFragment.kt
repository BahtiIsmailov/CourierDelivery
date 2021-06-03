package com.wb.logistics.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.wb.logistics.R
import com.wb.logistics.databinding.AuthLoaderFragmentBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoaderFragment : Fragment(R.layout.auth_loader_fragment) {

    private var _binding: AuthLoaderFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModel<LoaderViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = AuthLoaderFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()

        (activity as NavToolbarTitleListener).hideBackButton()
    }

    private fun initObserver() {
        viewModel.navState.observe(viewLifecycleOwner) { state ->
            when (state) {
                LoaderUINavState.NavigateToApp -> findNavController().navigate(
                    LoaderFragmentDirections.actionAuthLoaderFragmentToAppNavigation())
                LoaderUINavState.NavigateToNumberPhone -> findNavController().navigate(
                    LoaderFragmentDirections.actionAuthLoaderFragmentToAuthNavigation()
                )
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}