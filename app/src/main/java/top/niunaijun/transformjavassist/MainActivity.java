package top.niunaijun.transformjavassist;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import top.niunaijun.aop_api.annotations.AsyncThread;
import top.niunaijun.aop_api.annotations.Intercept;
import top.niunaijun.aop_api.annotations.TimeLog;
import top.niunaijun.aop_api.annotations.UIThread;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Account.setLogin(true);
                Toast.makeText(MainActivity.this, "模拟登陆成功", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btn_show).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start();
            }
        });
    }

    @AsyncThread
    private void start() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        show("测试11", "测试2");
    }

    @TimeLog
    @UIThread
    @Intercept(name = AOPEvent.EVENT_LOGIN)
    private void show(String nima, String nima2) {
        Toast.makeText(this, "获取我的信息成功:" + nima + ", " + nima2, Toast.LENGTH_SHORT).show();
    }
}