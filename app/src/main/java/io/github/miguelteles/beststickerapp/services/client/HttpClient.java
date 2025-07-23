package io.github.miguelteles.beststickerapp.services.client;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import io.github.miguelteles.beststickerapp.utils.Utils;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

abstract class HttpClient {
    public static final MediaType JSON = MediaType.parse("application/json");
    private final String baseUrl;
    private final OkHttpClient okHttpClient;

    protected HttpClient(String baseUrl) {
        this.baseUrl = baseUrl; //TODO: pensar num jeito de mudar esse ip dinamicamente
        okHttpClient = new OkHttpClient();
    }

    protected Call post(String endpoint, String bodyContent) {
        RequestBody requestBody = RequestBody.create(bodyContent, JSON);
        return okHttpClient.newCall(new Request.Builder().url(baseUrl + endpoint).post(requestBody).build());
    }

    protected Call get(String endpoint) {
        return okHttpClient.newCall(new Request.Builder().url(baseUrl+endpoint).get().build());
    }

    protected boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) Utils.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
