package ru.wb.perevozka.reader;

import android.content.Context;

import androidx.annotation.NonNull;

public class FreeTasksPath extends PathReader {

    private static final String COMPLETE_PATH = "mock/free_tasks_complete.json";
    private static final String INVALID_PATH = "mock/free_tasks_invalid.json";

    public FreeTasksPath(@NonNull MockType mockType) {
        super(mockType);
    }

    @Override
    protected String getComplete() {
        return COMPLETE_PATH;
    }

    @Override
    protected String getInvalid() {
        return INVALID_PATH;
    }

    @Override
    protected String getEmpty() {
        return null;
    }

    @Override
    public byte[] readPath(@NonNull Context context) {
        return readFileFromPath(context);
    }

}
