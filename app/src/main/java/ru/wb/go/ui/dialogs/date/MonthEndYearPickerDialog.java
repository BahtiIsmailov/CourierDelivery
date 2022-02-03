package ru.wb.go.ui.dialogs.date;

import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;

import androidx.annotation.NonNull;

import org.joda.time.DateTime;

public class MonthEndYearPickerDialog extends DatePickerDialogBase {

    public static MonthEndYearPickerDialog newInstance(@NonNull DateTime minDate,
                                                       @NonNull DateTime maxDate,
                                                       @NonNull DateTime date) {
        MonthEndYearPickerDialog fragment = new MonthEndYearPickerDialog();
        fragment.setArguments(getBundle(minDate, maxDate, date));
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        android.app.DatePickerDialog datePickerDialog =
                (android.app.DatePickerDialog) super.onCreateDialog(savedInstanceState);
        datePickerDialog.setTitle("");
        int identifierDay  = getIdentifierDay();
        if (identifierDay != 0) {
            DatePicker datePicker = datePickerDialog.getDatePicker();
            View daySpinner = datePicker.findViewById(identifierDay);
            if (daySpinner != null) {
                daySpinner.setVisibility(View.GONE);
            }
        }
        return datePickerDialog;
    }

    private int getIdentifierDay() {
        return Resources.getSystem().getIdentifier("day", "id", "android");
    }

}
