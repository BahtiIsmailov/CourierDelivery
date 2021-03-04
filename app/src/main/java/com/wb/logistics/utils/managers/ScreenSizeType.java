package com.wb.logistics.utils.managers;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({ScreenSizeType.NOT_SET,
        ScreenSizeType.MDPI,
        ScreenSizeType.HDPI,
        ScreenSizeType.XHDPI,
        ScreenSizeType.XXHDPI,
        ScreenSizeType.XXXHDPI,
        ScreenSizeType.IPHONE4,
        ScreenSizeType.IPHONE4S,
        ScreenSizeType.IPHONE5,
        ScreenSizeType.IPHONE5C,
        ScreenSizeType.IPHONE5S,
        ScreenSizeType.IPHONE6,
        ScreenSizeType.IPHONE6PLUS})
@Retention(RetentionPolicy.SOURCE)
public @interface ScreenSizeType {

    int NOT_SET = 0;
    int MDPI = 1;
    int HDPI = 2;
    int XHDPI = 3;
    int XXHDPI = 4;
    int XXXHDPI = 5;
    int IPHONE4 = 6;
    int IPHONE4S = 7;
    int IPHONE5 = 8;
    int IPHONE5C = 9;
    int IPHONE5S = 10;
    int IPHONE6 = 11;
    int IPHONE6PLUS = 12;

}