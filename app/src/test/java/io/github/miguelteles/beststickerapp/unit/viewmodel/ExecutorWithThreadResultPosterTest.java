package io.github.miguelteles.beststickerapp.unit.viewmodel;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.concurrent.Executor;

import io.github.miguelteles.beststickerapp.view.threadHandlers.ImmediateUiThreadPoster;
import io.github.miguelteles.beststickerapp.viewmodel.ExecutorWithThreadResultPoster;

@RunWith(RobolectricTestRunner.class)
public class ExecutorWithThreadResultPosterTest {

    private ExecutorWithThreadResultPoster<?> executorWithThreadResultPoster;

    @Before
    public void before() {
        executorWithThreadResultPoster = new ExecutorWithThreadResultPoster<>(new SameThreadExecutor(),
                new ImmediateUiThreadPoster());
    }

    public class SameThreadExecutor implements Executor {

        @Override
        public void execute(Runnable command) {
            command.run();
        }
    }

}
