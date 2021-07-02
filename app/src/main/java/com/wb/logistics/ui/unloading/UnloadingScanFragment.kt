package com.wb.logistics.ui.unloading

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.wb.logistics.databinding.UnloadingScanFragmentBinding
import com.wb.logistics.ui.dialogs.InformationDialogFragment
import com.wb.logistics.ui.splash.KeyboardListener
import com.wb.logistics.ui.splash.NavToolbarListener
import com.wb.logistics.ui.unloading.UnloadingHandleFragment.Companion.HANDLE_BARCODE_COMPLETE_KEY
import com.wb.logistics.ui.unloading.UnloadingHandleFragment.Companion.UNLOADING_HANDLE_BARCODE_COMPLETE
import com.wb.logistics.ui.unloading.views.UnloadingAcceptedMode
import com.wb.logistics.ui.unloading.views.UnloadingInfoMode
import com.wb.logistics.ui.unloading.views.UnloadingReturnMode
import com.wb.logistics.ui.unloadingforcedtermination.ForcedTerminationParameters
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
        initListener()
        initObserver()
        initResultListener()
        initKeyboard()
    }

    private fun initKeyboard() {
        (activity as KeyboardListener).adjustMode()
        hideKeyBoard(requireActivity())
    }

    private fun initResultListener() {
        setFragmentResultListener(UNLOADING_HANDLE_BARCODE_COMPLETE) { _, bundle ->
            val barcode = bundle.get(HANDLE_BARCODE_COMPLETE_KEY) as String
            viewModel.onBoxHandleInput(barcode)
        }
    }

    private fun initObserver() {

        viewModel.navigateToMessageInfo.observe(viewLifecycleOwner) {
            InformationDialogFragment.newInstance(it.title, it.message, it.button)
                .show(parentFragmentManager, "INFO_MESSAGE_TAG")
        }

        viewModel.toolbarBackState.observe(viewLifecycleOwner) {
            (activity as NavToolbarListener).hideBackButton()
        }

        viewModel.toolbarLabelState.observe(viewLifecycleOwner) {
            (activity as NavToolbarListener).updateTitle(it.label)
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

//        viewModel.toastEvent.observe(viewLifecycleOwner) { state ->
//            when (state) {
////                is UnloadingScanMessageEvent.BoxDelivery -> showToastBoxDelivery(state.message)
////                is UnloadingScanMessageEvent.BoxReturned -> showToastBoxReturned(state.message)
//            }
//        }

        viewModel.soundEvent.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UnloadingScanSoundEvent.BoxAdded -> beepAdded()
                is UnloadingScanSoundEvent.BoxSkipAdded -> beepSkip()
            }
        }

        viewModel.unloadedState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UnloadingScanBoxState.UnloadedBoxesEmpty -> {
                    binding.unloadingBox.setCountBox(state.accepted, UnloadingAcceptedMode.EMPTY)
                    binding.info.setCodeBox(UnloadingInfoMode.EMPTY)
                }
                is UnloadingScanBoxState.UnloadedBoxesComplete -> {
                    binding.unloadingBox.setCountBox(state.accepted, UnloadingAcceptedMode.COMPLETE)
                    binding.info.setCodeBox(UnloadingInfoMode.EMPTY)
                }
                is UnloadingScanBoxState.UnloadedBoxesActive -> {
                    binding.unloadingBox.setCountBox(state.accepted, UnloadingAcceptedMode.ACTIVE)
                    binding.info.setCodeBox(state.barcode, UnloadingInfoMode.UNLOADING)
                }

            }
        }

        viewModel.returnState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UnloadingReturnState.ReturnBoxesActive -> {
                    binding.returnBox.setCountBox(state.accepted, UnloadingReturnMode.ACTIVE)
                    binding.info.setCodeBox(state.barcode, UnloadingInfoMode.RETURN)
                }
                is UnloadingReturnState.ReturnBoxesComplete -> {
                    binding.returnBox.setCountBox(state.accepted, UnloadingReturnMode.ACTIVE)
                    binding.info.setCodeBox(state.barcode, UnloadingInfoMode.RETURN)
                }
                is UnloadingReturnState.ReturnBoxesEmpty ->
                    binding.returnBox.setState(UnloadingReturnMode.EMPTY)
            }
        }

        viewModel.bottomProgressEvent.observe(viewLifecycleOwner) { progress ->
            binding.complete.setState(
                if (progress) ProgressImageButtonMode.PROGRESS else ProgressImageButtonMode.ENABLED)
        }

    }

    private fun initListener() {

        binding.unloadingBox.setOnClickListener {
            viewModel.onUnloadingListClicked()
        }

        binding.returnBox.setOnClickListener {
            viewModel.onReturnListClicked()
        }

        binding.manualInputButton.setOnClickListener {
            viewModel.onaHandleClicked()
        }

        binding.complete.setOnClickListener {
            viewModel.onCompleteClicked()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun beepAdded() {
        val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
    }

    private fun beepSkip() {
        val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_CALL_SIGNAL_ISDN_NORMAL, 200)
    }

    companion object {
        const val UNLOADING_SCAN_KEY = "unloading_scan_key"
    }

}

@Parcelize
data class UnloadingScanParameters(val dstOfficeId: Int) : Parcelable