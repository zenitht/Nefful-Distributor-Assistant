package com.neffulapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.neffulapp.model.Contract;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // Constants
    private static final int LOADER_ORDER_TOTAL = 1;
    private static DecimalFormat formatter = new DecimalFormat("#,###");
    private Toast toast;
    private SharedPreferences pref;
    private List<String> pageTitles = new ArrayList<String>();
    private SectionsPagerAdapter adapter;
    private ViewPager viewPager;
    private TextView orderTotal;
    private TextView itemListName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //opening transition animations
        overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
        // Retrieve intent passed from previous activity.
        Intent intent = getIntent();
        String currentCategory = intent.getStringExtra(getPackageName() + ".Category");
        getActionBar().setTitle(currentCategory);
        // Restore preferences
        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        try {
            JSONArray jsonArray = new JSONArray(pref.getString(currentCategory, ""));
            for (int i = 0; i < jsonArray.length(); i++) {
                pageTitles.add(jsonArray.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Find views from the layout file.
        viewPager = (ViewPager) findViewById(R.id.pager);
        orderTotal = (TextView) findViewById(R.id.main_order_total);
        itemListName = (TextView) findViewById(R.id.item_list_name);
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.pager_tab_strip);
        // Create the adapter that will return a fragment for each of the three primary sections of the app.
        adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Hook the ViewPager to adapter.
        viewPager.setAdapter(adapter);
        final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
        viewPager.setPageMargin(pageMargin);
        // Bind the pager tab strip to the ViewPager
        tabs.setViewPager(viewPager);
        tabs.setIndicatorColor(getResources().getColor(R.color.action_bar_purple));
        // Initialize loader
        getSupportLoaderManager().initLoader(LOADER_ORDER_TOTAL, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate);
                return true;
            case R.id.clear:
                clear();
                return true;
            case R.id.done:
                startPreviewActivity();
                return true;
            case R.id.load:
                startSavedProfileActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setItemListName();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate);
    }

    // ************************************************************************************************
    // Action menu methods.
    // ************************************************************************************************
    private void clear() {
        // Delete the entire Table Temp
        getContentResolver().delete(Contract.Temp.CONTENT_URI, null, null);
        // Get current fragment and disable image reload
        int index = viewPager.getCurrentItem() % pageTitles.size();
        final Fragment fragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + viewPager.getId() + ":" + index);
        ((TabListFragment) fragment).getAdapter().isLoadImage = false;
        adapter.notifyDataSetChanged();
        // Sleep for 0.5 second and then re-enable image loading
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((TabListFragment) fragment).getAdapter().isLoadImage = true;
            }
        }, 500);
        // Delete remark associated with the default profile if any.
        int defaultProfileID = 0;
        getContentResolver().delete(Contract.Remark.CONTENT_URI, Contract.Remark.COLUMN_NAME_PROFILE_ID + "=?", new String[]{String.valueOf(defaultProfileID)});
        // Update profile name and ID in preference to "Default"
        pref.edit().putString(getString(R.string.list_name), getString(R.string.default_list_name)).apply();
        pref.edit().putInt(getString(R.string.list_id), defaultProfileID).apply();
        // Update displayed list name
        setItemListName();
        // Notify user
        showToast(this, "Cleared");
    }

    private void startPreviewActivity() {
        updateRemark();
        Intent intent = new Intent(getApplicationContext(), PreviewActivity.class);
        startActivity(intent);
    }

    private void startSavedProfileActivity() {
        Intent intent = new Intent(getApplicationContext(), SavedProfileActivity.class);
        startActivity(intent);
    }

    private void setItemListName() {
        String itemListName = pref.getString(getString(R.string.list_name), getString(R.string.default_list_name));
        if (itemListName.equals(getString(R.string.default_list_name))) {
            this.itemListName.setText("");
        } else {
            this.itemListName.setText("~" + itemListName);
        }
    }

    private void updateRemark() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<String> itemNameList = new ArrayList<String>();
                String defaultListID = getString(R.string.default_list_id);
                // Query column name of Table Temp and store them into a list.
                Cursor cTemp = getContentResolver().query(Contract.Temp.CONTENT_URI, new String[]{Contract.Temp.COLUMN_NAME_NAME}, null, null, null);
                if (cTemp != null && cTemp.moveToFirst()) {
                    do {
                        String itemName = cTemp.getString(cTemp.getColumnIndex(Contract.Temp.COLUMN_NAME_NAME));
                        itemNameList.add(itemName);
                    } while (cTemp.moveToNext());
                    cTemp.close();
                }
                // Query Table Remark and delete any remark that the item name doesn't match with any item in Table Temp
                Cursor cRemark = getContentResolver().query(Contract.Remark.CONTENT_URI, new String[]{Contract.Remark.COLUMN_NAME_ITEM_NAME},
                        Contract.Remark.COLUMN_NAME_PROFILE_ID + "=?", new String[]{defaultListID}, null);
                if (cRemark != null && cRemark.moveToFirst()) {
                    do {
                        String itemName = cRemark.getString(cRemark.getColumnIndex(Contract.Remark.COLUMN_NAME_ITEM_NAME));
                        if (!itemNameList.contains(itemName)) {
                            getContentResolver().delete(Contract.Remark.CONTENT_URI, Contract.Remark.COLUMN_NAME_PROFILE_ID + "=? AND " +
                                    Contract.Remark.COLUMN_NAME_ITEM_NAME + "=?", new String[]{defaultListID, itemName});
                        }
                    } while (cRemark.moveToNext());
                    cRemark.close();
                }
            }
        }).start();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        FragmentManager fm;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fm = fm;
        }

        @Override
        public Fragment getItem(int position) {
            return TabListFragment.newInstance(pageTitles.get(position));
        }

        @Override
        public int getCount() {
            return pageTitles.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return pageTitles.get(position);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = null;
        String[] projection = null;
        String selection = null;
        String[] selectionArgs = null;
        switch (id) {
            case LOADER_ORDER_TOTAL:
                uri = Contract.Temp.CONTENT_URI;
                projection = new String[]{Contract.Temp.COLUMN_NAME_PRICE};
                break;
            default:
                break;
        }
        return new CursorLoader(this, uri, projection, selection, selectionArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case LOADER_ORDER_TOTAL:
                long total = 0;
                long rowPrice;
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        rowPrice = cursor.getInt(cursor.getColumnIndex(Contract.Temp.COLUMN_NAME_PRICE));
                        total = total + rowPrice;
                    } while (cursor.moveToNext());
                }
                orderTotal.setText(getString(R.string.order_total) + " " + formatter.format(total));
                break;
            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    // Utility methods
    private void showToast(Context context, CharSequence text) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        toast.show();
    }
}
