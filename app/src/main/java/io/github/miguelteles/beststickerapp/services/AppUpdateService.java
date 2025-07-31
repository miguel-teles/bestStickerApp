package io.github.miguelteles.beststickerapp.services;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.github.miguelteles.beststickerapp.BuildConfig;
import io.github.miguelteles.beststickerapp.domain.pojo.ResponseAPIAppLatestVersion;
import io.github.miguelteles.beststickerapp.domain.pojo.Version;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.StickerFatalErrorException;
import io.github.miguelteles.beststickerapp.exception.StickerHttpClientException;
import io.github.miguelteles.beststickerapp.services.client.GetLatestAppVersionAPIImpl;
import io.github.miguelteles.beststickerapp.services.client.interfaces.GetLatestAppVersionAPI;
import io.github.miguelteles.beststickerapp.utils.Utils;
import io.github.miguelteles.beststickerapp.view.interfaces.UiThreadPoster;
import io.github.miguelteles.beststickerapp.view.threadHandlers.AndroidUiThreadPoster;

public class AppUpdateService {

    private static AppUpdateService instance;
    private final GetLatestAppVersionAPI getLatestAppVersionAPI;
    private final Executor executor;
    private final UiThreadPoster threadResultPoster;

    private AppUpdateService() throws StickerFatalErrorException {
        this.getLatestAppVersionAPI = new GetLatestAppVersionAPIImpl(Utils.getApplicationContext());
        this.executor = Executors.newSingleThreadExecutor();
        this.threadResultPoster = new AndroidUiThreadPoster();
    }

    public static AppUpdateService getInstance() throws StickerFatalErrorException {
        if (instance == null) {
            instance = new AppUpdateService();
        }
        return instance;
    }

    public void isNewVersionAvailable(CheckLatestVersionCallback callback) {
        executor.execute(() -> {
            boolean newUpdateAvailable;
            Version version;
            try {
                ResponseAPIAppLatestVersion responseAPIAppLatestVersion = getLatestAppVersionAPI.get();
                validateApiResponse(responseAPIAppLatestVersion);

                newUpdateAvailable = !responseAPIAppLatestVersion.getLatestVersion().getVersion().equals(BuildConfig.VERSION_NAME);
                version = responseAPIAppLatestVersion.getLatestVersion();
            } catch (StickerException ex) {
                throw new RuntimeException(ex);
            }

            if (newUpdateAvailable) {
                threadResultPoster.post(() -> callback.onUpdateAvailable(version));
            }
        });
    }

    private static void validateApiResponse(ResponseAPIAppLatestVersion responseAPIAppLatestVersion) {
        if (responseAPIAppLatestVersion.getLatestVersion() == null || responseAPIAppLatestVersion.getLatestVersion().getVersion() == null) {
            throw new RuntimeException("Erro ao validar nova atualização: " + responseAPIAppLatestVersion.getMessage());
        }
    }

    public interface CheckLatestVersionCallback {
        void onUpdateAvailable(Version version);
    }

}
