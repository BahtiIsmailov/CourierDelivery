package com.wb.logistics.utils.prefs;

import androidx.annotation.NonNull;

public interface SharedWorker {

    boolean load(@NonNull String key, boolean defValue);

    void save(@NonNull String key, boolean value);

}