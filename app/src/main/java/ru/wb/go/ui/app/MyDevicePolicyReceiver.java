package ru.wb.go.ui.app;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MyDevicePolicyReceiver extends DeviceAdminReceiver {

    @Override
    public void onDisabled(Context context, Intent intent) {
        Toast.makeText(context, "",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        Toast.makeText(context, "Truiton's Device Admin is now enabled",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        CharSequence disableRequestedSeq = "Requesting to disable Device Admin";
        return disableRequestedSeq;
    }


}