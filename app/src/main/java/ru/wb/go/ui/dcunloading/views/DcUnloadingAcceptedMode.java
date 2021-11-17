package ru.wb.go.ui.dcunloading.views;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({DcUnloadingAcceptedMode.EMPTY,
        DcUnloadingAcceptedMode.COMPLETE,
        DcUnloadingAcceptedMode.ACTIVE,
        DcUnloadingAcceptedMode.DENY})
@Retention(RetentionPolicy.SOURCE)
public @interface DcUnloadingAcceptedMode {

    int EMPTY = 0;

    int COMPLETE = 1;

    int ACTIVE = 2;

    int DENY = 3;

}