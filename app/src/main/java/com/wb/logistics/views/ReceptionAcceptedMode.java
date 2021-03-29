package com.wb.logistics.views;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({ReceptionAcceptedMode.EMPTY,
        ReceptionAcceptedMode.CONTAINS_COMPLETE,
        ReceptionAcceptedMode.CONTAINS_DENY})
@Retention(RetentionPolicy.SOURCE)
public @interface ReceptionAcceptedMode {

    int EMPTY = 0;

    int CONTAINS_COMPLETE = 1;

    int CONTAINS_DENY = 2;

}