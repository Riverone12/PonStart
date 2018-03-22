package com.reverone.kawahara.ponstart.common;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import static android.content.Context.WINDOW_SERVICE;

/**
 * フォントサイズを調整する
 * Created by kawahara on 2017/06/03.
 */

@SuppressWarnings("unused")
public class FontSizeAdjuster {
    private static float _scaleSize = 1;           // 文字サイズ調整倍率

    public FontSizeAdjuster(Context context) {
        _scaleSize = getScaleSize(context);
    }

    public static void initialize(Context context) {
        _scaleSize = getScaleSize(context);
    }

    public static void setDebugMode() {
        _scaleSize = 1f;
    }

    @SuppressWarnings("WeakerAccess")
    public static void adjustment(ViewGroup parent, boolean isExpandOnly) {
        if (parent == null) {
            return;
        }
        for(int i = 0; i < parent.getChildCount(); i++) {
            View view = parent.getChildAt(i);
            if (view instanceof ViewGroup) {
                adjustment((ViewGroup)view, isExpandOnly);
            } else if (view instanceof TextView) {
                adjustment((TextView)view, isExpandOnly);
            }
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static void adjustment(TextView view, boolean isExpandOnly) {
        if (!isExpandOnly || _scaleSize > 1) {
            setTextSize(view, (int) (view.getTextSize() * _scaleSize));
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static void setTextSize(TextView v, float textSize) {
        v.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
    }

    // 画面サイズを取得する
    public static double getDisplaySize(Activity activity) {
        // http://tono-n-chi.com/blog/2014/06/android-text-size-auto-adjust/
        final DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        // ピクセル数（width, height）を取得する
        final int widthPx = metrics.widthPixels;
        final int heightPx = metrics.heightPixels;
        // dpi (xdpi, ydpi) を取得する
        final float xdpi = metrics.xdpi;
        final float ydpi = metrics.ydpi;
        // インチ（width, height) を計算する
        final float widthIn = widthPx / xdpi;
        final float heightIn = heightPx / ydpi;

        // 画面サイズ（インチ）を計算する
        return (Math.sqrt(widthIn * widthIn + heightIn * heightIn));
    }

    private static float getScaleSize(Context context) {
        // http://qiita.com/POCOio_oi/items/145df41ef1a4d4d8c2b0
        // drawableにstone.png（480px×1px）を仕込ませて、幅サイズの基準値にして、
        // 画面サイズによって拡大縮小の調整をする。

        //stone.pngを読み込んでBitmap型で扱う
        Bitmap _bm = BitmapFactory.decodeResource(
                context.getResources(),
                com.reverone.kawahara.ponstart.R.drawable.stone);

        //画面サイズ取得の準備
        WindowManager wm = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        Display disp = wm.getDefaultDisplay();

        // AndroidのAPIレベルによって画面サイズ取得方法が異なるので条件分岐
            // SDK バージョン13未満には対応しない
        Point size = new Point();
        disp.getSize(size);
        float width = (size.x < size.y) ? size.x : size.y;
        /*
        if (Integer.valueOf(android.os.Build.VERSION.SDK_INT) >= 13) {
            Point size = new Point();
            disp.getSize(size);
            width = (size.x < size.y) ? size.x : size.y;
        } else {
            int x = disp.getWidth();
            int y = disp.getHeight();
            width = (x < y) ? x : y;
        }
        */
        return (width / (float) _bm.getWidth());
    }
}
