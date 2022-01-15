package ru.wb.go.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.go.R
import ru.wb.go.databinding.AppLoaderFragmentBinding

class AppLoaderFragment : Fragment(R.layout.app_loader_fragment) {

    private var _binding: AppLoaderFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModel<AppLoaderViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = AppLoaderFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initObserver()
    }

    private fun initView() {
        (activity as NavToolbarListener).hideToolbar()
    }

    private fun initObserver() {
        viewModel.navState.observe(viewLifecycleOwner) { state ->
            when (state) {
                AppLoaderNavigatioState.NavigateToAuth -> {
                    (activity as NavToolbarListener).showStatusBar()
                    findNavController().navigate(
                        AppLoaderFragmentDirections.actionAuthLoaderFragmentToAuthNavigation()
                    )
                }
                AppLoaderNavigatioState.NavigateToCourier ->
                    findNavController().navigate(
                        AppLoaderFragmentDirections.actionAuthLoaderFragmentToCourierNavigation()
                    )

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
