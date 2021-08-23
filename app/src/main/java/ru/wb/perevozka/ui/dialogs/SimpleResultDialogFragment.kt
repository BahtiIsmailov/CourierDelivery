package ru.wb.perevozka.ui.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import ru.wb.perevozka.R
import ru.wb.perevozka.app.AppExtras
import kotlin.properties.Delegates


class SimpleResultDialogFragment : DialogFragment() {

    private lateinit var title: String
    private lateinit var description: String
    private lateinit var positiveButtonName: String
    private lateinit var negativeButtonName: String

    private var positiveButtonColor by Delegates.notNull<Int>()
    private var negativeButtonColor by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        readArguments(arguments)
    }

    private fun readArguments(arguments: Bundle?) {
        if (arguments != null) {
            title = arguments.getString(AppExtras.EXTRA_DIALOG_TITLE, "")
            description = arguments.getString(AppExtras.EXTRA_DIALOG_MESSAGE, "")
            positiveButtonName =
                arguments.getString(AppExtras.EXTRA_DIALOG_POSITIVE_BUTTON_TITLE, "")
            negativeButtonName =
                arguments.getString(AppExtras.EXTRA_DIALOG_NEGATIVE_BUTTON_TITLE, "")
            positiveButtonColor = arguments.getInt(AppExtras.EXTRA_DIALOG_POSITIVE_BUTTON_COLOR,
                positiveTextColorButtonDefault)
            negativeButtonColor = arguments.getInt(AppExtras.EXTRA_DIALOG_NEGATIVE_BUTTON_COLOR,
                negativeTextColorButtonDefault)
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
                .setTextColor(positiveButtonColor)
            (dialog as AlertDialog)
                .getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(negativeButtonColor)
        }
    }

    private val positiveTextColorButtonDefault: Int
        get() = ContextCompat.getColor(requireContext(), R.color.danger)
    private val negativeTextColorButtonDefault: Int
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
            args.putString(AppExtras.EXTRA_DIALOG_MESSAGE, description)
            args.putString(AppExtras.EXTRA_DIALOG_POSITIVE_BUTTON_TITLE, positiveButtonName)
            args.putString(AppExtras.EXTRA_DIALOG_NEGATIVE_BUTTON_TITLE, negativeButtonName)
            val fragment = SimpleResultDialogFragment()
            fragment.arguments = args
            return fragment
        }

        fun newInstance(
            title: String,
            description: String,
            positiveButtonName: String,
            negativeButtonName: String,
            @ColorInt positiveTextColorButton: Int,
            @ColorInt negativeTextColorButton: Int,
        ): SimpleResultDialogFragment {
            val args = Bundle()
            args.putString(AppExtras.EXTRA_DIALOG_TITLE, title)
            args.putString(AppExtras.EXTRA_DIALOG_MESSAGE, description)
            args.putString(AppExtras.EXTRA_DIALOG_POSITIVE_BUTTON_TITLE, positiveButtonName)
            args.putString(AppExtras.EXTRA_DIALOG_NEGATIVE_BUTTON_TITLE, negativeButtonName)
            args.putInt(AppExtras.EXTRA_DIALOG_POSITIVE_BUTTON_COLOR, positiveTextColorButton)
            args.putInt(AppExtras.EXTRA_DIALOG_NEGATIVE_BUTTON_COLOR, negativeTextColorButton)
            val fragment = SimpleResultDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

}