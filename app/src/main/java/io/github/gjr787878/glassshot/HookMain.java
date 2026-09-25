package io.github.gjr787878.glassshot;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookMain implements IXposedHookLoadPackage {

    private static final String TAG = "GlassShot";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lp) throws Throwable {
        // Hook SystemUI 的截屏方法
        if (lp.packageName.equals("com.android.systemui")) {
            hookScreenshot(lp);
        }
    }

    private void hookScreenshot(XC_LoadPackage.LoadPackageParam lp) {
        try {
            // 尝试 hook GlobalScreenshot 或 Screenshot 相关类
            Class<?> globalScreenshot = XposedHelpers.findClassIfExists(
                    "com.android.systemui.screenshot.GlobalScreenshot", lp.classLoader);
            if (globalScreenshot != null) {
                XposedHelpers.findAndHookMethod(globalScreenshot, "inflate",
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) {
                                Log.d(TAG, "GlobalScreenshot inflate hooked");
                            }
                        });
            }
        } catch (Throwable t) {
            Log.e(TAG, "hookScreenshot error", t);
        }
    }
}
