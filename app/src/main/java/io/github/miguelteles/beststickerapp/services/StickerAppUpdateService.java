package io.github.miguelteles.beststickerapp.services;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.github.miguelteles.beststickerapp.BuildConfig;
import io.github.miguelteles.beststickerapp.domain.pojo.ResponseAPIAppLatestVersion;
import io.github.miguelteles.beststickerapp.exception.StickerHttpClientException;
import io.github.miguelteles.beststickerapp.services.client.GetLatestAppVersionImpl;
import io.github.miguelteles.beststickerapp.services.client.interfaces.GetLatestAppVersionAPI;
import io.github.miguelteles.beststickerapp.view.interfaces.UiThreadPoster;
import io.github.miguelteles.beststickerapp.view.threadHandlers.AndroidUiThreadPoster;

public class StickerAppUpdateService {

    private static StickerAppUpdateService instance;
    private final GetLatestAppVersionAPI getLatestAppVersionAPI;
    private final Executor executor;
    private final UiThreadPoster threadResultPoster;

    private StickerAppUpdateService() {
        this.getLatestAppVersionAPI = new GetLatestAppVersionImpl();
        this.executor = Executors.newSingleThreadExecutor();
        this.threadResultPoster = new AndroidUiThreadPoster();
    }

    public static StickerAppUpdateService getInstance() {
        if (instance == null) {
            instance = new StickerAppUpdateService();
        }
        return instance;
    }

    public void isNewVersionAvailable(CheckLatestVersionCallback callback) {
        executor.execute(() -> {
            boolean newUpdateAvailable;
            ResponseAPIAppLatestVersion.Version version;
            try {
                ResponseAPIAppLatestVersion responseAPIAppLatestVersion = getLatestAppVersionAPI.get();
                validateApiResponse(responseAPIAppLatestVersion);

                newUpdateAvailable = !responseAPIAppLatestVersion.getVersion().getLatestVersion().equals(BuildConfig.VERSION_NAME);
                version = responseAPIAppLatestVersion.getVersion();
            } catch (StickerHttpClientException ex) {
                throw new RuntimeException(ex);
            }

            if (newUpdateAvailable) {
                threadResultPoster.post(() -> callback.onUpdateAvailable(version));
            }
        });
    }

    private static void validateApiResponse(ResponseAPIAppLatestVersion responseAPIAppLatestVersion) {
        if (responseAPIAppLatestVersion.getVersion() == null || responseAPIAppLatestVersion.getVersion().getLatestVersion() == null) {
            throw new RuntimeException("Erro ao validar nova atualização: " + responseAPIAppLatestVersion.getMessage());
        }
    }

    public interface CheckLatestVersionCallback {
        void onUpdateAvailable(ResponseAPIAppLatestVersion.Version version);
    }

}
