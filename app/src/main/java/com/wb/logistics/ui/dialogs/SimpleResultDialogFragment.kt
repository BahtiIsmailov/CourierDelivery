package com.wb.logistics.ui.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.wb.logistics.R
import com.wb.logistics.app.AppExtras

class SimpleResultDialogFragment : DialogFragment() {

    private lateinit var title: String
    private lateinit var description: String
    private lateinit var positiveButtonName: String
    private lateinit var negativeButtonName: String

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
            negativeButtonName =
                arguments.getString(AppExtras.EXTRA_DIALOG_NEGATIVE_BUTTON_TITLE, "")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(title)
            .setMessage(description)
            .setCancelable(false)
            .setPositiveButton(positiveButtonName) { dialog: DialogInterface, _: Int ->
                dialog.cancel()
                onResult(Activity.RESULT_OK)
            }
            .setNegativeButton(negativeButtonName) { dialog: DialogInterface, _: Int ->
                dialog.cancel()
                onResult(Activity.RESULT_CANCELED)
            }
        return builder.create()
    }

    private fun onResult(resultCode: Int) {
        val targetFragment = targetFragment
        targetFragment?.onActivityResult(targetRequestCode, resultCode, null)
    }

    override fun onStart() {
        super.onStart()
        context?.let {
            (dialog as AlertDialog)
                .getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(positiveTextColorButton)
            (dialog as AlertDialog)
                .getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(negativeTextColorButton)
        }
    }

    private val positiveTextColorButton: Int
        get() = ContextCompat.getColor(requireContext(), R.color.danger)
    private val negativeTextColorButton: Int
        get() = ContextCompat.getColor(requireContext(), R.color.primary)

    companion object {
        fun newInstance(
            title: String,
            description: String,
            positiveButtonName: String,
            negativeButtonName: String,
        ): SimpleResultDialogFragment {
            val args = Bundle()
            args.putString(AppExtras.EXTRA_DIALOG_TITLE, title)
            args.putString(AppExtras.EXTRA_DIALOG_DESCRIPTION, description)
            args.putString(AppExtras.EXTRA_DIALOG_POSITIVE_BUTTON_TITLE, positiveButtonName)
            args.putString(AppExtras.EXTRA_DIALOG_NEGATIVE_BUTTON_TITLE, negativeButtonName)
            val fragment = SimpleResultDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

}