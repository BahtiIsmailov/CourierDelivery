package ru.wb.go.ui.couriercarnumber.keyboard;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({CarNumberKeyboardMode.NUMERIC,
        CarNumberKeyboardMode.SYMBOL})
@Retention(RetentionPolicy.SOURCE)
public @interface CarNumberKeyboardMode {

    int NUMERIC = 0;
    int SYMBOL = 1;

}