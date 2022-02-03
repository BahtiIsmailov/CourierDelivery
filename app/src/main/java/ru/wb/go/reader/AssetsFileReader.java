package ru.wb.go.reader;import android.content.Context;import androidx.annotation.NonNull;import java.io.BufferedReader;import java.io.IOException;import java.io.InputStreamReader;import java.util.ArrayList;import java.util.Arrays;import java.util.List;public class AssetsFileReader {    private static final  String CHARSET_NAME = "UTF-8";    @NonNull    private final Context context;    @NonNull    private final String filePath;    public AssetsFileReader(@NonNull Context context) {        this.context = context;        filePath = "";    }    public AssetsFileReader(@NonNull Context context, @NonNull String filePath) {        this.context = context;        this.filePath = filePath;    }    public List<String> getFilesInPath(@NonNull String path) {        try {            String[] pathFiles = context.getAssets().list(path);            return new ArrayList<>(Arrays.asList(pathFiles));        } catch (IOException e) {            return new ArrayList<>();        }    }    @NonNull    public String read() {        return read(this.filePath);    }    @NonNull    public String read(@NonNull String filePath) {        StringBuilder resultString = new StringBuilder();        BufferedReader reader = null;        try {            reader = new BufferedReader(                    new InputStreamReader(context.getAssets().open(filePath), CHARSET_NAME));            String line;            while ((line = reader.readLine()) != null) {                resultString.append(line);            }        } catch (IOException e) {            //create invoke        } finally {            if (reader != null) {                try {                    reader.close();                } catch (IOException e) {                    //create invoke                }            }        }        return resultString.toString();    }}