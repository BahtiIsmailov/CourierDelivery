package com.wb.logistics.ui.unloadingscan.views;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({UnloadingReturnMode.EMPTY,
        UnloadingReturnMode.COMPLETE,
        UnloadingReturnMode.ACTIVE,
        UnloadingReturnMode.DENY})
@Retention(RetentionPolicy.SOURCE)
public @interface UnloadingReturnMode {

    int EMPTY = 0;

    int COMPLETE = 1;

    int ACTIVE = 2;

    int DENY = 3;



}