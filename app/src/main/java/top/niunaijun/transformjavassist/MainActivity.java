package top.niunaijun.transformjavassist;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;

import top.niunaijun.aop_api.annotations.AsyncThread;
import top.niunaijun.aop_api.annotations.Intercept;
import top.niunaijun.aop_api.annotations.UIThread;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private View mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgress = findViewById(R.id.progressBar);
        findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Account.setLogin(true);
                Toast.makeText(MainActivity.this, "模拟登陆成功", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btn_show).setOnClickListener(new View.OnClickListener() {
            @Override
            @Intercept(name = "Login")
            public void onClick(View view) {
                mProgress.setVisibility(View.VISIBLE);
                getProfile();
            }
        });
    }

    @AsyncThread
    private void getProfile() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        showToast("获取个人信息成功");
    }

    @UIThread
    private void showToast(String string) {
        mProgress.setVisibility(View.INVISIBLE);
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
    }
}