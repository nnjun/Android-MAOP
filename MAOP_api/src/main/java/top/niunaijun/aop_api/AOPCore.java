package top.niunaijun.aop_api;

import android.content.Context;

public class AOPCore {

    private static AOPListener sListener;
    private static Context sContext;

    public static void register(AOPListener listener) {
        sListener = listener;
    }

    public static boolean intercept(String name) {
        if (sListener == null)
            throw new ExceptionInInitializerError("Register needs to be called.");
        return sListener.onIntercept(name);
    }

    public static boolean intercept(String... names) {
        if (sListener == null)
            throw new ExceptionInInitializerError("Register needs to be called.");
        for (String name : names) {
            if (sListener.onIntercept(name)) {
                return true;
            }
        }
        return false;
    }

    public static Context getContext() {
        return sContext;
    }

    public static void setContext(Context context) {
        sContext = context;
    }
}
