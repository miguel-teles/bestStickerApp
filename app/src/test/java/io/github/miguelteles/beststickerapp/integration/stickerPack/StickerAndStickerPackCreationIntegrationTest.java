package io.github.miguelteles.beststickerapp.integration.stickerPack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;

import io.github.miguelteles.beststickerapp.domain.entity.Sticker;
import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.StickerFolderException;
import io.github.miguelteles.beststickerapp.services.FileResourceManagement;
import io.github.miguelteles.beststickerapp.services.StickerImageConvertionService;
import io.github.miguelteles.beststickerapp.services.StickerPackService;
import io.github.miguelteles.beststickerapp.services.StickerService;
import io.github.miguelteles.beststickerapp.services.client.ImageConverterWebpAPIImpl;
import io.github.miguelteles.beststickerapp.services.interfaces.OperationCallback;
import io.github.miguelteles.beststickerapp.services.interfaces.ResourcesManagement;
import io.github.miguelteles.beststickerapp.validator.StickerPackValidator;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.LOLLIPOP) // Lollipop is API 21
public class StickerAndStickerPackCreationIntegrationTest {

    StickerPackService stickerPackService;
    StickerService stickerService;

    Uri stickerPackImage;
    Uri stickerImage;
    StickerPack createdStickerPack;
    Sticker createdSticker;
    Exception exception;
    OperationCallback<StickerPack> stickerPackCreationCallback;
    ResourcesManagement resourcesManagement;

    @Before
    public void init() throws StickerException {
        stickerPackImage = Uri.fromFile(new File("src/test/resources/io/github/miguelteles/beststickerapp/unit/service/test_image.jpg"));

        resourcesManagement = new FileResourceManagement(ApplicationProvider.getApplicationContext(),
                ApplicationProvider.getApplicationContext().getContentResolver());
        StickerImageConvertionService stickerImageConvertionService = new StickerImageConvertionService(resourcesManagement,
                new ImageConverterWebpAPIImpl(ApplicationProvider.getApplicationContext()),
                ApplicationProvider.getApplicationContext().getContentResolver());

        stickerService = new StickerService(new StickerMockRepository(),
                resourcesManagement,
                ApplicationProvider.getApplicationContext().getContentResolver(),
                StickerPackValidator.getInstance(),
                stickerImageConvertionService);
        stickerPackService = new StickerPackService(resourcesManagement,
                new StickerPackMockRepository(),
                ApplicationProvider.getApplicationContext().getContentResolver(),
                stickerService,
                StickerPackValidator.getInstance(),
                stickerImageConvertionService,
                ApplicationProvider.getApplicationContext().getResources());

        stickerPackCreationCallback = new OperationCallback<StickerPack>() {
            @Override
            public void onCreationFinish(StickerPack createdEntity, StickerException stickerException) {
                createdStickerPack = createdEntity;
                exception = stickerException;
            }

            @Override
            public void onProgressUpdate(int process) {

            }
        };
    }

    @Test
    public void stickerPackCreationTest() throws StickerFolderException {
        stickerPackService.createStickerPack("teste",
                "teste",
                stickerPackImage,
                stickerPackCreationCallback);

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

    public void createStickerPackWithStickers() throws StickerException {
        final StickerPack[] finalStickerPackArray = {null};
        stickerPackService.createStickerPack("teste",
                "teste",
                stickerPackImage,
                new OperationCallback<StickerPack>() {
                    @Override
                    public void onCreationFinish(StickerPack createdEntity, StickerException stickerException) {
                        finalStickerPackArray[0] = createdStickerPack;
                    }

                    @Override
                    public void onProgressUpdate(int process) {
                    }
                });

        final StickerPack createdStickerPack = finalStickerPackArray[0];

    }

    @Test
    public void stickerPackDeletionTest() throws StickerException {
        stickerPackCreationTest();

        stickerPackService.deleteStickerPack(createdStickerPack, new OperationCallback<StickerPack>() {
            @Override
            public void onCreationFinish(StickerPack createdEntity, StickerException stickerException) {
            }

            @Override
            public void onProgressUpdate(int process) {
            }
        });

        StickerPack stickerPack = stickerPackService.fetchStickerPackAssets(createdStickerPack);
        assertNull(stickerPack);
    }

    @Test
    public void stickerPackUpdateTest() throws StickerException {
        stickerPackCreationTest();

        stickerPackService.updateStickerPack(createdStickerPack,
                "nome do autor atualizado",
                "nome atualizado",
                new OperationCallback<StickerPack>() {
                    @Override
                    public void onCreationFinish(StickerPack createdEntity, StickerException stickerException) {
                        createdStickerPack = createdEntity;
                        exception = stickerException;
                    }

                    @Override
                    public void onProgressUpdate(int process) {
                    }
                });

        assertEquals(createdStickerPack.getPublisher(), "nome do autor atualizado");
        assertEquals(createdStickerPack.getName(), "nome atualizado");
    }

    @Test
    public void addStickerToStickerPack() throws StickerException {
        stickerPackCreationTest();
        stickerImage = Uri.fromFile(new File("src/test/resources/io/github/miguelteles/beststickerapp/unit/service/test_image.jpg"));

        stickerPackService.createSticker(createdStickerPack,
                stickerImage,
                new OperationCallback<Sticker>() {
                    @Override
                    public void onCreationFinish(Sticker createdEntity, StickerException stickerException) {
                        createdSticker = createdEntity;
                    }

                    @Override
                    public void onProgressUpdate(int process) {
                    }
                });

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

    @Test
    public void deleteSticker() throws StickerException {
        addStickerToStickerPack();

        stickerPackService.deleteSticker(createdSticker, createdStickerPack);

        List<Sticker> stickersFromPack = stickerService.fetchAllStickerFromPackWithoutAssets(createdStickerPack.getIdentifier());

        assertTrue(stickersFromPack.isEmpty());
    }

    @Test
    public void fetchStickerPackAssetsTest() throws StickerException {
        addStickerToStickerPack();

        stickerPackService.fetchStickerPackAssets(createdStickerPack);

        assertNotNull(createdStickerPack.getResizedTrayImageFileInBytes());
        for (Sticker sticker : createdStickerPack.getStickers()) {
            assertNotNull(sticker.getStickerImageFileInBytes());
            assertTrue(sticker.getSize() > 0);
        }
    }


}
