package ru.wb.go.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Html
import android.view.KeyEvent
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import ru.wb.go.R
import ru.wb.go.app.AppExtras

class DialogConfirmInfoFragment : DialogFragment() {

    interface SimpleDialogListener {
        fun onPositiveDialogClick(resultTag: String)
        fun onNegativeDialogClick(resultTag: String)
    }

    private var simpleDialogListener: SimpleDialogListener? = null

    private lateinit var resultTag: String
    private var style: Int = 0
    private lateinit var title: String
    private lateinit var message: String
    private lateinit var positive: String
    private lateinit var negative: String

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
            negative = arguments.getString(AppExtras.EXTRA_DIALOG_NEGATIVE_BUTTON_TITLE, "")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val dialogView =
            requireActivity().layoutInflater.inflate(R.layout.custom_layout_dialog_result, null)
        val titleLayout: ImageView = dialogView.findViewById(R.id.title_layout)
        val title: TextView = dialogView.findViewById(R.id.title)
        val message: TextView = dialogView.findViewById(R.id.message)
        val positive: Button = dialogView.findViewById(R.id.positive)
        val negative: Button = dialogView.findViewById(R.id.negative)
        builder.setView(dialogView)
        val imageTitle = when (DialogInfoStyle.values()[style]) {
            DialogInfoStyle.INFO -> R.drawable.ic_dialog_title_info
            DialogInfoStyle.WARNING -> R.drawable.ic_dialog_title_warning
            DialogInfoStyle.ERROR -> R.drawable.ic_dialog_title_alarm
        }
        titleLayout.setImageResource(imageTitle)

        title.text = this.title
        message.text = Html.fromHtml(this.message,HtmlCompat.FROM_HTML_MODE_LEGACY)
        positive.text = this.positive
        negative.text = this.negative

        positive.setOnClickListener {
            dismiss()
            sendPositiveResult(resultTag)
        }

        negative.setOnClickListener {
            dismiss()
            sendNegativeResult(resultTag)
        }

        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                dialog.dismiss()
                sendNegativeResult(resultTag)
            }
            true
        }

        return dialog
    }

    private fun sendPositiveResult(resultTag: String) {
        simpleDialogListener?.onPositiveDialogClick(resultTag)
        setFragmentResult(
            resultTag,
            bundleOf(DIALOG_CONFIRM_INFO_POSITIVE_KEY to DIALOG_CONFIRM_INFO_POSITIVE_VALUE)
        )
    }

    private fun sendNegativeResult(resultTag: String) {
        simpleDialogListener?.onNegativeDialogClick(resultTag)
        setFragmentResult(
            resultTag,
            bundleOf(DIALOG_CONFIRM_INFO_NEGATIVE_KEY to DIALOG_CONFIRM_INFO_NEGATIVE_VALUE)
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SimpleDialogListener) simpleDialogListener = context
    }

    override fun onDetach() {
        if (simpleDialogListener != null) simpleDialogListener = null
        super.onDetach()
    }

    companion object {
        fun newInstance(
            resultTag: String = DIALOG_CONFIRM_INFO_RESULT_TAG,
            type: Int,
            title: String,
            message: String,
            positiveButtonName: String,
            negativeButtonName: String,
        ): DialogConfirmInfoFragment {
            val args = Bundle()
            args.putString(AppExtras.EXTRA_DIALOG_RESULT_TAG, resultTag)
            args.putInt(AppExtras.EXTRA_DIALOG_TYPE, type)
            args.putString(AppExtras.EXTRA_DIALOG_TITLE, title)
            args.putString(AppExtras.EXTRA_DIALOG_MESSAGE, message)
            args.putString(AppExtras.EXTRA_DIALOG_POSITIVE_BUTTON_TITLE, positiveButtonName)
            args.putString(AppExtras.EXTRA_DIALOG_NEGATIVE_BUTTON_TITLE, negativeButtonName)
            val fragment = DialogConfirmInfoFragment()
            fragment.arguments = args
            return fragment
        }

        const val DIALOG_CONFIRM_INFO_RESULT_TAG = "DIALOG_CONFIRM_INFO_RESULT_TAG"
        const val DIALOG_CONFIRM_INFO_POSITIVE_KEY = "DIALOG_CONFIRM_INFO_POSITIVE_KEY"
        const val DIALOG_CONFIRM_INFO_POSITIVE_VALUE = 1000
        const val DIALOG_CONFIRM_INFO_NEGATIVE_KEY = "DIALOG_CONFIRM_INFO_NEGATIVE_KEY"
        const val DIALOG_CONFIRM_INFO_NEGATIVE_VALUE = 2000
        const val DIALOG_CONFIRM_INFO_TAG = "DIALOG_CONFIRM_INFO_TAG"

    }

}

data class NavigateToDialogConfirmInfo(
    val type: Int,
    val title: String,
    val message: String,
    val positive: String,
    val negative: String
)