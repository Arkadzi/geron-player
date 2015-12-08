package me.arkadiy.geronplayer;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;


/**
 * Created by Arkadiy on 07.12.2015.
 */
public class ImageRunnable implements Runnable {
    private final Bitmap defaultBmp;
    private ImageView imageView;
    private ImageLoader imageLoader;
    private String uri;
    private ImageSize imageSize;

    public ImageRunnable(ImageView imageView, Drawable defaultBmp, ImageLoader imageLoader) {
        this.imageView = imageView;
        this.imageLoader = imageLoader;
        this.defaultBmp = Utils.fastblur(Utils.convertToBitmap(defaultBmp, 50), 8);
        imageSize = new ImageSize(50, 50);
    }

    @Override
    public void run() {
        Bitmap bmp = imageLoader.loadImageSync(uri, imageSize);
        if (bmp != null) {
            bmp = Utils.fastblur(bmp, 8);
//            titleView.setTextColor(getDominantColor(bmp));
            imageView.setImageBitmap(bmp);

        } else {
//            titleView.setTextColor(Color.WHITE);
            imageView.setImageBitmap(defaultBmp);
        }

    }

    public void setUri(String uri) {
        this.uri = uri;
    }


//    public static int getDominantColor(Bitmap bitmap) {
//        long redBucket = 0;
//        long greenBucket = 0;
//        long blueBucket = 0;
//        int width = bitmap.getWidth();
//        int height = bitmap.getHeight();
//        int[] pixels = new int[width * height/16];
//        long pixelCount = pixels.length;
//        bitmap.getPixels(pixels, 0, width/4, 3*width/8,3*height/8, width/4, height/4);
//        for (int i = 0; i < pixelCount; i++) {
//                redBucket += Color.red(pixels[i]);
//                greenBucket += Color.green(pixels[i]);
//                blueBucket += Color.blue(pixels[i]);
//        }
//        int red = (int) (redBucket / pixelCount);
//        int green = (int) (greenBucket / pixelCount);
//        int blue = (int) (blueBucket / pixelCount);
//        if (red < 120 && green < 120 && blue < 120) {
//            if (red > green && red > blue) {
//                if (red <= 80) {
//                    green += 25;
//                    blue += 25;
//                }
//                red += 100;
//            } else if (green > red && green > blue) {
//                if (green <= 80) {
//                    red += 25;
//                    blue += 25;
//                }
//                green += 100;
//            } else {
//                if (blue <= 80) {
//                    red += 25;
//                    green += 25;
//                }
//                blue += 100;
//            }
//        }
//        Log.e("rgb", String.format("%d %d %d", red, green, blue));
//        return Color.rgb(red,
//                green,
//                blue);
//    }

    public static int getDominantColor(Bitmap bitmap) {
        Bitmap bitmap1 = Bitmap.createScaledBitmap(bitmap, 1, 1, true);
        int red = Color.red(bitmap1.getPixel(0, 0));
        int green = Color.green(bitmap1.getPixel(0, 0));
        int blue = Color.blue(bitmap1.getPixel(0, 0));
        Log.e("rgb before", String.format("%d %d %d", red, green, blue));
        if (red < 170 && green < 170 && blue < 170) {
            if (red <= 80 && green <= 80 && blue <= 80) {
                red += 150;
                green += 150;
                blue += 150;
            } else if (red > green && red > blue) {
                if (green > blue && (red - green < 30)) {
                    green += 90;
                } else if (green < blue && (red - blue < 30)) {
                    blue += 90;
                }
                red += 100;
            } else if (green > red && green > blue) {
                if (red > blue && (green - red < 30)) {
                    red += 90;
                } else if (red < blue && (green - blue < 30))
                    blue += 90;
                green += 100;
            } else if (blue > red && blue > green) {
                if (red > green && (blue - red < 30)) {
                    red += 90;
                } else if (red < green && (blue - green < 30))
                    green += 90;
                blue += 100;
            } else {
                red += 100;
                green += 100;
                blue += 100;
            }
        }
        if (red > 255) red = 255;
        if (green > 255) green = 255;
        if (blue > 255) blue = 255;
        Log.e("rgb after", String.format("%d %d %d", red, green, blue));
        return Color.rgb(red,
                green,
                blue);
    }
}
