package io.github.miguelteles.beststickerapp.unit.service;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.res.Resources;
import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.exception.StickerFolderException;
import io.github.miguelteles.beststickerapp.repository.StickerPackRepository;
import io.github.miguelteles.beststickerapp.repository.contentProvider.StickerUriProvider;
import io.github.miguelteles.beststickerapp.services.StickerPackServiceImpl;
import io.github.miguelteles.beststickerapp.services.interfaces.FoldersManagementService;
import io.github.miguelteles.beststickerapp.services.interfaces.StickerPackService;
import io.github.miguelteles.beststickerapp.services.interfaces.StickerService;
import io.github.miguelteles.beststickerapp.validator.StickerPackValidator;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.File;

public class StickerPackServiceTest {

    FoldersManagementService foldersManagementService = mock(FoldersManagementService.class);
    StickerUriProvider stickerUriProvider = mock(StickerUriProvider.class);
    StickerPackRepository stickerPackRepository = mock(StickerPackRepository.class);
    ContentResolver contentResolver = mock(ContentResolver.class);
    StickerService stickerService = mock(StickerService.class);
    StickerPackValidator stickerPackValidator = mock(StickerPackValidator.class);

    Resources resources = mock(Resources.class);
    Uri uri = mock(Uri.class);

    StickerPackService stickerPackService = new StickerPackServiceImpl(foldersManagementService,
            stickerUriProvider,
            stickerPackRepository,
            contentResolver,
            stickerService,
            stickerPackValidator,
            resources);

    StickerPack validStickerPack = new StickerPack(1,
            "teste",
            "teste",
            "app/src/main/assets/testImage.jpeg",
            "app/src/main/assets/testImage.jpeg",
            "teste",
            "teste",
            "teste",
            "teste",
            "teste",
            "1",
            true,
            false);

    @Before
    public void mockingDepedencies() throws StickerException {
        when(foldersManagementService.getStickerPackFolderByFolderName(any(String.class))).then(new Answer<File>() {
            @Override
            public File answer(InvocationOnMock invocation) throws Throwable {
                return new File("/home/miguel/StudioProjects/stickersProjeto/app/src/main/assets/testImage.jpeg");
            }
        });
        when(foldersManagementService.generateStickerImages(any(File.class), any(Uri.class), any(String.class), any(Integer.class), any(Boolean.class))).then(new Answer<FoldersManagementService.Image>() {
            @Override
            public FoldersManagementService.Image answer(InvocationOnMock invocation) throws Throwable {
                return new FoldersManagementService.Image(new File("/home/miguel/StudioProjects/stickersProjeto/app/src/main/assets/testImage.jpeg"),
                        new File("/home/miguel/StudioProjects/stickersProjeto/app/src/main/assets/testImage.jpeg"),
                        new byte[]{1, 1, 1, 1, 1, 1, 1, 1});
            }
        });
        Mockito.doNothing().when(stickerPackValidator).verifyStickerPackValidity(any(StickerPack.class));
        when(stickerPackRepository.save(any(StickerPack.class))).then(new Answer<StickerPack>() {
            @Override
            public StickerPack answer(InvocationOnMock invocation) throws Throwable {
                StickerPack pack = invocation.getArgument(0);
                pack.setIdentifier(1);
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
    }

    @Test
    public void testCreateStickerPack() throws StickerException {
        StickerPack stickerPack = stickerPackService.createStickerPack("teste", "teste", uri);
        assertNotNull(stickerPack.getName());
        assertNotNull(stickerPack.getIdentifier());
        assertNotNull(stickerPack.getFolderName());
        assertNotNull(stickerPack.getOriginalTrayImageFile());
        assertNotNull(stickerPack.getResizedTrayImageFile());
        assertNotNull(stickerPack.getResizedTrayImageFileInBytes());
        assertNotNull(stickerPack.getImageDataVersion());
        assertNotNull(stickerPack.getPublisher());

        stickerPack = stickerPackService.createStickerPack(null, "teste", uri);
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
    public void testCreateStickerPackInvalidInput() {

        assertThrows("IllegalArgumentException thrown when packName is empty",
                IllegalArgumentException.class,
                () -> stickerPackService.createStickerPack("teste", "", uri));
        assertThrows("IllegalArgumentException thrown when packName is null",
                IllegalArgumentException.class,
                () -> stickerPackService.createStickerPack("teste", null, uri));
        assertThrows("IllegalArgumentException thrown when uri is null",
                IllegalArgumentException.class,
                () -> stickerPackService.createStickerPack("teste", "teste", null));
    }

    @Test
    public void testUpdateStickerPack() throws StickerException {
        StickerPack stickerPack = stickerPackService.updateStickerPack(validStickerPack, "teste", "teste");
        assertNotNull(stickerPack.getName());
        assertNotNull(stickerPack.getIdentifier());
        assertNotNull(stickerPack.getFolderName());
        assertNotNull(stickerPack.getOriginalTrayImageFile());
        assertNotNull(stickerPack.getResizedTrayImageFile());
        assertNotNull(stickerPack.getImageDataVersion());
        assertNotNull(stickerPack.getPublisher());

        stickerPack = stickerPackService.updateStickerPack(validStickerPack, null, "teste");
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
                "teste"));
        assertThrows(IllegalArgumentException.class, () -> stickerPackService.updateStickerPack(validStickerPack,
                "teste",
                null));
    }

}
