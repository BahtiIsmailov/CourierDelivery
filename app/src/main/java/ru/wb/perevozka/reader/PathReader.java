package ru.wb.perevozka.reader;

import android.content.Context;

import androidx.annotation.NonNull;

public abstract class PathReader {

    @NonNull
    private final MockType mockType;

    public PathReader(@NonNull MockType mockType) {
        this.mockType = mockType;
    }

    public abstract byte[] readPath(@NonNull Context context);

    protected abstract String getComplete();

    protected abstract String getInvalid();

    protected abstract String getEmpty();

    protected byte[] readFileFromPath(@NonNull Context context) {
        return new AssetsFileReader(context, getPath()).read().getBytes();
    }

    private String getPath() {
        switch (mockType) {
            case INVALID:
                return getInvalid();
            case COMPLETE:
                return getComplete();
            case EMPTY:
                getEmpty();
            default:
                return getComplete();
        }
    }

}
