package com.wb.logistics.ui.reception

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.Result
import com.wb.logistics.R
import com.wb.logistics.databinding.ReceptionFragmentBinding
import com.wb.logistics.utils.LogUtils
import com.wb.logistics.views.ReceptionInfoMode
import me.dm7.barcodescanner.zxing.ZXingScannerView
import org.koin.androidx.viewmodel.ext.android.viewModel


class ReceptionFragment : Fragment(), ZXingScannerView.ResultHandler {

    private val viewModel by viewModel<ReceptionViewModel>()

    private var _binding: ReceptionFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ReceptionFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!hasPermissions(Manifest.permission.CAMERA)) {
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                PERMISSIONS_REQUEST_CAMERA_NO_ACTION
            )
        }
        //binding.scannerView.setFormats(listOf(BarcodeFormat.CODE_128))
        binding.scannerView.setResultHandler(this)
        binding.scannerView.background = AppCompatResources.getDrawable(
            requireContext(),
            R.drawable.reception_subtract
        )
//        binding.scannerView.setAspectTolerance(0.5f)

//        binding.addBoxButton.setOnClickListener {
//            findNavController().navigate(ScannerFragment2Directions.actionScannerFragment2ToAddBoxDialogFragment())
//        }
//        binding.loadingBoxesButton.setOnClickListener {
//            findNavController().navigate(ScannerFragment2Directions.actionScannerFragment2ToLoadingBoxesFragment())
//        }
//        viewModel.state.observe(viewLifecycleOwner) {
//            binding.currentBoxTextView.text = it.currentBox
//            binding.scannedBoxCountTextView.text = it.scannedBoxCount
//            binding.currentBoxDeliveryAddressTextView.text = it.currentBoxDestination
//        }
        viewModel.codeBox.observe(viewLifecycleOwner) {
            binding.info.setCodeBox(it, ReceptionInfoMode.SUBMERGE)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun handleResult(rawResult: Result?) {
        val code = rawResult?.text ?: return
        viewModel.onBoxScanned(code)
        LogUtils { logDebugApp("Cod $code") }

        beep()

        Snackbar.make(binding.scannerView, "Cod $code", Snackbar.LENGTH_LONG).show()
        binding.scannerView.resumeCameraPreview(this)
    }

    private fun beep() {
        val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
    }

    override fun onStart() {
        super.onStart()
        binding.scannerView.startCamera()
    }

    override fun onStop() {
        super.onStop()
        binding.scannerView.stopCamera()
    }

    companion object {
        const val PERMISSIONS_REQUEST_CAMERA_NO_ACTION = 1
    }

}

fun Fragment.hasPermissions(vararg permissions: String): Boolean = permissions.all(::hasPermission)

fun Fragment.hasPermission(permission: String): Boolean {
    return ActivityCompat.checkSelfPermission(
        requireContext(),
        permission
    ) == PackageManager.PERMISSION_GRANTED
}