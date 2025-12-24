package com.zds.ydkf_qm;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class Base64Util {
    public static String uriToBase64(Context ctx, Uri uri, int maxDim, int jpegQuality) throws Exception {
        Bitmap bitmap = decodeDownsampled(ctx, uri, maxDim);
        if (bitmap == null) throw new IllegalArgumentException("decode bitmap failed");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, jpegQuality, baos);
        byte[] bytes = baos.toByteArray();

        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    private static Bitmap decodeDownsampled(Context ctx, Uri uri, int maxDim) throws Exception {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;

        try (InputStream in = ctx.getContentResolver().openInputStream(uri)) {
            BitmapFactory.decodeStream(in, null, opts);
        }

        int w = opts.outWidth;
        int h = opts.outHeight;
        if (w <= 0 || h <= 0) return null;

        int inSampleSize = 1;
        while ((w / inSampleSize) > maxDim || (h / inSampleSize) > maxDim) {
            inSampleSize *= 2;
        }

        BitmapFactory.Options opts2 = new BitmapFactory.Options();
        opts2.inSampleSize = inSampleSize;
        opts2.inPreferredConfig = Bitmap.Config.ARGB_8888;

        try (InputStream in2 = ctx.getContentResolver().openInputStream(uri)) {
            return BitmapFactory.decodeStream(in2, null, opts2);
        }
    }
}

