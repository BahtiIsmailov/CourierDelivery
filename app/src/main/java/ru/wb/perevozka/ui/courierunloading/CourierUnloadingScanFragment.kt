package ru.wb.perevozka.ui.courierunloading

import android.app.AlertDialog
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Parcelable
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.wb.perevozka.R
import ru.wb.perevozka.databinding.CourierUnloadingFragmentBinding
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.ui.dialogs.DialogInfoFragment
import ru.wb.perevozka.ui.dialogs.DialogInfoFragment.Companion.DIALOG_INFO_TAG
import ru.wb.perevozka.ui.dialogs.ProgressDialogFragment
import ru.wb.perevozka.ui.splash.NavDrawerListener
import ru.wb.perevozka.ui.splash.NavToolbarListener
import ru.wb.perevozka.views.ProgressButtonMode

class CourierUnloadingScanFragment : Fragment() {


    companion object {
        const val COURIER_UNLOADING_ID_KEY = "courier_unloading_id_key"
    }

    private val viewModel by viewModel<CourierUnloadingScanViewModel> {
        parametersOf(
            requireArguments().getParcelable<CourierUnloadingScanParameters>(
                COURIER_UNLOADING_ID_KEY
            )
        )
    }

    private var _binding: CourierUnloadingFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = CourierUnloadingFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initListener()
        initObserver()
    }

    private fun initView() {
        (activity as NavToolbarListener).hideToolbar()
        (activity as NavDrawerListener).lock()
        binding.toolbarLayout.toolbarTitle.text = getText(R.string.courier_order_scanner_label)
        binding.toolbarLayout.back.visibility = View.INVISIBLE
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

    private fun initObserver() {
        viewModel.toolbarLabelState.observe(viewLifecycleOwner) {
            binding.toolbarLayout.toolbarTitle.text = it.label
        }

        viewModel.navigateToMessageInfo.observe(viewLifecycleOwner) {
            isDialogActive = true
            showSimpleDialog(it)
        }

        viewModel.toolbarNetworkState.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkState.Failed ->
                    binding.toolbarLayout.noInternetImage.visibility = View.VISIBLE
                is NetworkState.Complete ->
                    binding.toolbarLayout.noInternetImage.visibility = View.INVISIBLE
            }
        }

        viewModel.progressEvent.observe(viewLifecycleOwner) { state ->
            when (state) {
                CourierUnloadingScanProgress.LoaderProgress -> showProgressDialog()
                CourierUnloadingScanProgress.LoaderComplete -> closeProgressDialog()
            }
        }


        val navigationObserver = Observer<CourierUnloadingScanNavAction> { state ->
            when (state) {
                is CourierUnloadingScanNavAction.NavigateToUnknownBox -> {
                    findNavController().navigate(CourierUnloadingScanFragmentDirections.actionCourierUnloadingScanFragmentToCourierUnloadingUnknownBoxFragment())
                }
                is CourierUnloadingScanNavAction.NavigateToConfirmDialog -> {
                    showConfirmDialog(state.title, state.message)
                }
                CourierUnloadingScanNavAction.NavigateToIntransit ->
                    findNavController().navigate(CourierUnloadingScanFragmentDirections.actionCourierUnloadingScanFragmentToCourierIntransitFragment())
                CourierUnloadingScanNavAction.NavigateToBoxes -> {
                }
                is CourierUnloadingScanNavAction.NavigateToDialogInfo -> {
                }
            }
        }

        viewModel.navigationEvent.observe(viewLifecycleOwner, navigationObserver)

        viewModel.beepEvent.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CourierUnloadingScanBeepState.BoxAdded -> beepSuccess()
                is CourierUnloadingScanBeepState.UnknownBox -> beepError()
            }
        }

        viewModel.progressEvent.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CourierUnloadingScanProgress.LoaderProgress -> {
                    binding.receiveLayout.isEnabled = false
                    binding.complete.setState(ProgressButtonMode.DISABLE)
                }
                is CourierUnloadingScanProgress.LoaderComplete -> {
                    binding.receiveLayout.isEnabled = true
                    binding.complete.setState(ProgressButtonMode.ENABLE)
                }
            }
        }

        viewModel.bottomProgressEvent.observe(viewLifecycleOwner) { progress ->
            when (progress) {
                CourierUnloadingScanBottomState.Disable -> binding.complete.setState(
                    ProgressButtonMode.DISABLE
                )
                CourierUnloadingScanBottomState.Enable -> binding.complete.setState(
                    ProgressButtonMode.ENABLE
                )
                CourierUnloadingScanBottomState.Progress -> binding.complete.setState(
                    ProgressButtonMode.PROGRESS
                )
            }
        }

        viewModel.boxStateUI.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CourierUnloadingScanBoxState.Empty -> {
                    binding.status.text = state.status
                    binding.statusLayout.setBackgroundColor(grayColor())
                    binding.statusIcon.visibility = View.VISIBLE
                    binding.qrCode.text = state.qrCode
                    binding.qrCode.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.light_text)
                    )
                    binding.receive.text = state.accepted
                    binding.receiveLayout.isEnabled = false
                    binding.address.text = state.address
                    binding.complete.setState(ProgressButtonMode.ENABLE)
                }
                is CourierUnloadingScanBoxState.BoxInit -> {
                    binding.status.text = state.status
                    binding.statusLayout.setBackgroundColor(grayColor())
                    binding.statusIcon.visibility = View.VISIBLE
                    binding.qrCode.text = state.qrCode
                    binding.qrCode.setTextColor(colorBlack())
                    binding.receive.text = state.accepted
                    binding.receiveLayout.isEnabled = false
                    binding.address.text = state.address
                    binding.complete.setState(ProgressButtonMode.ENABLE)
                }
                is CourierUnloadingScanBoxState.BoxAdded -> {
                    binding.status.text = state.status
                    binding.statusLayout.setBackgroundColor(colorGreen())
                    binding.statusIcon.visibility = View.GONE
                    binding.qrCode.text = state.qrCode
                    binding.qrCode.setTextColor(colorBlack())
                    binding.receive.text = state.accepted
                    binding.address.text = state.address
                    binding.address.setTextColor(colorBlack())
                    binding.complete.setState(ProgressButtonMode.ENABLE)
                }
                is CourierUnloadingScanBoxState.UnknownBox -> {
                    binding.status.text = state.status
                    binding.statusLayout.setBackgroundColor(colorRed())
                    binding.statusIcon.visibility = View.GONE
                    binding.qrCode.text = state.qrCode
                    binding.qrCode.setTextColor(colorBlack())
                    binding.address.text = state.address
                    binding.address.setTextColor(colorBlack())
                    binding.receive.text = state.accepted
                }
                is CourierUnloadingScanBoxState.ScannerReady -> {
                    binding.status.text = state.status
                    binding.statusLayout.setBackgroundColor(grayColor())
                    binding.statusIcon.visibility = View.VISIBLE
                    binding.qrCode.text = state.qrCode
                    binding.qrCode.setTextColor(colorBlack())
                    binding.receive.text = state.accepted
                    binding.address.text = state.address
                    binding.address.setTextColor(colorBlack())
                    binding.complete.setState(ProgressButtonMode.ENABLE)
                }
            }
        }
    }

    private fun grayColor() = ContextCompat.getColor(requireContext(), R.color.disable_scan_status)

    private fun colorRed() = ContextCompat.getColor(requireContext(), R.color.unknown_scan_status)

    private fun colorGreen() =
        ContextCompat.getColor(requireContext(), R.color.complete_scan_status)

    private fun colorBlack() = ContextCompat.getColor(requireContext(), R.color.black_text)

    private fun showDialog(style: Int, title: String, message: String, positiveButtonName: String) {
        DialogInfoFragment.newInstance(style, title, message, positiveButtonName)
            .show(parentFragmentManager, DIALOG_INFO_TAG)
    }

    private fun showProgressDialog() {
        val progressDialog = ProgressDialogFragment.newInstance()
        progressDialog.show(parentFragmentManager, ProgressDialogFragment.PROGRESS_DIALOG_TAG)
    }

    private fun closeProgressDialog() {
        parentFragmentManager.findFragmentByTag(ProgressDialogFragment.PROGRESS_DIALOG_TAG)?.let {
            if (it is ProgressDialogFragment) it.dismiss()
        }
    }

    private fun showSimpleDialog(it: CourierUnloadingScanViewModel.NavigateToMessageInfo) {
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

    // TODO: 27.08.2021 переработать
    private fun showConfirmDialog(title: String, message: String) {
        val builder: AlertDialog.Builder =
            AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val viewGroup: ViewGroup = binding.main
        val dialogView: View =
            LayoutInflater.from(requireContext())
                .inflate(R.layout.custom_layout_dialog_result, viewGroup, false)
        val titleText: TextView = dialogView.findViewById(R.id.title)
        val messageText: TextView = dialogView.findViewById(R.id.message)
        val negative: Button = dialogView.findViewById(R.id.negative)
        val positive: Button = dialogView.findViewById(R.id.positive)

        builder.setView(dialogView)
        val alertDialog: AlertDialog = builder.create()

        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                alertDialog.dismiss()
                viewModel.cancelUnloadingClick()
            }
            true
        }

        titleText.text = title
        messageText.text = message
        negative.setOnClickListener {
            alertDialog.dismiss()
            viewModel.cancelUnloadingClick()
        }
        negative.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
        negative.text = getString(R.string.courier_order_scanner_dialog_negative_button)
        positive.setOnClickListener {
            alertDialog.dismiss()
            viewModel.confirmUnloadingClick()
        }
        positive.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
        positive.text = getString(R.string.courier_order_scanner_dialog_positive_button)
        alertDialog.show()
    }

    private fun initListener() {
        binding.complete.setOnClickListener { viewModel.onCompleteUnloadClicked() }
        binding.receiveLayout.setOnClickListener { viewModel.onListClicked() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun beepSuccess() {
        // TODO: 11.10.2021 unused
        //play(R.raw.sound_scan_success)
    }

    private fun beepError() {
        play(R.raw.qr_box_scan_failed)
    }

    private fun play(resId: Int) {
        MediaPlayer.create(context, resId).start()
    }

}

@Parcelize
data class CourierUnloadingScanParameters(val officeId: Int) : Parcelable