package ru.wb.perevozka.ui.auth.keyboard;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({KeyboardMode.SMS,
        KeyboardMode.PIN_INPUT,
        KeyboardMode.PIN_CREATE,
        KeyboardMode.PIN_CONFIRM,
        KeyboardMode.SIGN_UP})
@Retention(RetentionPolicy.SOURCE)
public @interface KeyboardMode {

    /**
     * Ввод смс-кода
     */
    int SMS = 0;
    /**
     * Ввод пин-кода
     */
    int PIN_INPUT = 1;
    /**
     * Создание пин-кода
     */
    int PIN_CREATE = 2;
    /**
     * Подтверждение пин-кода
     */
    int PIN_CONFIRM = 3;
    /**
     * Регистрация
     */
    int SIGN_UP = 4;

}