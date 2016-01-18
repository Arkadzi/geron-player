package me.arkadiy.geronplayer.statics;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import me.arkadiy.geronplayer.R;

/**
 * Created by Arkadiy on 28.11.2015.
 */
public class Utils {
    public static Bitmap fastblur(Bitmap sentBitmap, int radius) {

//        int width = Math.round(50);
//        int height = Math.round(50);
        /*Bitmap newSentBitmap = Bitmap.createScaledBitmap(sentBitmap, 50, 50, false);
        sentBitmap.recycle();
*/
        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
        sentBitmap.recycle();

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }


    public static Uri getArtworks(long album_id) {
        Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri uri = ContentUris.withAppendedId(artworkUri, album_id);
        Log.e("uri", uri.toString());
        return uri;
    }

    public static Bitmap convertToBitmap(Drawable drawable, int size) {
        Bitmap mutableBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mutableBitmap);
        drawable.setBounds(0, 0, size, size);
        drawable.draw(canvas);

        return mutableBitmap;
    }

    public static int getColor(Context c, int colorId) {
        if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.M)) {
            return c.getResources().getColor(colorId);
        } else {
            return c.getResources().getColor(colorId, null);
        }
    }

    public static int getColorAttribute(Context c, int resid) {
        TypedValue typedValue = new TypedValue();
        c.getTheme().resolveAttribute(resid, typedValue, true);
        return typedValue.data;
    }

    public static void setArtwork(Context c, Uri uri, long id) {
        Log.e("Utils", "setArtwork Uri " + uri + " " + id);
        if (uri != null && id != 0) {
            InputStream stream = null;
            try {
                stream = c.getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(stream);
                Utils.setArtwork(c, bitmap, id);
            } catch (IOException e) {
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
    }

    public static void setArtwork(Context c, Bitmap bitmap, long id) {
        Log.e("Utils", "setArtwork Bitmap " + id);
        if (id > 0) {
            String file = saveImage(c, bitmap, String.valueOf(System.currentTimeMillis()));
            if (file != null) {
                ContentResolver res = c.getContentResolver();

                deleteArtwork(id, res);

                ContentValues values = new ContentValues();
                values.put("album_id", id);
                values.put("_data", file);
                res.insert(Uri.parse("content://media/external/audio/albumart"), values);
            }
        }
    }

    private static String saveImage(Context c, Bitmap image, String name) {
        try {
            int scale = getScale(image, 500);
            Log.e("Utils", "scale " + scale);
            File root = new File(Environment.getExternalStorageDirectory()
                    + "/albumthumbs/");
            root.mkdirs();

            String filePath = root.toString() + File.separator + name;// + ".jpg";
            OutputStream fOut = new FileOutputStream(filePath);
            BufferedOutputStream bos = new BufferedOutputStream(fOut);

            image.compress(Bitmap.CompressFormat.JPEG, scale, bos);

            bos.flush();
            bos.close();
            image.recycle();

            MediaScannerConnection.scanFile(c, new String[]{filePath}, null, null);
            return filePath;
        } catch (Exception e) {
            Log.e("error", e.getLocalizedMessage());
        }
        return null;
    }

    public static int getScale(Bitmap image, float maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        if (Math.max(width, height) > (int) maxSize) {
            return (int) (100f * maxSize / Math.max(width, height));
        }
        return 100;
    }

    private static void deleteArtwork(long id, ContentResolver res) {
        Cursor cursor = res.query(getArtworks(id), null, null, null, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                res.delete(
                        getArtworks(id),
                        null,
                        null);
            }
            cursor.close();
        }
    }
}
