package io.github.gjr787878.glassshot;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(48), dp(24), dp(32));
        root.setBackgroundColor(0xFF1C1C1E);

        GlassTextView title = new GlassTextView(this);
        title.setText("GlassShot");
        title.setTextSize(24);
        title.setPadding(0, dp(20), 0, dp(12));
        root.addView(title);

        GlassTextView desc = new GlassTextView(this);
        desc.setText("Root 截屏工具\n下拉通知栏点「截屏」按钮");
        desc.setTextSize(14);
        desc.setPadding(0, 0, 0, dp(30));
        root.addView(desc);

        GlassCapsuleButton btnStart = new GlassCapsuleButton(this);
        btnStart.setLabel("开启截屏服务");
        btnStart.setOnClickListener(v -> {
            startService(new Intent(this, ScreenshotService.class));
            Toast.makeText(this, "服务已开启，下拉通知栏点截屏", Toast.LENGTH_LONG).show();
            finish();
        });
        root.addView(btnStart);

        setContentView(root);
    }

    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density); }
}
