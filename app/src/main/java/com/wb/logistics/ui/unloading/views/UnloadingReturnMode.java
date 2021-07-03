package com.wb.logistics.ui.unloading.views;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({UnloadingReturnMode.EMPTY,
        UnloadingReturnMode.ACTIVE,
        UnloadingReturnMode.DENY})
@Retention(RetentionPolicy.SOURCE)
public @interface UnloadingReturnMode {

    int EMPTY = 0;

    int ACTIVE = 1;

    int DENY = 2;



}