package com.korsolution.antif;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;

/**
 * Created by Kontin58 on 22/2/2560.
 */

public class ButtonShadow extends View {

    public ButtonShadow(Context context)
    {
        super(context);
    }

    @Override
    public void onDraw(Canvas canvas)
    {
        RectF space = new RectF(this.getLeft(), this.getTop(), this.getRight(), this.getBottom());

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setShader(new LinearGradient(0, getWidth(), 0, 0, Color.BLACK, Color.WHITE, Shader.TileMode.MIRROR));

        canvas.drawArc(space, 180, 360, true, paint);

        Rect rect = new Rect(this.getLeft(),this.getTop() + (this.getHeight() / 2),this.getRight(),this.getBottom());
        canvas.drawRect(rect, paint);
    }
}
