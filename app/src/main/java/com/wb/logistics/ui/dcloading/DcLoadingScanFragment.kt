package com.wb.logistics.ui.dcloading

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.wb.logistics.R
import com.wb.logistics.databinding.DcLoadingScanFragmentBinding
import com.wb.logistics.network.monitor.NetworkState
import com.wb.logistics.ui.dcloading.DcLoadingHandleFragment.Companion.HANDLE_BARCODE_RESULT
import com.wb.logistics.ui.dcloading.views.ReceptionAcceptedMode
import com.wb.logistics.ui.dcloading.views.ReceptionInfoMode
import com.wb.logistics.ui.dcloading.views.ReceptionParkingMode
import com.wb.logistics.ui.dialogs.InformationDialogFragment
import com.wb.logistics.ui.splash.NavToolbarListener
import com.wb.logistics.views.ProgressImageButtonMode
import org.koin.androidx.viewmodel.ext.android.viewModel


class DcLoadingFragment : Fragment() {

    private val viewModel by viewModel<DcLoadingScanViewModel>()

    private var _binding: DcLoadingScanFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DcLoadingScanFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initListener()
        initObserver()
        initReturnResult()
    }

    private fun initReturnResult() {
        setFragmentResultListener(HANDLE_BARCODE_RESULT) { _, bundle ->
            isHandleActive = false
            viewModel.onStartScanner()
            if (bundle.containsKey(DcLoadingHandleFragment.HANDLE_BARCODE_COMPLETE_KEY)) {
                val barcode = bundle.get(DcLoadingHandleFragment.HANDLE_BARCODE_COMPLETE_KEY) as String
                viewModel.onBoxHandleInput(barcode)
            }
        }
    }

    private var isHandleActive: Boolean = false

    override fun onStart() {
        super.onStart()
        if (!isHandleActive) viewModel.onStartScanner()
    }

    override fun onStop() {
        super.onStop()
        viewModel.onStopScanner()
    }

    private fun initView() {
        (activity as NavToolbarListener).hideToolbar()
        binding.toolbarLayout.toolbarTitle.text = getText(R.string.dc_loading_label)
    }

    private fun initObserver() {
        viewModel.navigateToMessageInfo.observe(viewLifecycleOwner) {
            InformationDialogFragment.newInstance(it.title, it.message, it.button)
                .show(parentFragmentManager, "INFO_MESSAGE_TAG")
        }

        viewModel.toolbarNetworkState.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Failed ->
                    binding.toolbarLayout.noInternetImage.visibility = View.VISIBLE

                is NetworkState.Complete ->
                    binding.toolbarLayout.noInternetImage.visibility = View.INVISIBLE
            }
        }

        val navigationObserver = Observer<DcLoadingScanNavAction> { state ->
            when (state) {
                is DcLoadingScanNavAction.NavigateToReceptionBoxNotBelong -> {
                    findNavController().navigate(
                        DcLoadingFragmentDirections.actionReceptionFragmentToReceptionBoxNotBelongFragment(
                            with(state) { DcLoadingBoxNotBelongParameters(title, box, address) }
                        )
                    )
                }
                DcLoadingScanNavAction.NavigateToBoxes -> findNavController().navigate(
                    DcLoadingFragmentDirections.actionReceptionFragmentToReceptionBoxesFragment())
                DcLoadingScanNavAction.NavigateToFlightDeliveries -> findNavController().navigate(
                    DcLoadingFragmentDirections.actionReceptionFragmentToFlightPickPointFragment())
                DcLoadingScanNavAction.NavigateToBack -> findNavController().popBackStack()
                DcLoadingScanNavAction.NavigateToHandle -> {
                    isHandleActive = true
                    findNavController().navigate(
                        DcLoadingFragmentDirections.actionDcLoadingFragmentToDcLoadingHandleFragment())
                }
            }
        }

        viewModel.navigationEvent.observe(viewLifecycleOwner, navigationObserver)

        viewModel.beepEvent.observe(viewLifecycleOwner) { state ->
            when (state) {
                is DcLoadingScanBeepState.BoxAdded -> beepSuccess()
                is DcLoadingScanBeepState.BoxSkipAdded -> beepError()
            }
        }

        viewModel.progressEvent.observe(viewLifecycleOwner) { state ->
            when (state) {
                is DcLoadingScanProgress.LoaderProgress -> {
                    viewModel.onStopScanner()
                    binding.received.isEnabled = false
                    binding.manualInput.setState(ProgressImageButtonMode.DISABLED)
                    binding.complete.setState(ProgressImageButtonMode.DISABLED)
                }
                is DcLoadingScanProgress.LoaderComplete -> {
                    viewModel.onStartScanner()
                    binding.received.isEnabled = true
                    binding.manualInput.setState(ProgressImageButtonMode.ENABLED)
                    binding.complete.setState(ProgressImageButtonMode.ENABLED)
                }
            }
        }

        viewModel.bottomProgressEvent.observe(viewLifecycleOwner) { progress ->
            binding.complete.setState(
                if (progress) ProgressImageButtonMode.PROGRESS else ProgressImageButtonMode.ENABLED)
        }

        viewModel.boxStateUI.observe(viewLifecycleOwner) { state ->
            when (state) {
                is DcLoadingScanBoxState.BoxAdded -> {
                    binding.received.setCountBox(state.accepted,
                        ReceptionAcceptedMode.CONTAINS_COMPLETE)
                    binding.parking.setParkingNumber(state.gate,
                        ReceptionParkingMode.CONTAINS_COMPLETE)
                    binding.info.setCodeBox(state.barcode, ReceptionInfoMode.SUBMERGE)
                }
                is DcLoadingScanBoxState.BoxDeny -> {
                    binding.received.setCountBox(state.accepted,
                        ReceptionAcceptedMode.CONTAINS_DENY)
                    binding.parking.setParkingNumber(state.gate,
                        ReceptionParkingMode.CONTAINS_DENY)
                    binding.info.setCodeBox(state.barcode, ReceptionInfoMode.RETURN)
                }
                DcLoadingScanBoxState.Empty -> {
                    binding.received.setState(ReceptionAcceptedMode.EMPTY)
                    binding.parking.setParkingNumber(ReceptionParkingMode.EMPTY)
                    binding.info.setCodeBox(ReceptionInfoMode.EMPTY)
                }
                is DcLoadingScanBoxState.BoxHasBeenAdded -> {
                    binding.received.setCountBox(state.accepted,
                        ReceptionAcceptedMode.CONTAINS_HAS_ADDED)
                    binding.parking.setParkingNumber(state.gate,
                        ReceptionParkingMode.CONTAINS_HAS_ADDED)
                    binding.info.setCodeBox(state.barcode, ReceptionInfoMode.CONTAINS_HAS_ADDED)
                }
                is DcLoadingScanBoxState.BoxInit -> {
                    binding.received.setCountBox(state.accepted,
                        ReceptionAcceptedMode.CONTAINS_COMPLETE)
                    binding.parking.setParkingNumber(state.gate,
                        ReceptionParkingMode.CONTAINS_COMPLETE)
                    binding.info.setCodeBox(state.barcode, ReceptionInfoMode.SUBMERGE)
                }
            }
        }
    }

    private fun initListener() {

        binding.toolbarLayout.back.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.toolbarLayout.noInternetImage.setOnClickListener {
            (activity as NavToolbarListener).showNetworkDialog()
        }

        binding.manualInput.setOnClickListener {
            viewModel.onHandleClicked()
        }

        binding.complete.setOnClickListener {
            viewModel.onCompleteClicked()
        }

        binding.received.setOnClickListener {
            viewModel.onListClicked()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun beepSuccess() {
        play(R.raw.sound_scan_success)
    }

    private fun beepError() {
        play(R.raw.sound_scan_error)
    }

    private fun play(resid: Int) {
        MediaPlayer.create(context, resid).start()
    }

}