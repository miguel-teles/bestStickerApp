package io.github.miguelteles.beststickerapp.unit.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.res.Resources;
import android.net.Uri;

import androidx.annotation.NonNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import java.util.UUID;

import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.repository.StickerPackRepository;
import io.github.miguelteles.beststickerapp.services.StickerPackService;
import io.github.miguelteles.beststickerapp.services.StickerService;
import io.github.miguelteles.beststickerapp.services.interfaces.ResourcesManagement;
import io.github.miguelteles.beststickerapp.services.interfaces.operationcallback.OnProgressUpdate;
import io.github.miguelteles.beststickerapp.services.mediaconvertion.StickerImageConvertionService;
import io.github.miguelteles.beststickerapp.validator.StickerPackValidator;

@RunWith(RobolectricTestRunner.class)
public class StickerPackServiceTest {

    @Mock
    ResourcesManagement resourcesManagement;
    @Mock
    StickerPackRepository stickerPackRepository;
    @Mock
    ContentResolver contentResolver;
    @Mock
    StickerService stickerService;
    @Mock
    StickerPackValidator stickerPackValidator;
    @Mock
    StickerImageConvertionService stickerImageConvertionService;
    @Mock
    Resources resources;
    @Mock
    Uri uri;

    StickerPackService stickerPackService = null;
    StickerPack validStickerPack = null;

    @Before
    public void mockingDepedencies() throws StickerException {
        MockitoAnnotations.initMocks(this);
        validStickerPack = createStickerPack();
        when(resourcesManagement.getOrCreateStickerPackDirectory(any(String.class))).then(new Answer<Uri>() {
            @Override
            public Uri answer(InvocationOnMock invocation) throws Throwable {
                return uri;
            }
        });
        when(stickerImageConvertionService.generateConvertedMedia(any(Uri.class),
                any(Uri.class),
                any(String.class),
                any(Integer.class),
                any(Boolean.class),
                eq(OnProgressUpdate.EMPTY))).then(new Answer<ResourcesManagement.Media>() {
            @Override
            public ResourcesManagement.Media answer(InvocationOnMock invocation) throws Throwable {
                return ResourcesManagement.Media.builder()
                        .originalImageFile(uri)
                        .convertedMedia(new byte[]{1, 1, 1, 1, 1, 1, 1, 1})
                        .build();
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
                return "service";
            }
        });
        when(stickerPackRepository.update(any(StickerPack.class))).then(new Answer<StickerPack>() {
            @Override
            public StickerPack answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArgument(0);
            }
        });
        when(resourcesManagement.saveFileToDevice(any(Uri.class), anyString(), any()))
                .then(new Answer<Object>() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        return Uri.parse("file:///teste.png");
                    }
                });

        stickerPackService = new StickerPackService(resourcesManagement,
                stickerPackRepository,
                contentResolver,
                stickerService,
                stickerPackValidator,
                stickerImageConvertionService);
    }

    @Test
    public void testCreateStickerPack() throws StickerException {
        StickerPack stickerPack = stickerPackService.createStickerPack("teste", "teste", uri, false, OnProgressUpdate.EMPTY);
        assertNotNull(stickerPack.getName());
        assertNotNull(stickerPack.getIdentifier());
        assertNotNull(stickerPack.getFolderName());
        assertNotNull(stickerPack.getOriginalTrayImageFile());
        assertNotNull(stickerPack.getResizedTrayImageFile());
        assertNotNull(stickerPack.getResizedTrayImageFileInBytes());
        assertNotNull(stickerPack.getImageDataVersion());
        assertNotNull(stickerPack.getPublisher());
    }

    @Test
    public void testUpdateAuthorNameAndPackNameStickerPack() throws StickerException {
        StickerPack stickerPack = stickerPackService.updateStickerPack(validStickerPack, "teste", "teste", OnProgressUpdate.EMPTY);
        assertNotNull(stickerPack.getName());
        assertNotNull(stickerPack.getIdentifier());
        assertNotNull(stickerPack.getFolderName());
        assertNotNull(stickerPack.getOriginalTrayImageFile());
        assertNotNull(stickerPack.getResizedTrayImageFile());
        assertNotNull(stickerPack.getImageDataVersion());
        assertNotNull(stickerPack.getPublisher());
    }

    @Test
    public void testUpdateStickerPackInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> stickerPackService.updateStickerPack(null,
                "teste",
                "teste",
                OnProgressUpdate.EMPTY));
        assertThrows(IllegalArgumentException.class, () -> stickerPackService.updateStickerPack(validStickerPack,
                "teste",
                null,
                OnProgressUpdate.EMPTY));
    }


    @NonNull
    private static StickerPack createStickerPack() {
        return new StickerPack(UUID.randomUUID(),
                "teste",
                "teste",
                "app/src/main/assets/test_image.jpg",
                "app/src/main/assets/test_image.jpg",
                "teste",
                1,
                true,
                null);
    }
}
