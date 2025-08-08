package io.github.miguelteles.beststickerapp.services;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.github.miguelteles.beststickerapp.BuildConfig;
import io.github.miguelteles.beststickerapp.domain.pojo.ResponseAPIAppLatestVersion;
import io.github.miguelteles.beststickerapp.domain.pojo.Version;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.StickerFatalErrorException;
import io.github.miguelteles.beststickerapp.exception.StickerFolderException;
import io.github.miguelteles.beststickerapp.services.client.GetLatestAppVersionAPIImpl;
import io.github.miguelteles.beststickerapp.services.client.interfaces.GetLatestAppVersionAPI;
import io.github.miguelteles.beststickerapp.services.interfaces.ResourcesManagement;
import io.github.miguelteles.beststickerapp.utils.Utils;
import io.github.miguelteles.beststickerapp.view.interfaces.UiThreadPoster;
import io.github.miguelteles.beststickerapp.view.threadHandlers.AndroidUiThreadPoster;

public class VersionValidatorService {

    private static VersionValidatorService instance;
    private final GetLatestAppVersionAPI getLatestAppVersionAPI;
    private final Executor executor;
    private final UiThreadPoster threadResultPoster;
    private final ResourcesManagement resourcesManagement;

    private VersionValidatorService() throws StickerFatalErrorException {
        this.getLatestAppVersionAPI = new GetLatestAppVersionAPIImpl(Utils.getApplicationContext());
        this.executor = Executors.newSingleThreadExecutor();
        this.threadResultPoster = new AndroidUiThreadPoster();
        this.resourcesManagement = new FileResourceManagement(Utils.getApplicationContext(), Utils.getApplicationContext().getContentResolver());
    }

    public static VersionValidatorService getInstance() throws StickerFatalErrorException {
        if (instance == null) {
            instance = new VersionValidatorService();
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

    public File verifyDownloadedApkVersion(Version version) {
        try {
            Uri apkFile = resourcesManagement.getFile(Utils.getApplicationContext().getExternalFilesDir(null).getPath(), "update.apk");

            PackageManager pm = Utils.getApplicationContext().getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(apkFile.getPath(), 0);

            if (info != null) {
                String versionName = info.versionName;
                if (version.getVersion().equals(versionName)) {
                    return new File(apkFile.getPath());
                }
            }
            return null;
        } catch (StickerFolderException e) {
            return null;
        }
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
