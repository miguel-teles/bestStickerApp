package io.github.miguelteles.beststickerapp.services;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.github.miguelteles.beststickerapp.BuildConfig;
import io.github.miguelteles.beststickerapp.domain.pojo.ResponseAPIGetDownloadAppUrl;
import io.github.miguelteles.beststickerapp.domain.pojo.Version;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.StickerFatalErrorException;
import io.github.miguelteles.beststickerapp.exception.StickerFolderException;
import io.github.miguelteles.beststickerapp.exception.StickerWebCommunicationException;
import io.github.miguelteles.beststickerapp.exception.enums.StickerExceptionEnum;
import io.github.miguelteles.beststickerapp.exception.enums.StickerWebCommunicationExceptionEnum;
import io.github.miguelteles.beststickerapp.services.client.GetDownloadAppUrlAPIImpl;
import io.github.miguelteles.beststickerapp.services.client.interfaces.GetDownloadAppUrlAPI;
import io.github.miguelteles.beststickerapp.services.client.interfaces.GetLatestAppVersionAPI;
import io.github.miguelteles.beststickerapp.services.interfaces.DownloadCallback;
import io.github.miguelteles.beststickerapp.services.interfaces.ResourcesManagement;
import io.github.miguelteles.beststickerapp.utils.Utils;
import io.github.miguelteles.beststickerapp.view.interfaces.UiThreadPoster;
import io.github.miguelteles.beststickerapp.view.threadHandlers.AndroidUiThreadPoster;

public class UpdateAppService {

    private final Executor executor;
    private final UiThreadPoster threadResultPoster;
    private final Version version;
    private final GetDownloadAppUrlAPI getDownloadAppUrlAPI;

    public UpdateAppService(Version version) throws StickerFatalErrorException {
        this.executor = Executors.newSingleThreadExecutor(); //roda criando uma nova  thread
        this.threadResultPoster = new AndroidUiThreadPoster();
        this.version = version;
        this.getDownloadAppUrlAPI = new GetDownloadAppUrlAPIImpl(Utils.getApplicationContext());
    }

    public void downloadUpdate(DownloadCallback downloadCallback) {
        executor.execute(() -> {
            try {
                downloadCallback.onProgressUpdate(5);
                ResponseAPIGetDownloadAppUrl downloadAppResponse = getDownloadAppUrlAPI.getDownloadAppUrl(version.getVersion());
                if (downloadAppResponse.getPresignedUrl() == null) {
                    throw new StickerException(null, StickerExceptionEnum.GUL, downloadAppResponse.getMessage());
                }
                downloadCallback.onProgressUpdate(10);

                URL url = new URL(downloadAppResponse.getPresignedUrl());
                URLConnection urlConnection = url.openConnection();
                long lenghtOfFile = urlConnection.getContentLength();

                File apkFile = new File(Utils.getApplicationContext().getExternalFilesDir(null) + "/update.apk");
                try (InputStream input = new BufferedInputStream(url.openStream(), 64 * 1024); // download the file
                     OutputStream output = new FileOutputStream(apkFile)) {

                    byte[] data = new byte[12 * 1024];
                    long totalByteCount = 0;
                    int byteCount;
                    while ((byteCount = input.read(data)) != -1) {
                        totalByteCount += byteCount;
                        long progress = (totalByteCount * 100) / lenghtOfFile;
                        if (progress > 10) {
                            downloadCallback.onProgressUpdate((int) progress);
                        }
                        output.write(data, 0, byteCount);
                    }
                    output.flush();
                }
                threadResultPoster.post(() -> downloadCallback.onProgressFinish(apkFile));
            } catch (Exception e) {
                threadResultPoster.post(() -> downloadCallback.onDownloadException(new StickerWebCommunicationException(e,
                        StickerWebCommunicationExceptionEnum.DOWNLOAD_UPDATE_EXCEPTION,
                        null)));
            }
        });
    }

}
