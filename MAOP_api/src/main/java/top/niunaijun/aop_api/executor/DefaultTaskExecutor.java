package top.niunaijun.aop_api.executor;

import android.os.Handler;
import android.os.Looper;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Milk on 2020/10/13.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * 此处无Bug
 */
public class DefaultTaskExecutor implements TaskExecutor {

    private final Object mLock = new Object();

    private final ExecutorService mAsync = Executors.newCachedThreadPool(new ThreadFactory() {
        private static final String THREAD_NAME_STEM = "arch_async_%d";

        private final AtomicInteger mThreadId = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setName(String.format(Locale.CHINA, THREAD_NAME_STEM, mThreadId.getAndIncrement()));
            return t;
        }
    });

    private volatile Handler mMainHandler;

    @Override
    public void executeOnAsync(Runnable runnable) {
        mAsync.execute(runnable);
    }

    @Override
    public void postToMainThread(Runnable runnable) {
        createHandler();
        mMainHandler.post(runnable);
    }

    @Override
    public void postToMainThreadDelayed(Runnable runnable, long delay) {
        createHandler();
        mMainHandler.postDelayed(runnable, delay);
    }

    @Override
    public boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    private void createHandler() {
        if (mMainHandler == null) {
            synchronized (mLock) {
                if (mMainHandler == null) {
                    mMainHandler = new Handler(Looper.getMainLooper());
                }
            }
        }
    }
}
