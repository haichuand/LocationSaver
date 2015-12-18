package com.jasonduan.android.locationsaver;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.IOException;

/**
 * Created by hduan on 11/11/2015.
 */
public class ImageUtils {

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

//    public static void saveResourceImageToSdCard (Context context, int resourceId, String imagePath) {
//        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId);
//        File imageFile = new File(imagePath);
//        try {
//            FileOutputStream out = new FileOutputStream(imageFile);
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
//            out.flush();
//            out.close();
//
//        } catch (FileNotFoundException e) {
//            Log.d("ImageUtils", "FileNotFound Exception on " + imagePath);
//        } catch (IOException e) {
//            Log.d("ImageUtils", "IO Exception on " + imagePath);
//        }
//    }

}