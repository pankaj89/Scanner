package example.com.scanner;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.Comparator;


public class FileUtils {
    private FileUtils() {
    } //private constructor to enforce Singleton pattern

    /**
     * TAG for log messages.
     */
    static final String TAG = "FileUtils";
    private static final boolean DEBUG = false; // Set to true to enable logging

    public static final String MIME_TYPE_AUDIO = "audio/*";
    public static final String MIME_TYPE_TEXT = "text/*";
    public static final String MIME_TYPE_IMAGE = "image/*";
    public static final String MIME_TYPE_VIDEO = "video/*";
    public static final String MIME_TYPE_APP = "application/*";

    public static final String DIRECTORY = "Onflo";
    /**
     * Directory name which will have filter output images. in
     */
    public static final String DIRECTORY_IMAGES = "images";
    public static final String DIRECTORY_VIDEOS = "videos";
    public static final String DIRECTORY_AUDIOS = "audios";
    public static final String DIRECTORY_PDF_FILEs = "pdfs";

    public static enum MediaType {
        IMAGES,
        VIDEOS,
        AUDIOS,
        PDF_FILEs
    }

    public static String SECURE_GALLERY_FOLDER_PATH = Environment.getExternalStorageDirectory().getPath() + File.separator + DIRECTORY + File.separator;
    public static final String HIDDEN_PREFIX = ".";

    /**
     * Gets the extension of a file name, like ".png" or ".jpg".
     *
     * @param uri
     * @return Extension including the dot("."); "" if there is no extension;
     * null if uri was null.
     */
    public static String getExtension(String uri) {
        if (uri == null) {
            return null;
        }

        int dot = uri.lastIndexOf(".");
        if (dot >= 0) {
            return uri.substring(dot);
        } else {
            // No extension.
            return "";
        }
    }

    /**
     * @return Whether the URI is a local one.
     */
    public static boolean isLocal(String url) {
        if (url != null && !url.startsWith("http://") && !url.startsWith("https://")) {
            return true;
        }
        return false;
    }

    /**
     * @return True if Uri is a MediaStore Uri.
     * @author paulburke
     */
    public static boolean isMediaUri(Uri uri) {
        return "media".equalsIgnoreCase(uri.getAuthority());
    }

    /**
     * Convert File into Uri.
     *
     * @param file
     * @return uri
     */
    public static Uri getUri(File file) {
        if (file != null) {
            return Uri.fromFile(file);
        }
        return null;
    }

    /**
     * Returns the path only (without file name).
     *
     * @param file
     * @return
     */
    public static File getPathWithoutFilename(File file) {
        if (file != null) {
            if (file.isDirectory()) {
                // no file to be split off. Return everything
                return file;
            } else {
                String filename = file.getName();
                String filepath = file.getAbsolutePath();

                // Construct path without file name.
                String pathwithoutname = filepath.substring(0,
                        filepath.length() - filename.length());
                if (pathwithoutname.endsWith("/")) {
                    pathwithoutname = pathwithoutname.substring(0, pathwithoutname.length() - 1);
                }
                return new File(pathwithoutname);
            }
        }
        return null;
    }

    /**
     * @return The MIME type for the given file.
     */
    public static String getMimeType(File file) {

        String extension = getExtension(file.getName());

        if (extension.length() > 0)
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.substring(1));

        return "application/octet-stream";
    }

    /**
     * @return The MIME type for the give Uri.
     */
    public static String getMimeType(Context context, Uri uri) {
        try {
            File file = new File(getPath(context, uri));
            return getMimeType(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * @param uri The Uri to check.
     * @author paulburke
     */
    public static boolean isLocalStorageDocument(Uri uri) {
        return LocalStorageProvider.AUTHORITY.equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     * @author paulburke
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     * @author paulburke
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     * @author paulburke
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     * @author paulburke
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                if (DEBUG)
                    DatabaseUtils.dumpCursor(cursor);

                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.<br>
     * <br>
     * Callers should check whether the path is local before assuming it
     * represents a local file.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     * @see #isLocal(String)
     * @see #getFile(Context, Uri)
     */
    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) throws Exception {

        if (DEBUG)
            Log.d(TAG + " File -",
                    "Authority: " + uri.getAuthority() +
                            ", Fragment: " + uri.getFragment() +
                            ", Port: " + uri.getPort() +
                            ", Query: " + uri.getQuery() +
                            ", Scheme: " + uri.getScheme() +
                            ", Host: " + uri.getHost() +
                            ", Segments: " + uri.getPathSegments().toString()
            );

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // LocalStorageProvider
            if (isLocalStorageDocument(uri)) {
                // The path is the id
                return DocumentsContract.getDocumentId(uri);
            }
            // ExternalStorageProvider
            else if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Convert Uri into File, if possible.
     *
     * @return file A local file that the Uri was pointing to, or null if the
     * Uri is unsupported or pointed to a remote resource.
     * @author paulburke
     * @see #getPath(Context, Uri)
     */
    public static File getFile(Context context, Uri uri) throws Exception {
        if (uri != null) {
            String path = getPath(context, uri);
            if (path != null && isLocal(path)) {
                return new File(path);
            }
        }
        return null;
    }

    /**
     * Get the file size in a human-readable string.
     *
     * @param size
     * @return
     * @author paulburke
     */
    public static String getReadableFileSize(int size) {
        final int BYTES_IN_KILOBYTES = 1024;
        final DecimalFormat dec = new DecimalFormat("###.#");
        final String KILOBYTES = " KB";
        final String MEGABYTES = " MB";
        final String GIGABYTES = " GB";
        float fileSize = 0;
        String suffix = KILOBYTES;

        if (size > BYTES_IN_KILOBYTES) {
            fileSize = size / BYTES_IN_KILOBYTES;
            if (fileSize > BYTES_IN_KILOBYTES) {
                fileSize = fileSize / BYTES_IN_KILOBYTES;
                if (fileSize > BYTES_IN_KILOBYTES) {
                    fileSize = fileSize / BYTES_IN_KILOBYTES;
                    suffix = GIGABYTES;
                } else {
                    suffix = MEGABYTES;
                }
            }
        }
        return String.valueOf(dec.format(fileSize) + suffix);
    }

    /**
     * Attempt to retrieve the thumbnail of given File from the MediaStore. This
     * should not be called on the UI thread.
     *
     * @param context
     * @param file
     * @return
     * @author paulburke
     */
    public static Bitmap getThumbnail(Context context, File file) {
        return getThumbnail(context, getUri(file), getMimeType(file));
    }

    /**
     * Attempt to retrieve the thumbnail of given Uri from the MediaStore. This
     * should not be called on the UI thread.
     *
     * @param context
     * @param uri
     * @return
     * @author paulburke
     */
    public static Bitmap getThumbnail(Context context, Uri uri) {
        return getThumbnail(context, uri, getMimeType(context, uri));
    }

    /**
     * Attempt to retrieve the thumbnail of given Uri from the MediaStore. This
     * should not be called on the UI thread.
     *
     * @param context
     * @param uri
     * @param mimeType
     * @return
     * @author paulburke
     */
    public static Bitmap getThumbnail(Context context, Uri uri, String mimeType) {
        if (DEBUG)
            Log.d(TAG, "Attempting to get thumbnail");

        if (!isMediaUri(uri)) {
            try {
                File file = getFile(context, uri);
                if (file != null && file.exists())
                    return ThumbnailUtils.createVideoThumbnail(file.getPath(), MediaStore.Images.Thumbnails.MINI_KIND);
            } catch (Exception e) {
                Log.e(TAG, "You can only retrieve thumbnails for images and videos.");
            }
            return null;
        }

        Bitmap bm = null;
        if (uri != null) {
            final ContentResolver resolver = context.getContentResolver();
            Cursor cursor = null;
            try {
                cursor = resolver.query(uri, null, null, null, null);
                if (cursor.moveToFirst()) {
                    final int id = cursor.getInt(0);
                    if (DEBUG)
                        Log.d(TAG, "Got thumb ID: " + id);

                    if (mimeType.contains("video")) {
                        bm = MediaStore.Video.Thumbnails.getThumbnail(
                                resolver,
                                id,
                                MediaStore.Video.Thumbnails.MINI_KIND,
                                null);
                    } else if (mimeType.contains(FileUtils.MIME_TYPE_IMAGE)) {
                        bm = MediaStore.Images.Thumbnails.getThumbnail(
                                resolver,
                                id,
                                MediaStore.Images.Thumbnails.MINI_KIND,
                                null);
                    }
                }
            } catch (Exception e) {
                if (DEBUG)
                    Log.e(TAG, "getThumbnail", e);
            } finally {
                if (cursor != null)
                    cursor.close();
            }
        }
        return bm;
    }

    /**
     * File and folder comparator. TODO Expose sorting option method
     *
     * @author paulburke
     */
    public static Comparator<File> sComparator = new Comparator<File>() {
        @Override
        public int compare(File f1, File f2) {
            // Sort alphabetically by lower case, which is much cleaner
            return f1.getName().toLowerCase().compareTo(
                    f2.getName().toLowerCase());
        }
    };

    /**
     * File (not directories) filter.
     *
     * @author paulburke
     */
    public static FileFilter sFileFilter = new FileFilter() {
        @Override
        public boolean accept(File file) {
            final String fileName = file.getName();
            // Return files only (not directories) and skip hidden files
            return file.isFile() && !fileName.startsWith(HIDDEN_PREFIX);
        }
    };

    /**
     * Folder (directories) filter.
     *
     * @author paulburke
     */
    public static FileFilter sDirFilter = new FileFilter() {
        @Override
        public boolean accept(File file) {
            final String fileName = file.getName();
            // Return directories only and skip hidden directories
            return file.isDirectory() && !fileName.startsWith(HIDDEN_PREFIX);
        }
    };

    /**
     * Get the Intent for selecting content to be used in an Intent Chooser.
     *
     * @return The intent for opening a file with Intent.createChooser()
     * @author paulburke
     */
    public static Intent createGetContentIntent() {
        // Implicitly allow the user to select a particular kind of data
        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        // The MIME data type filter
        intent.setType("*/*");
        // Only return URIs that can be opened with ContentResolver
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        return intent;
    }




    /*public static Bitmap getResizeImage(Context mContext, ScalingUtilities.ScalingLogic scalingLogic, boolean rotationNeeded, String mCurrentPhotoPath, Uri IMAGE_CAPTURE_URI) {
        try {
            int dstWidth, dstHeight;
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
            bmOptions.inJustDecodeBounds = false;
            String manufacturer = Build.MANUFACTURER;
            String model = Build.MODEL;


            if (bmOptions.outWidth > bmOptions.outHeight) {
                dstHeight = StaticData.IMAGE_SIZE;
                dstWidth = (bmOptions.outWidth * dstHeight) / bmOptions.outHeight;
            } else {
                dstWidth = StaticData.IMAGE_SIZE;
                dstHeight = (bmOptions.outHeight * dstWidth) / bmOptions.outWidth;
            }

            if (bmOptions.outWidth < dstWidth && bmOptions.outHeight < dstHeight) {
                Bitmap bitmap;
                bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
                return setSelectedImage(bitmap, mContext, mCurrentPhotoPath, IMAGE_CAPTURE_URI);
            } else {
                Bitmap unscaledBitmap = ScalingUtilities.decodeResource(mCurrentPhotoPath, dstWidth, dstHeight, scalingLogic);
                Matrix matrix = new Matrix();
                if (rotationNeeded || manufacturer.equalsIgnoreCase("samsung") || model.equalsIgnoreCase("samsung")) {
                    matrix.setRotate(getCameraPhotoOrientation(mContext, Uri.fromFile(new File(mCurrentPhotoPath)), mCurrentPhotoPath));
                    unscaledBitmap = Bitmap.createBitmap(unscaledBitmap, 0, 0, unscaledBitmap.getWidth(), unscaledBitmap.getHeight(),
                            matrix, false);
                }
                Bitmap scaledBitmap = ScalingUtilities.createScaledBitmap(unscaledBitmap, dstWidth, dstHeight, scalingLogic);
                unscaledBitmap.recycle();
                return scaledBitmap;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }*/

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

    private static Bitmap rotateBitmap(Context context, Bitmap bit, String imagePath, Uri IMAGE_CAPTURE_URI) {

        int rotation = FileUtils.getCameraPhotoOrientation(context, IMAGE_CAPTURE_URI,
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

    public static Bitmap getImageFromCamera(Context mContext, Uri IMAGE_CAPTURE_URI, int dstWidth, int dstHeight) {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (manufacturer.equalsIgnoreCase("samsung") || model.equalsIgnoreCase("samsung")) {
            int rotation = FileUtils.getCameraPhotoOrientation(mContext, IMAGE_CAPTURE_URI, IMAGE_CAPTURE_URI.getPath());
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


    public static File saveImageToSDCard(Bitmap bitmap, String fileName, Context context) {

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
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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


    /**
     * @param context   context
     * @param mediaType media type
     * @return returns media folder path
     */
    public static String getMediaPath(Context context, Enum<MediaType> mediaType) {
        try {
            String path = context.getExternalFilesDir(null).getAbsolutePath() + File.separator;
            if (mediaType == MediaType.IMAGES) {
                path += DIRECTORY_IMAGES;
            } else if (mediaType == MediaType.VIDEOS) {
                path += DIRECTORY_VIDEOS;
            } else if (mediaType == MediaType.PDF_FILEs) {
                path += DIRECTORY_PDF_FILEs;
            } else if (mediaType == MediaType.AUDIOS) {
                path += DIRECTORY_AUDIOS;
            }

            File file = new File(path);
            return getFilePath(file);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @NonNull
    private static String getFilePath(File file) {
        if (!file.exists()) {
            if (file.mkdirs()) {
                return file.getAbsolutePath();
            } else {
                return "";
            }
        }
        return file.getAbsolutePath();
    }
}
