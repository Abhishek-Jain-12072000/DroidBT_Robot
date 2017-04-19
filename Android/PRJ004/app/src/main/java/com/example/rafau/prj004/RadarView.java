package com.example.rafau.prj004;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

/**
 * Created by ulko on 06.03.2016.
 */
public class RadarView extends View {

    private RectF radarBounds;      // Needed for Canvas.drawOval
    private Paint paint;           // The paint (e.g. style, color) used for drawing
    private float centerX;
    private float centerY;
    private float radarR;
    private boolean dataAvailable = false;
    private int[] dat = null;

    public RadarView(Context context) {
        super(context);
        paint = new Paint();
    }

    public RadarView(Context context, int[] data) {
        super(context);
        paint = new Paint();
        dataAvailable = true;
        dat = data;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        centerX = canvas.getWidth() / 2;
        centerY = canvas.getHeight() * 4 / 5;
        radarR = canvas.getWidth() / 2 - 12;
        float a, b, c, d = 0;

        paint.setStrokeWidth(5);
        paint.setColor(Color.WHITE);
        for (int i = 0; i <= 180; i++) {        //podziałka na radarze
            a = (float) (centerX - radarR * Math.cos(Math.toRadians(i)));
            b = (float) (centerY - radarR * Math.sin(Math.toRadians(i)));
            c = (float) (centerX - (radarR - 3) * Math.cos(Math.toRadians(i)));
            d = (float) (centerY - (radarR - 3) * Math.sin(Math.toRadians(i)));
            canvas.drawLine(c, d, a, b, paint);
            a = (float) (centerX - radarR * Math.cos(Math.toRadians(i)) * 2 / 3);
            b = (float) (centerY - radarR * Math.sin(Math.toRadians(i)) * 2 / 3);
            c = (float) (centerX - (radarR - 3) * Math.cos(Math.toRadians(i)) * 2 / 3);
            d = (float) (centerY - (radarR - 3) * Math.sin(Math.toRadians(i)) * 2 / 3);
            canvas.drawLine(c, d, a, b, paint);
            a = (float) (centerX - radarR * Math.cos(Math.toRadians(i)) * 1 / 3);
            b = (float) (centerY - radarR * Math.sin(Math.toRadians(i)) * 1 / 3);
            c = (float) (centerX - (radarR - 3) * Math.cos(Math.toRadians(i)) * 1 / 3);
            d = (float) (centerY - (radarR - 3) * Math.sin(Math.toRadians(i)) * 1 / 3);
            canvas.drawLine(c, d, a, b, paint);
        }
        if (dataAvailable) {
            paint.setStrokeWidth(5);
            paint.setARGB(128, 0, 255, 0);
            for (int i = 0; i < dat.length; i++) {
                float angle = (float) i;
                a = (float) (centerX - radarR * (dat[i] / 100.0) * Math.cos(Math.toRadians(angle)));
                b = (float) (centerY - radarR * (dat[i] / 100.0) * Math.sin(Math.toRadians(angle)));
                canvas.drawLine(centerX, centerY, a, b, paint);
            }
        }
    }

}
