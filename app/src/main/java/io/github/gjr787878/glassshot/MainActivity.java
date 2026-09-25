package io.github.gjr787878.glassshot;

import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity {

    private MediaProjectionManager mpm;
    private GlassCapsuleButton btnStart;
    private GlassCapsuleButton btnPerm;
    private GlassTextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // §3.6 毛玻璃风格根布局
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(48), dp(24), dp(32));
        root.setBackgroundColor(0xFF1C1C1E);

        status = new GlassTextView(this);
        status.setText("GlassShot 截屏工具");
        status.setTextSize(20);
        status.setPadding(0, dp(20), 0, dp(20));
        root.addView(status);

        btnPerm = new GlassCapsuleButton(this);
        btnPerm.setLabel("开启悬浮窗权限");
        btnPerm.setOnClickListener(v -> {
            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(i);
        });
        root.addView(btnPerm);

        btnStart = new GlassCapsuleButton(this);
        btnStart.setLabel("开始截屏服务");
        btnStart.setOnClickListener(v -> requestProjection());
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) btnStart.getLayoutParams();
        if (lp == null) lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = dp(12);
        btnStart.setLayoutParams(lp);
        root.addView(btnStart);

        setContentView(root);
        mpm = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
    }

    private void requestProjection() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "先开悬浮窗权限", Toast.LENGTH_SHORT).show();
            return;
        }
        startActivityForResult(mpm.createScreenCaptureIntent(), 100);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == 100 && res == RESULT_OK) {
            Intent svc = new Intent(this, ScreenshotService.class);
            svc.putExtra("resultCode", res);
            svc.putExtra("data", data);
            startForegroundService(svc);
            Toast.makeText(this, "悬浮球已开启", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density); }
}
