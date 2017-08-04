package example.com.scanner;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * Created by Pankaj Sharma on 12/6/17.
 */

public class FileDownloadManager {
    private static FileDownloadManager fileDownloadManager;

    public static FileDownloadManager getInstance() {
        if (fileDownloadManager == null) {
            fileDownloadManager = new FileDownloadManager();
        }
        return fileDownloadManager;
    }

    //************

    private ArrayList<String> urls;

    private FileDownloadManager() {
    }

    public void downloadFile(String fileUrl, FileDownloadCallback callback) {
        if (urls == null) {
            urls = new ArrayList<>();
        }
        if (true || urls.contains(fileUrl) == false) {
            urls.add(fileUrl);
            new DownloadFileAsync(fileUrl, callback).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        }
    }

    private class DownloadFileAsync extends AsyncTask<Void, Integer, Object> {

        String fileUrl = null;
        FileDownloadCallback callback;

        public DownloadFileAsync(String fileUrl, FileDownloadCallback callback) {
            this.fileUrl = fileUrl;
            this.callback = callback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Object doInBackground(Void... params) {

            if (fileUrl == null) return new Exception("File url is empty");
            try {
                File mediaFile = callback.getLocalFile();
                if (mediaFile != null) {
                    if (!mediaFile.exists())
                        mediaFile.createNewFile();
                    URL url = new URL(fileUrl);
                    URLConnection conection = url.openConnection();
                    conection.connect();
                    if (conection.getHeaderField("Content-Type").contains("text/html")) {
                        mediaFile.delete();
                        return new FileNotDownloadableException("Html File");
                    }
                    int lenghtOfFile = conection.getContentLength();
                    InputStream input = new BufferedInputStream(
                            url.openStream(), 8192);
                    System.out.println("Data::" + url);
                    OutputStream output = new FileOutputStream(mediaFile);
                    byte data[] = new byte[1024];
                    long total = 0;
                    int count;
                    int lastPer = 0, currentPer = 0;
                    while ((count = input.read(data)) != -1) {
                        total += count;
                        currentPer = (int) ((total * 100) / lenghtOfFile);
                        if (lastPer != currentPer) {
                            publishProgress(currentPer);
                        }
                        lastPer = currentPer;
                        output.write(data, 0, count);
                    }
                    output.flush();
                    output.close();
                    input.close();
                    return mediaFile;
                }
                new Exception("Local File url is not found");
            } catch (Exception e) {
                Log.e("Error: ", e.getMessage() + "");
                return e;
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (callback != null) {
                callback.onProgress(values[0]);
                Log.i("TAG", callback.hashCode() + "," + values[0]);
            }
        }

        @Override
        protected void onPostExecute(Object obj) {
            super.onPostExecute(obj);
            if (obj != null && callback != null) {
                if (obj instanceof File) {
                    callback.done((File) obj, null);
                } else {
                    callback.done(null, (Exception) obj);
                }
            }
            urls.remove(fileUrl);
        }
    }

    public static abstract class FileDownloadCallback {

        public abstract void done(File file, Exception e);

        File getLocalFile() {
            return new File(getDownloadPath(), "File_" + System.currentTimeMillis() + ".wav");
        }

        public void onProgress(int progress) {

        }

        private File getDownloadPath() {
            File mainDir = new File(Environment.getExternalStorageDirectory(), "Downloads");

            if (mainDir != null && mainDir.exists() == false) {
                mainDir.mkdir();
            }
            return mainDir;
        }
    }

    public class FileNotDownloadableException extends Exception {

        public FileNotDownloadableException(String s) {
            super(s);
        }
    }
}
