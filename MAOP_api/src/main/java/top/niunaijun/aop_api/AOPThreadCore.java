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

    public static void runUIThread(final String className, final String method, final Object target, final Object[] args, final String[] paramClazz) {
        sHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Class<?>[] classes = new Class[paramClazz.length];
                    for (int i = 0; i < paramClazz.length; i++) {
                        classes[i] = Class.forName(paramClazz[i]);
                    }
                    Method method1 = Class.forName(className).getDeclaredMethod(method, classes);
                    method1.setAccessible(true);
                    method1.invoke(target, args);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void runAsyncThread(final String className, final String method, final Object target, final Object[] args, final String[] paramClazz) {
        sExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Class<?>[] classes = new Class[paramClazz.length];
                    for (int i = 0; i < paramClazz.length; i++) {
                        classes[i] = Class.forName(paramClazz[i]);
                    }
                    Method method1 = Class.forName(className).getDeclaredMethod(method, classes);
                    method1.setAccessible(true);
                    method1.invoke(target, args);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
