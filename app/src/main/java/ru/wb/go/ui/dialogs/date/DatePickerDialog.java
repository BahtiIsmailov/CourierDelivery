package ru.wb.go.ui.dialogs.date;

import androidx.annotation.NonNull;

import org.joda.time.DateTime;

public class DatePickerDialog extends DatePickerDialogBase {

    public static DatePickerDialog newInstance(@NonNull DateTime minDate,
                                               @NonNull DateTime maxDate,
                                               @NonNull DateTime date) {
        DatePickerDialog fragment = new DatePickerDialog();
        fragment.setArguments(getBundle(minDate, maxDate, date));
        return fragment;
    }

}