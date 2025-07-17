package io.github.miguelteles.beststickerapp.unit.service;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;

import io.github.miguelteles.beststickerapp.domain.entity.Sticker;
import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.repository.StickerRepository;
import io.github.miguelteles.beststickerapp.repository.contentProvider.StickerUriProvider;
import io.github.miguelteles.beststickerapp.services.FoldersManagementService;
import io.github.miguelteles.beststickerapp.services.StickerImageConvertionService;
import io.github.miguelteles.beststickerapp.services.StickerService;
import io.github.miguelteles.beststickerapp.services.interfaces.EntityOperationCallback;
import io.github.miguelteles.beststickerapp.utils.Utils;
import io.github.miguelteles.beststickerapp.validator.StickerPackValidator;

public class StickerServiceTest {

    StickerRepository stickerRepository = mock(StickerRepository.class);
    FoldersManagementService foldersManagementService = mock(FoldersManagementService.class);
    StickerUriProvider stickerUriProvider = mock(StickerUriProvider.class);
    ContentResolver contentResolver = mock(ContentResolver.class);
    StickerPackValidator stickerPackValidator = mock(StickerPackValidator.class);
    StickerImageConvertionService stickerImageConvertionService = mock(StickerImageConvertionService.class);
    StickerService stickerService = mock(StickerService.class);
    Uri uri = mock(Uri.class);
    Executor testExecutor = Runnable::run;

    EntityOperationCallback<Sticker> callback = new EntityOperationCallback<Sticker>() {
        @Override
        public void onCreationFinish(Sticker createdEntity, StickerException stickerException) {
            generatedSticker = createdEntity;
        }

        @Override
        public void onProgressUpdate(int process) {

        }

        @Override
        public void runProgressBarAnimation(int process) {

        }
    };

    Sticker generatedSticker = null;

    StickerPack validStickerPack = new StickerPack(UUID.randomUUID(),
            "teste",
            "teste",
            "app/src/main/assets/test_image.jpg",
            "app/src/main/assets/test_image.jpg",
            "teste",
            "teste",
            "teste",
            "teste",
            "teste",
            1,
            true,
            false);

    Sticker validSticker = new Sticker(UUID.randomUUID(), UUID.randomUUID(), "/home/miguel/StudioProjects/stickersProjeto/app/src/main/assets/test_image.jpg");



    @Before
    public void mockingDepedencies() throws StickerException {
        when(stickerRepository.save(any(Sticker.class))).then(new Answer<Sticker>() {
            @Override
            public Sticker answer(InvocationOnMock invocation) {
                Sticker sticker = invocation.getArgument(0);
                sticker.setIdentifier(UUID.randomUUID());
                sticker.setPackIdentifier(UUID.randomUUID());
                return sticker;
            }
        });
        when(foldersManagementService.getStickerPackFolderByFolderName(any(String.class))).then(new Answer<File>() {
            @Override
            public File answer(InvocationOnMock invocation) throws Throwable {
                return new File("hey");
            }
        });
        when(stickerImageConvertionService.generateStickerImages(any(File.class), any(Uri.class), any(String.class), any(Integer.class), any(Boolean.class))).then(new Answer<FoldersManagementService.Image>() {
            @Override
            public FoldersManagementService.Image answer(InvocationOnMock invocation) throws Throwable {
                return new FoldersManagementService.Image(new File("/home/miguel/StudioProjects/stickersProjeto/app/src/main/assets/test_image.jpg"),
                        new File("/home/miguel/StudioProjects/stickersProjeto/app/src/main/assets/test_image.jpg"),
                        new byte[]{1,1,1,1,1,1,1,1});
            }
        });
        when(stickerUriProvider.getStickerInsertUri()).then(new Answer<Uri>() {
            @Override
            public Uri answer(InvocationOnMock invocation) throws Throwable {
                return uri;
            }
        });
        when(contentResolver.insert(any(Uri.class), any(ContentValues.class))).then(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return null;
            }
        });
        Mockito.doNothing().when(stickerPackValidator).validateSticker(any(UUID.class), any(Sticker.class), any(Boolean.class));
        
        when(stickerRepository.findByPackIdentifier(any(UUID.class))).then(new Answer<List<Sticker>>() {
            @Override
            public List<Sticker> answer(InvocationOnMock invocation) throws Throwable {
                return List.of(validSticker.clone(), validSticker.clone(), validSticker.clone(), validSticker.clone());
            }
        });
        try {
            when(contentResolver.openInputStream(any(Uri.class))).then(new Answer<InputStream>() {
                @Override
                public InputStream answer(InvocationOnMock invocation) throws Throwable {
                    return Files.newInputStream(Paths.get(validSticker.getStickerImageFile()));
                }
            });
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
        when(stickerUriProvider.getStickerAssetUri(any(UUID.class), any(String.class))).then(new Answer<Uri>() {
            @Override
            public Uri answer(InvocationOnMock invocation) throws Throwable {
                return uri;
            }
        });
        when(foldersManagementService.readBytesFromInputStream(any(InputStream.class), any(String.class))).then(new Answer<byte[]>() {

            @Override
            public byte[] answer(InvocationOnMock invocation) throws Throwable {
                return new byte[]{1,11,11,1,1,1,11,1,1};
            }
        });

        stickerService = new StickerService(stickerRepository, foldersManagementService, stickerUriProvider, contentResolver, stickerPackValidator, stickerImageConvertionService, testExecutor);
    }

    @Test
    public void testCreateSticker() throws StickerException {
        stickerService.createSticker(validStickerPack, uri, callback);

        assertFalse("StickerImageFile is not null", Utils.isNothing(generatedSticker.getStickerImageFile()));
        assertNotNull("Sticker has identifier", generatedSticker.getPackIdentifier());
        assertTrue("Sticker size is greater then 0",generatedSticker.getSize()>0);
    }

    @Test
    public void testCreateStickerInputInput() {
        assertThrows("IllegalArgumentException thrown when stickerPack is null", IllegalArgumentException.class, () -> stickerService.createSticker(null, uri, callback));
        assertThrows("IllegalArgumentException thrown when selectedUriImage is null", IllegalArgumentException.class, () -> stickerService.createSticker(validStickerPack, null, callback));
        assertThrows("IllegalArgumentException thrown when callbackClass is null", IllegalArgumentException.class, () -> stickerService.createSticker(validStickerPack, uri, null));
    }

    @Test
    public void testDeleteSticker() throws StickerException {
        stickerService.deleteSticker(validSticker, validStickerPack);
    }

    @Test
    public void testDeleteStickerInputInvalido() throws StickerException {
        assertThrows("IllegalArgumentException thrown when sticker is null", IllegalArgumentException.class, () -> stickerService.deleteSticker(null, validStickerPack));
        assertThrows("IllegalArgumentException thrown when stickerPack is null", IllegalArgumentException.class, () -> stickerService.deleteSticker(validSticker, null));
    }

    @Test
    public void testFetchAllStickerFromPackWithAssets() throws StickerException {
        List<Sticker> stickers = stickerService.fetchAllStickerFromPackWithAssets(validStickerPack.getIdentifier());
        assertNotNull("Sticker list cannot be null, even thought the sticker pack is empty", stickers);
        for(Sticker sticker : stickers) {
            assertNotNull(sticker.getIdentifier());
            assertNotNull(sticker.getStickerImageFile());
            assertTrue(sticker.getSize()!=0);
            assertNotNull(sticker.getPackIdentifier());
            assertNotNull(sticker.getStickerImageFileInBytes());
        }
    }

    @Test
    public void testFetchAllStickerFromPackWithoutAssets() throws StickerException {
        List<Sticker> stickers = stickerService.fetchAllStickerFromPackWithoutAssets(validStickerPack.getIdentifier());
        assertNotNull("Sticker list cannot be null, even thought the sticker pack is empty", stickers);
        for (Sticker sticker : stickers) {
            assertNotNull(sticker.getIdentifier());
            assertNotNull(sticker.getStickerImageFile());
            assertTrue(sticker.getSize() == 0);
            assertNotNull(sticker.getPackIdentifier());
            assertNull(sticker.getStickerImageFileInBytes());
        }
    }

}
