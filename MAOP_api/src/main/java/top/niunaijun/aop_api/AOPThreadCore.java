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

    public static void runUIThread(final Class<?> clazz, final String method, final Object target, final Object[] args, final String[] paramClazz) {
        runUIThread(clazz, method, target, args, paramClazz, 0);
    }

    public static void runUIThread(final Class<?> clazz, final String method, final Object target, final Object[] args, final String[] paramClazz, long delay) {
        sHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    Class<?>[] classes = new Class[paramClazz.length];
                    for (int i = 0; i < paramClazz.length; i++) {
                        classes[i] = parseClass(paramClazz[i]);
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

    public static void delayAsyncThread(final Class<?> clazz, final String method, final Object target, final Object[] args, final String[] paramClazz, final long delay) {
        if (delay <= 0) {
            runAsyncThread(clazz, method, target, args, paramClazz);
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
                runAsyncThread(clazz, method, target, args, paramClazz);
            }
        });
    }

    public static void runAsyncThread(final Class<?> clazz, final String method, final Object target, final Object[] args, final String[] paramClazz) {
        sExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Class<?>[] classes = new Class[paramClazz.length];
                    for (int i = 0; i < paramClazz.length; i++) {
                        classes[i] = parseClass(paramClazz[i]);
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

    private static Class<?> parseClass(String className) throws ClassNotFoundException {
        int count = 0;
        while (className.contains("[]")) {
            className = className.replaceFirst("[\\[]", "").replaceFirst("[\\]]", "");
            count++;
        }

        String base;
        if (count == 0) {
            base = parseBaseClass(className);
            if (base != null)
                return Class.forName(base);
            return Class.forName(className);
        } else {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < count; i++) {
                builder.append("[");
            }
            base = parseBaseClass(className);
            if (base == null){
                builder.append("L").append(className).append(";");
            }else{
                builder.append(base);
            }
            return Class.forName(builder.toString());
        }
    }

    private static String  parseBaseClass(String clazz) {
        switch (clazz) {
            case "int":
                return "I";
            case "byte":
                return "B";
            case "short":
                return "S";
            case "long":
                return "J";
            case "float":
                return "F";
            case "double":
                return "D";
            case "boolean":
                return "Z";
            case "char":
                return "C";
        }
        return null;
    }
}
