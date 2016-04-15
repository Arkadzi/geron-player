package me.arkadiy.geronplayer.statics;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import me.arkadiy.geronplayer.BuildConfig;
import me.arkadiy.geronplayer.MainActivity;
import me.arkadiy.geronplayer.R;

public class Utils {

    private static ImageLoader LOADER;
    private static DisplayImageOptions OPTIONS;

    public static boolean isShouldPlay(Context c) throws PackageManager.NameNotFoundException {
            PackageInfo info = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
            long currentTimeDifference = System.currentTimeMillis() - info.firstInstallTime;
            int days = 15;
            long trialTime = days * 24 * 60 * 60 * 1000;
            long trialTimeLeft =  trialTime - currentTimeDifference;
            return !(BuildConfig.FLAVOR.equals("free") && trialTimeLeft < 0);
    }

    public static String formatMillis(long millis) {
        long seconds = millis/1000;
        long minutes = seconds / 60 ;
        long hours = minutes/60;
        seconds %= 60;
        minutes %= 60;

        StringBuilder builder = new StringBuilder();
        if (hours > 0) {
            builder.append(String.format("%d:", hours));
        }
        builder.append(String.format("%02d:", minutes));
        builder.append(String.format("%02d", seconds));
        return builder.toString();
    }

    public static Bitmap fastblur(Bitmap sentBitmap, int radius) {

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

    public static void setArtwork(Context c, long id, String file) {
        ContentResolver res = c.getContentResolver();

        deleteArtwork(id, res);

        ContentValues values = new ContentValues();
        values.put("album_id", id);
        values.put("_data", file);
        res.insert(Uri.parse("content://media/external/audio/albumart"), values);
        res.notifyChange(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null);
    }

    public static String saveImage(Context c, Bitmap image, String name) {
        try {
            Log.e("size", image.getWidth() + " " + image.getHeight());
//            int scale = getScale(image, 500);
            File root = new File(Environment.getExternalStorageDirectory()
                    + "/albumthumbs/");
            root.mkdirs();

            String filePath = root.toString() + File.separator + name;// + ".jpg";
            OutputStream fOut = new FileOutputStream(filePath);
            BufferedOutputStream bos = new BufferedOutputStream(fOut);

            image.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
            image.recycle();

            ContentValues cv = new ContentValues();
            cv.put(MediaStore.Images.Media.DATA, filePath);
            cv.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            c.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
            return filePath;
        } catch (Exception e) {
            Log.e("error", e.getLocalizedMessage());
        }
        return null;
    }

    public static Bitmap getBitmap(Context c, Uri uri, ImageSize size) {
        return getLoader(c).loadImageSync(uri.toString(), size);
    }

    public static ImageLoader getLoader(Context c) {
        if (LOADER == null) {
            initLoader(c.getApplicationContext());
        }
        return LOADER;
    }

    private static void initLoader(Context c) {

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .resetViewBeforeLoading(true)
//                .cacheInMemory(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(c.getApplicationContext())
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .memoryCacheSize(5000000)
                .defaultDisplayImageOptions(options)
//                .denyCacheImageMultipleSizesInMemory()
//                .memoryCache(new UsingFreqLimitedMemoryCache(5000000))
                .build();

        LOADER = ImageLoader.getInstance();
        LOADER.init(config);
        OPTIONS = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.RGB_565)
//                .resetViewBeforeLoading(true)
                .cacheInMemory(true)
                .build();
    }

    public static int getScale(Bitmap image, float maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        if (Math.max(width, height) > (int) maxSize) {
            return (int) (100f * maxSize / Math.max(width, height));
        }
        return 100;
    }

    public static void deleteArtwork(long id, ContentResolver res) {
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

    public static DisplayImageOptions getOptions(Context context) {
        if (OPTIONS == null) {
            initLoader(context);
        }
        return OPTIONS;
    }
}
