package ru.wb.go.network.headers;

import static ru.wb.go.app.AppConsts.SERVICE_CODE_OK;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import ru.wb.go.reader.FreeTasksOfficesPath;
import ru.wb.go.reader.FreeTasksPath;
import ru.wb.go.reader.MockResponse;
import ru.wb.go.reader.MockType;

public class AppMockResponseInterceptor implements Interceptor {

    private static final String GET_APP_FREE_TASKS_OFFICES_URL = "http://ip-api.com/json/208.80.152.200";
    private static final String GET_APP_FREE_TASKS_OFFICES_API = "free-tasks/offices";
    private static final String GET_APP_FREE_TASKS_URL = "http://ip-api.com/json/208.80.152.201";
    private static final String GET_APP_FREE_TASKS_API = "free-tasks?srcOfficeID=234";

    private static final String SLASH_SYMBOL = "/";

    @NonNull
    private final String apiServer;

    @NonNull
    private final MockResponse mockResponse;

    public AppMockResponseInterceptor(@NonNull String apiServer, @NonNull MockResponse mockResponse) {
        this.apiServer = apiServer;
        this.mockResponse = mockResponse;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = getRequestBuilder(chain.request()).build();
        Response response = chain.proceed(request);
        return getResponseBuilder(request, response).build();
    }

    @NonNull
    private Request.Builder getRequestBuilder(@NonNull Request request) {
        Request.Builder builder = request.newBuilder();
        String requestUrl = request.url().toString();
        switch (getApiMethod(requestUrl)) {
            case GET_APP_FREE_TASKS_OFFICES_API:
                return builder.url(GET_APP_FREE_TASKS_OFFICES_URL).get();
            case GET_APP_FREE_TASKS_API:
                return builder.url(GET_APP_FREE_TASKS_URL).get();
            default:
                return builder;
        }
    }

    @NonNull
    private Response.Builder getResponseBuilder(@NonNull Request request,
                                                @NonNull Response response) throws IOException {
        String url = request.url().toString();
        ResponseBody responseBody = response.body();
        MediaType contentType = responseBody == null ? MediaType.parse("application") : responseBody.contentType();
        switch (url) {
            case GET_APP_FREE_TASKS_OFFICES_URL:
                return response.newBuilder()
                        .body(ResponseBody.create(contentType, mockResponse.read(new FreeTasksOfficesPath(MockType.COMPLETE))))
                        .code(SERVICE_CODE_OK);
            case GET_APP_FREE_TASKS_URL:
                return response.newBuilder()
                        .body(ResponseBody.create(contentType, mockResponse.read(new FreeTasksPath(MockType.COMPLETE))))
                        .code(SERVICE_CODE_OK);
            default:
                return response.newBuilder()
                        .body(ResponseBody.create(contentType, response.body().string().getBytes()));
        }
    }

    @NonNull
    private String getApiMethod(@NonNull String url) {
        return url.substring(apiServer.length() + 1);
    }

    @NonNull
    private String getSingleApiMethod(@NonNull String url) {
        return url.substring(url.lastIndexOf(SLASH_SYMBOL) + 1);
    }

}
