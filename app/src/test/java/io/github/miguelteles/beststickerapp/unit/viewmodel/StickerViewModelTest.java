package io.github.miguelteles.beststickerapp.unit.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.net.Uri;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import io.github.miguelteles.beststickerapp.domain.entity.Sticker;
import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.services.StickerService;
import io.github.miguelteles.beststickerapp.services.interfaces.operationcallback.OperationCallback;
import io.github.miguelteles.beststickerapp.services.mediaconvertion.StickerVideoConvertionService;
import io.github.miguelteles.beststickerapp.utils.thread.StickerExceptionSupplier;
import io.github.miguelteles.beststickerapp.viewmodel.ExecutorWithThreadResultPoster;
import io.github.miguelteles.beststickerapp.viewmodel.StickerViewModel;

@RunWith(RobolectricTestRunner.class)
public class StickerViewModelTest {

    private StickerViewModel stickerViewModel;

    @Mock
    private ExecutorWithThreadResultPoster<Sticker> executorWithThreadResultPoster;
    @Mock
    private StickerService stickerService;
    @Mock
    private StickerPack mockedStickerPack;
    @Mock
    private Sticker mockedSticker;

    private AutoCloseable closeable;

    @Before
    public void before() {
        closeable = MockitoAnnotations.openMocks(this);

        stickerViewModel = new StickerViewModel(
                executorWithThreadResultPoster,
                stickerService
        );
    }

    @Test
    public void teste_getInstance() throws StickerException {
        try (MockedStatic<StickerService> mockedStickerService = Mockito.mockStatic(StickerService.class)){
            mockedStickerService.when(()->StickerService.getInstance()).thenReturn(mock(StickerService.class));
            StickerViewModel instance = StickerViewModel.getInstance();

            assertNotNull(instance);
        }
    }

    @Test
    public void teste_createSticker() throws StickerException {
        stickerViewModel.createSticker(mockedStickerPack,
                Uri.parse("file:///teste.png"),
                OperationCallback.getDefault());

        ArgumentCaptor<StickerExceptionSupplier> runnable = ArgumentCaptor.forClass(StickerExceptionSupplier.class);
        verify(executorWithThreadResultPoster).execute(
                runnable.capture(),
                any()
        );
        runnable.getValue().get();
        verify(stickerService).createSticker(eq(mockedStickerPack),
                any(Uri.class),
                any(OperationCallback.class));
    }

    @Test
    public void teste_deleteSticker() throws StickerException {
        stickerViewModel.deleteSticker(mockedSticker,
                mockedStickerPack);

        verify(stickerService).deleteSticker(mockedSticker,
                mockedStickerPack);
    }

    @Test
    public void teste_getMaxFileSizeAllowed() {
        assertEquals(
                StickerVideoConvertionService.MAX_FILE_SIZE_ALLOWED_IN_BYTES,
                stickerViewModel.getMaxFileSizeAllowed()
        );
    }

    @After
    public void tearDown() throws Exception {
        closeable.close();
    }

}
