package ru.wb.go.ui.dcunloading

import android.app.AlertDialog
import android.media.MediaPlayer
import android.os.Bundle
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
import ru.wb.go.R
import ru.wb.go.databinding.DcUnloadingScanFragmentBinding
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.dcunloading.DcUnloadingHandleFragment.Companion.DC_UNLOADING_HANDLE_BARCODE_COMPLETE_KEY
import ru.wb.go.ui.dcunloading.DcUnloadingHandleFragment.Companion.DC_UNLOADING_HANDLE_BARCODE_RESULT
import ru.wb.go.ui.dcunloading.views.DcUnloadingAcceptedMode
import ru.wb.go.ui.dcunloading.views.DcUnloadingInfoMode
import ru.wb.go.ui.splash.KeyboardListener
import ru.wb.go.ui.splash.NavToolbarListener
import ru.wb.go.utils.SoftKeyboard
import ru.wb.go.views.ProgressImageButtonMode
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.go.ui.dialogs.NavigateToInformation

class DcUnloadingScanFragment : Fragment() {

    private val viewModel by viewModel<DcUnloadingScanViewModel>()

    private var _binding: DcUnloadingScanFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DcUnloadingScanFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initListener()
        initObserver()
        initKeyboard()
        initReturnResult()
    }

    private var isDialogActive: Boolean = false

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
        binding.toolbarLayout.toolbarTitle.text = getText(R.string.dc_unloading_boxes_label)
        binding.toolbarLayout.back.visibility = View.INVISIBLE
    }

    private fun initReturnResult() {
        setFragmentResultListener(DC_UNLOADING_HANDLE_BARCODE_RESULT) { _, bundle ->
            val barcode = bundle.get(DC_UNLOADING_HANDLE_BARCODE_COMPLETE_KEY) as String
            viewModel.onBoxHandleInput(barcode)
            viewModel.onStartScanner()
        }
    }

    private fun initKeyboard() {
        (activity as KeyboardListener).adjustMode()
        SoftKeyboard.hideKeyBoard(requireActivity())
    }

    private fun initObserver() {

        viewModel.navigateToInformation.observe(viewLifecycleOwner) {
            isDialogActive = true
            showSimpleDialog(it)
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

        val eventObserver = Observer<DcUnloadingScanNavAction> { state ->
            when (state) {
                is DcUnloadingScanNavAction.NavigateToDcHandleInput -> {
                    viewModel.onStopScanner()
                    findNavController().navigate(
                        DcUnloadingScanFragmentDirections.actionDcUnloadingScanFragmentToDcUnloadingHandleFragment())
                }

                is DcUnloadingScanNavAction.NavigateToUnloadingBoxNotBelongDc -> {
                    findNavController().navigate(
                        DcUnloadingScanFragmentDirections.actionDcUnloadingScanFragmentToDcUnloadingBoxNotBelongFragment(
                            with(state) { DcUnloadingBoxNotBelongParameters(toolbarTitle) }
                        )
                    )
                }

                DcUnloadingScanNavAction.NavigateToBack -> findNavController().popBackStack()

                DcUnloadingScanNavAction.NavigateToDcUploadedBoxes -> findNavController().navigate(
                    DcUnloadingScanFragmentDirections.actionDcUnloadingScanFragmentToDcUnloadingBoxesFragment())

                DcUnloadingScanNavAction.NavigateToDcCongratulation -> findNavController().navigate(
                    DcUnloadingScanFragmentDirections.actionDcUnloadingScanFragmentToDcUnloadingCongratulationFragment())
                DcUnloadingScanNavAction.NavigateToDcForcedTermination -> findNavController().navigate(
                    DcUnloadingScanFragmentDirections.actionDcUnloadingScanFragmentToDcForcedTerminationFragment())
            }
        }
        viewModel.navigationEvent.observe(viewLifecycleOwner, eventObserver)

        viewModel.toastEvent.observe(viewLifecycleOwner) { state ->
            when (state) {
//                is DcUnloadingScanMessageEvent.BoxAdded -> showToastBoxAdded(state.message)
//                is DcUnloadingScanMessageEvent.BoxAlreadyUnloaded -> showToastBoxHasBeenAdded(state.message)
            }
        }

        viewModel.soundEvent.observe(viewLifecycleOwner) { state ->
            when (state) {
                is DcUnloadingScanSoundEvent.BoxAdded -> beepSuccess()
                is DcUnloadingScanSoundEvent.BoxSkipAdded -> beepError()
            }
        }

        viewModel.progressEvent.observe(viewLifecycleOwner) { state ->
            when (state) {
                is DcUnloadingScanProgress.LoaderProgress -> {
                    binding.unloadingBox.isEnabled = false
                    binding.manualInput.setState(ProgressImageButtonMode.DISABLED)
                    binding.complete.setState(ProgressImageButtonMode.DISABLED)
                }
                is DcUnloadingScanProgress.LoaderComplete -> {
                    binding.unloadingBox.isEnabled = true
                    binding.manualInput.setState(ProgressImageButtonMode.ENABLED)
                    binding.complete.setState(ProgressImageButtonMode.ENABLED)
                }
            }
        }

        viewModel.unloadedState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is DcUnloadedState.Empty -> binding.unloadingBox.setCountBox(state.accepted,
                    DcUnloadingAcceptedMode.EMPTY)
                is DcUnloadedState.Complete -> binding.unloadingBox.setCountBox(state.accepted,
                    DcUnloadingAcceptedMode.COMPLETE)
                is DcUnloadedState.Active -> binding.unloadingBox.setCountBox(state.accepted,
                    DcUnloadingAcceptedMode.ACTIVE)
                is DcUnloadedState.Error -> binding.unloadingBox.setCountBox(state.accepted,
                    DcUnloadingAcceptedMode.DENY)
            }
        }

        viewModel.infoState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is DcUnloadingInfoState.Empty -> binding.info.setCodeBox(
                    DcUnloadingInfoMode.EMPTY)
                is DcUnloadingInfoState.Complete -> binding.info.setCodeBox(state.barcode,
                    DcUnloadingInfoMode.COMPLETE)
                is DcUnloadingInfoState.Error -> binding.info.setCodeBox(state.barcode,
                    DcUnloadingInfoMode.NOT_INFO_DENY)
            }
        }

        viewModel.bottomProgressEvent.observe(viewLifecycleOwner) { progress ->
            binding.complete.setState(
                if (progress) ProgressImageButtonMode.PROGRESS else ProgressImageButtonMode.ENABLED)
        }

    }

    private fun showSimpleDialog(it: NavigateToInformation) {
        val builder: AlertDialog.Builder =
            AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val viewGroup: ViewGroup = binding.main
        val dialogView: View =
            LayoutInflater.from(requireContext())
                .inflate(R.layout.custom_layout_dialog_, viewGroup, false)
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

//    private fun showToastBoxAdded(message: String) {
//        val container: ViewGroup? = activity?.findViewById(R.id.custom_toast_container)
//        val layout: ViewGroup =
//            layoutInflater.inflate(R.layout.dc_loading_added_box_toast, container) as ViewGroup
//        val text: TextView = layout.findViewById(R.id.text)
//        text.text = message
//        with(Toast(context)) {
//            setGravity(Gravity.TOP or Gravity.CENTER, 0, 200)
//            duration = Toast.LENGTH_LONG
//            view = layout
//            show()
//        }
//    }
//
//    private fun showToastBoxHasBeenAdded(message: String) {
//        val container: ViewGroup? = activity?.findViewById(R.id.custom_toast_container)
//        val layout: ViewGroup =
//            layoutInflater.inflate(R.layout.dc_loading_has_been_added_box_toast,
//                container) as ViewGroup
//        val text: TextView = layout.findViewById(R.id.text)
//        text.text = message
//        with(Toast(context)) {
//            setGravity(Gravity.TOP or Gravity.CENTER, 0, 200)
//            duration = Toast.LENGTH_LONG
//            view = layout
//            show()
//        }
//    }

    private fun initListener() {

        binding.toolbarLayout.back.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.toolbarLayout.noInternetImage.setOnClickListener {
            (activity as NavToolbarListener).showNetworkDialog()
        }

        binding.unloadingBox.setOnClickListener {
            viewModel.onUnloadingListClicked()
        }

        binding.manualInput.setOnClickListener {
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