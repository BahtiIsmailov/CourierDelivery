package ru.wb.go.ui.unloadingscan.views;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({UnloadingInfoMode.EMPTY,
        UnloadingInfoMode.UNLOADING,
        UnloadingInfoMode.RETURN,
        UnloadingInfoMode.UNLOAD_DENY,
        UnloadingInfoMode.NOT_INFO_DENY})
@Retention(RetentionPolicy.SOURCE)
public @interface UnloadingInfoMode {

    int EMPTY = 0;

    int UNLOADING = 1;

    int RETURN = 2;

    int UNLOAD_DENY = 3;

    int NOT_INFO_DENY = 4;

}