package ru.wb.go.ui.scanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.wb.go.R
import ru.wb.go.app.AppConsts
import ru.wb.go.databinding.CourierScannerFragmentBinding
import ru.wb.go.ui.scanner.domain.ScannerState
import ru.wb.go.utils.hasPermission

open class CourierScannerFragment : Fragment() {

    private val viewModel by viewModel<CourierScannerViewModel>()

    private var _binding: CourierScannerFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var barcodeView: DecoratedBarcodeView
    private lateinit var viewFinder: ViewfinderView

    private lateinit var beepManager: BeepManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        beepManager = BeepManager(activity)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {

        _binding = CourierScannerFragmentBinding.inflate(inflater, container, false)
        barcodeView = binding.zxingBarcodeScanner
        viewFinder = binding.zxingViewfinderView

        val switchFlashlightButton = binding.flash

        if (!hasFlash()) {
            switchFlashlightButton.visibility = GONE
        }

        val formats: Collection<BarcodeFormat?> = listOf(BarcodeFormat.QR_CODE)
        barcodeView.barcodeView.decoderFactory = DefaultDecoderFactory(formats)
        barcodeView.statusView.text = ""

        barcodeView.barcodeView.decodeSingle(callback)

        changeLaserVisibility(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
        initObserver()
        initPermission()
    }

    private fun initPermission() {
        if (hasPermission(Manifest.permission.CAMERA)) {
            binding.permissionInfo.visibility = GONE
            binding.requestPermission.visibility = GONE
        } else {
            requestPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                binding.permissionInfo.visibility = GONE
                binding.requestPermission.visibility = GONE
                binding.requestPermissionSetting.visibility = GONE
                onResume()
            } else {
                binding.permissionInfo.visibility = View.VISIBLE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (requireActivity().shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                        binding.requestPermissionSetting.visibility = GONE
                        binding.requestPermission.visibility = View.VISIBLE
                    } else {
                        binding.requestPermissionSetting.visibility = View.VISIBLE
                        binding.requestPermission.visibility = GONE
                    }
                }
                onPause()
            }
        }

    private val callback: BarcodeCallback = object : BarcodeCallback {
        override fun barcodeResult(scanResult: BarcodeResult) {
            beepManager.playBeepSoundAndVibrate()
            stopScanning()
            viewModel.onBarcodeScanned(scanResult.text)
        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
    }

    override fun onResume() {
        super.onResume()
        binding.scanStatus.visibility = GONE
        barcodeView.resume()
    }

    override fun onStop() {
        barcodeView.pauseAndWait()
        super.onStop()
    }

    private fun startScanning() {
        changeLaserVisibility(true)
        onResume()
        barcodeView.barcodeView.decodeSingle(callback)
    }

    private fun hasFlash(): Boolean {
        return activity?.applicationContext?.packageManager!!.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    }

    private fun changeLaserVisibility(visible: Boolean) {
        requireActivity().runOnUiThread {
            viewFinder.setLaserVisibility(visible)
        }

    }

    private fun initObserver() {
        viewModel.scannerAction.observe(viewLifecycleOwner) {
            when (it) {
                ScannerState.Start -> startScanning()
                ScannerState.StopScan -> stopScanning()

                ScannerState.HoldScanComplete -> hold(R.drawable.ic_scan_complete)
                ScannerState.HoldScanError -> hold(R.drawable.ic_scan_error)
                ScannerState.HoldScanUnknown -> hold(R.drawable.ic_scan_unknown)
            }
        }

        viewModel.flashState.observe(viewLifecycleOwner){
            if(it){
                barcodeView.setTorchOn()
            }else{
                barcodeView.setTorchOff()
            }
        }
    }

    private fun stopScanning(){
        changeLaserVisibility(false)
        barcodeView.pauseAndWait()
    }

    private fun hold(icon: Int) {
        binding.scanStatus.setImageDrawable(ContextCompat.getDrawable(requireContext(), icon))
        binding.scanStatus.visibility = View.VISIBLE
    }

    private fun initListener() {
        binding.flash.setOnClickListener { viewModel.switchFlashlight() }

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

    companion object {
        const val PERMISSION_FROM_SETTING_REQUEST_CODE = 502
    }

}