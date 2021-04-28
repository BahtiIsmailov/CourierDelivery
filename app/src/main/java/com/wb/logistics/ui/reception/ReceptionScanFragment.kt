package com.wb.logistics.ui.reception

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.wb.logistics.R
import com.wb.logistics.databinding.ReceptionScanFragmentBinding
import com.wb.logistics.ui.reception.ReceptionHandleFragment.Companion.HANDLE_BARCODE_RESULT
import com.wb.logistics.ui.reception.views.ReceptionAcceptedMode
import com.wb.logistics.ui.reception.views.ReceptionInfoMode
import com.wb.logistics.ui.reception.views.ReceptionParkingMode
import com.wb.logistics.views.ProgressImageButtonMode
import org.koin.androidx.viewmodel.ext.android.viewModel


class ReceptionFragment : Fragment() {

    private val viewModel by viewModel<ReceptionScanViewModel>()

    private var _binding: ReceptionScanFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = ReceptionScanFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
        initObserver()
    }

    private fun initObserver() {
        val navigationObserver = Observer<ReceptionScanNavAction> { state ->
            when (state) {
                is ReceptionScanNavAction.NavigateToReceptionBoxNotBelong -> {
                    findNavController().navigate(
                        ReceptionFragmentDirections.actionReceptionFragmentToReceptionBoxNotBelongFragment(
                            with(state) {
                                ReceptionBoxNotBelongParameters(toolbarTitle, title, box, address)
                            }
                        )
                    )
                }
                ReceptionScanNavAction.NavigateToBoxes -> findNavController().navigate(
                    ReceptionFragmentDirections.actionReceptionFragmentToReceptionBoxesFragment())
                ReceptionScanNavAction.NavigateToFlightDeliveries -> findNavController().navigate(
                    ReceptionFragmentDirections.actionReceptionFragmentToFlightDeliveriesFragment())
                ReceptionScanNavAction.NavigateToBack -> findNavController().popBackStack()
            }
        }

        viewModel.navigationEvent.observe(viewLifecycleOwner, navigationObserver)

        viewModel.toastEvent.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ReceptionScanToastState.BoxAdded -> showToastBoxAdded(state.message)
                is ReceptionScanToastState.BoxHasBeenAdded -> showToastBoxHasBeenAdded(state.message)
            }
        }

        viewModel.beepEvent.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ReceptionScanBeepState.BoxAdded -> beepAdded()
                is ReceptionScanBeepState.BoxSkipAdded -> beepSkip()
            }
        }

        viewModel.bottomProgressEvent.observe(viewLifecycleOwner) { progress ->
            binding.completeButton.setState(
                if (progress) ProgressImageButtonMode.PROGRESS else ProgressImageButtonMode.ENABLED)
        }

        viewModel.boxStateUI.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ReceptionScanBoxState.BoxAdded -> {
                    binding.received.setCountBox(state.accepted,
                        ReceptionAcceptedMode.CONTAINS_COMPLETE)
                    binding.parking.setParkingNumber(state.gate,
                        ReceptionParkingMode.CONTAINS_COMPLETE)
                    binding.info.setCodeBox(state.barcode, ReceptionInfoMode.SUBMERGE)
                }
                is ReceptionScanBoxState.BoxDeny -> {
                    binding.received.setCountBox(state.accepted,
                        ReceptionAcceptedMode.CONTAINS_DENY)
                    binding.parking.setParkingNumber(state.gate,
                        ReceptionParkingMode.CONTAINS_DENY)
                    binding.info.setCodeBox(state.barcode, ReceptionInfoMode.RETURN)
                }
                ReceptionScanBoxState.Empty -> {
                    binding.received.setState(ReceptionAcceptedMode.EMPTY)
                    binding.parking.setParkingNumber(ReceptionParkingMode.EMPTY)
                    binding.info.setCodeBox(ReceptionInfoMode.EMPTY)
                }
                is ReceptionScanBoxState.BoxHasBeenAdded -> {
                    binding.received.setCountBox(state.accepted,
                        ReceptionAcceptedMode.CONTAINS_HAS_ADDED)
                    binding.parking.setParkingNumber(state.gate,
                        ReceptionParkingMode.CONTAINS_COMPLETE)
                    binding.info.setCodeBox(state.barcode, ReceptionInfoMode.SUBMERGE)
                }
                is ReceptionScanBoxState.BoxInit -> {
                    binding.received.setCountBox(state.accepted,
                        ReceptionAcceptedMode.CONTAINS_COMPLETE)
                    binding.parking.setParkingNumber(state.gate,
                        ReceptionParkingMode.CONTAINS_COMPLETE)
                    binding.info.setCodeBox(state.barcode, ReceptionInfoMode.SUBMERGE)
                }
            }
        }
    }

    private fun showToastBoxAdded(message: String) {
        val container: ViewGroup? = activity?.findViewById(R.id.custom_toast_container)
        val layout: ViewGroup =
            layoutInflater.inflate(R.layout.reception_added_box_toast, container) as ViewGroup
        val text: TextView = layout.findViewById(R.id.text)
        text.text = message
        with(Toast(context)) {
            setGravity(Gravity.TOP or Gravity.CENTER, 0, 200)
            duration = Toast.LENGTH_LONG
            view = layout
            show()
        }
    }

    private fun showToastBoxHasBeenAdded(message: String) {
        val container: ViewGroup? = activity?.findViewById(R.id.custom_toast_container)
        val layout: ViewGroup =
            layoutInflater.inflate(R.layout.reception_has_been_added_box_toast,
                container) as ViewGroup
        val text: TextView = layout.findViewById(R.id.text)
        text.text = message
        with(Toast(context)) {
            setGravity(Gravity.TOP or Gravity.CENTER, 0, 200)
            duration = Toast.LENGTH_LONG
            view = layout
            show()
        }
    }

    private fun initListener() {
        binding.manualInputButton.setOnClickListener {
            // TODO: 22.04.2021 заменить на вызовы
            viewModel.onStopScanner()
            showHandleInput()
        }

        binding.completeButton.setOnClickListener {
            viewModel.onCompleteClicked()
        }

        binding.received.setOnClickListener {
            viewModel.onListClicked()
        }

    }

    private fun showHandleInput() {
        val receptionHandleFragment = ReceptionHandleFragment.newInstance()
        receptionHandleFragment.setTargetFragment(this, REQUEST_HANDLE_CODE)
        receptionHandleFragment.show(parentFragmentManager, REQUEST_HANDLE_TAG)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // TODO: 22.04.2021 заменить на вызовы
        viewModel.onStartScanner()

        if (resultCode == RESULT_OK)
            resultScanner(requestCode, data)
    }

    private fun resultScanner(requestCode: Int, data: Intent?) {
        if (requestCode == REQUEST_HANDLE_CODE) {
            data?.apply {
                val barcode = data.getStringExtra(HANDLE_BARCODE_RESULT)
                if (data.hasExtra(HANDLE_BARCODE_RESULT) && barcode != null) {
                    viewModel.onBoxHandleInput(barcode)
                }
            }
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
        const val REQUEST_HANDLE_CODE = 100
        const val REQUEST_HANDLE_TAG = "request_handle_tag"
    }

}