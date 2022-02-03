package ru.wb.go.views;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({ProgressImageButtonMode.ENABLED,
        ProgressImageButtonMode.PROGRESS,
        ProgressImageButtonMode.DISABLED})
@Retention(RetentionPolicy.SOURCE)
public @interface ProgressImageButtonMode {

    int ENABLED = 0;

    int PROGRESS = 1;

    int DISABLED = 2;

}