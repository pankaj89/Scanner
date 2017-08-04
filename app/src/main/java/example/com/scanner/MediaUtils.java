package example.com.scanner;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v4.content.FileProvider;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class MediaUtils {

    public static final int DEFAULT_IMAGE = 50;
    public static final int DEFAULT_VIDEO = 51;
    public static final int DEFAULT_AUDIO = 52;
    public static final int DEFAULT_PDF = 53;
    public static final int DEFAULT_TEXT = 54;
    public static final int DEFAULT_YOUTUBE= 55;

    public static final String AUTHORITY = "com.onflo.provider";

    public static String SECURE_GALLERY_FOLDER_PATH = Environment.getExternalStorageDirectory().getPath() + "/" + "ONFLO" + "/";

    //image take from camera or gallery
//    public static final int MEDIA_TYPE_IMAGE = 1;
//    public static final int MEDIA_TYPE_VIDEO = 2;
    public static final String IMAGE_DIRECTORY_NAME = ".ONFLO";
    public static String mCurrentPhotoPath;

    //image take from camera or gallery
    public static Uri getOutputMediaFileUri(Context context, File file) {
        //For nougat support we have to use fileprovider with authority
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            return FileProvider.getUriForFile(context, AUTHORITY, file);
        else
            return Uri.fromFile(file);
    }

    public static Uri getOutputMediaFileUri1(Context context, int type) {
        //For nougat support we have to use fileprovider with authority
        return FileProvider.getUriForFile(context, AUTHORITY, getOutputMediaFile(context, type));
//        return Uri.fromFile(getOutputMediaFile(type));
    }

    public static File getMediaFile(Context context, int type) {
        File mediaDir = getMediaDirectory(context, type);
        if (mediaDir != null && mediaDir.exists()) {
            File mediaFile = null;
            try {
                switch (type) {
                    case DEFAULT_IMAGE:
                        mediaFile = File.createTempFile("img_" + System.currentTimeMillis(), ".jpg", mediaDir);
                        break;
                    case DEFAULT_VIDEO:
                        mediaFile = File.createTempFile("vid_" + System.currentTimeMillis(), ".mp4", mediaDir);
                        break;
                    case DEFAULT_AUDIO:
                        mediaFile = File.createTempFile("aud_" + System.currentTimeMillis(), ".mp3", mediaDir);
                        break;
                    case DEFAULT_PDF:
                        mediaFile = File.createTempFile("pdf_" + System.currentTimeMillis(), ".pdf", mediaDir);
                        break;
                }

                if (mediaFile != null && mediaFile.exists() == false) {
                    //Crashlytics.log(1, MainActivity.CAMERA_TAG, "Creating " + mediaFile.getPath());
                    mediaFile.createNewFile();
                    //Crashlytics.log(1, MainActivity.CAMERA_TAG, "After Creating " + mediaFile.getPath() + " status mediaFile.exists() = " + mediaFile.exists());
                }
                //Crashlytics.log(1, MainActivity.CAMERA_TAG, "media file returning is " + mediaFile.getPath() + ", status mediaFile.exists() =" + mediaFile.exists());
                return mediaFile;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //Crashlytics.log(1, MainActivity.CAMERA_TAG, "Media Dir not exist");
        }
        return null;
    }

    //image take from camera or gallery
    public static File getOutputMediaFile(Context context, int type) {
        return getMediaFile(context, type);
        /*// Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File mediaFile;
        File mediaStorageDir;
        if (type == StaticData.DEFAULT_IMAGE) {

            // External sdcard location
//            mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), IMAGE_DIRECTORY_NAME);
            mediaStorageDir = new File(context.getFilesDir(), IMAGE_DIRECTORY_NAME);
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create " + IMAGE_DIRECTORY_NAME + " directory");
                    return null;
                }
            }

            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        } else if (type == StaticData.DEFAULT_VIDEO) {

            // External sdcard location
//            mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), IMAGE_DIRECTORY_NAME);
            mediaStorageDir = new File(context.getFilesDir(), IMAGE_DIRECTORY_NAME);
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create " + IMAGE_DIRECTORY_NAME + " directory");
                    return null;
                }
            }
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");

        } else if (type == StaticData.DEFAULT_PDF) {

            // External sdcard location
//            mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), IMAGE_DIRECTORY_NAME);
            mediaStorageDir = new File(context.getFilesDir(), IMAGE_DIRECTORY_NAME);
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create " + IMAGE_DIRECTORY_NAME + " directory");
                    return null;
                }
            }
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "PDF_" + timeStamp + ".pdf");

        } else if (type == StaticData.DEFAULT_AUDIO) {

            // External sdcard location
//            mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), IMAGE_DIRECTORY_NAME);
            mediaStorageDir = new File(context.getFilesDir(), IMAGE_DIRECTORY_NAME);
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create " + IMAGE_DIRECTORY_NAME + " directory");
                    return null;
                }
            }
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "AUD_" + timeStamp + ".mp3");

        } else {
            return null;
        }
        try {
            if (mediaFile != null && mediaFile.exists() == false)
                mediaFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mediaFile;*/
    }



    public static File getMediaDirectory(Context context, int type) {
        try {
            File mainDir = new File(Environment.getExternalStorageDirectory(), ".ONFLO");

            if (mainDir != null && mainDir.exists() == false) {
                //Crashlytics.log(1, MainActivity.CAMERA_TAG, "Creating ONFLO Folder");
                mainDir.mkdir();
                //Crashlytics.log(1, MainActivity.CAMERA_TAG, "After ONFLO Folder Created status mainDir.exists()=" + mainDir.exists());
            } else {
                //Crashlytics.log(1, MainActivity.CAMERA_TAG, "ONFLO Folder Already Exist");
            }

            File mediaDir = null;
            switch (type) {
                case DEFAULT_IMAGE:
                    mediaDir = new File(mainDir, "image");
                    break;
                case DEFAULT_VIDEO:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        mainDir = new File(context.getFilesDir(), IMAGE_DIRECTORY_NAME);
                        if (mainDir != null && mainDir.exists() == false) {
                            mainDir.mkdir();
                        }
                    } else {
                        if (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).exists() == false) {
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).mkdirs();
                        }
                        mainDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), IMAGE_DIRECTORY_NAME);
                        if (mainDir != null && mainDir.exists() == false) {
                            mainDir.mkdir();
                        }
                    }
                    mediaDir = new File(mainDir, "video");
                    break;
                case DEFAULT_AUDIO:
                    mediaDir = new File(mainDir, "audio");
                    break;
                case DEFAULT_PDF:
                    mediaDir = new File(mainDir, "pdf");
                    break;
                default:
                    mediaDir = new File(mainDir, "others");

            }

            if (mediaDir != null && mediaDir.exists() == false) {
                //Crashlytics.log(1, MainActivity.CAMERA_TAG, "Creating " + mediaDir.getPath() + " Folder");
                mediaDir.mkdir();
                //Crashlytics.log(1, MainActivity.CAMERA_TAG, "After Creating " + mediaDir.getPath() + " Folder status mediaDir.exists() = " + mediaDir.exists());
            }
            return mediaDir;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /*public static File getOutputMediaFile1(int type) {
        // External sdcard location
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), IMAGE_DIRECTORY_NAME);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                android.util.Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create " + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }
        return mediaFile;
    }*/

    public interface CallbackWithFile {
        public void onSuccess(File file);
    }

    //Use this method for background handling
    public static File createTempFileFromURI(final Context context, final int type, final Uri uri, final CallbackWithFile callbackWithFile) throws IOException {

        try {
            File file = FileUtils.getFile(context, uri);
            if (file != null && file.exists()) {
                if (callbackWithFile != null) {
                    callbackWithFile.onSuccess(file);
                }
                return file;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        new AsyncTask<Void, Void, Void>() {
            File outputFile;

            @Override
            protected Void doInBackground(Void... voids) {
                InputStream inputStream = null;
                try {
                    inputStream = context.getContentResolver().openInputStream(uri);

                    outputFile = MediaUtils.getOutputMediaFile(context, type);
                    OutputStream outputStream = new FileOutputStream(outputFile);

                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buf)) > 0) {
                        outputStream.write(buf, 0, len);
                    }
                    outputStream.close();
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (callbackWithFile != null) {
                    callbackWithFile.onSuccess(outputFile);
                }
            }
        }.execute();
        return null;
    }

    public static File createTempFileFromURI(Context context, int type, Uri uri) throws IOException {
        try {
            File file = FileUtils.getFile(context, uri);
            if (file != null && file.exists())
                return file;
        } catch (Exception e) {

        }
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        File outputFile = MediaUtils.getOutputMediaFile(context, type);
        OutputStream outputStream = new FileOutputStream(outputFile);

        byte[] buf = new byte[1024];
        int len;
        while ((len = inputStream.read(buf)) > 0) {
            outputStream.write(buf, 0, len);
        }
        outputStream.close();
        inputStream.close();

        return outputFile;
    }

    /*public static File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "Camera");
        File image = File.createTempFile(
                imageFileName,  *//* prefix *//*
                ".jpg",         *//* suffix *//*
                storageDir      *//* directory *//*
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }*/
//    public static Uri getImageUrlWithAuthority(Context context, Uri uri) {
//        try {
//            InputStream is = null;
//            if (uri.getAuthority() != null) try {
//                is = context.getContentResolver().openInputStream(uri);
//                Bitmap bmp = BitmapFactory.decodeStream(is);
//                return writeToTempImageAndGetPathUri(context, bmp);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } finally {
//                try {
//                    is.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        } catch (OutOfMemoryError e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    public static Uri writeToTempImageAndGetPathUri(Context inContext, Bitmap inImage) {
//        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
//        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
//        return Uri.parse(path);
//    }

    /**
     * This method is used resize image
     *
     * @param context
     * @param dstWidth
     * @param dstHeight
     * @param scalingLogic
     * @param rotationNeeded
     * @param currentPhotoPath
     * @return scaledBitmap
     */
    public static Bitmap getResizeImage(Context context, int dstWidth, int dstHeight, ScalingUtilities.ScalingLogic scalingLogic, boolean rotationNeeded, String currentPhotoPath, Uri IMAGE_CAPTURE_URI) {
        try {
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
            bmOptions.inJustDecodeBounds = false;
            if (bmOptions.outWidth < dstWidth && bmOptions.outHeight < dstHeight) {
                Bitmap bitmap;
                bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
                return setSelectedImage(bitmap, context, currentPhotoPath, IMAGE_CAPTURE_URI);
            } else {
                Bitmap unscaledBitmap = ScalingUtilities.decodeResource(currentPhotoPath, dstWidth, dstHeight, scalingLogic);
                Matrix matrix = new Matrix();
                if (rotationNeeded) {
                    matrix.setRotate(getCameraPhotoOrientation(context, Uri.fromFile(new File(currentPhotoPath)), currentPhotoPath));
                    unscaledBitmap = Bitmap.createBitmap(unscaledBitmap, 0, 0, unscaledBitmap.getWidth(), unscaledBitmap.getHeight(), matrix, false);
                }
                Bitmap scaledBitmap = ScalingUtilities.createScaledBitmap(unscaledBitmap, dstWidth, dstHeight, scalingLogic);
                unscaledBitmap.recycle();
                return scaledBitmap;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * This method is used set selected image
     *
     * @param orignalBitmap This parameter is bitmap type
     * @param context
     * @param imagePath     This parameter is String type
     * @return orignalBitmap
     */
    public static Bitmap setSelectedImage(Bitmap orignalBitmap, Context context, String imagePath, Uri IMAGE_CAPTURE_URI) {
        try {
            String manufacturer = Build.MANUFACTURER;
            String model = Build.MODEL;
            if (manufacturer.equalsIgnoreCase("samsung") || model.equalsIgnoreCase("samsung")) {
                Bitmap mBitmap = rotateBitmap(context, orignalBitmap, imagePath, IMAGE_CAPTURE_URI);
                return mBitmap;
            } else {
                return orignalBitmap;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return orignalBitmap;
        }

    }

    public static Bitmap getImageFromCamera(Context mContext, Uri IMAGE_CAPTURE_URI, int dstWidth, int dstHeight) {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (manufacturer.equalsIgnoreCase("samsung") || model.equalsIgnoreCase("samsung")) {
            int rotation = getCameraPhotoOrientation(mContext, IMAGE_CAPTURE_URI, IMAGE_CAPTURE_URI.getPath());
            Log.e("Rotate", String.valueOf(rotation));
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8;

            if (options.outWidth < dstWidth && options.outHeight < dstHeight) {
                Bitmap bitmap;
                bitmap = BitmapFactory.decodeFile(IMAGE_CAPTURE_URI.getPath(), options);
                return bitmap;

            } else {
                final Bitmap bitmap = BitmapFactory.decodeFile(IMAGE_CAPTURE_URI.getPath(), options);
                Bitmap orignalBitmap = Bitmap.createBitmap(bitmap, 0, 0, dstWidth, dstHeight, matrix, true);
                return orignalBitmap;
            }

        } else {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8;

            if (options.outWidth < dstWidth && options.outHeight < dstHeight) {
                Bitmap bitmap;
                bitmap = BitmapFactory.decodeFile(IMAGE_CAPTURE_URI.getPath(), options);
                return bitmap;

            } else {
                final Bitmap bitmap = BitmapFactory.decodeFile(IMAGE_CAPTURE_URI.getPath(), options);
                Bitmap orignalBitmap = Bitmap.createBitmap(bitmap, 0, 0, dstWidth, dstHeight);
                return orignalBitmap;
            }
        }
    }

    private static Bitmap rotateBitmap(Context context, Bitmap bit, String imagePath, Uri IMAGE_CAPTURE_URI) {

        int rotation = getCameraPhotoOrientation(context, IMAGE_CAPTURE_URI,
                imagePath);
        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);
        Bitmap orignalBitmap = Bitmap.createBitmap(bit, 0, 0, bit.getWidth(), bit.getHeight(), matrix, true);
        return orignalBitmap;
    }

    /**
     * This method is used get orientation of camera photo
     *
     * @param context
     * @param imageUri  This parameter is Uri type
     * @param imagePath This parameter is String type
     * @return rotate
     */
    public static int getCameraPhotoOrientation(Context context, Uri imageUri, String imagePath) {
        int rotate = 0;
        try {
            try {
                if (imageUri != null)
                    context.getContentResolver().notifyChange(imageUri, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            File imageFile = new File(imagePath);
            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                case ExifInterface.ORIENTATION_NORMAL:
                    rotate = 0;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    public static File bitmapToFile(Bitmap bitmap) {
        try {
            long current = System.currentTimeMillis();
            File dir = new File(SECURE_GALLERY_FOLDER_PATH);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            if (dir.exists()) {
                File file = new File(SECURE_GALLERY_FOLDER_PATH + current + ".png");
                if (file.exists())
                    file.delete();
                file.createNewFile();
                FileOutputStream fOut;
                fOut = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, fOut);
                fOut.flush();
                fOut.close();
                return file;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 70, stream);
        return stream.toByteArray();
    }

    public static Bitmap getImageBitmap(byte[] image) {
        ByteArrayInputStream imageStream = new ByteArrayInputStream(image);
        return BitmapFactory.decodeStream(imageStream);
    }

    public static boolean deleteDirectory(File SecureFolder) {
        if (SecureFolder.exists()) {
            File[] files = SecureFolder.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (SecureFolder.delete());
    }

    /**
     * It saved image in device's sd card.
     *
     * @param bitmap
     * @param fileName       stores filename in a sd card.
     * @param needToCompress shows true or false
     * @return final file
     */
    public static File saveImageToSDCard(Bitmap bitmap, String fileName, boolean needToCompress) {

        try {
            String current;
            if (fileName.length() > 0)
                current = fileName;
            else
                current = "" + System.currentTimeMillis();

            File file;
            File myNewFolder = new File(SECURE_GALLERY_FOLDER_PATH);
            if (!myNewFolder.isDirectory())
                myNewFolder.mkdir();

            if (current.contains(".png")) {
                file = new File(SECURE_GALLERY_FOLDER_PATH, current);
            } else {
                file = new File(SECURE_GALLERY_FOLDER_PATH, current + ".png");
            }

            if (file.exists())
                file.delete();

            FileOutputStream fOut = new FileOutputStream(file);

            if (needToCompress)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fOut);
            else
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * It gives image file from sd card
     *
     * @param mBitmap
     * @param fileName
     * @return
     */
    public static File getImageFileFromSDCard(Bitmap mBitmap, String fileName) {

        try {

            File file;
            File myNewFolder = new File(SECURE_GALLERY_FOLDER_PATH);
            if (!myNewFolder.isDirectory())
                myNewFolder.mkdir();

            if (fileName.contains(".jpg")) {
                file = new File(SECURE_GALLERY_FOLDER_PATH, fileName);
            } else if (fileName.contains(".png")) {
                file = new File(SECURE_GALLERY_FOLDER_PATH, fileName);
            } else {
                file = new File(SECURE_GALLERY_FOLDER_PATH, fileName + ".jpg");
            }
            FileOutputStream mOutputStream = new FileOutputStream(file);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, mOutputStream);
            mOutputStream.flush();
            mOutputStream.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getImageNameFromURL(String url) {
        String fileName = "";
        if (null != url && url.length() > 0) {
            int endIndex = url.lastIndexOf("/");
            if (endIndex != -1) {

                String nameWithExt = url.substring(endIndex + 1, url.length()); // not forgot to put check if(endIndex != -1)
                fileName = nameWithExt.substring(0, nameWithExt.lastIndexOf("."));
            }
        }
        return fileName;
    }

    public static Bitmap getBitmapFromBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    public static String getBase64FromBitmap(Bitmap image) {
        Bitmap immagex = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immagex.compress(Bitmap.CompressFormat.PNG, 60, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        return imageEncoded;
    }

    public static String getBase64FromString(String input) {
        String base64 = "";
        try {
            byte[] data = input.getBytes("UTF-8");
            base64 = Base64.encodeToString(data, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return base64;
    }

    public static Bitmap createBitmapFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        Bitmap bitmap = null;
        try {
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
            view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
            view.buildDrawingCache();
            bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_4444);

            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public static Uri getPhotoUri(Context context, Long contactId) {
        ContentResolver contentResolver = context.getContentResolver();

        try {
            Cursor cursor = contentResolver
                    .query(ContactsContract.Data.CONTENT_URI,
                            null,
                            ContactsContract.Data.CONTACT_ID
                                    + "="
                                    + contactId
                                    + " AND "

                                    + ContactsContract.Data.MIMETYPE
                                    + "='"
                                    + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
                                    + "'", null, null);

            if (cursor != null) {
                if (!cursor.moveToFirst()) {
                    return null; // no photo
                }
            } else {
                return null; // error in cursor process
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        Uri person = ContentUris.withAppendedId(
                ContactsContract.Contacts.CONTENT_URI, contactId);
        return Uri.withAppendedPath(person,
                ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
    }

}