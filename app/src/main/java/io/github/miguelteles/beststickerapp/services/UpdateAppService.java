package io.github.miguelteles.beststickerapp.services;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.github.miguelteles.beststickerapp.BuildConfig;
import io.github.miguelteles.beststickerapp.exception.StickerWebCommunicationException;
import io.github.miguelteles.beststickerapp.exception.enums.StickerWebCommunicationExceptionEnum;
import io.github.miguelteles.beststickerapp.services.interfaces.DownloadCallback;
import io.github.miguelteles.beststickerapp.utils.Utils;
import io.github.miguelteles.beststickerapp.view.interfaces.UiThreadPoster;
import io.github.miguelteles.beststickerapp.view.threadHandlers.AndroidUiThreadPoster;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

public class UpdateAppService {

    private final Executor executor;
    private final UiThreadPoster threadResultPoster;

    public UpdateAppService() {
        this.executor = Executors.newSingleThreadExecutor(); //roda criando uma nova  thread
        this.threadResultPoster = new AndroidUiThreadPoster();
    }

    public void downloadUpdate(String versionName, DownloadCallback downloadCallback) {
        executor.execute(() -> {
            int count;
            AwsBasicCredentials credentials = AwsBasicCredentials.create(BuildConfig.AWS_ACCESS_KEY, BuildConfig.AWS_SECRET_ACCESS_KEY);
            try (S3Client s3Client = S3Client.builder()
                    .region(Region.of(BuildConfig.AWS_S3_BUCKET_REGION_UPDATE_APP))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build()) {

                ResponseInputStream<GetObjectResponse> object = s3Client.getObject(GetObjectRequest.builder()
                        .bucket(BuildConfig.AWS_S3_BUCKET_NAME_UPDATE_APP)
                        .key(versionName + "/app-release.apk")
                        .build());
                Long lenghtOfFile = object.response().contentLength();

                try (InputStream input = new BufferedInputStream(object, 8192); // download the file
                     OutputStream output = new FileOutputStream(Utils.getApplicationContext().getExternalFilesDir(null) + "/update.apk")) { // Output stream

                    byte[] data = new byte[1024];
                    long total = 0;
                    while ((count = input.read(data)) != -1) {
                        total += count;
                        downloadCallback.onProgressUpdate((int) ((total * 100) / lenghtOfFile));
                        output.write(data, 0, count);
                    }
                    output.flush();
                }
            } catch (Exception e) {
                threadResultPoster.post(() -> downloadCallback.onDownloadException(new StickerWebCommunicationException(e,
                        StickerWebCommunicationExceptionEnum.DOWNLOAD_UPDATE_EXCEPTION,
                        null)));
                ;
            }
        });
    }

}
