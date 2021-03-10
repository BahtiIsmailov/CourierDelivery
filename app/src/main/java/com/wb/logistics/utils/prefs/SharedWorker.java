package com.wb.logistics.utils.prefs;

import androidx.annotation.NonNull;

public interface SharedWorker {

    boolean load(@NonNull String key, boolean defValue);

    void save(@NonNull String key, boolean value);

    @NonNull
    String load(@NonNull String key, @NonNull String defValue);

    void save(@NonNull String key, @NonNull String value);

    void delete(@NonNull String... keys);

}