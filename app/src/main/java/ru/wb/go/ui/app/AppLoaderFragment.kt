package ru.wb.go.ui.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.go.R
import ru.wb.go.databinding.AppLoaderFragmentBinding


class AppLoaderFragment : Fragment() {

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
        (activity as NavToolbarListener).hideToolbar()
        initObserver()
        initListener()
    }

    private fun initListener() {
        with(binding){
            toRegistration.setOnClickListener {
                viewModel.toAuth()
            }
            toDemo.setOnClickListener {
                viewModel.toCourier()
            }
        }
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

        viewModel.demoState.observe(viewLifecycleOwner) {
            when (it) {
                true -> {
                    binding.toRegistration.visibility = VISIBLE
                    binding.toDemo.visibility = VISIBLE
                    binding.progress.visibility = GONE
                }
                false -> {
                    binding.toRegistration.visibility = GONE
                    binding.toDemo.visibility = GONE
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
