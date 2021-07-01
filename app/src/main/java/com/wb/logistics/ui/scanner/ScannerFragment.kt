package com.wb.logistics.ui.scanner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.zxing.Result
import com.wb.logistics.databinding.ScannerFragmentBinding
import com.wb.logistics.ui.scanner.domain.ScannerAction
import com.wb.logistics.utils.LogUtils
import me.dm7.barcodescanner.core.IViewFinder
import me.dm7.barcodescanner.core.ViewFinderView
import me.dm7.barcodescanner.zxing.ZXingScannerView
import org.koin.androidx.viewmodel.ext.android.viewModel


class ScannerFragment : Fragment(), ZXingScannerView.ResultHandler {

    private val viewModel by viewModel<ScannerViewModel>()

    private var _binding: ScannerFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var scannerView: ZXingScannerZoomView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = ScannerFragmentBinding.inflate(inflater, container, false)
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
        if (hasPermissions(Manifest.permission.CAMERA)) {
            binding.permissionInfo.visibility = View.GONE
        } else {
            binding.permissionInfo.visibility = View.VISIBLE
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA), PERMISSIONS_REQUEST_CAMERA_NO_ACTION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CAMERA_NO_ACTION
            && grantResults.isNotEmpty()
            && grantResults.first() == PackageManager.PERMISSION_GRANTED
        ) {
            binding.permissionInfo.visibility = View.GONE
            startScanner()
        }
    }

    private fun initScanner() {
        scannerView = object : ZXingScannerZoomView(context) {
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
        viewModel.scannerAction.observe(viewLifecycleOwner) {
            when (it) {
                ScannerAction.Start -> startScanner()
                ScannerAction.Stop -> stopScanner()
            }
        }
    }

    private fun initListener() {
        binding.sun.setOnClickListener { scannerView.toggleFlash() }
    }

    var onSeekBarChangeListener: OnSeekBarChangeListener = object : OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            scannerView.zoom(progress)
        }
        override fun onStartTrackingTouch(seekBar: SeekBar) {}
        override fun onStopTrackingTouch(seekBar: SeekBar) {}
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun handleResult(rawResult: Result?) {
        val barcode = rawResult?.text ?: return
        viewModel.onBarcodeScanned(barcode)
        LogUtils { logDebugApp("Barcode scan: $barcode") }
        holdScanner()
    }

    private fun holdScanner() {
        Handler(Looper.getMainLooper()).postDelayed({
            scannerView.resumeCameraPreview(this)
        }, HOLD_SCANNER)
    }

    override fun onStart() {
        super.onStart()
        startScanner()
    }

    private fun startScanner() {
        scannerView.setResultHandler(this)
        scannerView.startCamera()
        scannerView.setCameraCompleteListener {
            binding.zoom.max = scannerView.maxZoom
            binding.zoom.setOnSeekBarChangeListener(onSeekBarChangeListener)
        }
    }

    override fun onStop() {
        super.onStop()
        stopScanner()
    }

    private fun stopScanner() {
        scannerView.stopCamera()
    }

    companion object {
        const val PERMISSIONS_REQUEST_CAMERA_NO_ACTION = 1
        const val HOLD_SCANNER = 1000L
    }

    private class CustomViewFinderView : ViewFinderView {
        //val paint = Paint()

        constructor(context: Context?) : super(context) {
            init()
        }

        constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
            init()
        }

        private fun init() {
//            paint.color = Color.WHITE
//            paint.isAntiAlias = true
//            val textPixelSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
//                TRADE_MARK_TEXT_SIZE_SP.toFloat(), resources.displayMetrics)
//            paint.textSize = textPixelSize
//            setSquareViewFinder(true)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            //drawTradeMark(canvas)
        }

//        private fun drawTradeMark(canvas: Canvas) {
//            val framingRect = framingRect
//            val tradeMarkTop: Float
//            val tradeMarkLeft: Float
//            if (framingRect != null) {
//                tradeMarkTop = framingRect.bottom + paint.textSize + 10
//                tradeMarkLeft = framingRect.left.toFloat()
//            } else {
//                tradeMarkTop = 10f
//                tradeMarkLeft = canvas.height - paint.textSize - 10
//            }
//            //canvas.drawText(TRADE_MARK_TEXT, tradeMarkLeft, tradeMarkTop, paint)
//        }

        companion object {
//            const val TRADE_MARK_TEXT = "WB"
//            const val TRADE_MARK_TEXT_SIZE_SP = 40
        }
    }

}

fun Fragment.hasPermissions(vararg permissions: String): Boolean =
    permissions.all(::hasPermission)

fun Fragment.hasPermission(permission: String): Boolean {
    return ActivityCompat.checkSelfPermission(
        requireContext(),
        permission
    ) == PackageManager.PERMISSION_GRANTED
}