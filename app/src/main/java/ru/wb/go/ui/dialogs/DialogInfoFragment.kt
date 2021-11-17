package ru.wb.go.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import ru.wb.go.R
import ru.wb.go.app.AppExtras

class DialogInfoFragment : DialogFragment() {

    private lateinit var resultTag: String
    private var style: Int = 0
    private lateinit var title: String
    private lateinit var message: String
    private lateinit var positive: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        readArguments(arguments)
    }

    private fun readArguments(arguments: Bundle?) {
        if (arguments != null) {
            resultTag = arguments.getString(AppExtras.EXTRA_DIALOG_RESULT_TAG, "")
            style = arguments.getInt(AppExtras.EXTRA_DIALOG_TYPE, 0)
            title = arguments.getString(AppExtras.EXTRA_DIALOG_TITLE, "")
            message = arguments.getString(AppExtras.EXTRA_DIALOG_MESSAGE, "")
            positive = arguments.getString(AppExtras.EXTRA_DIALOG_POSITIVE_BUTTON_TITLE, "")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val dialogView =
            requireActivity().layoutInflater.inflate(R.layout.custom_dialog_info_fragment, null)
        val titleLayout: View = dialogView.findViewById(R.id.title_layout)
        val title: TextView = dialogView.findViewById(R.id.title)
        val message: TextView = dialogView.findViewById(R.id.message)
        val positive: Button = dialogView.findViewById(R.id.positive)
        builder.setView(dialogView)
        val color = when (DialogInfoStyle.values()[style]) {
            DialogInfoStyle.INFO -> R.color.dialog_title_info
            DialogInfoStyle.WARNING -> R.color.dialog_title_warning
            DialogInfoStyle.ERROR -> R.color.dialog_title_alarm
        }
        titleLayout.setBackgroundColor(ContextCompat.getColor(requireActivity(), color))
        title.text = this.title
        message.text = this.message
        positive.text = this.positive
        positive.setOnClickListener {
            dismiss()
            sendResult(resultTag)
        }

        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                dialog.dismiss()
                sendResult(resultTag)
            }
            true
        }
        return dialog
    }

    private fun sendResult(resultTag: String) {
        setFragmentResult(
            resultTag,
            bundleOf(DIALOG_INFO_BACK_KEY to DIALOG_INFO_BACK_VALUE)
        )
    }

    companion object {
        fun newInstance(
            resultTag: String = DIALOG_INFO_RESULT_TAG,
            type: Int,
            title: String,
            message: String,
            positiveButtonName: String,
        ): DialogInfoFragment {
            val args = Bundle()
            args.putString(AppExtras.EXTRA_DIALOG_RESULT_TAG, resultTag)
            args.putInt(AppExtras.EXTRA_DIALOG_TYPE, type)
            args.putString(AppExtras.EXTRA_DIALOG_TITLE, title)
            args.putString(AppExtras.EXTRA_DIALOG_MESSAGE, message)
            args.putString(AppExtras.EXTRA_DIALOG_POSITIVE_BUTTON_TITLE, positiveButtonName)
            val fragment = DialogInfoFragment()
            fragment.arguments = args
            return fragment
        }

        const val DIALOG_INFO_RESULT_TAG = "DIALOG_INFO_RESULT_TAG"
        const val DIALOG_INFO_BACK_KEY = "DIALOG_INFO_BACK_KEY"
        const val DIALOG_INFO_BACK_VALUE = 1000
        const val DIALOG_INFO_TAG = "DIALOG_INFO_TAG"

    }

}

data class NavigateToDialogInfo(
    val type: Int, val title: String, val message: String, val button: String
)