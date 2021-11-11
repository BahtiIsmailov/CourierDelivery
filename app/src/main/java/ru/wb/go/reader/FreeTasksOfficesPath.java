package ru.wb.go.reader;

import android.content.Context;

import androidx.annotation.NonNull;

public class FreeTasksOfficesPath extends PathReader {

    private static final String COMPLETE_PATH = "mock/free_tasks_offices_complete.json";
    private static final String INVALID_PATH = "mock/free_tasks_offices_invalid.json";

    public FreeTasksOfficesPath(@NonNull MockType mockType) {
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
