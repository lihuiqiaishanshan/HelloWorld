package com.hisilicon.tvui.installtion;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class InputDialog extends Dialog {
    public InputDialog(Context context,View layout, int style) {

        super(context, style);

        setContentView(layout);

        Window window = getWindow();

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);

        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width = (int) (point.x * 0.5);
        layoutParams.height = (int) (point.y * 0.3);

        layoutParams.gravity = Gravity.CENTER;

        window.setAttributes(layoutParams);
    }
}
