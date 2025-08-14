package io.github.miguelteles.beststickerapp.services.client;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.concurrent.TimeUnit;

import io.github.miguelteles.beststickerapp.BuildConfig;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.StickerFatalErrorException;
import io.github.miguelteles.beststickerapp.exception.enums.StickerFatalExceptionEnum;
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
    private final String secureToken;
    private final Context context;

    protected HttpClient(String baseUrl, Context context) throws StickerFatalErrorException {
        this.baseUrl = baseUrl;
        this.secureToken = BuildConfig.SECURE_TOKEN;
        this.context = context;
        if (Utils.isNothing(secureToken)) {
            throw new StickerFatalErrorException(null, StickerFatalExceptionEnum.NO_SECURE_TOKEN_FOUND, "Ops! Há algo de muito errado com esta versão do aplicativo.");
        }
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .build();
    }

    protected Call post(String endpoint, String bodyContent) throws StickerException {
        RequestBody requestBody = RequestBody.create(bodyContent, JSON);
        return okHttpClient.newCall(getRequestBuilderWithCommonAttributes(endpoint).post(requestBody).build());
    }

    protected Call get(String endpoint) throws StickerException {
        return okHttpClient.newCall(getRequestBuilderWithCommonAttributes(endpoint).get().build());
    }

    private Request.Builder getRequestBuilderWithCommonAttributes(String endpoint) {
        return new Request.Builder()
                .url(baseUrl + endpoint)
                .addHeader("x-api-key", secureToken);
    }

    protected boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
