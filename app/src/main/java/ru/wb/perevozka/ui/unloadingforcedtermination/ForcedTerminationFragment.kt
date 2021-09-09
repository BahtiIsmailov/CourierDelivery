package ru.wb.perevozka.ui.unloadingforcedtermination

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.wb.perevozka.R
import ru.wb.perevozka.databinding.UnloadingForcedTerminationFragmentBinding
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.ui.dialogs.InformationDialogFragment
import ru.wb.perevozka.ui.splash.NavToolbarListener
import ru.wb.perevozka.views.ProgressImageButtonMode
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ForcedTerminationFragment : Fragment() {

    private val viewModel by viewModel<ForcedTerminationViewModel> {
        parametersOf(requireArguments().getParcelable<ForcedTerminationParameters>(
            FORCED_TERMINATION_KEY))
    }

    private var _binding: UnloadingForcedTerminationFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = UnloadingForcedTerminationFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initObserver()
        initListener()
    }

    private fun initView() {
        binding.toolbarLayout.toolbarTitle.text = getText(R.string.force_termination_label)
    }

    private fun initTitleBoxes(title: String) {
        binding.boxesCount.text = title
    }

    private fun initBoxes(routeItems: List<ForcedTerminationItem>) {
        val chartLegendAdapter =
            ForcedTerminationAdapter(
                requireContext(),
                routeItems)
        binding.boxes.adapter = chartLegendAdapter
    }

    private fun initObserver() {

        viewModel.navigateToMessage.observe(viewLifecycleOwner) {
            InformationDialogFragment.newInstance(it.title, it.message, it.button)
                .show(parentFragmentManager, "INFO_MESSAGE_TAG")
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

        viewModel.boxesState.observe(viewLifecycleOwner) {
            when (it) {
                is ForcedTerminationState.Title ->
                    (activity as NavToolbarListener).updateTitle(it.toolbarTitle)
                is ForcedTerminationState.BoxesComplete -> {
                    initTitleBoxes(it.title)
                    initBoxes(it.boxes)
                }
                ForcedTerminationState.BoxesEmpty -> TODO()
            }
        }

        viewModel.navigateToBack.observe(viewLifecycleOwner) {
            when (it) {
                ForcedTerminationNavAction.NavigateToBack -> findNavController().popBackStack()
                ForcedTerminationNavAction.NavigateToFlightDeliveries ->
                    findNavController().navigate(
                        ForcedTerminationFragmentDirections.actionForcedTerminationFragmentToFlightDeliveriesFragment())
            }
        }
    }

    private fun initListener() {
        binding.toolbarLayout.back.setOnClickListener { findNavController().popBackStack() }
        binding.toolbarLayout.noInternetImage.setOnClickListener {
            (activity as NavToolbarListener).showNetworkDialog()
        }
        binding.checkedBoxNotFound.setOnCheckedChangeListener { _, isChecked ->
            binding.complete.setState(if (isChecked) ProgressImageButtonMode.ENABLED else ProgressImageButtonMode.DISABLED)
        }
        binding.complete.setOnClickListener { viewModel.onCompleteClick() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val FORCED_TERMINATION_KEY = "forced_termination_key"
    }

}

@Parcelize
data class ForcedTerminationParameters(val currentOfficeId: Int) : Parcelable