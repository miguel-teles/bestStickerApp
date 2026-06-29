package io.github.miguelteles.beststickerapp.unit.viewmodel;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import java.util.List;

import io.github.miguelteles.beststickerapp.domain.entity.Sticker;
import io.github.miguelteles.beststickerapp.domain.entity.StickerPack;
import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.services.StickerPackService;
import io.github.miguelteles.beststickerapp.services.interfaces.operationcallback.OperationCallback;
import io.github.miguelteles.beststickerapp.utils.thread.StickerExceptionRunnable;
import io.github.miguelteles.beststickerapp.utils.thread.StickerExceptionSupplier;
import io.github.miguelteles.beststickerapp.viewmodel.ExecutorWithThreadResultPoster;
import io.github.miguelteles.beststickerapp.viewmodel.StickerPackViewModel;

@RunWith(RobolectricTestRunner.class)
public class StickerPackViewModelTest {

    private StickerPackViewModel stickerPackViewModel;

    @Mock
    private ExecutorWithThreadResultPoster<StickerPack> executorWithThreadResultPoster;
    @Mock
    private StickerPackService stickerPackService;
    @Mock
    private StickerPack stickerPack;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);

        stickerPackViewModel = new StickerPackViewModel(
                executorWithThreadResultPoster,
                stickerPackService
        );
    }

    @Test
    public void teste_getInstance() throws StickerException {
        try (MockedStatic<StickerPackService> mockedStickerPackService = Mockito.mockStatic(StickerPackService.class)){
            mockedStickerPackService.when(()->StickerPackService.getInstance()).thenReturn(mock(StickerPackService.class));
            StickerPackViewModel instance = StickerPackViewModel.getInstance();

            assertNotNull(instance);
        }
    }

    @Test
    public void teste_createStickerPack() throws StickerException {
        Uri stickerPackLogo = Uri.parse("file:///teste.png");

        stickerPackService.createStickerPack("authorName",
                "packName",
                stickerPackLogo,
                true,
                OperationCallback.getDefault());

        ArgumentCaptor<StickerExceptionSupplier> supplier = ArgumentCaptor.forClass(StickerExceptionSupplier.class);
        verify(executorWithThreadResultPoster).execute(
                supplier.capture(),
                any()
        );
        supplier.getValue().get();
        verify(stickerPackService).createStickerPack(eq("authorName"),
                eq("packName"),
                eq(stickerPackLogo),
                true,
                any(OperationCallback.class));
    }

    @Test
    public void teste_updateStickerPack() throws StickerException {
        stickerPackViewModel.updateStickerPack(stickerPack,
                "authorName",
                "packName",
                OperationCallback.getDefault());

        ArgumentCaptor<StickerExceptionSupplier> supplier = ArgumentCaptor.forClass(StickerExceptionSupplier.class);
        verify(executorWithThreadResultPoster).execute(
                supplier.capture(),
                any()
        );
        supplier.getValue().get();
        verify(stickerPackService).updateStickerPack(stickerPack,
                eq("authorName"),
                eq("packName"),
                any(OperationCallback.class));
    }

    @Test
    public void teste_fetchStickerPackAsserts() throws StickerException {
        when(stickerPackService.fetchStickerPackAssets(stickerPack)).then(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                StickerPack stickerPack = invocation.getArgument(0);
                stickerPack.setStickers(List.of(mock(Sticker.class)));
                return stickerPack;
            }
        });

        StickerPack pack = stickerPackViewModel.fetchStickerPackAssets(stickerPack);

        assertNotNull(pack.getStickers());
        verify(executorWithThreadResultPoster, never()).execute(any(StickerExceptionSupplier.class), any());
        verify(executorWithThreadResultPoster, never()).execute(any(StickerExceptionRunnable.class), any());
    }

    @Test
    public void teste_deleteStickerPack() throws StickerException {
        stickerPackViewModel.deleteStickerPack(stickerPack,
                OperationCallback.getDefault());

        ArgumentCaptor<StickerExceptionRunnable> supplier = ArgumentCaptor.forClass(StickerExceptionRunnable.class);
        verify(executorWithThreadResultPoster).execute(
                supplier.capture(),
                any()
        );
        supplier.getValue().run();
        verify(stickerPackService).deleteStickerPack(stickerPack,
                any(OperationCallback.class));
    }

}
