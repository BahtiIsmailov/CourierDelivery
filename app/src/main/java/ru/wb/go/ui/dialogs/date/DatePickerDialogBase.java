package ru.wb.go.ui.dialogs.date;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import org.joda.time.DateTime;

public abstract class DatePickerDialogBase extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private OnDateSelected activityCallback;

    private static final String EXTRA_MIN_DATE = "EXTRA_MIN_DATE";
    private static final String EXTRA_MAX_DATE = "EXTRA_MAX_DATE";
    private static final String EXTRA_SELECT_DATE = "EXTRA_SELECT_DATE";

    protected DateTime minDate;
    protected DateTime maxDate;
    protected DateTime date;

    protected static Bundle getBundle(@NonNull DateTime minDate,
                                      @NonNull DateTime maxDate,
                                      @NonNull DateTime date){
        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTRA_MIN_DATE, minDate);
        bundle.putSerializable(EXTRA_MAX_DATE, maxDate);
        bundle.putSerializable(EXTRA_SELECT_DATE, date);
        return bundle;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        readArguments(getArguments());
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                AlertDialog.THEME_HOLO_LIGHT,this, date.getYear(),
                date.getMonthOfYear(), date.getDayOfMonth());
        datePickerDialog.getDatePicker().setMinDate(minDate.getMillis());
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getMillis());
        datePickerDialog.getDatePicker().setSpinnersShown(true);
        datePickerDialog.getDatePicker().setCalendarViewShown(false);
        return datePickerDialog;
    }

    private void readArguments(@Nullable Bundle arguments) {
        if (arguments != null) {
            minDate = (DateTime) arguments.getSerializable(EXTRA_MIN_DATE);
            maxDate = (DateTime) arguments.getSerializable(EXTRA_MAX_DATE);
            date = (DateTime) arguments.getSerializable(EXTRA_SELECT_DATE);
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        DateTime selDate = new DateTime(year, month + 1, dayOfMonth, 0, 0);
        activityCallback.onDateSelected(selDate);
    }

    @Override
    public void onCancel(DialogInterface dialog){
        activityCallback.onCanceled();
    }

    public void setListener(@NonNull OnDateSelected activityCallback) {
        this.activityCallback = activityCallback;
    }

}
