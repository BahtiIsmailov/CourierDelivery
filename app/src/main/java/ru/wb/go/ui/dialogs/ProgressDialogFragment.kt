package ru.wb.go.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import ru.wb.go.R
import ru.wb.go.ui.courierwarehouses.CourierWarehousesFragment

class ProgressDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomProgressAlertDialog)
        val dialogView =
            requireActivity().layoutInflater.inflate(R.layout.custom_progress_layout_dialog, null)
        builder.setView(dialogView)
        val progressDialog = builder.create()
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                progressDialog.dismiss()
                setFragmentResult(
                    PROGRESS_DIALOG_RESULT,
                    bundleOf(PROGRESS_DIALOG_BACK_KEY to PROGRESS_DIALOG_BACK_VALUE)
                )
            }
            true
        }
        return progressDialog
    }

    companion object {
        fun newInstance(): ProgressDialogFragment {
            return ProgressDialogFragment()
        }

        const val PROGRESS_DIALOG_RESULT = "PROGRESS_DIALOG_RESULT"
        const val PROGRESS_DIALOG_BACK_KEY = "PROGRESS_DIALOG_BACK_KEY"
        const val PROGRESS_DIALOG_BACK_VALUE = 1000
        const val PROGRESS_DIALOG_TAG = "PROGRESS_DIALOG_TAG"
    }

}