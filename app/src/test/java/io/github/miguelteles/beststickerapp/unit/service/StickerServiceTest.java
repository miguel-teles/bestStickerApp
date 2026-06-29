package io.github.miguelteles.beststickerapp.unit.service;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.net.URL;
import java.util.UUID;

import io.github.miguelteles.beststickerapp.domain.entity.Sticker;
import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;
import io.github.miguelteles.beststickerapp.domain.pojo.VisualMediaType;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.StickerFolderException;
import io.github.miguelteles.beststickerapp.repository.MyDatabase;
import io.github.miguelteles.beststickerapp.repository.StickerRepository;
import io.github.miguelteles.beststickerapp.services.client.ImageConverterWebpAPIImpl;
import io.github.miguelteles.beststickerapp.services.client.VideoConverterWebpAPIImpl;
import io.github.miguelteles.beststickerapp.services.interfaces.operationcallback.OnProgressUpdate;
import io.github.miguelteles.beststickerapp.services.mediaconvertion.StickerImageConvertionService;
import io.github.miguelteles.beststickerapp.services.StickerService;
import io.github.miguelteles.beststickerapp.services.interfaces.ResourcesManagement;
import io.github.miguelteles.beststickerapp.services.mediaconvertion.StickerVideoConvertionService;
import io.github.miguelteles.beststickerapp.utils.Utils;
import io.github.miguelteles.beststickerapp.utils.thread.StickerExceptionRunnable;
import io.github.miguelteles.beststickerapp.validator.StickerPackValidator;

/**
 * Métodos públicos:
 * - getInstance
 * - createSticker
 * - deleteSticker
 * - fetchAllStickerFromPackWithAssets
 * - fetchAllStickerFromPackWithoutAssets
 * - fetchStickerAsset
 */
@RunWith(RobolectricTestRunner.class)
public class StickerServiceTest {

    @Mock
    private StickerRepository stickerRepository;

    @Mock
    private ResourcesManagement resourcesManagement;

    @Mock
    private ContentResolver contentResolver;

    @Mock
    private StickerPackValidator stickerPackValidator;

    @Mock
    private StickerImageConvertionService imageConvertionService;

    @Mock
    private StickerVideoConvertionService videoConvertionService;

    @Mock
    private OnProgressUpdate progressUpdate;
    @Mock
    private Context context;

    private StickerService stickerService;

    private AutoCloseable closeable;

    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        stickerService = new StickerService(
                stickerRepository,
                resourcesManagement,
                contentResolver,
                stickerPackValidator,
                imageConvertionService,
                videoConvertionService
        );
    }

    @Test
    public void shouldGetInstance() throws StickerException {
        runWithMockedStatic(()-> {
            StickerService instance = StickerService.getInstance();

            assertNotNull(instance);
        });
    }

    @Test
    public void shouldBeSameInstance() throws StickerException {
        runWithMockedStatic(()-> {
            doReturn(contentResolver).when(context).getContentResolver();


            StickerService instance1 = StickerService.getInstance();
            StickerService instance2 = StickerService.getInstance();

            assertEquals(instance1, instance2);
        });
    }

    public void runWithMockedStatic(StickerExceptionRunnable runnable) throws StickerException {
        try (MockedStatic<Utils> utils = Mockito.mockStatic(Utils.class);
             MockedStatic<MyDatabase> mockMyDatabase = Mockito.mockStatic(MyDatabase.class);
             MockedStatic<ImageConverterWebpAPIImpl> mockImageConverterWebpAPIImpl = Mockito.mockStatic(ImageConverterWebpAPIImpl.class);
             MockedStatic<VideoConverterWebpAPIImpl> mockVideoConverterWebpAPIImpl = Mockito.mockStatic(VideoConverterWebpAPIImpl.class)
        ) {
            utils.when(() -> Utils.getApplicationContext()).thenReturn(context);
            mockMyDatabase.when(() -> MyDatabase.getInstance()).thenReturn(mock(MyDatabase.class));
            mockImageConverterWebpAPIImpl.when(() -> ImageConverterWebpAPIImpl.getInstance()).thenReturn(mock(ImageConverterWebpAPIImpl.class));
            mockVideoConverterWebpAPIImpl.when(() -> VideoConverterWebpAPIImpl.getInstance()).thenReturn(mock(VideoConverterWebpAPIImpl.class));

            runnable.run();
        }
    }

    @Test
    public void shouldCreateStaticSticker() throws Exception {
        StickerPack stickerPack = createMockedStickerPack(false);

        Uri selectedImage = Uri.parse("file:///input.png");
        Uri stickerFolder = Uri.parse("file:///stickers");
        Uri savedImage = Uri.parse("file:///stickers/sticker.webp");

        ResourcesManagement.Media media = buildStaticMedia();

        mockGetOrCreateStickerPackDirectory(stickerPack.getFolderName(), stickerFolder);
        mockGenerateConvertedStaticMedia(media);
        mockSaveFileToDevice(media, savedImage);
        when(resourcesManagement.getTypeOfVisualMedia(any())).thenReturn(VisualMediaType.IMAGE);

        Sticker result = stickerService.createSticker(
                stickerPack,
                selectedImage,
                progressUpdate);

        assertNotNull(result);

        verify(stickerRepository).save(any(Sticker.class));
        verify(stickerPackValidator).validateSticker(eq(stickerPack.getIdentifier()), any(Sticker.class), anyBoolean());
    }

    @Test
    public void shouldCreateAnimatedSticker() throws Exception {
        StickerPack stickerPack = createMockedStickerPack(true);

        Uri selectedImage = Uri.parse("file:///input.png");
        Uri stickerFolder = Uri.parse("file:///stickers");
        Uri savedImage = Uri.parse("file:///stickers/sticker.webp");

        ResourcesManagement.Media media = buildAnimatedMedia();

        mockGetOrCreateStickerPackDirectory("mypack", stickerFolder);
        mockGenerateConvertedAnimatedMedia(media);
        mockSaveFileToDevice(media, savedImage);
        mockGetOrCreateFile(stickerFolder, savedImage);
        byte[] stickerInBytes = {1, 2, 3, 5};
        mockContentAsBytes(stickerInBytes);
        when(resourcesManagement.getTypeOfVisualMedia(any())).thenReturn(VisualMediaType.GIF);

        Sticker result = stickerService.createSticker(
                stickerPack,
                selectedImage,
                progressUpdate);

        assertNotNull(result);
        assertEquals(result.getPackIdentifier(), stickerPack.getIdentifier());
        assertEquals(result.getStickerImageFileInBytes(), stickerInBytes);

        verify(stickerRepository).save(any(Sticker.class));
        verify(stickerPackValidator)
                .validateSticker(eq(stickerPack.getIdentifier()), any(Sticker.class), anyBoolean());
    }

    private void mockContentAsBytes(byte[] stickerInBytes) throws StickerFolderException {
        doReturn(stickerInBytes).when(resourcesManagement).getContentAsBytes(any());
    }

    private void mockGetOrCreateFile(Uri stickerFolder, Uri savedImage) throws StickerFolderException {
        doReturn(savedImage).when(resourcesManagement).getOrCreateFile(eq(stickerFolder), anyString());
    }

    private void mockGenerateConvertedStaticMedia(ResourcesManagement.Media media) throws StickerException {
        when(imageConvertionService.generateConvertedMedia(
                any(),
                any(),
                anyString(),
                eq(Sticker.STICKER_IMAGE_SIZE),
                eq(false),
                any()))
                .thenReturn(media);
    }

    private void mockGenerateConvertedAnimatedMedia(ResourcesManagement.Media media) throws StickerException {
        when(videoConvertionService.generateConvertedMedia(
                any(),
                any(),
                anyString(),
                any()))
                .thenReturn(media);
    }

    private static ResourcesManagement.Media buildStaticMedia() {
        return ResourcesManagement.Media
                .builder()
                .convertedMedia(new byte[]{1, 2, 3})
                .build();
    }

    private static ResourcesManagement.Media buildAnimatedMedia() throws Exception {
        return ResourcesManagement.Media
                .builder()
                .linkToDownloadMedia(new URL("http://google.com"))
                .build();
    }

    private void mockSaveFileToDevice(ResourcesManagement.Media media, Uri savedImage) throws StickerFolderException {
        when(resourcesManagement.saveFileToDevice(
                any(),
                anyString(),
                eq(media.getConvertedMedia())))
                .thenReturn(savedImage);
    }

    private void mockGetOrCreateStickerPackDirectory(String folderName, Uri stickerFolder) {
        doReturn(stickerFolder)
                .when(resourcesManagement)
                .getOrCreateStickerPackDirectory(folderName);
    }

    @NonNull
    private static StickerPack createMockedStickerPack(Boolean isAnimated) {
        UUID packId = UUID.randomUUID();

        StickerPack stickerPack = mock(StickerPack.class);
        when(stickerPack.isStandardStickerPack()).thenReturn(!isAnimated);
        when(stickerPack.getIdentifier()).thenReturn(packId);
        when(stickerPack.getFolderName()).thenReturn("mypack");
        return stickerPack;
    }

    @After
    public void tearDown() throws Exception {
        closeable.close();
    }
}
