package ru.wb.go.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import ru.wb.go.R
import ru.wb.go.utils.RebootApplication

class RebootDialog : DialogFragment(){



    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        val dialogView =
            requireActivity().layoutInflater.inflate(R.layout.custom_lauout_dialog_title_info_fragment, null)
        val titleLayout: ImageView = dialogView.findViewById(R.id.title_layout)
        val title: TextView = dialogView.findViewById(R.id.title)
        val message: TextView = dialogView.findViewById(R.id.message)
        val positive: Button = dialogView.findViewById(R.id.positive)
        builder.setView(dialogView)
        titleLayout.setImageResource(R.drawable.ic_dialog_title_alarm)
        if (globalCode == 409) {
            title.text = requireContext().getString(R.string.error_title)
            message.text = requireContext().getString(R.string.http409)
            positive.text = requireContext().getString(R.string.courier_expects_positive)
            positive.setOnClickListener {
                dismiss()
                RebootApplication.doRestart(requireContext())
            }
        }else if (globalCode!! >= 500){
            title.text = requireContext().getString(R.string.error_service, globalCode)
            message.text = requireContext().getString(R.string.unknown_generic_error)
            positive.text = requireContext().getString(R.string.courier_expects_positive)
            positive.setOnClickListener {
                dismiss()
            }
        }

        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        return dialog
    }

    companion object{
        fun newInstance(code:Int):RebootDialog{
             globalCode = code
             return RebootDialog()
        }
        const val TAG = "reboot_dialog"
        var globalCode:Int? = null
    }
}