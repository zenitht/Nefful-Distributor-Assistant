package com.neffulapp;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class PrintDialogActivity extends Activity {

    private static final String PRINT_DIALOG_URL = "https://www.google.com/cloudprint/dialog.html";
    private static final String JS_INTERFACE = "AndroidPrintDialog";
    private static final String CONTENT_TRANSFER_ENCODING = "base64";
    private static final String ZXING_URL = "http://zxing.appspot.com";
    private static final int ZXING_SCAN_REQUEST = 65743;
    /**
     * Post message that is sent by Print Dialog web page when the printing dialog needs to be closed.
     */
    private static final String CLOSE_POST_MESSAGE_NAME = "cp-dialog-on-close";
    /**
     * Web view element to show the printing dialog in.
     */
    private WebView dialogWebView;
    /**
     * Other views.
     */
    private ProgressBar mProgress;
    /**
     * Intent that started the action.
     */
    Intent cloudPrintIntent;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        // Adds Progrss bar Support
        this.getWindow().requestFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.activity_print_dialog);
        // Makes Window Progress bar Visible
        getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
        // Get Activity ProgressBar view
        mProgress = (ProgressBar) findViewById(R.id.progressBar);
        // Get Web view
        dialogWebView = (WebView) findViewById(R.id.webview);
        cloudPrintIntent = this.getIntent();
        WebSettings settings = dialogWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        dialogWebView.setWebViewClient(new PrintDialogWebClient());
        dialogWebView.addJavascriptInterface(new PrintDialogJavaScriptInterface(), JS_INTERFACE);
        dialogWebView.loadUrl(PRINT_DIALOG_URL);
        // Sets the Chrome Client, and defines the onProgressChanged
        // This makes the Progress bar be updated.
        final Activity MyActivity = this;
        dialogWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                // Make the bar disappear after URL is loaded, and changes string to Loading...
                MyActivity.setTitle("Loading...");
                MyActivity.setProgress(progress * 100); // Make the bar disappear after URLis loaded
                // Return the app name after finish loading
                if (progress > 40) {
                    mProgress.setVisibility(View.GONE);
                    if (progress == 100) {
                        MyActivity.setTitle(R.string.title_activity_print_dialog);
                    }
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == ZXING_SCAN_REQUEST && resultCode == RESULT_OK) {
            dialogWebView.loadUrl(intent.getStringExtra("SCAN_RESULT"));
        }
    }

    final class PrintDialogJavaScriptInterface {

        @JavascriptInterface
        public String getType() {
            return cloudPrintIntent.getType();
        }

        @JavascriptInterface
        public String getTitle() {
            return cloudPrintIntent.getExtras().getString("title");
        }

        @JavascriptInterface
        public String getContent() {
            try {
                ContentResolver contentResolver = getContentResolver();
                InputStream is = contentResolver.openInputStream(cloudPrintIntent.getData());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int n = is.read(buffer);
                while (n >= 0) {
                    baos.write(buffer, 0, n);
                    n = is.read(buffer);
                }
                is.close();
                baos.flush();
                return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }

        @JavascriptInterface
        public String getEncoding() {
            return CONTENT_TRANSFER_ENCODING;
        }

        @JavascriptInterface
        public void onPostMessage(String message) {
            if (message.startsWith(CLOSE_POST_MESSAGE_NAME)) {
                finish();
            }
        }
    }

    private final class PrintDialogWebClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith(ZXING_URL)) {
                Intent intentScan = new Intent("com.google.zxing.client.android.SCAN");
                intentScan.putExtra("SCAN_MODE", "QR_CODE_MODE");
                try {
                    startActivityForResult(intentScan, ZXING_SCAN_REQUEST);
                } catch (ActivityNotFoundException error) {
                    view.loadUrl(url);
                }
            } else {
                view.loadUrl(url);
            }
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (PRINT_DIALOG_URL.equals(url)) {
                // Submit print document.
                view.loadUrl("javascript:printDialog.setPrintDocument(printDialog.createPrintDocument(" + "window." + JS_INTERFACE
                        + ".getType(),window." + JS_INTERFACE + ".getTitle()," + "window." + JS_INTERFACE + ".getContent(),window." + JS_INTERFACE
                        + ".getEncoding()))");
                // Add post messages listener.
                view.loadUrl("javascript:window.addEventListener('message'," + "function(evt){window." + JS_INTERFACE
                        + ".onPostMessage(evt.data)}, false)");
            }
        }
    }
}