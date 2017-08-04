package example.com.scanner;

import android.Manifest;
import android.app.ProgressDialog;
import android.arch.persistence.room.Room;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.master.permissionhelper.PermissionHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import example.com.scanner.database.AppDatabase;
import example.com.scanner.database.Attachment;
import example.com.scanner.database.AttachmentDao;
import example.com.scanner.databinding.ActivityHomeListBinding;

public class HomeListActivity extends AppCompatActivity {

    private static final String TAG = "HomeListActivity";
    private final String AUTHORITY = "com.scanner.fileprovider";
    ActivityHomeListBinding binding;
    HomeListRecyclerViewAdapter homeListRecyclerViewAdapter;
    PermissionHelper permissionHelper;

    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home_list);

        permissionHelper = new PermissionHelper(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);

        final Attachment attachment = new Attachment();
        attachment.path = "http://www.largesound.com/ashborytour/sound/AshboryBYU.mp3";
        attachment.id = System.currentTimeMillis() + "";

        getInstance(getApplicationContext()).attachmentDao().getAllAsync(new AttachmentDao.GetAllCallback() {
            @Override
            public void getAll(ArrayList<Attachment> list) {
                loadList(list);
            }
        });

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                permissionHelper.request(new PermissionHelper.PermissionCallback() {
                    @Override
                    public void onPermissionGranted() {
                        Intent intent = new Intent(HomeListActivity.this, ScannerActivity.class);
                        startActivityForResult(intent, 110);
                    }

                    @Override
                    public void onPermissionDenied() {

                    }

                    @Override
                    public void onPermissionDeniedBySystem() {
                        permissionHelper.openAppDetailsActivity();
                    }
                });

                /*Attachment attachment = new Attachment();
                attachment.path = count % 2 == 0 ? "http://www.largesound.com/ashborytour/sound/AshboryBYU.mp3" : "Hello world";
                attachment.id = System.currentTimeMillis() + "";
                count++;
                Intent data = new Intent();
                data.putExtra("data", attachment);
                onActivityResult(110, RESULT_OK, data);*/
            }
        });

        handleSendData();
    }

    private void handleSendData() {
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            Uri selectedPdf = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
            Log.d(TAG, "handleSendData() called," + selectedPdf + "," + type);

            String path;
            try {
                File file = MediaUtils.createTempFileFromURI(this, MediaUtils.DEFAULT_PDF, selectedPdf);
                path = file.getPath();
            } catch (IOException e) {
                e.printStackTrace();
                path = getPath(selectedPdf);
            }
            Log.d(TAG, "handleSendData() called," + selectedPdf + "[" + path + "||" + new File(path).getName() + "]," + type);
            final String finalPath = path;
            permissionHelper.request(new PermissionHelper.PermissionCallback() {
                @Override
                public void onPermissionGranted() {
                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    if (mAuth.getCurrentUser() == null) {
                        showProgressDialog();
                        mAuth.signInWithEmailAndPassword("pankaj.sharma@gmail.com", "123456").addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Log.d(TAG, "onComplete() called with: task = [" + task + "]");
                                if (task.isSuccessful()) {
                                    uploadFile(new File(finalPath));
                                } else {
                                    Log.d(TAG, "Exception: " + task.getException());
                                }
                            }
                        });
                    } else {
                        showProgressDialog();
                        uploadFile(new File(finalPath));
                    }
                }

                @Override
                public void onPermissionDenied() {

                }

                @Override
                public void onPermissionDeniedBySystem() {
                    permissionHelper.openAppDetailsActivity();
                }
            });
        }
    }

    ProgressDialog progressDialog;

    private void showProgressDialog() {
        progressDialog = ProgressDialog.show(this, null, "Uploading, Please wait...");
    }

    private void hideProgressDialog() {
        if (progressDialog != null)
            progressDialog.dismiss();
    }


    private String getPath(Uri uri) {

        String filePath;
        String[] projection = {MediaStore.Images.ImageColumns.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null,
                null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(projection[0]);
            filePath = cursor.getString(columnIndex);
            cursor.close();
        } else {
            filePath = null;
        }
        if (filePath == null) {
            filePath = uri.getPath();
        }
        return filePath;
    }

    private void loadList(ArrayList<Attachment> list) {
        homeListRecyclerViewAdapter = new HomeListRecyclerViewAdapter(list);
        homeListRecyclerViewAdapter.setOnItemClickListener(new HomeListRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Attachment attachment = homeListRecyclerViewAdapter.getItem(position);
                if (ConstantCodes.FILE.equalsIgnoreCase(attachment.contentType)) {
                    if (!TextUtils.isEmpty(attachment.localPath)) {
                        openFile(attachment.localPath);
                    } else {
                        Toast.makeText(HomeListActivity.this, "Not downloaded", Toast.LENGTH_SHORT).show();
                    }
                } else if (ConstantCodes.WEBLINK.equalsIgnoreCase(attachment.contentType)) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(attachment.path));
                    if (i.resolveActivity(getPackageManager()) != null)
                        startActivity(i);
                }
            }
        });

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(homeListRecyclerViewAdapter);
    }

    private void openFile(String file) {
        try {
            MimeTypeMap myMime = MimeTypeMap.getSingleton();
            Intent newIntent = new Intent(Intent.ACTION_VIEW);
            String mimeType = myMime.getMimeTypeFromExtension(fileExt(file).substring(1));
            newIntent.setDataAndType(FileProvider.getUriForFile(HomeListActivity.this, AUTHORITY, new File(file)), mimeType);
            newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(newIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(HomeListActivity.this, "No handler for this type of file.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String fileExt(String url) {
        if (url.indexOf("?") > -1) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return null;
        } else {
            String ext = url.substring(url.lastIndexOf(".") + 1);
            if (ext.indexOf("%") > -1) {
                ext = ext.substring(0, ext.indexOf("%"));
            }
            if (ext.indexOf("/") > -1) {
                ext = ext.substring(0, ext.indexOf("/"));
            }
            return ext.toLowerCase();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionHelper != null) {
            permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 110 && resultCode == RESULT_OK) {
            final Attachment attachment = data.getParcelableExtra("data");

//            getInstance(getApplicationContext()).attachmentDao().insertAllAsync(new Attachment[]{attachment}, null);

            final int position = homeListRecyclerViewAdapter.addItem(attachment);
            if (attachment.isDownloadable()) {

                FileDownloadManager.getInstance().downloadFile(attachment.path, new FileDownloadManager.FileDownloadCallback() {
                    @Override
                    public void done(File file, Exception e) {
                        if (e != null && e instanceof FileDownloadManager.FileNotDownloadableException) {
                            attachment.contentType = ConstantCodes.WEBLINK;
                            attachment.isDone = true;
                        } else if (file != null) {
                            attachment.contentType = ConstantCodes.FILE;
                            attachment.localPath = file.getPath();
                            attachment.isDone = true;
                        }
                        getInstance(getApplicationContext()).attachmentDao().updateAsync(attachment, null);
                        homeListRecyclerViewAdapter.updateItem(position, attachment);
                    }

                    @Override
                    public void onProgress(int progress) {
                        super.onProgress(progress);
                        attachment.progress = progress;

                        homeListRecyclerViewAdapter.updateItem(position, attachment);
                    }

                    @Override
                    File getLocalFile() {
                        return guessLocalFileForUrl(HomeListActivity.this, attachment.path);
                    }
                });
            } else {
                attachment.isDone = true;
                attachment.contentType = ConstantCodes.TEXT;
            }
            getInstance(getApplicationContext()).attachmentDao().insertAllAsync(new Attachment[]{attachment}, null);
        }
    }

    public static File guessLocalFileForUrl(Context context, String url) {

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "Download/");
        if (mediaStorageDir.exists() == false)
            mediaStorageDir.mkdirs();

        String mimeType = getMimeTypeFromURL(url);
        File mediaFile = new File(mediaStorageDir.getPath(), URLUtil.guessFileName(url, null, mimeType));
        return mediaFile;
    }

    public static String getMimeTypeFromURL(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(getExtension(url));
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
        }
        return type;
    }

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

    static AppDatabase appDatabase;

    public static AppDatabase getInstance(Context context) {
        if (appDatabase == null)
            appDatabase = Room.databaseBuilder(context, AppDatabase.class, "database-name").build();
        return appDatabase;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.send, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.send) {

        }
        return false;
    }

    private void uploadFile(final File file) {
        try {
            InputStream stream = new FileInputStream(file);
            // Create a storage reference from our app
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            // Create a reference to "mountains.jpg"
            StorageReference mountainsRef = storageRef.child(file.getName());

            UploadTask uploadTask = mountainsRef.putStream(stream);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Log.d(TAG, "onFailure() called with: exception = [" + exception + "]");
                    hideProgressDialog();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    Log.d(TAG, "onSuccess() called with: taskSnapshot = [" + taskSnapshot + "]," + downloadUrl);

                    Attachment attachment = new Attachment();
                    attachment.inbound = false;
                    attachment.path = downloadUrl.toString();
                    attachment.isDone = true;
                    attachment.contentType = ConstantCodes.FILE;
                    generateQRCode(file.getName(), downloadUrl.toString());

                    getInstance(getApplicationContext()).attachmentDao().insertAllAsync(new Attachment[]{attachment}, null);
                    homeListRecyclerViewAdapter.addItem(attachment);
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.setProgress((int) ((taskSnapshot.getBytesTransferred() * 100) / taskSnapshot.getTotalByteCount()));
                }
            });

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private QRCodeWriter qrWriter;

    private void generateQRCode(final String filename, String url) {
        ScalingUtilities.setWindowDimensions(this);
        int QRCODE_IMAGE_WIDTH = (int) (ScalingUtilities.SCREEN_WIDTH * 0.5);
        int QRCODE_IMAGE_HEIGHT = QRCODE_IMAGE_WIDTH;

        if (qrWriter == null)
            qrWriter = new QRCodeWriter();

        try {
            final BitMatrix bitMatrix = qrWriter.encode(url,
                    BarcodeFormat.QR_CODE,
                    QRCODE_IMAGE_WIDTH,
                    QRCODE_IMAGE_HEIGHT);

            final int height = bitMatrix.getHeight();
            final int width = bitMatrix.getWidth();
            new AsyncTask<Void, Void, Bitmap>() {

                @Override
                protected Bitmap doInBackground(Void... params) {
                    Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                        }
                    }
                    return bmp;
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    super.onPostExecute(bitmap);
                    openQRCodeDialog(filename, bitmap);
                    hideProgressDialog();
                }
            }.execute();

        } catch (WriterException e) {
            e.printStackTrace();
            hideProgressDialog();
        }
    }

    private void openQRCodeDialog(String filename, Bitmap bitmap) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = View.inflate(this, R.layout.qrcode_view, null);
        ((ImageView) view.findViewById(R.id.img)).setImageBitmap(bitmap);
        ((TextView) view.findViewById(R.id.name)).setText(filename);
        builder.setView(view);

        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(true);
        alertDialog.show();
    }
}
