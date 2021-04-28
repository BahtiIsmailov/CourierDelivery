package com.wb.logistics.ui.unloading.views;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({UnloadingInfoMode.EMPTY,
        UnloadingInfoMode.UNLOADING,
        UnloadingInfoMode.RETURN})
@Retention(RetentionPolicy.SOURCE)
public @interface UnloadingInfoMode {

    int EMPTY = 0;

    int UNLOADING = 1;

    int RETURN = 2;

}