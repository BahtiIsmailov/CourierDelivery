package com.wb.logistics.ui.unloadingscan

import android.app.AlertDialog
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.wb.logistics.R
import com.wb.logistics.databinding.UnloadingScanFragmentBinding
import com.wb.logistics.network.monitor.NetworkState
import com.wb.logistics.ui.splash.KeyboardListener
import com.wb.logistics.ui.splash.NavToolbarListener
import com.wb.logistics.ui.unloadingboxes.UnloadingBoxesParameters
import com.wb.logistics.ui.unloadingforcedtermination.ForcedTerminationParameters
import com.wb.logistics.ui.unloadinghandle.UnloadingHandleFragment.Companion.HANDLE_BARCODE_COMPLETE_KEY
import com.wb.logistics.ui.unloadinghandle.UnloadingHandleFragment.Companion.UNLOADING_HANDLE_BARCODE_RESULT
import com.wb.logistics.ui.unloadinghandle.UnloadingHandleParameters
import com.wb.logistics.ui.unloadingreturnboxes.UnloadingReturnParameters
import com.wb.logistics.ui.unloadingscan.views.UnloadingAcceptedMode
import com.wb.logistics.ui.unloadingscan.views.UnloadingInfoMode
import com.wb.logistics.ui.unloadingscan.views.UnloadingReturnMode
import com.wb.logistics.utils.LogUtils
import com.wb.logistics.utils.SoftKeyboard.hideKeyBoard
import com.wb.logistics.views.ProgressImageButtonMode
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class UnloadingScanFragment : Fragment() {

    private val viewModel by viewModel<UnloadingScanViewModel> {
        parametersOf(requireArguments().getParcelable<UnloadingScanParameters>(UNLOADING_SCAN_KEY))
    }

    private var _binding: UnloadingScanFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = UnloadingScanFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initListener()
        initObserver()
        initResultListener()
        initKeyboard()
    }

    override fun onStart() {
        super.onStart()
        if (!isDialogActive) viewModel.onStartScanner()
    }

    override fun onStop() {
        super.onStop()
        viewModel.onStopScanner()
    }

    private fun initView() {
        (activity as NavToolbarListener).hideToolbar()
        binding.toolbarLayout.toolbarTitle.text = getText(R.string.unloading_boxes_label)
    }

    private fun initKeyboard() {
        (activity as KeyboardListener).adjustMode()
        hideKeyBoard(requireActivity())
    }

    private fun initResultListener() {
        setFragmentResultListener(UNLOADING_HANDLE_BARCODE_RESULT) { _, bundle ->
            val barcode = bundle.get(HANDLE_BARCODE_COMPLETE_KEY) as String
            viewModel.onBoxHandleInput(barcode)
        }
    }

    private var isDialogActive: Boolean = false

    private fun initObserver() {

        viewModel.navigateToMessageInfo.observe(viewLifecycleOwner) {
            isDialogActive = true
//            InformationDialogFragment.newInstance(it.title, it.message, it.button)
//                .show(parentFragmentManager, "INFO_MESSAGE_TAG")
            showSimpleDialog(it)
        }

        viewModel.toolbarBackState.observe(viewLifecycleOwner) {
            binding.toolbarLayout.back.visibility = View.INVISIBLE
        }

        viewModel.toolbarLabelState.observe(viewLifecycleOwner) {
            binding.toolbarLayout.toolbarTitle.text = it.label
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

        val eventObserver = Observer<UnloadingScanNavAction> { state ->
            when (state) {
                is UnloadingScanNavAction.NavigateToUnloadingBoxNotBelongPvz -> {
                    findNavController().navigate(
                        UnloadingScanFragmentDirections.actionUnloadingScanFragmentToUnloading(
                            with(state) {
                                UnloadingBoxNotBelongParameters(title,
                                    description,
                                    box,
                                    address)
                            }
                        )
                    )
                }

                is UnloadingScanNavAction.NavigateToDelivery -> findNavController().navigate(
                    UnloadingScanFragmentDirections.actionUnloadingScanFragmentToFlightDeliveriesFragment())

                is UnloadingScanNavAction.NavigateToUploadedBoxes -> findNavController().navigate(
                    UnloadingScanFragmentDirections.actionUnloadingScanFragmentToUnloadingBoxesFragment(
                        UnloadingBoxesParameters(state.dstOfficeId)))

                UnloadingScanNavAction.NavigateToBack -> findNavController().popBackStack()

                is UnloadingScanNavAction.NavigateToReturnBoxes -> findNavController().navigate(
                    UnloadingScanFragmentDirections.actionUnloadingScanFragmentToUnloadingReturnBoxesFragment(
                        UnloadingReturnParameters(state.dstOfficeId)))

                is UnloadingScanNavAction.NavigateToHandleInput -> {
                    viewModel.onStopScanner()
                    findNavController().navigate(
                        UnloadingScanFragmentDirections.actionUnloadingScanFragmentToUnloadingHandleFragment(
                            UnloadingHandleParameters(state.dstOfficeId)))
                }
                is UnloadingScanNavAction.NavigateToForcedTermination ->
                    findNavController().navigate(
                        UnloadingScanFragmentDirections.actionUnloadingScanFragmentToForcedTerminationFragment(
                            ForcedTerminationParameters(state.dstOfficeId)))
            }
        }

        viewModel.navigationEvent.observe(viewLifecycleOwner, eventObserver)

        viewModel.soundEvent.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UnloadingScanSoundEvent.BoxAdded -> beepSuccess()
                is UnloadingScanSoundEvent.BoxSkipAdded -> beepError()
            }
        }

        viewModel.progressEvent.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UnloadingScanProgress.LoaderProgress -> {
                    viewModel.onStopScanner()
                    binding.unloadingBox.isEnabled = false
                    binding.returnBox.isEnabled = false
                    binding.manualInput.setState(ProgressImageButtonMode.DISABLED)
                    binding.complete.setState(ProgressImageButtonMode.DISABLED)
                }
                is UnloadingScanProgress.LoaderComplete -> {
                    viewModel.onStartScanner()
                    binding.unloadingBox.isEnabled = true
                    binding.returnBox.isEnabled = true
                    binding.manualInput.setState(ProgressImageButtonMode.ENABLED)
                    binding.complete.setState(ProgressImageButtonMode.ENABLED)
                }
            }
        }

        viewModel.unloadedState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UnloadingScanBoxState.Empty ->
                    binding.unloadingBox.setCountBox(state.accepted, UnloadingAcceptedMode.EMPTY)
                is UnloadingScanBoxState.Complete ->
                    binding.unloadingBox.setCountBox(state.accepted, UnloadingAcceptedMode.COMPLETE)
                is UnloadingScanBoxState.Active ->
                    binding.unloadingBox.setCountBox(state.accepted, UnloadingAcceptedMode.ACTIVE)
                is UnloadingScanBoxState.Error ->
                    binding.unloadingBox.setCountBox(state.accepted, UnloadingReturnMode.DENY)
            }
        }

        viewModel.returnState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UnloadingScanReturnState.Empty ->
                    binding.returnBox.setState(UnloadingReturnMode.EMPTY)
                is UnloadingScanReturnState.Complete ->
                    binding.returnBox.setCountBox(state.accepted, UnloadingReturnMode.COMPLETE)
                is UnloadingScanReturnState.Active ->
                    binding.returnBox.setCountBox(state.accepted, UnloadingReturnMode.ACTIVE)
                is UnloadingScanReturnState.Error ->
                    binding.returnBox.setCountBox(state.accepted, UnloadingReturnMode.DENY)
            }
        }

        viewModel.infoState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UnloadingScanInfoState.Empty ->
                    binding.info.setState(UnloadingInfoMode.EMPTY)
                is UnloadingScanInfoState.Unloading ->
                    binding.info.setState(state.barcode, UnloadingInfoMode.UNLOADING)
                is UnloadingScanInfoState.Return ->
                    binding.info.setState(state.barcode, UnloadingInfoMode.RETURN)
                is UnloadingScanInfoState.UnloadDeny ->
                    binding.info.setState(state.barcode, UnloadingInfoMode.UNLOAD_DENY)
                is UnloadingScanInfoState.NotInfoDeny ->
                    binding.info.setState(state.barcode, UnloadingInfoMode.NOT_INFO_DENY)
            }
        }

        viewModel.bottomProgressEvent.observe(viewLifecycleOwner) { progress ->
            binding.complete.setState(
                if (progress) ProgressImageButtonMode.PROGRESS else ProgressImageButtonMode.ENABLED)
        }

    }

    private fun showSimpleDialog(it: UnloadingScanViewModel.NavigateToMessageInfo) {
        val builder: AlertDialog.Builder =
            AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val viewGroup: ViewGroup = binding.main
        val dialogView: View =
            LayoutInflater.from(requireContext())
                .inflate(R.layout.simple_layout_dialog, viewGroup, false)
        val title: TextView = dialogView.findViewById(R.id.title)
        val message: TextView = dialogView.findViewById(R.id.message)
        val negative: Button = dialogView.findViewById(R.id.negative)
        builder.setView(dialogView)

        val alertDialog: AlertDialog = builder.create()

        title.text = it.title
        message.text = it.message
        negative.setOnClickListener {
            isDialogActive = false
            alertDialog.dismiss()
        }
        negative.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
        negative.text = it.button
        alertDialog.setOnDismissListener {
            isDialogActive = false
            viewModel.onStartScanner()
        }
        alertDialog.show()
    }

    private fun initListener() {

        binding.toolbarLayout.back.setOnClickListener { findNavController().popBackStack() }
        binding.toolbarLayout.noInternetImage.setOnClickListener {
            (activity as NavToolbarListener).showNetworkDialog()
        }
        binding.unloadingBox.setOnClickListener { viewModel.onUnloadingListClicked() }
        binding.returnBox.setOnClickListener { viewModel.onReturnListClicked() }
        binding.manualInput.setOnClickListener { viewModel.onaHandleClicked() }
        binding.complete.setOnClickListener { viewModel.onCompleteClicked() }

    }

    override fun onDestroyView() {
        LogUtils { logDebugApp("onDestroyView " + this@UnloadingScanFragment.toString()) }
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

    companion object {
        const val UNLOADING_SCAN_KEY = "unloading_scan_key"
    }

}

@Parcelize
data class UnloadingScanParameters(val currentOfficeId: Int) : Parcelable