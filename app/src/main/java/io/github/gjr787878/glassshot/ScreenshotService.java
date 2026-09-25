package io.github.gjr787878.glassshot;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

public class ScreenshotService extends Service {

    private MediaProjection mp;
    private WindowManager wm;
    private View floatBtn;
    private VirtualDisplay vd;
    private MediaProjectionManager mpm;

    @Override
    public IBinder onBind(Intent i) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        createNotification();
    }

    @Override
    public int onStartCommand(Intent i, int flags, int id) {
        int res = i.getIntExtra("resultCode", 0);
        Intent data = i.getParcelableExtra("data");
        mpm = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        mp = mpm.getMediaProjection(res, data);
        showFloat();
        return START_STICKY;
    }

    private void showFloat() {
        if (floatBtn != null) return;
        ImageView v = new ImageView(this);
        v.setImageResource(android.R.drawable.ic_menu_camera);
        v.setPadding(20,20,20,20);
        v.setBackgroundColor(0x88000000);
        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                dp(50), dp(50), type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.TOP | Gravity.START;
        lp.x = 0; lp.y = 200;
        v.setOnTouchListener(new View.OnTouchListener() {
            int sx, sy; float mx, my;
            public boolean onTouch(View v, MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        sx = lp.x; sy = lp.y;
                        mx = e.getRawX(); my = e.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        lp.x = sx + (int)(e.getRawX() - mx);
                        lp.y = sy + (int)(e.getRawY() - my);
                        wm.updateViewLayout(v, lp);
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (Math.abs(e.getRawX()-mx) < 10 && Math.abs(e.getRawY()-my) < 10) {
                            shoot();
                        }
                        return true;
                }
                return false;
            }
        });
        wm.addView(v, lp);
        floatBtn = v;
    }

    private void shoot() {
        int w = getResources().getDisplayMetrics().widthPixels;
        int h = getResources().getDisplayMetrics().heightPixels;
        int dpi = getResources().getDisplayMetrics().densityDpi;

        ImageReader ir = ImageReader.newInstance(w, h, PixelFormat.RGBA_8888, 2);
        vd = mp.createVirtualDisplay("shot", w, h, dpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                ir.getSurface(), null, null);

        ir.setOnImageAvailableListener(reader -> {
            Image img = reader.acquireLatestImage();
            if (img == null) return;
            Image.Plane[] planes = img.getPlanes();
            ByteBuffer buf = planes[0].getBuffer();
            int pixelStride = planes[0].getPixelStride();
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * w;
            Bitmap bmp = Bitmap.createBitmap(w + rowPadding/pixelStride, h, Bitmap.Config.ARGB_8888);
            bmp.copyPixelsFromBuffer(buf);
            img.close();
            vd.release();

            try {
                File f = new File(getCacheDir(), "shot.png");
                FileOutputStream fos = new FileOutputStream(f);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
                Intent i = new Intent(this, EditorActivity.class);
                i.putExtra("path", f.getAbsolutePath());
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            } catch (Exception e) {
                Toast.makeText(this, "保存失败: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, null);
    }

    private void createNotification() {
        String ch = "glassshot";
        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.createNotificationChannel(ch, "GlassShot", NotificationManager.IMPORTANCE_LOW);
        Notification n = new Notification.Builder(this, ch)
                .setContentTitle("GlassShot 运行中")
                .setSmallIcon(android.R.drawable.ic_menu_camera)
                .build();
        startForeground(1, n);
    }

    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density); }
}
