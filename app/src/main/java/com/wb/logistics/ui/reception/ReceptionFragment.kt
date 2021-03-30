package com.wb.logistics.ui.reception

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
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
import com.wb.logistics.ui.reception.ReceptionHandleFragment.Companion.HANDLE_INPUT_RESULT
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

        binding.manualInputButton.setOnClickListener {
            val receptionHandleFragment = ReceptionHandleFragment.newInstance()
            receptionHandleFragment.setTargetFragment(this, REQUEST_HANDLE_CODE)
            receptionHandleFragment.show(parentFragmentManager, "add_reception_handle_fragment")
        }

        viewModel.codeBox.observe(viewLifecycleOwner) {
            binding.info.setCodeBox(it, ReceptionInfoMode.SUBMERGE)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
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
        const val REQUEST_HANDLE_CODE = 100
    }

}

fun Fragment.hasPermissions(vararg permissions: String): Boolean = permissions.all(::hasPermission)

fun Fragment.hasPermission(permission: String): Boolean {
    return ActivityCompat.checkSelfPermission(
        requireContext(),
        permission
    ) == PackageManager.PERMISSION_GRANTED
}