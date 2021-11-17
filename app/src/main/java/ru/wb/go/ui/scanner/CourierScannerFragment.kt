package ru.wb.go.ui.scanner

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.go.R
import ru.wb.go.app.AppConsts
import ru.wb.go.databinding.CourierScannerFragmentBinding
import ru.wb.go.ui.scanner.domain.ScannerState
import ru.wb.go.utils.LogUtils


open class CourierScannerFragment : Fragment(), ZXingScannerView.ResultHandler {

    var holdScanner = HOLD_SCANNER

    private val viewModel by viewModel<CourierScannerViewModel>()

    private var _binding: CourierScannerFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var scannerView: ZXingScannerZoomView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = CourierScannerFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initScanner()
        initListener()
        initObserver()
        initPermission()
    }

    private fun initPermission() {
        if (hasPermission(Manifest.permission.CAMERA)) {
            binding.permissionInfo.visibility = View.GONE
            binding.requestPermission.visibility = View.GONE
        } else {
            requestPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                binding.permissionInfo.visibility = View.GONE
                binding.requestPermission.visibility = View.GONE
                binding.requestPermissionSetting.visibility = View.GONE
                startScanner()
            } else {
                binding.permissionInfo.visibility = View.VISIBLE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (requireActivity().shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                        binding.requestPermissionSetting.visibility = View.GONE
                        binding.requestPermission.visibility = View.VISIBLE
                    } else {
                        binding.requestPermissionSetting.visibility = View.VISIBLE
                        binding.requestPermission.visibility = View.GONE
                    }
                }
                stopScanner()
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
                ScannerState.Start -> startScanner()
                ScannerState.Stop -> stopScanner()
                ScannerState.LoaderProgress -> loader()
                ScannerState.LoaderComplete -> loaderComplete()
                ScannerState.BeepScan -> beepScan()
            }
        }
    }

    private fun loader() {
        scannerView.setLaserEnabled(false)
        scannerView.setFlashLoaderEnabled()
        binding.loaderProgress.visibility = View.VISIBLE
        binding.loader.visibility = View.VISIBLE
        stopLoaderScanner()
    }

    private fun loaderComplete() {
        scannerView.setLaserEnabled(true)
        scannerView.setFlashLoaderComplete()
        binding.loaderProgress.visibility = View.GONE
        binding.loader.visibility = View.GONE
        startScanner()
    }

    private fun initListener() {
        binding.sun.setOnClickListener { scannerView.toggleFlash() }
        binding.requestPermission.setOnClickListener {
            requestPermission.launch(Manifest.permission.CAMERA)
        }

        binding.requestPermissionSetting.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts(AppConsts.APP_PACKAGE, requireContext().packageName, null)
            intent.data = uri
            startActivityForResult(intent, PERMISSION_FROM_SETTING_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PERMISSION_FROM_SETTING_REQUEST_CODE) {
            requestPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun vibrate() {
        val vibrator = getSystemService(requireContext(), Vibrator::class.java) as Vibrator
        vibrator.let {
            if (Build.VERSION.SDK_INT >= 26) {
                it.vibrate(
                    VibrationEffect.createOneShot(
                        VIBRATE_MS,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(VIBRATE_MS)
            }
        }
    }

    override fun handleResult(rawResult: Result?) {
        val barcode = rawResult?.text ?: return
        scanResult(barcode)
        vibrate()
        holdScanner()
    }

    private fun scanResult(barcode: String) {
        viewModel.onBarcodeScanned(barcode)
    }

    // TODO: 20.09.2021 передать управаление в интерактор менеджера
    private fun holdScanner() {
        Handler(Looper.getMainLooper()).postDelayed({
            scannerView.resumeCameraPreview(this)
//            viewModel.onScannerReady()
        }, holdScanner)
    }

    private fun startScanner() {
        LogUtils { logDebugApp("Scanner startScanner()") }
        scannerView.setResultHandler(this)

        if (hasPermission(Manifest.permission.CAMERA)) {
            scannerView.startCamera()
        }
    }

    private fun stopLoaderScanner() {
        LogUtils { logDebugApp("Scanner stopLoaderScanner()") }
        scannerView.stopCamera()
    }

    private fun stopScanner() {
        LogUtils { logDebugApp("Scanner stopScanner()") }
        scannerView.stopCamera()
    }

    private fun beepScan() {
        val mediaPlayer = MediaPlayer.create(
            context,
            R.raw.qr_box_scan_accepted
        )
        mediaPlayer.start()
    }

    companion object {
        const val HOLD_SCANNER = 1000L
        const val VIBRATE_MS = 100L
        const val PERMISSION_FROM_SETTING_REQUEST_CODE = 502
    }

}