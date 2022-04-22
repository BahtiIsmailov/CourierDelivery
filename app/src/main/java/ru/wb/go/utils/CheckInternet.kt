package com.example.englishapp.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.fragment.app.Fragment
import com.google.android.gms.dynamic.SupportFragmentWrapper
import ru.wb.go.R
import ru.wb.go.ui.dialogs.DialogInfoFragment
import ru.wb.go.ui.dialogs.DialogInfoStyle

class CheckInternet {

    companion object {
        fun checkConnection(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val nw = connectivityManager.activeNetwork ?: return false
            val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                else -> false
            }
        }

        fun showDialogHaveNotInternet(context: Context): DialogInfoFragment{
            return DialogInfoFragment.newInstance(
                type = DialogInfoStyle.WARNING.ordinal,
                title = context.getString(R.string.unknown_internet_title_error),
                message = context.getString(R.string.unknown_internet_message_error),
                positiveButtonName = context.getString(R.string.ok_button_title)
            )
        }
    }
}