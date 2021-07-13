package com.wb.logistics.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.google.zxing.Result
import com.wb.logistics.databinding.ScannerFragmentBinding
import com.wb.logistics.ui.scanner.domain.ScannerAction
import com.wb.logistics.utils.LogUtils
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
            binding.requestPermission.visibility = View.GONE
        } else {
            binding.permissionInfo.visibility = View.VISIBLE
            binding.requestPermission.visibility = View.VISIBLE
            requestPermission()
        }
    }

    private fun requestPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.CAMERA), PERMISSIONS_REQUEST_CAMERA_NO_ACTION
        )
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
            binding.requestPermission.visibility = View.GONE
            startScanner()
        }
    }

    private fun initScanner() {
        scannerView = ZXingScannerZoomView(context)
        scannerView.setBorderColor(Color.WHITE)
        scannerView.setLaserEnabled(true)
        scannerView.setIsBorderCornerRounded(true)
        scannerView.setBorderCornerRadius(40)
        scannerView.setBorderAlpha(0.5F)
        scannerView.setSquareViewFinder(false)
        binding.scannerLayout.addView(scannerView)
    }

    private fun initObserver() {
        viewModel.scannerAction.observe(viewLifecycleOwner) {
            when (it) {
                ScannerAction.Start -> startScanner()
                ScannerAction.Stop -> stopScanner()
                ScannerAction.LoaderProgress -> loader()
                ScannerAction.LoaderComplete -> loaderComplete()
            }
        }
    }

    private fun loader() {
        scannerView.flashLoader()
        binding.loaderProgress.visibility = View.VISIBLE
        binding.loader.visibility = View.VISIBLE
    }

    private fun loaderComplete() {
        scannerView.flashLoaderComplete()
        binding.loaderProgress.visibility = View.GONE
        binding.loader.visibility = View.GONE
    }


    private fun initListener() {
        binding.sun.setOnClickListener { scannerView.toggleFlash() }
        binding.requestPermission.setOnClickListener { requestPermission() }
    }

    private fun vibrate() {
        val vibrator = getSystemService(requireContext(), Vibrator::class.java) as Vibrator
        vibrator.let {
            if (Build.VERSION.SDK_INT >= 26) {
                it.vibrate(VibrationEffect.createOneShot(VIBRATE_MS,
                    VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(VIBRATE_MS)
            }
        }
    }

    private var onSeekBarChangeListener: OnSeekBarChangeListener =
        object : OnSeekBarChangeListener {
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
        scanResult(barcode)
        vibrate()
        holdScanner()
    }

    private fun scanResult(barcode: String) {
        viewModel.onBarcodeScanned(barcode)
        LogUtils { logDebugApp("Barcode scan: $barcode") }
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
//        scannerView.setCameraCompleteListener {
//            binding.zoom.max = scannerView.maxZoom
//            binding.zoom.setOnSeekBarChangeListener(onSeekBarChangeListener)
//        }
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
        const val VIBRATE_MS = 100L
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