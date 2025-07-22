package io.github.miguelteles.beststickerapp.unit.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.res.Resources;
import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.Executor;

import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.repository.StickerPackRepository;
import io.github.miguelteles.beststickerapp.repository.contentProvider.StickerUriProvider;
import io.github.miguelteles.beststickerapp.services.StickerImageConvertionService;
import io.github.miguelteles.beststickerapp.services.StickerPackService;
import io.github.miguelteles.beststickerapp.services.StickerService;
import io.github.miguelteles.beststickerapp.services.interfaces.EntityOperationCallback;
import io.github.miguelteles.beststickerapp.services.interfaces.ResourcesManagement;
import io.github.miguelteles.beststickerapp.validator.StickerPackValidator;

public class StickerPackServiceTest {

    ResourcesManagement resourcesManagement = mock(ResourcesManagement.class);
    StickerUriProvider stickerUriProvider = mock(StickerUriProvider.class);
    StickerPackRepository stickerPackRepository = mock(StickerPackRepository.class);
    ContentResolver contentResolver = mock(ContentResolver.class);
    StickerService stickerService = mock(StickerService.class);
    StickerPackValidator stickerPackValidator = mock(StickerPackValidator.class);
    StickerImageConvertionService stickerImageConvertionService = mock(StickerImageConvertionService.class);

    Resources resources = mock(Resources.class);
    Uri uri = mock(Uri.class);
    Executor testExecutor = Runnable::run;

    EntityOperationCallback<StickerPack> callback = new EntityOperationCallback<>() {
        @Override
        public void onCreationFinish(StickerPack createdStickerPack, StickerException stickerException) {
            generatedStickerPack = createdStickerPack;
        }

        @Override
        public void onProgressUpdate(int process) {
            //do nothing...
        }
    };

    StickerPack generatedStickerPack = null;

    StickerPackService stickerPackService = null;

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

    @Before
    public void mockingDepedencies() throws StickerException {
        MockitoAnnotations.initMocks(this);
        when(resourcesManagement.getOrCreateStickerPackDirectory(any(String.class))).then(new Answer<Uri>() {
            @Override
            public Uri answer(InvocationOnMock invocation) throws Throwable {
                return uri;
            }
        });
        when(stickerImageConvertionService.generateStickerImages(any(Uri.class), any(Uri.class), any(String.class), any(Integer.class), any(Boolean.class))).then(new Answer<ResourcesManagement.Image>() {
            @Override
            public ResourcesManagement.Image answer(InvocationOnMock invocation) throws Throwable {
                return new ResourcesManagement.Image(uri,
                        uri,
                        new byte[]{1, 1, 1, 1, 1, 1, 1, 1});
            }
        });
        when(uri.getLastPathSegment()).then(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return "falagalera.jpg";
            }
        });
        Mockito.doNothing().when(stickerPackValidator).verifyStickerPackValidity(any(StickerPack.class));
        when(stickerPackRepository.save(any(StickerPack.class))).then(new Answer<StickerPack>() {
            @Override
            public StickerPack answer(InvocationOnMock invocation) throws Throwable {
                StickerPack pack = invocation.getArgument(0);
                pack.setIdentifier(UUID.randomUUID());
                return pack;
            }
        });
        when(contentResolver.insert(any(Uri.class), any(ContentValues.class))).then(new Answer<Uri>() {
            @Override
            public Uri answer(InvocationOnMock invocation) throws Throwable {
                return uri;
            }
        });
        when(resources.getString(any(Integer.class))).then(new Answer<String>() {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return "test";
            }
        });
        when(stickerPackRepository.update(any(StickerPack.class))).then(new Answer<StickerPack>() {
            @Override
            public StickerPack answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArgument(0);
            }
        });

        stickerPackService = new StickerPackService(resourcesManagement,
                stickerUriProvider,
                stickerPackRepository,
                contentResolver,
                stickerService,
                stickerPackValidator,
                stickerImageConvertionService,
                resources,
                testExecutor);
    }

    @Test
    public void testCreateStickerPack() {
        stickerPackService.createStickerPack("teste", "teste", uri, callback);
        assertNotNull(generatedStickerPack.getName());
        assertNotNull(generatedStickerPack.getIdentifier());
        assertNotNull(generatedStickerPack.getFolderName());
        assertNotNull(generatedStickerPack.getOriginalTrayImageFile());
        assertNotNull(generatedStickerPack.getResizedTrayImageFile());
        assertNotNull(generatedStickerPack.getResizedTrayImageFileInBytes());
        assertNotNull(generatedStickerPack.getImageDataVersion());
        assertNotNull(generatedStickerPack.getPublisher());

        stickerPackService.createStickerPack(null, "teste", uri, callback);
        assertNotNull(generatedStickerPack.getName());
        assertNotNull(generatedStickerPack.getIdentifier());
        assertNotNull(generatedStickerPack.getFolderName());
        assertNotNull(generatedStickerPack.getOriginalTrayImageFile());
        assertNotNull(generatedStickerPack.getResizedTrayImageFile());
        assertNotNull(generatedStickerPack.getResizedTrayImageFileInBytes());
        assertNotNull(generatedStickerPack.getImageDataVersion());
        assertNotNull(generatedStickerPack.getPublisher());
    }

    @Test
    public void testCreateStickerPackInvalidInput() {

        assertThrows("IllegalArgumentException thrown when packName is empty",
                IllegalArgumentException.class,
                () -> stickerPackService.createStickerPack("teste", "", uri, callback));
        assertThrows("IllegalArgumentException thrown when packName is null",
                IllegalArgumentException.class,
                () -> stickerPackService.createStickerPack("teste", null, uri, callback));
        assertThrows("IllegalArgumentException thrown when uri is null",
                IllegalArgumentException.class,
                () -> stickerPackService.createStickerPack("teste", "teste", null, callback));
        assertThrows("IllegalArgumentException thrown when callbackClass is null",
                IllegalArgumentException.class,
                () -> stickerPackService.createStickerPack("teste", "teste", uri, null));
    }

    @Test
    public void testUpdateStickerPack() throws StickerException {
        stickerPackService.updateStickerPack(validStickerPack, "teste", "teste", callback);
        assertNotNull(generatedStickerPack.getName());
        assertNotNull(generatedStickerPack.getIdentifier());
        assertNotNull(generatedStickerPack.getFolderName());
        assertNotNull(generatedStickerPack.getOriginalTrayImageFile());
        assertNotNull(generatedStickerPack.getResizedTrayImageFile());
        assertNotNull(generatedStickerPack.getImageDataVersion());
        assertNotNull(generatedStickerPack.getPublisher());

        stickerPackService.updateStickerPack(validStickerPack, null, "teste", callback);
        assertNotNull(generatedStickerPack.getName());
        assertNotNull(generatedStickerPack.getIdentifier());
        assertNotNull(generatedStickerPack.getFolderName());
        assertNotNull(generatedStickerPack.getOriginalTrayImageFile());
        assertNotNull(generatedStickerPack.getResizedTrayImageFile());
        assertNotNull(generatedStickerPack.getImageDataVersion());
        assertNotNull(generatedStickerPack.getPublisher());


    }

    @Test
    public void testUpdateStickerPackInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> stickerPackService.updateStickerPack(null,
                "teste",
                "teste", callback));
        assertThrows(IllegalArgumentException.class, () -> stickerPackService.updateStickerPack(validStickerPack,
                "teste",
                null, callback));
    }

}
