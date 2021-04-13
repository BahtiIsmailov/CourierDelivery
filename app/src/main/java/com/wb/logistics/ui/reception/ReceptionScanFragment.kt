package com.wb.logistics.ui.reception

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.Handler
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.zxing.Result
import com.wb.logistics.R
import com.wb.logistics.databinding.ReceptionScanFragmentBinding
import com.wb.logistics.ui.reception.ReceptionHandleFragment.Companion.HANDLE_INPUT_RESULT
import com.wb.logistics.utils.LogUtils
import com.wb.logistics.views.ProgressImageButtonMode
import com.wb.logistics.views.ReceptionAcceptedMode
import com.wb.logistics.views.ReceptionInfoMode
import com.wb.logistics.views.ReceptionParkingMode
import me.dm7.barcodescanner.core.IViewFinder
import me.dm7.barcodescanner.core.ViewFinderView
import me.dm7.barcodescanner.zxing.ZXingScannerView
import org.koin.androidx.viewmodel.ext.android.viewModel


class ReceptionFragment : Fragment(), ZXingScannerView.ResultHandler {

    private val viewModel by viewModel<ReceptionScanViewModel>()

    private var _binding: ReceptionScanFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var scannerView: ZXingScannerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = ReceptionScanFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPermission()
        initScanner()
        initListener()
        initObserver()
    }

    private fun initPermission() {
        if (!hasPermissions(Manifest.permission.CAMERA)) {
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                PERMISSIONS_REQUEST_CAMERA_NO_ACTION
            )
        }
    }

    private fun initScanner() {
        scannerView = object : ZXingScannerView(context) {
            override fun createViewFinderView(context: Context): IViewFinder {
                return CustomViewFinderView(context)
            }
        }
        scannerView.setBorderColor(Color.WHITE)
        scannerView.setLaserEnabled(true)
        scannerView.setIsBorderCornerRounded(true)
        scannerView.setBorderCornerRadius(10)
        scannerView.setBorderAlpha(0.5F)
        binding.scannerLayout.addView(scannerView)
    }

    private fun initObserver() {
        val eventObserver = Observer<ReceptionScanNavigationEvent> { state ->
            when (state) {
                is ReceptionScanNavigationEvent.NavigateToReceptionBoxNotBelong -> {
                    findNavController().navigate(
                        ReceptionFragmentDirections.actionReceptionFragmentToReceptionBoxNotBelongFragment(
                            ReceptionBoxNotBelongParameters(
                                state.toolbarTitle,
                                state.title,
                                state.box,
                                state.address)
                        )
                    )
                }
                ReceptionScanNavigationEvent.NavigateToBoxes -> findNavController().navigate(
                    ReceptionFragmentDirections.actionReceptionFragmentToReceptionBoxesFragment())
            }
        }

        viewModel.navigationEvent.observe(viewLifecycleOwner, eventObserver)

        viewModel.toastEvent.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ReceptionScanToastState.BoxAdded -> showToastBoxAdded(state.message)
                is ReceptionScanToastState.BoxHasBeenAdded -> showToastBoxHasBeenAdded(state.message)
            }
        }

        viewModel.beepEvent.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ReceptionScanBeepState.BoxAdded -> {
                }
                is ReceptionScanBeepState.BoxSkipAdded -> beepSkip()
            }
        }

        viewModel.bottomNavigationEvent.observe(viewLifecycleOwner) { state ->
            binding.completeButton.setState(
                if (state) ProgressImageButtonMode.ENABLED else ProgressImageButtonMode.DISABLED)
        }

        viewModel.boxStateUI.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ReceptionScanBoxUIState.BoxAdded -> {
                    binding.received.setCountBox(state.accepted,
                        ReceptionAcceptedMode.CONTAINS_COMPLETE)
                    binding.parking.setParkingNumber(state.gate,
                        ReceptionParkingMode.CONTAINS_COMPLETE)
                    binding.info.setCodeBox(state.barcode, ReceptionInfoMode.SUBMERGE)
                }
                is ReceptionScanBoxUIState.BoxDeny -> {
                    binding.received.setCountBox(state.accepted,
                        ReceptionAcceptedMode.CONTAINS_DENY)
                    binding.parking.setParkingNumber(state.gate,
                        ReceptionParkingMode.CONTAINS_DENY)
                    binding.info.setCodeBox(state.barcode, ReceptionInfoMode.RETURN)
                }
                ReceptionScanBoxUIState.Empty -> {
                    binding.received.setState(ReceptionAcceptedMode.EMPTY)
                    binding.parking.setParkingNumber(ReceptionParkingMode.EMPTY)
                    binding.info.setCodeBox(ReceptionInfoMode.EMPTY)
                }
                is ReceptionScanBoxUIState.BoxHasBeenAdded -> {
                    binding.received.setCountBox(state.accepted,
                        ReceptionAcceptedMode.CONTAINS_HAS_ADDED)
                    binding.parking.setParkingNumber(state.gate,
                        ReceptionParkingMode.CONTAINS_COMPLETE)
                    binding.info.setCodeBox(state.barcode, ReceptionInfoMode.SUBMERGE)
                }
                is ReceptionScanBoxUIState.BoxInit -> {
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
            stoopScanner()
            showHandleInput()
        }

        binding.received.setOnClickListener {
            viewModel.onListClicked()
        }
    }

    private fun showHandleInput() {
        val receptionHandleFragment = ReceptionHandleFragment.newInstance()
        receptionHandleFragment.setTargetFragment(this, REQUEST_HANDLE_CODE)
        receptionHandleFragment.show(parentFragmentManager, "add_reception_handle_fragment")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        startScanner()

        if (resultCode == RESULT_OK)
            resultScannerCardNumber(requestCode, data)
    }

    private fun resultScannerCardNumber(requestCode: Int, data: Intent?) {
        if (requestCode == REQUEST_HANDLE_CODE) {
            data?.apply {
                val result = data.getStringExtra(HANDLE_INPUT_RESULT)
                if (data.hasExtra(HANDLE_INPUT_RESULT) && result != null) {
                    viewModel.onBoxHandleInput(result)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun handleResult(rawResult: Result?) {
        val code = rawResult?.text ?: return
        viewModel.onBoxScanned(code)
        LogUtils { logDebugApp("Cod $code") }
        beepComplete()
        val handler = Handler()
        handler.postDelayed({ scannerView.resumeCameraPreview(this) },
            2000)
    }

    private fun beepComplete() {
        val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
    }

    private fun beepSkip() {
        val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_CALL_SIGNAL_ISDN_NORMAL, 200)
    }

    override fun onStart() {
        super.onStart()
        startScanner()
    }

    private fun startScanner() {
        scannerView.setResultHandler(this)
        scannerView.startCamera()
    }

    override fun onStop() {
        super.onStop()
        stoopScanner()
    }

    private fun stoopScanner() {
        scannerView.stopCamera()
    }

    companion object {
        const val PERMISSIONS_REQUEST_CAMERA_NO_ACTION = 1
        const val REQUEST_HANDLE_CODE = 100
    }


    private class CustomViewFinderView : ViewFinderView {
        val paint = Paint()

        constructor(context: Context?) : super(context) {
            init()
        }

        constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
            init()
        }

        private fun init() {
            paint.color = Color.WHITE
            paint.isAntiAlias = true
            val textPixelSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                TRADE_MARK_TEXT_SIZE_SP.toFloat(), resources.displayMetrics)
            paint.textSize = textPixelSize
//            setSquareViewFinder(true)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            drawTradeMark(canvas)
        }

        private fun drawTradeMark(canvas: Canvas) {
            val framingRect = framingRect
            val tradeMarkTop: Float
            val tradeMarkLeft: Float
            if (framingRect != null) {
                tradeMarkTop = framingRect.bottom + paint.textSize + 10
                tradeMarkLeft = framingRect.left.toFloat()
            } else {
                tradeMarkTop = 10f
                tradeMarkLeft = canvas.height - paint.textSize - 10
            }
            canvas.drawText(TRADE_MARK_TEXT, tradeMarkLeft, tradeMarkTop, paint)
        }

        companion object {
            const val TRADE_MARK_TEXT = "WB"
            const val TRADE_MARK_TEXT_SIZE_SP = 40
        }
    }

}

fun Fragment.hasPermissions(vararg permissions: String): Boolean = permissions.all(::hasPermission)

fun Fragment.hasPermission(permission: String): Boolean {
    return ActivityCompat.checkSelfPermission(
        requireContext(),
        permission
    ) == PackageManager.PERMISSION_GRANTED
}