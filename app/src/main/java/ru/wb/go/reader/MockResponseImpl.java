package ru.wb.go.reader;import android.content.Context;import androidx.annotation.NonNull;public final class MockResponseImpl implements MockResponse {    @NonNull    private final Context context;    public MockResponseImpl(@NonNull Context context) {        this.context = context;    }    @Override    public byte[] read(@NonNull PathReader pathReader) {        return pathReader.readPath(context);    }}