package top.niunaijun.aop_api;

import android.os.Handler;
import android.os.Looper;

import java.lang.ref.WeakReference;
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

    public static void runUIThread(final Class<?> clazz, final String method, Object target, Object[] args, final String[] paramClazz, long delay) {
        final boolean staticV = target == null;
        final WeakReference<?> targetWeak = new WeakReference<>(target);
        final WeakReference<?>[] argsWeak = new WeakReference<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            argsWeak[i] = new WeakReference<>(args[i]);
        }
        sHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    call(clazz, method, targetWeak, argsWeak, paramClazz, staticV);
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
        final boolean staticV = target == null;
        final WeakReference<?> targetWeak = new WeakReference<>(target);
        final WeakReference<?>[] argsWeak = new WeakReference<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            argsWeak[i] = new WeakReference<>(args[i]);
        }
        sExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                call(clazz, method, targetWeak, argsWeak, paramClazz, staticV);
            }
        });
    }

    private static void call(final Class<?> clazz, final String method, WeakReference<?> target, WeakReference<?>[] args, final String[] paramClazz, boolean staticV) {
        try {
            Class<?>[] classes = new Class[paramClazz.length];
            for (int i = 0; i < paramClazz.length; i++) {
                classes[i] = parseClass(paramClazz[i]);
            }
            Method method1 = clazz.getDeclaredMethod(method, classes);
            method1.setAccessible(true);

            Object[] argsObj = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                argsObj[i] = args[i].get();
            }

            Object targetIns = target.get();
            if (targetIns == null && !staticV)
                return;
            method1.invoke(targetIns, argsObj);
            target.clear();
            for (WeakReference<?> weakReference : args) {
                weakReference.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static Class<?> parseClass(String className) throws ClassNotFoundException {
        System.out.println("find " + className);
        int count = 0;
        while (className.contains("[]")) {
            className = className.replaceFirst("[\\[]", "").replaceFirst("[\\]]", "");
            count++;
        }

        if (count == 0) {
            Class<?> base2 = parseBaseClass2(className);
            if (base2 != null)
                return base2;
            return Class.forName(className);
        } else {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < count; i++) {
                builder.append("[");
            }
            String base = parseBaseClass(className);
            if (base == null) {
                builder.append("L").append(className).append(";");
            } else {
                builder.append(base);
            }
            return Class.forName(builder.toString());
        }
    }

    private static Class<?> parseBaseClass2(String className) {
        switch (className) {
            case "int":
                return int.class;
            case "byte":
                return byte.class;
            case "short":
                return short.class;
            case "long":
                return long.class;
            case "float":
                return float.class;
            case "double":
                return double.class;
            case "boolean":
                return boolean.class;
            case "char":
                return char.class;
        }
        return null;
    }

    private static String parseBaseClass(String clazz) {
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
