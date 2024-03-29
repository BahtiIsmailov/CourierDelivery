package ru.wb.go.ui.courierintransitofficescanner

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.FrameLayout
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.go.R
import ru.wb.go.databinding.CourierIntransitOfficeScannerFragmentBinding
import ru.wb.go.ui.BaseServiceFragment
import ru.wb.go.ui.app.NavDrawerListener
import ru.wb.go.ui.app.NavToolbarListener
import ru.wb.go.ui.courierunloading.CourierUnloadingScanParameters
import ru.wb.go.ui.dialogs.DialogConfirmInfoFragment
import ru.wb.go.ui.dialogs.DialogInfoFragment
import ru.wb.go.ui.dialogs.DialogInfoFragment.Companion.DIALOG_INFO_TAG
import ru.wb.go.ui.dialogs.ProgressDialogFragment
import ru.wb.go.utils.WaitLoader
import ru.wb.go.utils.managers.ErrorDialogData

class CourierIntransitOfficeScannerFragment :
    BaseServiceFragment<CourierIntransitOfficeScannerViewModel, CourierIntransitOfficeScannerFragmentBinding>(
        CourierIntransitOfficeScannerFragmentBinding::inflate
    ) {

    private val bottomSheetQrOfficeFailed: BottomSheetBehavior<FrameLayout>
        get() = BottomSheetBehavior.from(binding.qrOfficeFailed)

    override val viewModel by viewModel<CourierIntransitOfficeScannerViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initObservable()
        initListeners()
        initReturnDialogResult()
    }

    private fun initBottomSheet() {
        bottomSheetQrOfficeFailed.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) viewModel.onAccessiblyClick()
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
    }

    private fun hideAllBottomSheet() {
        bottomSheetQrOfficeFailed.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun showQrOfficeFailed() {
        bottomSheetQrOfficeFailed.state = BottomSheetBehavior.STATE_EXPANDED
    }


    private fun initReturnDialogResult() {
        setFragmentResultListener(DIALOG_INFO_TAG) { _, bundle ->
            if (bundle.containsKey(DialogInfoFragment.DIALOG_INFO_BACK_KEY)) {
                viewModel.onErrorDialogConfirmClick()
            }
        }
    }

    private fun initView() {
        (activity as NavToolbarListener).hideToolbar()
        (activity as NavDrawerListener).lockNavDrawer()
        initBottomSheet()
        hideAllBottomSheet()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initObservable() {

        viewModel.toolbarLabelState.observe(viewLifecycleOwner) {
            binding.toolbarLayout.toolbarTitle.text = it
        }

        viewModel.infoCameraVisibleState.observe(viewLifecycleOwner) {
            binding.infoStatus.visibility = if (it) VISIBLE else GONE
        }

        viewModel.navigateToErrorDialog.observe(viewLifecycleOwner) {
            showDialogInfo(it)
        }

        viewModel.scannerBeepEvent.observe(viewLifecycleOwner) { state ->
            when (state) {
                CourierIntransitOfficeScannerBeepState.Office -> scanOfficeAccepted()
                CourierIntransitOfficeScannerBeepState.UnknownQrOffice -> scanOfficeFailed()
                CourierIntransitOfficeScannerBeepState.WrongOffice -> scanWrongOffice()
            }
        }

        viewModel.waitLoader.observe(viewLifecycleOwner) {
            when (it) {
                WaitLoader.Wait -> showProgressDialog()
                WaitLoader.Complete -> closeProgressDialog()
            }
        }

        viewModel.navigateToDialogConfirmInfo.observe(viewLifecycleOwner) {
            showDialogConfirmInfo(it.type, it.title, it.message, it.positive, it.negative)
        }

        viewModel.officeScannerNavigationState.observe(viewLifecycleOwner) {
            when (it) {
                CourierIntransitOfficeScannerNavigationState.NavigateToMap -> {
                    findNavController().popBackStack()
                }
                is CourierIntransitOfficeScannerNavigationState.NavigateToOfficeFailed -> {
                    binding.title.text = it.title
                    binding.message.text = it.message
                    showQrOfficeFailed()
                }

                is CourierIntransitOfficeScannerNavigationState.NavigateToUnloadingScanner -> {
                    findNavController().navigate(
                        CourierIntransitOfficeScannerFragmentDirections.actionCourierIntransitOfficeScannerFragmentToCourierUnloadingScanFragment(
                            CourierUnloadingScanParameters(it.officeId)
                        )
                    )
                }
                CourierIntransitOfficeScannerNavigationState.NavigateToScanner -> {
                    hideAllBottomSheet()
                }
            }
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

    private fun initListeners() {
        binding.toolbarLayout.back.setOnClickListener { viewModel.onCloseScannerClick() }
        binding.accessibly.setOnClickListener { viewModel.onAccessiblyClick() }
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

    private fun showDialogConfirmInfo(
        style: Int,
        title: String,
        message: String,
        positiveButtonName: String,
        negativeButtonName: String
    ) {
        DialogConfirmInfoFragment.newInstance(
            type = style,
            title = title,
            message = message,
            positiveButtonName = positiveButtonName,
            negativeButtonName = negativeButtonName
        ).show(parentFragmentManager, DialogConfirmInfoFragment.DIALOG_CONFIRM_INFO_TAG)
    }

    private fun scanOfficeAccepted() {
        viewModel.play(R.raw.qr_office_accepted)
    }

    private fun scanOfficeFailed() {
        viewModel.play(R.raw.qr_office_failed)
    }

    private fun scanWrongOffice() {
        viewModel.play(R.raw.wrongoffice)
    }

}