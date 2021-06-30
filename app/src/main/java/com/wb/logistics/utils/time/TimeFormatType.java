package com.wb.logistics.utils.time;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@StringDef({TimeFormatType.NOT_SET,
        TimeFormatType.DATE_AND_TIME,
        TimeFormatType.ONLY_DATE,
        TimeFormatType.ONLY_DATE_YMD,
        TimeFormatType.ONLY_TIME,
        TimeFormatType.ONLY_FULL_TIME,
        TimeFormatType.ONLY_MONTH,
        TimeFormatType.MIN_AND_SEC,
        TimeFormatType.DAY_AND_LETTER_MONTH,
        TimeFormatType.DAY_LETTER_MONTH_YEAR,
        TimeFormatType.HUMAN_DATE,
        TimeFormatType.FULL_DATE_AND_TIME})
@Retention(RetentionPolicy.SOURCE)
public @interface TimeFormatType {
    String NOT_SET = "";
    String DATE_AND_TIME = "dd.MM.yyyy HH:mm";
    String ONLY_DATE = "dd.MM.yyyy";
    String ONLY_DATE_YMD = "yyyy-MM-dd";
    String ONLY_TIME = "HH:mm";
    String ONLY_FULL_TIME = "HH:mm:ss";
    String ONLY_MONTH = "MMMM";
    String MIN_AND_SEC = "mm:ss";
    String DAY_AND_LETTER_MONTH = "day_letter_month";
    String DAY_LETTER_MONTH_YEAR = "day_letter_month_year";
    String HUMAN_DATE = "human_date";
    String FULL_DATE_AND_TIME = "full_date_and_time";
}