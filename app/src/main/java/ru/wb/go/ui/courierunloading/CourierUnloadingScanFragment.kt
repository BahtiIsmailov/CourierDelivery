package ru.wb.go.ui.courierunloading

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import ru.wb.go.R
import ru.wb.go.databinding.CourierUnloadingFragmentBinding
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.dialogs.DialogConfirmInfoFragment
import ru.wb.go.ui.dialogs.DialogConfirmInfoFragment.Companion.DIALOG_CONFIRM_INFO_NEGATIVE_KEY
import ru.wb.go.ui.dialogs.DialogConfirmInfoFragment.Companion.DIALOG_CONFIRM_INFO_POSITIVE_KEY
import ru.wb.go.ui.dialogs.DialogConfirmInfoFragment.Companion.DIALOG_CONFIRM_INFO_RESULT_TAG
import ru.wb.go.ui.dialogs.DialogConfirmInfoFragment.Companion.DIALOG_CONFIRM_INFO_TAG
import ru.wb.go.ui.dialogs.DialogInfoFragment
import ru.wb.go.ui.dialogs.DialogInfoFragment.Companion.DIALOG_INFO_BACK_KEY
import ru.wb.go.ui.dialogs.DialogInfoFragment.Companion.DIALOG_INFO_RESULT_TAG
import ru.wb.go.ui.dialogs.DialogInfoFragment.Companion.DIALOG_INFO_TAG
import ru.wb.go.ui.dialogs.ProgressDialogFragment
import ru.wb.go.ui.splash.NavDrawerListener
import ru.wb.go.ui.splash.NavToolbarListener
import ru.wb.go.ui.splash.OnSoundPlayer
import ru.wb.go.views.ProgressButtonMode

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

    private var isDialogActive: Boolean = false

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
        initReturnDialogResult()
    }

    private fun initReturnDialogResult() {
        setFragmentResultListener(DIALOG_INFO_RESULT_TAG) { _, bundle ->
            if (bundle.containsKey(DIALOG_INFO_BACK_KEY)) {
                isDialogActive = false
                viewModel.onStartScanner()
            }
        }

        setFragmentResultListener(DIALOG_CONFIRM_INFO_RESULT_TAG) { _, bundle ->
            if (bundle.containsKey(DIALOG_CONFIRM_INFO_POSITIVE_KEY)) {
                viewModel.onConfirmUnloadingClick()
            }
            if (bundle.containsKey(DIALOG_CONFIRM_INFO_NEGATIVE_KEY)) {
                viewModel.onCancelUnloadingClick()
            }
        }
    }

    private fun initView() {
        (activity as NavToolbarListener).hideToolbar()
        (activity as NavDrawerListener).lock()
        binding.toolbarLayout.toolbarTitle.text = getText(R.string.courier_order_scanner_label)
        binding.toolbarLayout.back.visibility = View.INVISIBLE
    }

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

        viewModel.navigateToDialogInfo.observe(viewLifecycleOwner) {
            isDialogActive = true
            showDialogInfo(it.type, it.title, it.message, it.button)
        }

        viewModel.navigateToDialogConfirmInfo.observe(viewLifecycleOwner) {
            showDialogConfirmInfo(it.type, it.title, it.message, it.positive, it.negative)
        }

        viewModel.toolbarNetworkState.observe(viewLifecycleOwner) {
            val ic = when (it) {
                is NetworkState.Complete -> R.drawable.ic_inet_complete
                else -> R.drawable.ic_inet_failed
            }
            binding.toolbarLayout.noInternetImage.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), ic)
            )
        }

        viewModel.versionApp.observe(viewLifecycleOwner) {
            binding.toolbarLayout.toolbarVersion.text = it
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
                CourierUnloadingScanNavAction.NavigateToIntransit ->
                    findNavController().navigate(CourierUnloadingScanFragmentDirections.actionCourierUnloadingScanFragmentToCourierIntransitFragment())
                CourierUnloadingScanNavAction.NavigateToBoxes -> {
                }
            }
        }

        viewModel.navigationEvent.observe(viewLifecycleOwner, navigationObserver)

        viewModel.beepEvent.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CourierUnloadingScanBeepState.BoxAdded -> beepSuccess()
                is CourierUnloadingScanBeepState.UnknownBox -> beepUnknownBox()
                CourierUnloadingScanBeepState.UnknownQR -> beepUnknownQR()
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

    private fun showProgressDialog() {
        val progressDialog = ProgressDialogFragment.newInstance()
        progressDialog.show(parentFragmentManager, ProgressDialogFragment.PROGRESS_DIALOG_TAG)
    }

    private fun closeProgressDialog() {
        parentFragmentManager.findFragmentByTag(ProgressDialogFragment.PROGRESS_DIALOG_TAG)?.let {
            if (it is ProgressDialogFragment) it.dismiss()
        }
    }

    private fun showDialogInfo(
        type: Int,
        title: String,
        message: String,
        positiveButtonName: String
    ) {
        DialogInfoFragment.newInstance(
            type = type,
            title = title,
            message = message,
            positiveButtonName = positiveButtonName
        ).show(parentFragmentManager, DIALOG_INFO_TAG)
    }

    private fun showDialogConfirmInfo(
        type: Int,
        title: String,
        message: String,
        positiveButtonName: String,
        negativeButtonName: String
    ) {
        DialogConfirmInfoFragment.newInstance(
            type = type,
            title = title,
            message = message,
            positiveButtonName = positiveButtonName,
            negativeButtonName = negativeButtonName
        ).show(parentFragmentManager, DIALOG_CONFIRM_INFO_TAG)
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
        play(R.raw.qr_box_first_accepted)
    }

    private fun beepUnknownQR() {
        play(R.raw.unloading_scan_unknown_qr)
    }

    private fun beepUnknownBox() {
        play(R.raw.unloading_unknown_box)
    }

    private fun play(resId: Int) {
        (activity as OnSoundPlayer).play(resId)
    }

}

@Parcelize
data class CourierUnloadingScanParameters(val officeId: Int) : Parcelable