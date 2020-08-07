package top.niunaijun.aop_api;

public class AOPCore {

    private static AOPListener sListener;

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
}
