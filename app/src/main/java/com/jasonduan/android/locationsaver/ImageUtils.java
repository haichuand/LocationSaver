package com.jasonduan.android.locationsaver;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.IOException;

/**
 * Class with static methods for manipulating images
 */
public class ImageUtils {

    /**
     * Rotate an image bitmap to the correct orientation based on the image exif data
     * @param src Path of source image
     * @param bitmap bitmap of source image
     * @return the rotated image bitmap
     */
    public static Bitmap rotateBitmap(String src, Bitmap bitmap) {
        int orientation = 1;

        try {
            ExifInterface exif = new ExifInterface(src);
            orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

            if (orientation == 1) {
                return bitmap;
            }

            Matrix matrix = new Matrix();
            switch (orientation) {
                case 2:
                    matrix.setScale(-1, 1);
                    break;
                case 3:
                    matrix.setRotate(180);
                    break;
                case 4:
                    matrix.setRotate(180);
                    matrix.postScale(-1, 1);
                    break;
                case 5:
                    matrix.setRotate(90);
                    matrix.postScale(-1, 1);
                    break;
                case 6:
                    matrix.setRotate(90);
                    break;
                case 7:
                    matrix.setRotate(-90);
                    matrix.postScale(-1, 1);
                    break;
                case 8:
                    matrix.setRotate(-90);
                    break;
                default:
                    return bitmap;
            }

            try {
                Bitmap oriented = Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                bitmap.recycle();
                return oriented;
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                return bitmap;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

}