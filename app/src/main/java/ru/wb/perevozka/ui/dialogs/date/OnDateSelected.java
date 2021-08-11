package ru.wb.perevozka.ui.dialogs.date;

import org.joda.time.DateTime;

public interface OnDateSelected {

    void onDateSelected(DateTime date);

    void onCanceled();

}
