package com.wb.logistics.views;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({ProgressButtonMode.DISABLE,
        ProgressButtonMode.ENABLE,
        ProgressButtonMode.PROGRESS})
@Retention(RetentionPolicy.SOURCE)
public @interface ProgressButtonMode {

    int DISABLE = 0;

    int ENABLE = 1;

    int PROGRESS = 2;

}