package com.wb.logistics.views;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({ReceptionInfoMode.EMPTY,
        ReceptionInfoMode.SUBMERGE,
        ReceptionInfoMode.RETURN})
@Retention(RetentionPolicy.SOURCE)
public @interface ReceptionInfoMode {

    int EMPTY = 0;

    int SUBMERGE = 1;

    int RETURN = 2;

}