package top.niunaijun.transformjavassist;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;


import top.niunaijun.aop_api.annotations.DelayAsyncThread;
import top.niunaijun.aop_api.annotations.DelayUIThread;
import top.niunaijun.aop_api.annotations.Intercept;

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

    @DelayAsyncThread(delayTime = 2000)
    private void getProfile() {
        showToast("获取个人信息成功 ");
    }

    @DelayUIThread(delayTime = 2000)
    private void showToast(String string) {
        mProgress.setVisibility(View.INVISIBLE);
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
    }
}