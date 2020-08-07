package top.niunaijun.transformjavassist;

public class Account {
    private static boolean sLogin = false;

    public static boolean isLogin() {
        return sLogin;
    }

    public static void setLogin(boolean login) {
        sLogin = login;
    }
}
