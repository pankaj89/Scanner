package example.com.scanner;

import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;

import example.com.scanner.database.Attachment;

public class ScannerActivity extends AppCompatActivity implements QRCodeReaderView.OnQRCodeReadListener {

    private TextView resultTextView;
    private QRCodeReaderView qrCodeReaderView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resultTextView = (TextView) findViewById(R.id.resultTextView);
        resultTextView.setVisibility(View.GONE);
        qrCodeReaderView = (QRCodeReaderView) findViewById(R.id.qrdecoderview);
        qrCodeReaderView.setOnQRCodeReadListener(this);

        // Use this function to enable/disable decoding
        qrCodeReaderView.setQRDecodingEnabled(true);

        // Use this function to change the autofocus interval (default is 5 secs)
        qrCodeReaderView.setAutofocusInterval(2000L);

        // Use this function to enable/disable Torch
        qrCodeReaderView.setTorchEnabled(true);

        // Use this function to set front camera preview
        qrCodeReaderView.setFrontCamera();

        // Use this function to set back camera preview
        qrCodeReaderView.setBackCamera();
    }

    @Override
    public void onQRCodeRead(String text, PointF[] points) {
        resultTextView.setText(text + "");
        qrCodeReaderView.stopCamera();
        qrCodeReaderView.setVisibility(View.GONE);

        Attachment attachment = new Attachment();
        attachment.path = text;
        attachment.id = System.currentTimeMillis() + "";
        attachment.inbound=true;

        Intent intent = new Intent();
        intent.putExtra("data", attachment);
        setResult(RESULT_OK, intent);

        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        qrCodeReaderView.startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        qrCodeReaderView.stopCamera();
    }
}
