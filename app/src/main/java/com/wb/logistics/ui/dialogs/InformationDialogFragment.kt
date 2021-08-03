package com.wb.logistics.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.wb.logistics.R
import com.wb.logistics.app.AppExtras

class InformationDialogFragment : DialogFragment() {

    private lateinit var title: String
    private lateinit var description: String
    private lateinit var positiveButtonName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        readArguments(arguments)
    }

    private fun readArguments(arguments: Bundle?) {
        if (arguments != null) {
            title = arguments.getString(AppExtras.EXTRA_DIALOG_TITLE, "")
            description = arguments.getString(AppExtras.EXTRA_DIALOG_DESCRIPTION, "")
            positiveButtonName =
                arguments.getString(AppExtras.EXTRA_DIALOG_POSITIVE_BUTTON_TITLE, "")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(title)
            .setMessage(description)
            .setCancelable(true)
            .setPositiveButton(positiveButtonName) { dialog, _ ->
                setFragmentResult(INFO_DIALOG_RESULT, bundleOf(INFO_DIALOG_CANCEL_KEY to ""))
                dialog.cancel()
            }

        dialog?.setOnCancelListener {
            setFragmentResult(INFO_DIALOG_RESULT, bundleOf(INFO_DIALOG_CANCEL_KEY to ""))
        }
        dialog?.setOnDismissListener {
            setFragmentResult(INFO_DIALOG_RESULT, bundleOf(INFO_DIALOG_CANCEL_KEY to ""))
        }
        return builder.create()
    }

    companion object {
        fun newInstance(
            title: String,
            description: String,
            positiveButtonName: String,
        ): InformationDialogFragment {
            val args = Bundle()
            args.putString(AppExtras.EXTRA_DIALOG_TITLE, title)
            args.putString(AppExtras.EXTRA_DIALOG_DESCRIPTION, description)
            args.putString(AppExtras.EXTRA_DIALOG_POSITIVE_BUTTON_TITLE, positiveButtonName)
            val fragment = InformationDialogFragment()
            fragment.arguments = args
            return fragment
        }

        const val INFO_DIALOG_RESULT = "INFO_DIALOG_RESULT"
        const val INFO_DIALOG_CANCEL_KEY = "INFO_DIALOG_CANCEL_KEY"
    }

    override fun onStart() {
        super.onStart()
        context?.let {
            (dialog as AlertDialog)
                .getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(it, R.color.button_text))
        }
    }

}