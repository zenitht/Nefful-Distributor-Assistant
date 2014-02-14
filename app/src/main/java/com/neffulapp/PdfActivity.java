package com.neffulapp;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ShareActionProvider;

import net.sf.andpdf.pdfviewer.PdfViewerActivity;

import java.io.File;

public class PdfActivity extends PdfViewerActivity {

    public static final String EXTRA_PDFURI = "Pdf Uri";
    private ShareActionProvider mShareActionProvider;
    private String mPath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPath = getIntent().getStringExtra(PdfActivity.EXTRA_PDFURI);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.pdfviewer, menu);
        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.share);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // Fetch and store ShareActionProvider
            mShareActionProvider = (ShareActionProvider) item.getActionProvider();
            doShare(createShareIntent());
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.print:
                Intent printIntent = new Intent(this, PrintDialogActivity.class);
                printIntent.setDataAndType(Uri.parse(mPath), "application/pdf");
                printIntent.putExtra("title", "Full Set Form");
                startActivity(printIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        //shareIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { "zen.ithtan89@gmail.com" });
        //shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Full Set");
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(mPath));
        return shareIntent;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void doShare(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    public int getPreviousPageImageResource() {
        return R.drawable.left_arrow;
    }

    public int getNextPageImageResource() {
        return R.drawable.right_arrow;
    }

    public int getZoomInImageResource() {
        return R.drawable.zoom_in;
    }

    public int getZoomOutImageResource() {
        return R.drawable.zoom_out;
    }

    public int getPdfPasswordLayoutResource() {
        return R.layout.pdf_file_password;
    }

    public int getPdfPageNumberResource() {
        return R.layout.dialog_pagenumber;
    }

    public int getPdfPasswordEditField() {
        return R.id.etPassword;
    }

    public int getPdfPasswordOkButton() {
        return R.id.btOK;
    }

    public int getPdfPasswordExitButton() {
        return R.id.btExit;
    }

    public int getPdfPageNumberEditField() {
        return R.id.pagenum_edit;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            trimCache(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void trimCache(Context context) {
        try {
            File dir = context.getExternalCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                boolean success = deleteDir(new File(dir, aChildren));
                if (!success) {
                    return false;
                }
            }
        }
        // The directory is now empty so delete it
        return dir.delete();
    }
}
