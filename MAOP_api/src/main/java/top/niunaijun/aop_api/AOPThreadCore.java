package top.niunaijun.aop_api;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Arrays;

import top.niunaijun.aop_api.executor.ArchTaskExecutor;

public class AOPThreadCore {

    public static void runUIThread(final Class<?> clazz, final String method, final Object target, final Object[] args, final String[] paramClazz) {
        runUIThread(clazz, method, target, args, paramClazz, 0);
    }

    public static void delayAsyncThread(final Class<?> clazz, final String method, final Object target, final Object[] args, final String[] paramClazz, final long delay) {
        if (delay <= 0) {
            runAsyncThread(clazz, method, target, args, paramClazz);
            return;
        }
        ArchTaskExecutor.getInstance().executeOnAsync(new Runnable() {
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

    public static void runUIThread(final Class<?> clazz, final String method, Object target, Object[] args, final String[] paramClazz, long delay) {
        Call.create().runUIThread(clazz, method, target, args, paramClazz, delay);
    }

    public static void runAsyncThread(final Class<?> clazz, final String method, final Object target, final Object[] args, final String[] paramClazz) {
        Call.create().runAsyncThread(clazz, method, target, args, paramClazz);
    }

    static class Call {
        public static Call create() {
            return new Call();
        }

        public void runAsyncThread(final Class<?> clazz, final String method, Object target, final Object[] args, final String[] paramClazz) {
            final boolean staticV = target == null;
            final WeakReference<?> targetWeak = new WeakReference<>(target);
            ArchTaskExecutor.getInstance().executeOnAsync(new Runnable() {
                @Override
                public void run() {
                    call(clazz, method, targetWeak, args, paramClazz, staticV);
                }
            });
        }

        public void runUIThread(final Class<?> clazz, final String method, Object target, final Object[] args, final String[] paramClazz, long delay) {
            final boolean staticV = target == null;
            final WeakReference<?> targetWeak = new WeakReference<>(target);
            ArchTaskExecutor.getInstance().postToMainThreadDelayed(new Runnable() {
                @Override
                public void run() {
                    call(clazz, method, targetWeak, args, paramClazz, staticV);
                }
            }, delay);
        }
    }

    private static void call(final Class<?> clazz, final String method, WeakReference<?> target, Object[] args, final String[] paramClazz, boolean staticV) {
        try {
            Class<?>[] classes = new Class[paramClazz.length];
            for (int i = 0; i < paramClazz.length; i++) {
                classes[i] = parseClass(paramClazz[i]);
            }
            Method method1 = clazz.getDeclaredMethod(method, classes);
            method1.setAccessible(true);

            Object targetIns = target.get();
            if (targetIns == null && !staticV)
                return;
            method1.invoke(targetIns, args);
            Arrays.fill(args, null);
        } catch (IllegalArgumentException l) {
            l.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static Class<?> parseClass(String className) throws ClassNotFoundException {
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
