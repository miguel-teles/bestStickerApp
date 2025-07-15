package io.github.miguelteles.beststickerapp.services.client;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public abstract class HttpClient {
    public static final MediaType JSON = MediaType.parse("application/json");
    private String baseUrl;
    private OkHttpClient okHttpClient;

    protected HttpClient(String baseUrl) {
        this.baseUrl = "http://192.168.68.103:8080/" + baseUrl;
        okHttpClient = new OkHttpClient();
    }

    protected Call post(String endpoint, String bodyContent) {
        RequestBody requestBody = RequestBody.create(bodyContent, JSON);
        Request request = new Request.Builder().url(baseUrl + endpoint).post(requestBody).build();
        return okHttpClient.newCall(request);
    }

}
