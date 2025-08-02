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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.github.miguelteles.beststickerapp.BuildConfig;
import io.github.miguelteles.beststickerapp.domain.pojo.Version;
import io.github.miguelteles.beststickerapp.exception.StickerFolderException;
import io.github.miguelteles.beststickerapp.exception.StickerWebCommunicationException;
import io.github.miguelteles.beststickerapp.exception.enums.StickerWebCommunicationExceptionEnum;
import io.github.miguelteles.beststickerapp.services.interfaces.DownloadCallback;
import io.github.miguelteles.beststickerapp.services.interfaces.ResourcesManagement;
import io.github.miguelteles.beststickerapp.utils.Utils;
import io.github.miguelteles.beststickerapp.view.interfaces.UiThreadPoster;
import io.github.miguelteles.beststickerapp.view.threadHandlers.AndroidUiThreadPoster;

public class UpdateAppService {

    private final Executor executor;
    private final UiThreadPoster threadResultPoster;
    private final Version version;
    private final ResourcesManagement resourcesManagement;

    public UpdateAppService(Version version) {
        this.executor = Executors.newSingleThreadExecutor(); //roda criando uma nova  thread
        this.threadResultPoster = new AndroidUiThreadPoster();
        this.version = version;
        resourcesManagement = new FileResourceManagement(Utils.getApplicationContext(), Utils.getApplicationContext().getContentResolver());
    }

    public File verifyDownloadedApkVersion() {
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

    public void downloadUpdate(String versionName, DownloadCallback downloadCallback) {
        executor.execute(() -> {

            AWSCredentials credentials = new BasicAWSCredentials(BuildConfig.AWS_ACCESS_KEY, BuildConfig.AWS_SECRET_ACCESS_KEY);
            AmazonS3Client amazonS3Client = new AmazonS3Client(credentials);
            try (S3Object object = amazonS3Client.getObject(new GetObjectRequest(BuildConfig.AWS_S3_BUCKET_NAME_UPDATE_APP, versionName + "/app-release.apk"))) {
                S3ObjectInputStream objectContent = object.getObjectContent();

                long lenghtOfFile = object.getObjectMetadata().getContentLength();

                File apkFile = new File(Utils.getApplicationContext().getExternalFilesDir(null) + "/update.apk");
                try (InputStream input = new BufferedInputStream(objectContent, 64 * 1024); // download the file
                     OutputStream output = new FileOutputStream(apkFile)) {

                    byte[] data = new byte[12 * 1024];
                    long totalByteCount = 0;
                    int byteCount;
                    while ((byteCount = input.read(data)) != -1) {
                        totalByteCount += byteCount;
                        downloadCallback.onProgressUpdate((int) ((totalByteCount * 100) / lenghtOfFile));
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
