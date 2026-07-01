package io.github.miguelteles.beststickerapp.unit.viewmodel;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;

import java.util.concurrent.Executor;

import io.github.miguelteles.beststickerapp.exception.StickerException;
import io.github.miguelteles.beststickerapp.services.interfaces.operationcallback.OperationCallback;
import io.github.miguelteles.beststickerapp.view.interfaces.UiThreadPoster;
import io.github.miguelteles.beststickerapp.view.threadHandlers.ImmediateUiThreadPoster;
import io.github.miguelteles.beststickerapp.viewmodel.ExecutorWithThreadResultPoster;

@RunWith(RobolectricTestRunner.class)
public class ExecutorWithThreadResultPosterTest {

    private Executor executor;
    private ImmediateUiThreadPoster uiThreadPoster;

    private ExecutorWithThreadResultPoster<String> sut;

    @Before
    public void setUp() {
        executor = Runnable::run;
        uiThreadPoster = new ImmediateUiThreadPoster();

        sut = new ExecutorWithThreadResultPoster<>(
                executor,
                uiThreadPoster
        );
    }

    @Test
    public void executeSupplier_shouldCallCallbackWithResult_whenNoException()
            throws StickerException {

        OperationCallback<String> callback = mock(OperationCallback.class);

        sut.execute(() -> "success", callback);

        verify(callback).onCreationFinish("success", null);
    }

    @Test
    public void executeSupplier_shouldCallCallbackWithException_whenSupplierThrows()
            throws StickerException {

        OperationCallback<String> callback = mock(OperationCallback.class);
        StickerException exception = mock(StickerException.class);

        sut.execute(() -> {
            throw exception;
        }, callback);

        verify(callback).onCreationFinish(null, exception);
    }

    @Test
    public void executeRunnable_shouldCallCallbackWithNullResult_whenNoException()
            throws StickerException {

        OperationCallback<String> callback = mock(OperationCallback.class);

        sut.execute(() -> {
            // do nothing
        }, callback);

        verify(callback).onCreationFinish(null, null);
    }

    @Test
    public void executeRunnable_shouldCallCallbackWithException_whenRunnableThrows()
            throws StickerException {

        OperationCallback<String> callback = mock(OperationCallback.class);
        StickerException exception = mock(StickerException.class);

        sut.execute(() -> {
            throw exception;
        }, callback);

        verify(callback).onCreationFinish(null, exception);
    }

    @Test
    public void executeSupplier_shouldPostResultUsingUiThreadPoster()
            throws StickerException {

        UiThreadPoster poster = mock(UiThreadPoster.class);

        ExecutorWithThreadResultPoster<String> executorWithPoster =
                new ExecutorWithThreadResultPoster<>(Runnable::run, poster);

        OperationCallback<String> callback = mock(OperationCallback.class);

        executorWithPoster.execute(() -> "result", callback);

        ArgumentCaptor<Runnable> runnableCaptor =
                ArgumentCaptor.forClass(Runnable.class);

        verify(poster).post(runnableCaptor.capture());

        runnableCaptor.getValue().run();

        verify(callback).onCreationFinish("result", null);
    }

}
