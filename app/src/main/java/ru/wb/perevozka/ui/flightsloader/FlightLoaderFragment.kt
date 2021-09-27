package ru.wb.perevozka.ui.flightsloader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.wb.perevozka.R
import ru.wb.perevozka.databinding.FlightLoaderFragmentBinding
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.ui.splash.*
import ru.wb.perevozka.views.ProgressImageButtonMode
import org.koin.androidx.viewmodel.ext.android.viewModel

class FlightLoaderFragment : Fragment(R.layout.flight_loader_fragment) {

    private var _binding: FlightLoaderFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModel<FlightLoaderViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FlightLoaderFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
        initView()
        initObserver()
    }

    private fun initView() {
        (activity as NavToolbarListener).hideToolbar()
        (activity as NavDrawerListener).unlock()
        (activity as KeyboardListener).adjustMode()
        binding.toolbarLayout.toolbarTitle.text = getText(R.string.flights_label)
        binding.toolbarLayout.back.visibility = View.INVISIBLE
        viewModel.update()
    }

    private fun initObserver() {

        viewModel.navHeader.observe(viewLifecycleOwner) {
            (activity as OnUserInfo).userInfo(it.name, it.company)
        }

        viewModel.toolbarNetworkState.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Failed -> {
                    binding.toolbarLayout.noInternetImage.visibility = View.VISIBLE
                }

                is NetworkState.Complete -> {
                    binding.toolbarLayout.noInternetImage.visibility = View.INVISIBLE
                }
            }
        }

        viewModel.flightLoaderUIState.observe(viewLifecycleOwner) {
            when(it) {
                is FlightLoaderUIState.Error -> {
                    binding.progress.visibility = View.INVISIBLE
                    binding.titleProgress.visibility = View.INVISIBLE
                    binding.emptyFlightMessage.visibility = View.INVISIBLE
                    binding.errorMessage.visibility = View.VISIBLE
                    binding.errorMessage.text = it.message
                    binding.update.setState(ProgressImageButtonMode.ENABLED)
                    binding.update.visibility = View.VISIBLE
                }
                is FlightLoaderUIState.InTransit -> {
                    findNavController().navigate(it.navDirections)
                }
                FlightLoaderUIState.NotAssigned -> {

                    (activity as OnFlightsStatus).flightNotAssigned("Доставка")

                    binding.progress.visibility = View.INVISIBLE
                    binding.titleProgress.visibility = View.INVISIBLE
                    binding.emptyFlightMessage.visibility = View.VISIBLE
                    binding.errorMessage.visibility = View.INVISIBLE
                    binding.update.setState(ProgressImageButtonMode.ENABLED)
                    binding.update.visibility = View.VISIBLE
                }
                FlightLoaderUIState.InitProgress -> {
                    binding.progress.visibility = View.VISIBLE
                    binding.titleProgress.visibility = View.VISIBLE
                    binding.emptyFlightMessage.visibility = View.INVISIBLE
                    binding.errorMessage.visibility = View.INVISIBLE
                    binding.update.setState(ProgressImageButtonMode.ENABLED)
                    binding.update.visibility = View.INVISIBLE
                }
                FlightLoaderUIState.Progress -> {
                    binding.update.setState(ProgressImageButtonMode.PROGRESS)
                }
            }
        }
    }

    private fun initListener() {
        binding.toolbarLayout.noInternetImage.setOnClickListener {
            (activity as NavToolbarListener).showNetworkDialog()
        }
        binding.update.setOnClickListener { viewModel.onUpdate() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
