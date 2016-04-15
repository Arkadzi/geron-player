package me.arkadiy.geronplayer.statics;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;

import me.arkadiy.geronplayer.statics.Utils;


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
            imageView.setImageBitmap(bmp);

        } else {
            imageView.setImageBitmap(defaultBmp);
        }

    }

    public void setUri(String uri) {
        this.uri = uri;
    }


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
        return Color.rgb(red,
                green,
                blue);
    }
}
