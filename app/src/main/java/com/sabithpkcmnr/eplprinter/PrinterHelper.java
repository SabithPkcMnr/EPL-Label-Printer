package com.sabithpkcmnr.eplprinter;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

class PrinterHelper {

    public static byte[] ReadFromFile(String fileName) {
        byte[] data = null;
        try {
            InputStream in = new FileInputStream(fileName);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (Throwable tr) {
            tr.printStackTrace();
        }

        return data;
    }

    public static Bitmap getImageFromAssetsFile(Context ctx, String fileName) {
        Bitmap image = null;
        AssetManager am = ctx.getResources().getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    public static void showMessageOnUiThread(final Activity activity, final String msg) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static Bitmap getPrintBitmap(Context context, int width, int height) {

        float SMALL_TEXT = 18;
        float LARGE_TEXT = 30;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        Paint paint = new Paint();
        paint.setColor(Color.BLACK); // Always Black

        //SET FONT
        Typeface tfContent = Typeface.createFromAsset(context.getAssets(), "fonts" + File.separator + "Zfull-GB.ttf");
        paint.setTypeface(tfContent);

        float xPoint, yPoint;
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        int smallHeight = (int) Math.abs(fontMetrics.leading) + (int) Math.abs(fontMetrics.ascent) + (int) Math.abs(fontMetrics.descent);
        yPoint = (int) Math.abs(fontMetrics.leading) + (int) Math.abs(fontMetrics.ascent);

        paint.setTextSize((float) LARGE_TEXT);
        String name1 = "花糕玉米(500g)";
        xPoint = (width - paint.measureText(name1)) / 2.0f;
        yPoint = yPoint + 15;
        canvas.drawText(name1, xPoint, yPoint, paint);
        yPoint = yPoint + smallHeight*2-5;
        String saleTip = "如重量不足，将自动退还差额";
        paint.setTextSize(SMALL_TEXT);
        xPoint = (width - paint.measureText(saleTip)) / 2.0f;
        canvas.drawText(saleTip, xPoint, yPoint, paint);
        yPoint = yPoint + smallHeight;
        xPoint = 0;
        float lineWidth = 2.0f;
        paint.setStrokeWidth(3.0f);
        canvas.drawLine(xPoint, yPoint-5, width, yPoint-5, paint);
        yPoint = yPoint + 15;
        canvas.drawText("上市日期", xPoint, yPoint, paint);
        xPoint = width/3+30;
        canvas.drawText("存储条件", xPoint, yPoint, paint);
        xPoint = width*2/3 + 20;
        canvas.drawText("123456", xPoint, yPoint, paint);
        paint.setStrokeWidth(lineWidth);
        canvas.drawLine(width/3+20, yPoint-15, width/3+20, yPoint+2*smallHeight, paint);
        paint.setStrokeWidth(lineWidth);
        canvas.drawLine(width*2/3, yPoint-15, width*2/3, yPoint+2*smallHeight, paint);
        yPoint = yPoint + smallHeight+5;
        xPoint = 0;
        canvas.drawText("2019/12/31 24:00", xPoint, yPoint, paint);
        xPoint = width/3+30;
        canvas.drawText("冷藏", xPoint, yPoint, paint);
        xPoint = width*2/3 + 20;
        canvas.drawText("0312", xPoint, yPoint, paint);
        yPoint = yPoint + 70;

        paint.setStrokeWidth(3.0f);
        xPoint = 0;
        canvas.drawLine(xPoint, yPoint-0, width, yPoint-0, paint);
        canvas.drawText("客服电话:4099503665", xPoint, yPoint+20, paint);
        yPoint = yPoint + 45;
        canvas.drawText("服务时间:07:00-21:00", xPoint, yPoint, paint);

        canvas.save();
        return bitmap;
    }

}