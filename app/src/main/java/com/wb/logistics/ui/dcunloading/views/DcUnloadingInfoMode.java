package com.wb.logistics.ui.dcunloading.views;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({DcUnloadingInfoMode.EMPTY,
        DcUnloadingInfoMode.UNLOADING,
        DcUnloadingInfoMode.NOT_INFO_DENY})
@Retention(RetentionPolicy.SOURCE)
public @interface DcUnloadingInfoMode {

    int EMPTY = 0;

    int UNLOADING = 1;

    int NOT_INFO_DENY = 2;

}