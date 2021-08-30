package ru.wb.perevozka.ui.courierloading

import android.app.AlertDialog
import android.graphics.PorterDuff
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
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.perevozka.R
import ru.wb.perevozka.databinding.CourierOrderScannerFaragmentBinding
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.ui.dcloading.DcLoadingHandleFragment.Companion.HANDLE_BARCODE_RESULT
import ru.wb.perevozka.ui.splash.NavToolbarListener
import ru.wb.perevozka.views.ProgressButtonMode
import ru.wb.perevozka.views.ProgressImageButtonMode


class CourierLoadingScanFragment : Fragment() {

    private val viewModel by viewModel<CourierLoadingScanViewModel>()

    private var _binding: CourierOrderScannerFaragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = CourierOrderScannerFaragmentBinding.inflate(inflater, container, false)
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
                    binding.timeIcon.visibility = View.VISIBLE
                    binding.timeDigit.visibility = View.VISIBLE
                    binding.timeDigit.text = it.timeDigit
                }
                CourierLoadingScanTimerState.TimeIsOut -> {
                    binding.timeIcon.visibility = View.GONE
                    binding.timeDigit.visibility = View.GONE
                }
            }
        }

        val navigationObserver = Observer<CourierLoadingScanNavAction> { state ->
            when (state) {
                is CourierLoadingScanNavAction.NavigateToUnknownBox -> { }
                CourierLoadingScanNavAction.NavigateToBoxes -> { }
                CourierLoadingScanNavAction.NavigateToFlightDeliveries -> { }
                CourierLoadingScanNavAction.NavigateToBack -> { }
                CourierLoadingScanNavAction.NavigateToHandle -> { }
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
                    binding.complete.setState(ProgressImageButtonMode.DISABLED)
                }
                is CourierLoadingScanProgress.LoaderComplete -> {
                    binding.complete.setState(ProgressImageButtonMode.ENABLED)
                }
            }
        }

        viewModel.bottomProgressEvent.observe(viewLifecycleOwner) { progress ->
            binding.complete.setState(
                if (progress) ProgressImageButtonMode.PROGRESS else ProgressImageButtonMode.ENABLED
            )
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
                    binding.address.text = "Адрес доставки"
                    binding.address.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.light_text
                        )
                    )
                    binding.listImageView.setColorFilter(
                        ContextCompat.getColor(requireContext(), R.color.disable_icon),
                        PorterDuff.Mode.SRC_ATOP
                    )
                    binding.receive.text = "0"
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
                    binding.address.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.black_text
                        )
                    )
                    binding.listImageView.setColorFilter(
                        ContextCompat.getColor(requireContext(), R.color.disable_icon),
                        PorterDuff.Mode.SRC_ATOP
                    )
                    binding.receive.text = state.accepted
                    binding.listLayout.setOnClickListener { viewModel.onListClicked() }
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
                    binding.listImageView.setColorFilter(
                        ContextCompat.getColor(requireContext(), R.color.disable_icon),
                        PorterDuff.Mode.SRC_ATOP
                    )
                    binding.receive.text = state.accepted
                    binding.listLayout.setOnClickListener { viewModel.onListClicked() }
                    binding.complete.setState(ProgressButtonMode.ENABLE)
                }
                is CourierLoadingScanBoxState.UnknownBox -> {
                    binding.toolbarLayout.back.visibility = View.INVISIBLE
                    binding.status.text = "НЕИЗВЕСТНАЯ КОРОБКА"
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
                    binding.listImageView.setColorFilter(
                        ContextCompat.getColor(requireContext(), R.color.disable_icon),
                        PorterDuff.Mode.SRC_ATOP
                    )
                    binding.receive.text = state.accepted
                    binding.listLayout.setOnClickListener { viewModel.onListClicked() }
                    binding.complete.setState(ProgressButtonMode.ENABLE)

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