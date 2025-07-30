package io.github.miguelteles.beststickerapp.unit.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.io.Files;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import io.github.miguelteles.beststickerapp.domain.pojo.ResponseAPIConvertedWebp;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.StickerFolderException;
import io.github.miguelteles.beststickerapp.services.StickerImageConvertionService;
import io.github.miguelteles.beststickerapp.services.client.interfaces.ImageConverterWebpAPI;
import io.github.miguelteles.beststickerapp.services.interfaces.ResourcesManagement;

@RunWith(RobolectricTestRunner.class)
public class StickerImageConvertionServiceTest {

    @Mock
    ResourcesManagement resourcesManagement;
    @Mock
    ImageConverterWebpAPI imageConverterWebpAPI;
    @Mock
    ContentResolver contentResolver;

    StickerImageConvertionService stickerImageConvertionService;

    Uri resultFolder = Uri.fromFile(new File("src/test/resources/io/github/miguelteles/beststickerapp/unit/service/"));
    Uri stickerImage = Uri.fromFile(new File("src/test/resources/io/github/miguelteles/beststickerapp/unit/service/test_image.jpg"));

    @Before
    public void init() throws StickerException, FileNotFoundException {
        MockitoAnnotations.openMocks(this);
        when(this.resourcesManagement.getFileExtension(any(Uri.class), any(Boolean.class))).then(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return ".jpg";
            }
        });
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
        when(this.contentResolver.openInputStream(any(Uri.class))).then(new Answer<InputStream>() {
            @Override
            public InputStream answer(InvocationOnMock invocation) throws Throwable {
                return this.getClass().getResourceAsStream("test_image.jpg");
            }
        });
        when(this.resourcesManagement.readBytesFromInputStream(any(InputStream.class))).then(new Answer<byte[]>() {
            @Override
            public byte[] answer(InvocationOnMock invocation) throws Throwable {
                return new byte[]{100, 22, 5, 50, 5, 55, 50, 6, 5, 5};
            }
        });
        when(this.resourcesManagement.getCacheFolder()).then(new Answer<Uri>() {
            @Override
            public Uri answer(InvocationOnMock invocation) throws Throwable {
                return Uri.fromFile(new File("src/test/resources/io/github/miguelteles/beststickerapp/unit/service/"));
            }
        });

        stickerImageConvertionService = new StickerImageConvertionService(resourcesManagement,
                imageConverterWebpAPI,
                contentResolver);
    }

    @Test
    public void testGenerateStickerImages() throws StickerException {
        ResourcesManagement.Image generatedStickerImage = stickerImageConvertionService.generateStickerImages(resultFolder,
                stickerImage,
                "generatedStickerImage",
                96,
                true);

        assertTrue(generatedStickerImage.originalImageFile() != null);
        assertTrue(generatedStickerImage.resizedImageFile() != null);
        assertTrue(generatedStickerImage.residezImageFileInBytes() != null);


        generatedStickerImage = stickerImageConvertionService.generateStickerImages(resultFolder,
                stickerImage,
                "generatedStickerImage",
                96,
                false);

        assertFalse(generatedStickerImage.originalImageFile() != null);
        assertTrue(generatedStickerImage.resizedImageFile() != null);
        assertTrue(generatedStickerImage.residezImageFileInBytes() != null);
    }

    @Test
    public void testGenerateStickerImagesInvalidInput() throws StickerException {
        assertThrows(IllegalArgumentException.class, () -> stickerImageConvertionService.generateStickerImages(null,
                stickerImage,
                "generatedStickerImage",
                96,
                true));

        assertThrows(IllegalArgumentException.class, () -> stickerImageConvertionService.generateStickerImages(resultFolder,
                null,
                "generatedStickerImage",
                96,
                true));

        assertThrows(IllegalArgumentException.class, () -> stickerImageConvertionService.generateStickerImages(resultFolder,
                stickerImage,
                null,
                96,
                true));

        assertThrows(IllegalArgumentException.class, () -> stickerImageConvertionService.generateStickerImages(resultFolder,
                stickerImage,
                "generatedStickerImage",
                0,
                true));
        assertThrows(IllegalArgumentException.class, () -> stickerImageConvertionService.generateStickerImages(resultFolder,
                stickerImage,
                "generatedStickerImage",
                null,
                true));

    }
}
