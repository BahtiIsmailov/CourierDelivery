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
import ru.wb.go.ui.app.NavDrawerListener
import ru.wb.go.ui.app.NavToolbarListener
import ru.wb.go.ui.dialogs.DialogConfirmInfoFragment
import ru.wb.go.ui.dialogs.DialogConfirmInfoFragment.Companion.DIALOG_CONFIRM_INFO_NEGATIVE_KEY
import ru.wb.go.ui.dialogs.DialogConfirmInfoFragment.Companion.DIALOG_CONFIRM_INFO_POSITIVE_KEY
import ru.wb.go.ui.dialogs.DialogConfirmInfoFragment.Companion.DIALOG_CONFIRM_INFO_TAG
import ru.wb.go.ui.dialogs.DialogInfoFragment
import ru.wb.go.ui.dialogs.DialogInfoFragment.Companion.DIALOG_INFO_BACK_KEY
import ru.wb.go.ui.dialogs.DialogInfoFragment.Companion.DIALOG_INFO_TAG
import ru.wb.go.ui.dialogs.ProgressDialogFragment
import ru.wb.go.utils.WaitLoader
import ru.wb.go.utils.managers.ErrorDialogData
import ru.wb.go.views.ProgressButtonMode

class CourierUnloadingScanFragment : Fragment() {

    companion object {
        const val COURIER_UNLOADING_ID_KEY = "courier_unloading_id_key"
        const val DIALOG_ERROR_RESULT_TAG = "DIALOG_ERROR_RESULT_TAG"
        const val DIALOG_SCORE_ERROR_RESULT_TAG = "DIALOG_SCORE_ERROR_RESULT_TAG"
        const val DIALOG_CONFIRM_SCORE_UNLOADING_RESULT_TAG =
            "DIALOG_CONFIRM_SCORE_UNLOADING_RESULT_TAG"
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
        initReturnDialogResult()
        viewModel.update()
    }

    private fun initReturnDialogResult() {

        setFragmentResultListener(DIALOG_ERROR_RESULT_TAG) { _, bundle ->
            if (bundle.containsKey(DIALOG_INFO_BACK_KEY)) {
                viewModel.onScoreDialogInfoClick()
            }
        }

        setFragmentResultListener(DIALOG_SCORE_ERROR_RESULT_TAG) { _, bundle ->
            if (bundle.containsKey(DIALOG_INFO_BACK_KEY)) {
                viewModel.onScoreDialogConfirmClick()
            }
        }

        setFragmentResultListener(DIALOG_CONFIRM_SCORE_UNLOADING_RESULT_TAG) { _, bundle ->
            if (bundle.containsKey(DIALOG_CONFIRM_INFO_POSITIVE_KEY)) {
                viewModel.onConfirmScoreUnloadingClick()
            }
            if (bundle.containsKey(DIALOG_CONFIRM_INFO_NEGATIVE_KEY)) {
                viewModel.onCancelScoreUnloadingClick()
            }
        }
    }

    private fun initView() {
        (activity as NavToolbarListener).hideToolbar()
        (activity as NavDrawerListener).lockNavDrawer()
        binding.toolbarLayout.toolbarTitle.text = getText(R.string.courier_order_scanner_label)
        binding.toolbarLayout.back.visibility = View.INVISIBLE
    }

    private fun initObserver() {

        viewModel.toolbarLabelState.observe(viewLifecycleOwner) {
            binding.toolbarLayout.toolbarTitle.text = it.label
        }

        viewModel.navigateToDialogInfo.observe(viewLifecycleOwner) {
            showDialogInfo(it)
        }

        viewModel.navigateToDialogScoreError.observe(viewLifecycleOwner) {
            showDialogScoreError(it.type, it.title, it.message, it.button)
        }

        viewModel.navigateToDialogConfirmScoreInfo.observe(viewLifecycleOwner) {
            showDialogConfirmScoreInfo(it.type, it.title, it.message, it.positive, it.negative)
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

        viewModel.waitLoader.observe(viewLifecycleOwner) { state ->
            when (state) {
                WaitLoader.Wait -> showProgressDialog()
                WaitLoader.Complete -> closeProgressDialog()
            }
        }

        val navigationObserver = Observer<CourierUnloadingScanNavAction> { state ->
            when (state) {
                CourierUnloadingScanNavAction.NavigateToIntransit ->
                    findNavController().navigate(
                        CourierUnloadingScanFragmentDirections.actionCourierUnloadingScanFragmentToCourierIntransitFragment()
                    )
                is CourierUnloadingScanNavAction.NavigateToBoxes -> {
                    findNavController().navigate(
                        CourierUnloadingScanFragmentDirections.actionCourierUnloadingScanFragmentToRemainBoxFragment(
                            RemainBoxParameters(
                                officeId = state.officeId
                            )
                        )
                    )
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

        viewModel.completeButtonEnable.observe(viewLifecycleOwner) { progress ->
            when (progress) {
                true -> binding.completeButton.setState(ProgressButtonMode.ENABLE)
                false -> binding.completeButton.setState(ProgressButtonMode.DISABLE)
            }
        }

        viewModel.fragmentStateUI.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UnloadingFragmentState.Empty -> {
                    binding.ribbonStatus.text = state.data.status
                    binding.statusLayout.setBackgroundColor(primaryColor())
                    binding.qrCode.text = state.data.qrCode
                    binding.totalBoxes.text = state.data.accepted
                    binding.counterLayout.isEnabled = false
                    binding.boxAddress.text = state.data.address
                    binding.completeButton.setState(ProgressButtonMode.ENABLE)
                }
                is UnloadingFragmentState.BoxInit -> {
                    binding.ribbonStatus.text = state.data.status
                    binding.statusLayout.setBackgroundColor(grayColor())
                    binding.qrCode.text = state.data.qrCode
                    binding.qrCode.setTextColor(colorBlack())
                    binding.totalBoxes.text = state.data.accepted
                    binding.counterLayout.isEnabled = false
                    binding.boxAddress.text = state.data.address
                    binding.completeButton.setState(ProgressButtonMode.ENABLE)
                }
                is UnloadingFragmentState.BoxAdded -> {
                    binding.ribbonStatus.text = state.data.status
                    binding.statusLayout.setBackgroundColor(colorGreen())
                    binding.qrCode.text = state.data.qrCode
                    binding.qrCode.setTextColor(colorBlack())
                    binding.totalBoxes.text = state.data.accepted
                    binding.boxAddress.text = state.data.address
                    binding.boxAddress.setTextColor(colorBlack())
                    binding.completeButton.setState(ProgressButtonMode.ENABLE)
                }
                is UnloadingFragmentState.UnknownQr -> {
                    binding.ribbonStatus.text = state.data.status
                    binding.statusLayout.setBackgroundColor(getColor(R.color.yellow))
                    binding.qrCode.text = state.data.qrCode
                    binding.qrCode.setTextColor(colorBlack())
                    binding.boxAddress.text = state.data.address
                    binding.boxAddress.setTextColor(colorBlack())
                    binding.totalBoxes.text = state.data.accepted
                }
                is UnloadingFragmentState.ScannerReady -> {
                    binding.ribbonStatus.text = state.data.status
                    binding.statusLayout.setBackgroundColor(grayColor())
                    binding.qrCode.text = state.data.qrCode
                    binding.qrCode.setTextColor(colorBlack())
                    binding.totalBoxes.text = state.data.accepted
                    binding.boxAddress.text = state.data.address
                    binding.boxAddress.setTextColor(colorBlack())
                    binding.completeButton.setState(ProgressButtonMode.ENABLE)
                }
                is UnloadingFragmentState.ForbiddenBox -> {
                    binding.ribbonStatus.text = state.data.status
                    binding.statusLayout.setBackgroundColor(colorRed())
                    binding.qrCode.text = state.data.qrCode
                    binding.qrCode.setTextColor(colorBlack())
                    binding.boxAddress.text = state.data.address
                    binding.boxAddress.setTextColor(colorBlack())
                    binding.totalBoxes.text = state.data.accepted
                }
                is UnloadingFragmentState.WrongBox -> {
                    binding.ribbonStatus.text = state.data.status
                    binding.statusLayout.setBackgroundColor(colorRed())
                    binding.qrCode.text = state.data.qrCode
                    binding.qrCode.setTextColor(colorBlack())
                    binding.boxAddress.text = state.data.address
                    binding.boxAddress.setTextColor(colorBlack())
                    binding.totalBoxes.text = state.data.accepted
                }
            }
        }
    }

    private fun getColor(colorId: Int) = ContextCompat.getColor(requireContext(), colorId)

    private fun primaryColor() = ContextCompat.getColor(requireContext(), R.color.colorPrimary)

    private fun grayColor() = ContextCompat.getColor(requireContext(), R.color.lvl_2)

    private fun colorRed() = ContextCompat.getColor(requireContext(), R.color.red)

    private fun colorGreen() =
        ContextCompat.getColor(requireContext(), R.color.green)

    private fun colorBlack() = ContextCompat.getColor(requireContext(), R.color.primary)

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
        errorDialogData: ErrorDialogData
    ) {
        DialogInfoFragment.newInstance(
            resultTag = errorDialogData.dlgTag,
            type = errorDialogData.type,
            title = errorDialogData.title,
            message = errorDialogData.message,
            positiveButtonName = requireContext().getString(R.string.ok_button_title)
        ).show(parentFragmentManager, DIALOG_INFO_TAG)
    }

    private fun showDialogScoreError(
        type: Int,
        title: String,
        message: String,
        positiveButtonName: String
    ) {
        DialogInfoFragment.newInstance(
            DIALOG_SCORE_ERROR_RESULT_TAG,
            type,
            title,
            message,
            positiveButtonName
        ).show(parentFragmentManager, DIALOG_INFO_TAG)
    }

    private fun showDialogConfirmScoreInfo(
        type: Int,
        title: String,
        message: String,
        positiveButtonName: String,
        negativeButtonName: String
    ) {
        DialogConfirmInfoFragment.newInstance(
            DIALOG_CONFIRM_SCORE_UNLOADING_RESULT_TAG,
            type,
            title,
            message,
            positiveButtonName,
            negativeButtonName
        ).show(parentFragmentManager, DIALOG_CONFIRM_INFO_TAG)
    }

    private fun initListener() {
        binding.counterLayout.setOnClickListener { viewModel.onListClicked() }
        binding.totalBoxes.setOnClickListener { viewModel.onListClicked() }
        binding.completeButton.setOnClickListener { viewModel.onCompleteUnloadClick() }
    }

    override fun onDestroyView() {
        viewModel.onDestroy()
        super.onDestroyView()
        _binding = null
    }

    private fun beepSuccess() {
        // TODO: 11.10.2021 unused
        viewModel.play(R.raw.qr_box_first_accepted)
    }

    private fun beepUnknownQR() {
        viewModel.play(R.raw.unloading_scan_unknown_qr)
    }

    private fun beepUnknownBox() {
        viewModel.play(R.raw.unloading_unknown_box)
    }

}

@Parcelize
data class CourierUnloadingScanParameters(val officeId: Int) : Parcelable