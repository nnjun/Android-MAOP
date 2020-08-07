package top.niunaijun.transformjavassist;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

//import top.niunaijun.aop_api.AOPCore;
//import top.niunaijun.aop_api.AOPListener;

public class App extends Application {
    public static final String TAG = "App";

    @Override
    public void onCreate() {
        super.onCreate();
//        AOPCore.register(new AOPListener() {
//            @Override
//            public boolean onIntercept(String name) {
//                if (AOPEvent.EVENT_LOGIN.equals(name)) {
//                    if (Account.isLogin()) {
//                        return false;// 放行
//                    } else {
//                        Toast.makeText(App.this, "用户未登录！", Toast.LENGTH_SHORT).show();
//                        // do login
//                        return true;// 拦截方法
//                    }
//                }
//                return false;
//            }
//        });
    }
}
