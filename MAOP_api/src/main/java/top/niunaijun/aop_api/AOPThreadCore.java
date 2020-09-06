package top.niunaijun.aop_api;

import android.os.Handler;
import android.os.Looper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AOPThreadCore {
    private static ExecutorService sExecutorService = Executors.newCachedThreadPool();
    private static Handler sHandler = new Handler(Looper.getMainLooper());

    public static void runUIThread(final Class<?> clazz, final String method, final Object target, final Object[] args) {
        runUIThread(clazz, method, target, args, 0);
    }

    public static void runUIThread(final Class<?> clazz, final String method, final Object target, final Object[] args, long delay) {
        sHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    Class<?>[] classes = new Class[args.length];
                    for (int i = 0; i < args.length; i++) {
                        classes[i] = args.getClass();
                    }
                    Method method1 = clazz.getDeclaredMethod(method, classes);
                    method1.setAccessible(true);
                    method1.invoke(target, args);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }, delay);
    }

    public static void delayAsyncThread(final Class<?> clazz, final String method, final Object target, final Object[] args, final long delay) {
        if (delay <= 0) {
            runAsyncThread(clazz, method, target, args);
            return;
        }
        sExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runAsyncThread(clazz, method, target, args);
            }
        });
    }

    public static void runAsyncThread(final Class<?> clazz, final String method, final Object target, final Object[] args) {
        sExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Class<?>[] classes = new Class[args.length];
                    for (int i = 0; i < args.length; i++) {
                        classes[i] = args.getClass();
                    }
                    Method method1 = clazz.getDeclaredMethod(method, classes);
                    method1.setAccessible(true);
                    method1.invoke(target, args);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        });
    }

}
