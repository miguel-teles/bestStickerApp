package io.github.miguelteles.beststickerapp.unit.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.ContentResolver;
import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.UUID;

import io.github.miguelteles.beststickerapp.domain.entity.Sticker;
import io.github.miguelteles.beststickerapp.domain.pojo.ResponseAPIConvertedWebp;
import io.github.miguelteles.beststickerapp.domain.pojo.ResponseAPIDownloadSourceVideoConverter;
import io.github.miguelteles.beststickerapp.domain.pojo.ResponseAPIUploadDestinationVideoConverter;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.services.client.interfaces.ImageConverterWebpAPI;
import io.github.miguelteles.beststickerapp.services.client.interfaces.VideoConverterWebpAPI;
import io.github.miguelteles.beststickerapp.services.interfaces.ResourcesManagement;
import io.github.miguelteles.beststickerapp.services.interfaces.operationcallback.OnProgressUpdate;
import io.github.miguelteles.beststickerapp.services.mediaconvertion.StickerImageConvertionService;
import io.github.miguelteles.beststickerapp.services.mediaconvertion.StickerVideoConvertionService;

@RunWith(RobolectricTestRunner.class)
public class StickerMediaConvertionServiceTest {

    @Mock
    ResourcesManagement resourcesManagement;
    @Mock
    ImageConverterWebpAPI imageConverterWebpAPI;
    @Mock
    VideoConverterWebpAPI videoConverterWebpAPI;
    @Mock
    ContentResolver contentResolver;

    StickerImageConvertionService stickerImageConvertionService;

    StickerVideoConvertionService stickerVideoConvertionService;

    Uri resultFolder = Uri.fromFile(new File("src/test/resources/io/github/miguelteles/beststickerapp/unit"));
    Uri stickerImage = Uri.fromFile(new File("src/test/resources/io/github/miguelteles/beststickerapp/unit/test_image.jpg"));
    Uri stickerVideo = Uri.fromFile(new File("src/test/resources/io/github/miguelteles/beststickerapp/unit/test_video.mp4"));
    Uri stickerGif = Uri.fromFile(new File("src/test/resources/io/github/miguelteles/beststickerapp/unit/test_video.gif"));

    @Before
    public void init() throws StickerException, FileNotFoundException {
        MockitoAnnotations.openMocks(this);
        when(this.resourcesManagement.getFileExtension(any(Uri.class), any(Boolean.class))).thenReturn(".jpg");
        when(this.resourcesManagement.getOrCreateFile(any(Uri.class), any(String.class))).then(new Answer<Uri>() {

            @Override
            public Uri answer(InvocationOnMock invocation) throws Throwable {
                return Uri.fromFile(new File(((Uri) invocation.getArgument(0)).getPath(), invocation.getArgument(1)));
            }
        });
        when(this.imageConverterWebpAPI.convertImageToWebp(any(String.class))).then(new Answer<ResponseAPIConvertedWebp>() {
            @Override
            public ResponseAPIConvertedWebp answer(InvocationOnMock invocation) throws Throwable {
                InputStream inputStream = this.getClass().getResourceAsStream("webpBase64.txt");

                return new ResponseAPIConvertedWebp(String.join("",
                        new String(inputStream.readAllBytes())));
            }
        });
        when(this.resourcesManagement.getFileFromURI(any(Uri.class))).thenReturn(new File("localhost"));
        when(this.contentResolver.openInputStream(any(Uri.class))).then(new Answer<InputStream>() {
            @Override
            public InputStream answer(InvocationOnMock invocation) throws Throwable {
                return this.getClass().getResourceAsStream("test_image.jpg");
            }
        });
        when(this.resourcesManagement.readBytesFromInputStream(any(InputStream.class))).thenReturn(
                new byte[]{100, 22, 5, 50, 5, 55, 50, 6, 5, 5}
        );
        when(this.resourcesManagement.getCacheFolder()).thenReturn(
                Uri.fromFile(new File("src/test/resources/io/github/miguelteles/beststickerapp/unit/"))
        );
        when(this.videoConverterWebpAPI.createUploadDestination(any(String.class))).thenReturn(
                new ResponseAPIUploadDestinationVideoConverter("signedurl", "outconvertedfilename")
        );
        when(videoConverterWebpAPI.createDownloadSource(anyString())).thenReturn(
                new ResponseAPIDownloadSourceVideoConverter("http://google.com")
        );

        stickerImageConvertionService = new StickerImageConvertionService(resourcesManagement,
                imageConverterWebpAPI,
                contentResolver);

        stickerVideoConvertionService = new StickerVideoConvertionService(resourcesManagement,
                videoConverterWebpAPI,
                contentResolver);
    }

    @Test
    public void testGenerateStaticStickerMedias() throws StickerException {
        ResourcesManagement.Media generatedStickerMedia = stickerImageConvertionService.generateConvertedMedia(resultFolder,
                stickerImage,
                UUID.randomUUID().toString(),
                96,
                true,
                OnProgressUpdate.EMPTY);

        assertNotNull(generatedStickerMedia.getOriginalImageFile());
        assertNotNull(generatedStickerMedia.getConvertedMedia());
    }

    @Test
    public void testGenerateAnimatedStickerMediasFromVideo() throws StickerException {
        ResourcesManagement.Media generatedStickerMedia = stickerVideoConvertionService.generateConvertedMedia(resultFolder,
                stickerVideo,
                UUID.randomUUID().toString(),
                OnProgressUpdate.EMPTY);

        assertNotNull(generatedStickerMedia.getLinkToDownloadMedia());
    }

    @Test
    public void testGenerateAnimatedStickerMediasFromGif() throws StickerException {
        ResourcesManagement.Media generatedStickerMedia = stickerVideoConvertionService.generateConvertedMedia(resultFolder,
                stickerGif,
                UUID.randomUUID().toString(),
                OnProgressUpdate.EMPTY);

        assertNotNull(generatedStickerMedia.getLinkToDownloadMedia());
    }

    @Test
    public void testGenerateStickerMediasInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> stickerImageConvertionService.generateConvertedMedia(null,
                stickerImage,
                UUID.randomUUID().toString(),
                96,
                true,
                null));

        assertThrows(IllegalArgumentException.class, () -> stickerImageConvertionService.generateConvertedMedia(resultFolder,
                null,
                UUID.randomUUID().toString(),
                96,
                true,
                null));

        assertThrows(IllegalArgumentException.class, () -> stickerImageConvertionService.generateConvertedMedia(resultFolder,
                stickerImage,
                null,
                96,
                true,
                null));

        assertThrows(IllegalArgumentException.class, () -> stickerImageConvertionService.generateConvertedMedia(resultFolder,
                stickerImage,
                UUID.randomUUID().toString(),
                0,
                true,
                null));
        assertThrows(IllegalArgumentException.class, () -> stickerImageConvertionService.generateConvertedMedia(resultFolder,
                stickerImage,
                UUID.randomUUID().toString(),
                null,
                true,
                null));

    }

    @Test
    public void testShouldDeleteFileWhenImageConvertionFails() throws StickerException {
        when(imageConverterWebpAPI.convertImageToWebp(anyString())).thenThrow(StickerException.class);

        assertThrows(StickerException.class, () -> stickerImageConvertionService.generateConvertedMedia(
                resultFolder,
                stickerVideo,
                UUID.randomUUID().toString(),
                Sticker.STICKER_IMAGE_SIZE,
                false,
                OnProgressUpdate.EMPTY
        ));

        verify(resourcesManagement).deleteFile(any());
    }

    @Test
    public void testShouldDeleteFileWhenVideoConvertionFails() throws StickerException {
        when(videoConverterWebpAPI.uploadVideo(anyString(), any())).thenThrow(StickerException.class);

        assertThrows(StickerException.class, () -> stickerVideoConvertionService.generateConvertedMedia(
                resultFolder,
                stickerVideo,
                UUID.randomUUID().toString(),
                OnProgressUpdate.EMPTY
        ));

        verify(resourcesManagement).deleteFile(any());
    }

}
