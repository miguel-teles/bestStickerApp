package io.github.miguelteles.beststickerapp.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.webkit.MimeTypeMap;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;

import io.github.miguelteles.beststickerapp.domain.entity.Sticker;
import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.StickerFolderException;
import io.github.miguelteles.beststickerapp.exception.enums.StickerExceptionEnum;
import io.github.miguelteles.beststickerapp.services.FileResourceManagement;
import io.github.miguelteles.beststickerapp.services.StickerPackService;
import io.github.miguelteles.beststickerapp.services.StickerService;
import io.github.miguelteles.beststickerapp.services.client.ImageConverterWebpAPIImpl;
import io.github.miguelteles.beststickerapp.services.client.VideoConverterWebpAPIImpl;
import io.github.miguelteles.beststickerapp.services.interfaces.ResourcesManagement;
import io.github.miguelteles.beststickerapp.services.interfaces.operationcallback.OnProgressUpdate;
import io.github.miguelteles.beststickerapp.services.interfaces.operationcallback.OperationCallback;
import io.github.miguelteles.beststickerapp.services.mediaconvertion.StickerImageConvertionService;
import io.github.miguelteles.beststickerapp.services.mediaconvertion.StickerVideoConvertionService;
import io.github.miguelteles.beststickerapp.utils.Utils;
import io.github.miguelteles.beststickerapp.validator.StickerPackValidator;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.LOLLIPOP) // Lollipop is API 21
public class StickerAndStickerPackCreationIntegrationTest {

    StickerPackService stickerPackService;
    StickerService stickerService;

    Uri stickerPackImage;
    Exception exception;
    ResourcesManagement resourcesManagement;

    @Before
    public void init() throws StickerException {
        stickerPackImage = Uri.fromFile(new File("src/test/resources/io/github/miguelteles/beststickerapp/integration/test_image.jpg"));

        resourcesManagement = new FileResourceManagement(
                ApplicationProvider.getApplicationContext(),
                ApplicationProvider.getApplicationContext().getContentResolver()
        );
        StickerImageConvertionService stickerImageConvertionService = new StickerImageConvertionService(
                resourcesManagement,
                ImageConverterWebpAPIImpl.getInstance(),
                ApplicationProvider.getApplicationContext().getContentResolver()
        );
        StickerVideoConvertionService stickerVideoConvertionService = new StickerVideoConvertionService(
                resourcesManagement,
                VideoConverterWebpAPIImpl.getInstance(),
                ApplicationProvider.getApplicationContext().getContentResolver()
        );

        stickerService = new StickerService(new StickerMockRepository(),
                resourcesManagement,
                ApplicationProvider.getApplicationContext().getContentResolver(),
                StickerPackValidator.getInstance(),
                stickerImageConvertionService,
                stickerVideoConvertionService);
        stickerPackService = new StickerPackService(resourcesManagement,
                new StickerPackMockRepository(),
                ApplicationProvider.getApplicationContext().getContentResolver(),
                stickerService,
                StickerPackValidator.getInstance(),
                stickerImageConvertionService);

        shadowOf(MimeTypeMap.getSingleton()).addExtensionMimeTypMapping("jpg", "image/jpeg");
        shadowOf(MimeTypeMap.getSingleton()).addExtensionMimeTypMapping("mp4", "video/mp4");
        shadowOf(MimeTypeMap.getSingleton()).addExtensionMimeTypMapping("gif", "image/gif");

    }

    /* ============= */
    /*  StickerPack  */

    @Test
    public void staticStickerPackCreationTest() throws StickerException {
        StickerPack createdStickerPack = createStickerPack(false);

        assertCreatedValues(createdStickerPack);
    }

    @Test
    public void animatedStickerPackCreationTest() throws StickerException {
        StickerPack createdStickerPack = createStickerPack(true);

        assertCreatedValues(createdStickerPack);
    }

    private StickerPack createStickerPack(boolean isAnimatedStickerPack) throws StickerException {
        return stickerPackService.createStickerPack("teste",
                "teste",
                stickerPackImage,
                isAnimatedStickerPack,
                OnProgressUpdate.EMPTY);
    }

    private void assertCreatedValues(StickerPack createdStickerPack) throws StickerFolderException {
        assertNull(exception);
        assertNotNull(createdStickerPack);
        assertNotNull(createdStickerPack.getIdentifier());
        assertNotNull(createdStickerPack.getName());
        assertNotNull(createdStickerPack.getPublisher());
        assertNotNull(createdStickerPack.getOriginalTrayImageFile());
        assertNotNull(createdStickerPack.getResizedTrayImageFile());
        assertNotNull(createdStickerPack.getResizedTrayImageFileInBytes());
        assertNotNull(createdStickerPack.getImageDataVersion());
        assertNotNull(createdStickerPack.getFolderName());

        Uri stickerPackDirectory = this.resourcesManagement.getOrCreateStickerPackDirectory(createdStickerPack.getFolderName());

        Uri originalTrayImageFileUri = this.resourcesManagement.getOrCreateFile(stickerPackDirectory, createdStickerPack.getOriginalTrayImageFile());
        Uri resizedTrayImageFileUri = this.resourcesManagement.getOrCreateFile(stickerPackDirectory, createdStickerPack.getResizedTrayImageFile());

        Bitmap originalTrayImageBitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(this.resourcesManagement.getContentAsBytes(originalTrayImageFileUri)));
        assertNotNull(originalTrayImageBitmap);

        Bitmap resizedTrayImageBitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(this.resourcesManagement.getContentAsBytes(resizedTrayImageFileUri)));
        assertNotNull(resizedTrayImageBitmap);
    }

    @Test
    public void stickerPackDeletionTest() throws StickerException {
        StickerPack createdStickerPack = createStickerPack(false);
        stickerPackService.deleteStickerPack(createdStickerPack, OperationCallback.EMPTY);

        StickerPack stickerPack = stickerPackService.fetchStickerPackAssets(createdStickerPack);
        assertNull(stickerPack);
    }

    @Test
    public void stickerPackUpdateTest() throws StickerException {
        StickerPack createdStickerPack = createStickerPack(false);

        stickerPackService.updateStickerPack(createdStickerPack,
                "nome do autor atualizado",
                "nome atualizado",
                OperationCallback.EMPTY);

        assertEquals(createdStickerPack.getPublisher(), "nome do autor atualizado");
        assertEquals(createdStickerPack.getName(), "nome atualizado");
    }

    /*  StickerPack  */
    /* ============= */

    @Test
    public void addStaticStickerToStaticStickerPackTest() throws StickerException {
        StickerPack createdStickerPack = createStickerPack(false);

        Sticker sticker = addStaticStickerToPack(createdStickerPack);

        assertStickerValues(createdStickerPack, sticker);
    }

    @Test
    public void addMp4StickerToAnimatedStickerPackTest() throws StickerException {
        try (MockedStatic<Utils> mockedUtils = Mockito.mockStatic(Utils.class)) {
            Context context = mock(Context.class);
            mockedUtils.when(()->Utils.getApplicationContext()).thenReturn(context);
            when(context.getString(anyInt())).thenReturn("Arquivo não encontrado");

            StickerPack createdStickerPack = createStickerPack(true);

            Sticker sticker = addAnimatedStickerToPack(createdStickerPack, false);

            assertStickerValues(createdStickerPack, sticker);
        }
    }

    @Test
    public void addGifStickerToAnimatedStickerPackTest() throws StickerException {
        try (MockedStatic<Utils> mockedUtils = Mockito.mockStatic(Utils.class)){
            Context context = mock(Context.class);
            mockedUtils.when(()->Utils.getApplicationContext()).thenReturn(context);
            when(context.getString(anyInt())).thenReturn("Arquivo não encontrado");

            StickerPack createdStickerPack = createStickerPack(true);

            Sticker sticker = addAnimatedStickerToPack(createdStickerPack, true);

            assertStickerValues(createdStickerPack, sticker);
        }
    }

    @Test
    public void shouldFailWhen_addStaticStickerToAnimatedStickerPack() throws StickerException {
        StickerPack createdStickerPack = createStickerPack(true);

        StickerException exception = assertThrows(StickerException.class, () -> addStaticStickerToPack(createdStickerPack));
        assertEquals(StickerExceptionEnum.WTSP, exception.getStickerExceptionEnum());
    }

    @Test
    public void shouldFailWhen_addMp4StickerToStaticStickerPack() throws StickerException {
        StickerPack createdStickerPack = createStickerPack(false);

        StickerException exception = assertThrows(StickerException.class, () -> addAnimatedStickerToPack(createdStickerPack, false));
        assertEquals(StickerExceptionEnum.WTSP, exception.getStickerExceptionEnum());
    }

    @Test
    public void shouldFailWhen_addGifStickerToStaticStickerPack() throws StickerException {
        StickerPack createdStickerPack = createStickerPack(false);

        StickerException exception = assertThrows(StickerException.class, () -> addAnimatedStickerToPack(createdStickerPack, true));
        assertEquals(StickerExceptionEnum.WTSP, exception.getStickerExceptionEnum());
    }

    @Test
    public void deleteSticker() throws StickerException {
        StickerPack createdStickerPack = createStickerPack(false);
        Sticker createdSticker = addStaticStickerToPack(createdStickerPack);

        stickerService.deleteSticker(createdSticker, createdStickerPack);

        List<Sticker> stickersFromPack = stickerService.fetchAllStickerFromPackWithoutAssets(createdStickerPack.getIdentifier());

        assertTrue(stickersFromPack.isEmpty());
    }

    @Test
    public void fetchStickerPackAssetsTest() throws StickerException {
        StickerPack createdStickerPack = createStickerPack(false);
        addStaticStickerToPack(createdStickerPack);

        stickerPackService.fetchStickerPackAssets(createdStickerPack);

        assertNotNull(createdStickerPack.getResizedTrayImageFileInBytes());
        for (Sticker sticker : createdStickerPack.getStickers()) {
            assertNotNull(sticker.getStickerImageFileInBytes());
            assertTrue(sticker.getSize() > 0);
        }
    }

    private Sticker addAnimatedStickerToPack(StickerPack createdStickerPack,
                                             boolean isGif) throws StickerException {
        Uri stickerVideo;
        if (isGif) {
            stickerVideo = Uri.fromFile(new File("src/test/resources/io/github/miguelteles/beststickerapp/integration/test_gif.gif"));
        } else {
            stickerVideo = Uri.fromFile(new File("src/test/resources/io/github/miguelteles/beststickerapp/integration/test_video.mp4"));
        }

        return createSticker(createdStickerPack, stickerVideo);
    }

    public Sticker addStaticStickerToPack(StickerPack createdStickerPack) throws StickerException {
        Uri stickerImage = Uri.fromFile(new File("src/test/resources/io/github/miguelteles/beststickerapp/integration/test_image.jpg"));

        return createSticker(createdStickerPack, stickerImage);
    }

    private Sticker createSticker(StickerPack createdStickerPack,
                                  Uri stickerMedia) throws StickerException {
        return stickerService.createSticker(createdStickerPack,
                stickerMedia,
                OperationCallback.EMPTY);
    }

    private void assertStickerValues(StickerPack createdStickerPack, Sticker createdSticker) throws StickerFolderException {
        assertNull(exception);
        assertNotNull(createdSticker);
        assertNotNull(createdSticker.getIdentifier());
        assertNotNull(createdSticker.getPackIdentifier());
        assertNotNull(createdSticker.getStickerImageFileInBytes());
        assertNotNull(createdSticker.getStickerImageFile());
        assertTrue(createdSticker.getSize() != 0);

        Uri stickerPackDirectory = this.resourcesManagement.getOrCreateStickerPackDirectory(createdStickerPack.getFolderName());
        Uri stickerImageUri = this.resourcesManagement.getOrCreateFile(stickerPackDirectory, createdSticker.getStickerImageFile());

        Bitmap stickerImageBitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(this.resourcesManagement.getContentAsBytes(stickerImageUri)));
        assertNotNull(stickerImageBitmap);
    }

}
