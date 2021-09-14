package ru.wb.perevozka.ui.courierloading

import android.app.AlertDialog
import android.media.MediaPlayer
import android.os.Bundle
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
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.perevozka.R
import ru.wb.perevozka.databinding.CourierLoadingFragmentBinding
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.ui.splash.NavToolbarListener
import ru.wb.perevozka.views.ProgressButtonMode

class CourierLoadingScanFragment : Fragment() {

    private val viewModel by viewModel<CourierLoadingScanViewModel>()

    private var _binding: CourierLoadingFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = CourierLoadingFragmentBinding.inflate(inflater, container, false)
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
        binding.toolbarLayout.toolbarTitle.text = getText(R.string.courier_order_scanner_label)
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

        viewModel.orderTimer.observe(viewLifecycleOwner) {
            when (it) {
                is CourierLoadingScanTimerState.Timer -> {
                    binding.timeDigit.visibility = View.VISIBLE
                    binding.timer.visibility = View.VISIBLE
                    binding.timeDigit.text = it.timeDigit
                    binding.timer.setProgress(it.timeAnalog)
                }
                is CourierLoadingScanTimerState.TimeIsOut -> {
                    showTimeIsOutDialog(it.title, it.message, it.button)
                }
                CourierLoadingScanTimerState.Stopped -> {
                    binding.timeDigit.visibility = View.GONE
                    binding.timer.visibility = View.GONE
                }
            }
        }

        val navigationObserver = Observer<CourierLoadingScanNavAction> { state ->
            when (state) {
                is CourierLoadingScanNavAction.NavigateToUnknownBox -> {
                    findNavController().navigate(CourierLoadingScanFragmentDirections.actionCourierScannerLoadingScanFragmentToCourierScannerLoadingBoxNotBelongFragment())
                }
                CourierLoadingScanNavAction.NavigateToBoxes -> {
                    findNavController().navigate(CourierLoadingScanFragmentDirections.actionCourierScannerLoadingScanFragmentToCourierLoadingBoxesFragment())
                }
                CourierLoadingScanNavAction.NavigateToConfirmDialog -> {
                    showConfirmDialog("Завершить погрузку?", "Вы уверены, что хотите начать развозить коробки")
                }
                CourierLoadingScanNavAction.NavigateToBack -> {
                }
                CourierLoadingScanNavAction.NavigateToWarehouse ->
                    findNavController().navigate(CourierLoadingScanFragmentDirections.actionCourierScannerLoadingScanFragmentToCourierWarehouseFragment())
            }
        }

        viewModel.navigationEvent.observe(viewLifecycleOwner, navigationObserver)

        viewModel.beepEvent.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CourierLoadingScanBeepState.BoxAdded -> beepSuccess()
                is CourierLoadingScanBeepState.UnknownBox -> beepError()
            }
        }

        viewModel.progressEvent.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CourierLoadingScanProgress.LoaderProgress -> {
                    binding.listLayout.isEnabled = false
                    binding.complete.setState(ProgressButtonMode.DISABLE)
                }
                is CourierLoadingScanProgress.LoaderComplete -> {
                    binding.listLayout.isEnabled = true
                    binding.complete.setState(ProgressButtonMode.ENABLE)
                }
            }
        }

        viewModel.bottomProgressEvent.observe(viewLifecycleOwner) { progress ->
            when (progress) {
                CourierLoadingScanBottomState.Disable -> binding.complete.setState(
                    ProgressButtonMode.DISABLE
                )
                CourierLoadingScanBottomState.Enable -> binding.complete.setState(
                    ProgressButtonMode.ENABLE
                )
                CourierLoadingScanBottomState.Progress -> binding.complete.setState(
                    ProgressButtonMode.PROGRESS
                )
            }
        }

        viewModel.boxStateUI.observe(viewLifecycleOwner) { state ->
            when (state) {
                CourierLoadingScanBoxState.Empty -> {
                    binding.toolbarLayout.back.visibility = View.VISIBLE
                    binding.status.text = "НАЧНИТЕ СКАНИРОВАНИЕ"
                    binding.status.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.disable_scan_status
                        )
                    )
                    binding.qrCode.text = "0000000000"
                    binding.qrCode.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.light_text
                        )
                    )
                    binding.address.text = "-"
                    binding.receive.text = "0 шт."
                    binding.listLayout.setOnClickListener { null }
                    binding.complete.setState(ProgressButtonMode.DISABLE)
                }
                is CourierLoadingScanBoxState.BoxInit -> {
                    binding.toolbarLayout.back.visibility = View.INVISIBLE
                    binding.status.text = "ПОГРУЗИТЕ В МАШИНУ"
                    binding.status.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.complete_scan_status
                        )
                    )
                    binding.qrCode.text = state.qrCode
                    binding.qrCode.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.black_text
                        )
                    )
                    binding.address.text = state.address
                    binding.receive.text = state.accepted
                    binding.listLayout.setOnClickListener { viewModel.onListClicked() }

                    binding.timeDigit.visibility = View.GONE
                    binding.timer.visibility = View.GONE
                    binding.complete.setState(ProgressButtonMode.ENABLE)

                }
                is CourierLoadingScanBoxState.BoxAdded -> {
                    binding.toolbarLayout.back.visibility = View.INVISIBLE
                    binding.status.text = "ПОГРУЗИТЕ В МАШИНУ"
                    binding.status.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.complete_scan_status
                        )
                    )
                    binding.qrCode.text = state.qrCode
                    binding.qrCode.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.black_text
                        )
                    )
                    binding.address.text = state.address
                    binding.address.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.black_text
                        )
                    )
                    binding.receive.text = state.accepted
                    binding.listLayout.setOnClickListener { viewModel.onListClicked() }
                }
                is CourierLoadingScanBoxState.UnknownBox -> {
                    binding.toolbarLayout.back.visibility = View.INVISIBLE
                    binding.status.text = "КОРОБКУ БРАТЬ ЗАПРЕЩЕНО"
                    binding.status.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.unknown_scan_status
                        )
                    )
                    binding.qrCode.text = state.qrCode
                    binding.qrCode.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.black_text
                        )
                    )
                    binding.address.text = state.address
                    binding.address.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.black_text
                        )
                    )
                    binding.receive.text = state.accepted
                    binding.listLayout.setOnClickListener { viewModel.onListClicked() }
                }

            }
        }
    }

    private fun showSimpleDialog(it: CourierLoadingScanViewModel.NavigateToMessageInfo) {
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

    private fun showTimeIsOutDialog(title: String, message: String, button: String) {
        val builder: AlertDialog.Builder =
            AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val viewGroup: ViewGroup = binding.main
        val dialogView: View =
            LayoutInflater.from(requireContext())
                .inflate(R.layout.custom_layout_dialog_info_result, viewGroup, false)
        val titleText: TextView = dialogView.findViewById(R.id.title)
        val messageText: TextView = dialogView.findViewById(R.id.message)
        val positive: Button = dialogView.findViewById(R.id.positive)

        builder.setView(dialogView)
        val alertDialog: AlertDialog = builder.create()
        titleText.text = title
        messageText.text = message
        positive.setOnClickListener {
            alertDialog.dismiss()
            viewModel.returnToListOrderClick()
        }

        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                alertDialog.dismiss()
                viewModel.returnToListOrderClick()
            }
            true
        }

        positive.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
        positive.text = button
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
        titleText.text = title
        messageText.text = message
        negative.setOnClickListener { alertDialog.dismiss() }
        negative.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
        negative.text = getString(R.string.courier_order_scanner_dialog_negative_button)
        positive.setOnClickListener {
            alertDialog.dismiss()
            viewModel.confirmLoadingClick()
        }
        positive.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
        positive.text = getString(R.string.courier_order_scanner_dialog_positive_button)
        alertDialog.show()
    }

    private fun initListener() {
        binding.complete.setOnClickListener { viewModel.onCompleteClicked() }
        binding.listLayout.setOnClickListener { viewModel.onListClicked() }
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