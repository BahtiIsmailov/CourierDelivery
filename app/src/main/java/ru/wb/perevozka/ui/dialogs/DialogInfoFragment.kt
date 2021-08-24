package ru.wb.perevozka.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import ru.wb.perevozka.R
import ru.wb.perevozka.app.AppExtras

class DialogInfoFragment : DialogFragment() {

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
            style = arguments.getInt(AppExtras.EXTRA_DIALOG_STYLE, 0)
            title = arguments.getString(AppExtras.EXTRA_DIALOG_TITLE, "")
            message = arguments.getString(AppExtras.EXTRA_DIALOG_MESSAGE, "")
            positive = arguments.getString(AppExtras.EXTRA_DIALOG_POSITIVE_BUTTON_TITLE, "")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val dialogView =
            requireActivity().layoutInflater.inflate(R.layout.dialog_info_fragment, null)
        val titleLayout: View = dialogView.findViewById(R.id.title_layout)
        val title: TextView = dialogView.findViewById(R.id.title)
        val message: TextView = dialogView.findViewById(R.id.message)
        val positive: Button = dialogView.findViewById(R.id.positive)
        builder.setView(dialogView)
        val color = when (DialogStyle.values()[style]) {
            DialogStyle.INFO -> R.color.dialog_info
            DialogStyle.WARNING -> R.color.dialog_warning
            DialogStyle.ERROR -> R.color.dialog_alarm
        }
        titleLayout.setBackgroundColor(ContextCompat.getColor(requireActivity(), color))
        title.text = this.title
        message.text = this.message
        positive.text = this.positive
        positive.setOnClickListener { dismiss() }
        return builder.create()
    }

    companion object {
        fun newInstance(
            style: Int,
            title: String,
            message: String,
            positiveButtonName: String,
        ): DialogInfoFragment {
            val args = Bundle()
            args.putInt(AppExtras.EXTRA_DIALOG_STYLE, style)
            args.putString(AppExtras.EXTRA_DIALOG_TITLE, title)
            args.putString(AppExtras.EXTRA_DIALOG_MESSAGE, message)
            args.putString(AppExtras.EXTRA_DIALOG_POSITIVE_BUTTON_TITLE, positiveButtonName)
            val fragment = DialogInfoFragment()
            fragment.arguments = args
            return fragment
        }
        const val DIALOG_INFO_TAG = "DIALOG_INFO_TAG"

    }

}