package com.neffulapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.neffulapp.model.Contract;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class MainCategoriesActivity extends Activity implements OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String URL_NEFFUL_HOME = "http://www.nefful.com.my";
    private static final String URL_NEFFUL_MES = "http://www.nefful.com.tw/web-vol-e/inquery_history_form.php";
    private static final String URL_APP_VERSION = "http://neffulapp.net76.net/version.php";
    private static final String URL_APP_APK = "http://neffulapp.net76.net/NDA.apk";
    private static final int LOADER_BEAUTIFUL_STORY = 1;
    private static final int LOADER_TEVIRON_STORY = 2;
    private static final int LOADER_YOUNGLIFE_STORY = 3;
    private static final int LOADER_ALL_SUBCATEGORY = 4;
    private SharedPreferences pref;
    private DownloadManager downloadManager;
    private long downloadID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_categories);
        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //pref.registerOnSharedPreferenceChangeListener(prefListener);

        ImageView mCategory1 = (ImageView) findViewById(R.id.beautiful_story);
        ImageView mCategory2 = (ImageView) findViewById(R.id.teviron_story);
        ImageView mCategory3 = (ImageView) findViewById(R.id.young_life_story);
        Button mNeffulHome = (Button) findViewById(R.id.nefful_home);
        Button mNeffulMES = (Button) findViewById(R.id.nefful_mes);

        mCategory1.setOnClickListener(this);
        mCategory2.setOnClickListener(this);
        mCategory3.setOnClickListener(this);
        mNeffulHome.setOnClickListener(this);
        mNeffulMES.setOnClickListener(this);

        long lastUpdateTime = pref.getLong("lastUpdateTime", 0);
        if ((lastUpdateTime + (24 * 60 * 60 * 1000)) < System.currentTimeMillis()) {
            lastUpdateTime = System.currentTimeMillis();
            pref.edit().putLong("lastUpdateTime", lastUpdateTime).commit();
            checkUpdate();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pref.getBoolean("isFirstRun", true)) {
            Bundle bundle = new Bundle();
            // Load all subcategories.
            getLoaderManager().initLoader(LOADER_ALL_SUBCATEGORY, null, this);
            // Load Beautiful Story subcategories.
            bundle.putString("Category", getString(R.string.beautiful_story));
            getLoaderManager().initLoader(LOADER_BEAUTIFUL_STORY, bundle, this);
            // Load Teviron Story subcategories.
            bundle.putString("Category", getString(R.string.teviron_story));
            getLoaderManager().initLoader(LOADER_TEVIRON_STORY, bundle, this);
            // Load YoungLife Story subcategories.
            bundle.putString("Category", getString(R.string.young_life_story));
            getLoaderManager().initLoader(LOADER_YOUNGLIFE_STORY, bundle, this);
            // Sets the profile name and ID to default on first run.
            pref.edit().putString(getString(R.string.list_name), getString(R.string.default_list_name)).apply();
            pref.edit().putInt(getString(R.string.list_id), 0).apply();
            // Put isFirstRun value to "false"
            pref.edit().putBoolean("isFirstRun", false).apply();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_categories, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                Intent addItemIntent = new Intent(getApplicationContext(), AddItemActivity.class);
                startActivity(addItemIntent);
                return true;
            case R.id.delete:
                Intent deleteItemIntent = new Intent(getApplicationContext(), DeleteItemActivity.class);
                startActivity(deleteItemIntent);
                return true;
            case R.id.settings:
                Intent settingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.about:
                Intent aboutIntent = new Intent(getApplicationContext(), AboutActivity.class);
                startActivity(aboutIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.beautiful_story:
                Intent beautiful = new Intent(getApplicationContext(), MainActivity.class);
                beautiful.putExtra(getPackageName() + ".Category", v.getContentDescription());
                startActivity(beautiful);
                break;
            case R.id.teviron_story:
                Intent teviron = new Intent(getApplicationContext(), MainActivity.class);
                teviron.putExtra(getPackageName() + ".Category", v.getContentDescription());
                startActivity(teviron);
                break;
            case R.id.young_life_story:
                Intent younglife = new Intent(getApplicationContext(), MainActivity.class);
                younglife.putExtra(getPackageName() + ".Category", v.getContentDescription());
                startActivity(younglife);
                break;
            case R.id.nefful_home:
                Intent webHome = new Intent(Intent.ACTION_VIEW, Uri.parse(URL_NEFFUL_HOME));
                startActivity(webHome);
                break;
            case R.id.nefful_mes:
                Intent webMES = new Intent(getApplicationContext(), WebViewActivity.class);
                webMES.putExtra(getPackageName() + ".WebView", URL_NEFFUL_MES);
                webMES.putExtra("Title", getString(R.string.title_activity_webview_mes));
                startActivity(webMES);
                break;
        }
    }

    SharedPreferences.OnSharedPreferenceChangeListener prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            //Todo: add update database records in following update.
        }
    };

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = null;
        String[] projection = null;
        String selection = null;
        String[] selectionArgs = null;

        if (id == LOADER_ALL_SUBCATEGORY) {
            uri = Contract.Product.DISTINCT_CONTENT_URI;
            projection = new String[]{Contract.Product.COLUMN_NAME_SUBCATEGORY};
            selection = null;
            selectionArgs = null;
        } else {
            uri = Contract.Product.DISTINCT_CONTENT_URI;
            projection = new String[]{Contract.Product.COLUMN_NAME_SUBCATEGORY};
            selection = Contract.Product.COLUMN_NAME_CATEGORY + " = ?";
            selectionArgs = new String[]{args.getString("Category")};
        }
        return new CursorLoader(this, uri, projection, selection, selectionArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            JSONArray jsonArray = new JSONArray();
            do {
                String subcategory = cursor.getString(cursor.getColumnIndex(Contract.Product.COLUMN_NAME_SUBCATEGORY));
                jsonArray.put(subcategory);
            } while (cursor.moveToNext());
            setPrefInitialValue(loader.getId(), jsonArray);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private void setPrefInitialValue(int id, JSONArray jsonArray) {
        SharedPreferences.Editor editor = pref.edit();
        switch (id) {
            case LOADER_ALL_SUBCATEGORY:
                editor.putString(getString(R.string.all_subcategories), jsonArray.toString()).apply();
                break;
            case LOADER_BEAUTIFUL_STORY:
                editor.putString(getString(R.string.beautiful_story), jsonArray.toString()).apply();
                break;
            case LOADER_TEVIRON_STORY:
                editor.putString(getString(R.string.teviron_story), jsonArray.toString()).apply();
                break;
            case LOADER_YOUNGLIFE_STORY:
                editor.putString(getString(R.string.young_life_story), jsonArray.toString()).apply();
                break;
            default:
                break;
        }
    }

    private void checkUpdate() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new GetLatestAppVersion().execute(URL_APP_VERSION);
        }
    }

    private class GetLatestAppVersion extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String url = urls[0];
            String version = null;

            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(url);
                HttpResponse response = httpClient.execute(httpPost);
                HttpEntity entity = response.getEntity();
                InputStream is = entity.getContent();

                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "ISO-8859-1"), 8);
                    version = reader.readLine();
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return version;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                int latestVersion = Integer.parseInt(result);

                try {
                    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    int currentVersion = pInfo.versionCode;

                    if (currentVersion < latestVersion) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainCategoriesActivity.this);
                        builder.setTitle("Newer version available");
                        builder.setMessage("Do you want to download the latest version of the application?");

                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                                registerReceiver(onNotificationClick, new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));

                                if (isDownloadManagerAvailable(getApplicationContext())) {
                                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(URL_APP_APK));
                                    request.setTitle("Nefful Distributor Assistant");

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                        request.allowScanningByMediaScanner();
                                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                    }
                                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "NDA.apk");

                                    downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                                    downloadID = downloadManager.enqueue(request);
                                } else {
                                    Toast.makeText(getApplicationContext(), "Request cannot be processed.", Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                        builder.setNegativeButton("No", null);
                        builder.create();
                        builder.show();
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean isDownloadManagerAvailable(Context context) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
                return false;
            }
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setClassName("com.android.providers.downloads.ui", "com.android.providers.downloads.ui.DownloadList");
            List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            return list.size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    BroadcastReceiver onComplete = new BroadcastReceiver() {
        public void onReceive(Context ctx, Intent intent) {
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                Query query = new Query();
                query.setFilterById(downloadID);
                Cursor c = downloadManager.query(query);
                if (c.moveToFirst()) {
                    int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                        String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                        Intent intentApk = new Intent(Intent.ACTION_VIEW);
                        intentApk.setDataAndType(Uri.fromFile(new File(uriString)), "application/vnd.android.package-archive");
                        startActivity(intentApk);
                    }
                }
            }
        }
    };

    BroadcastReceiver onNotificationClick = new BroadcastReceiver() {
        public void onReceive(Context ctx, Intent intent) {
            Intent i = new Intent();
            i.setAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
            startActivity(i);
        }
    };
}
