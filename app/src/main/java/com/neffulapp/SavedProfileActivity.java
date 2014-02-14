package com.neffulapp;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.neffulapp.model.Contract;

public class SavedProfileActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_PROFILE = 321;
    private ProfileAdapter adapter;
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_profile);
        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        adapter = new ProfileAdapter(this, null, false);
        setListAdapter(adapter);
        getListView().setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                getListView().setEnabled(false);
                final String listName = adapter.getItem(position);
                loadList(listName);
                setResult(RESULT_OK);
                finish();
            }
        });
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        getListView().setMultiChoiceModeListener(new ModeCallback());
        getLoaderManager().initLoader(LOADER_PROFILE, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.saved_profile, menu);
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = Contract.Profile.CONTENT_URI;
        String[] projection = new String[]{Contract.Profile.COLUMN_NAME_ID, Contract.Profile.COLUMN_NAME_NAME,
                Contract.Profile.COLUMN_NAME_CREATED_AT};
        String selection = null;
        String[] selectionArgs = null;
        return new CursorLoader(this, uri, projection, selection, selectionArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    public class ProfileAdapter extends CursorAdapter {

        private LayoutInflater mLayoutInflater;

        public ProfileAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
            mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = mLayoutInflater.inflate(R.layout.load_list_row, parent, false);
            view.setTag(new ViewHolder(view));
            return view;
        }

        @Override
        public void bindView(View v, final Context context, Cursor c) {
            String sProfileName = c.getString(c.getColumnIndex(Contract.Profile.COLUMN_NAME_NAME));
            String sCreatedAt = c.getString(c.getColumnIndex(Contract.Profile.COLUMN_NAME_CREATED_AT));
            sCreatedAt = sCreatedAt.replace(" ", "\n");
            ViewHolder vh = (ViewHolder) v.getTag();
            vh.profileNameTxt.setText(sProfileName);
            vh.createdAtTxt.setText(sCreatedAt);
        }

        @Override
        public String getItem(int position) {
            Cursor c = getCursor();
            c.moveToPosition(position);
            return c.getString(c.getColumnIndex(Contract.Profile.COLUMN_NAME_NAME));
        }

        private class ViewHolder {

            TextView profileNameTxt;
            TextView createdAtTxt;

            ViewHolder(View v) {
                profileNameTxt = (TextView) v.findViewById(R.id.profile_name_txt);
                createdAtTxt = (TextView) v.findViewById(R.id.profile_created_at_txt);
            }
        }
    }

    private class ModeCallback implements ListView.MultiChoiceModeListener {

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.context, menu);
            mode.setTitle("Select Items");
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.delete:
                    final SparseBooleanArray checkedItems = getListView().getCheckedItemPositions();
                    int checkedCount = getListView().getCheckedItemCount();
                    String plural = (checkedCount > 1) ? "items" : "item";

                    AlertDialog.Builder builder = new AlertDialog.Builder(SavedProfileActivity.this);
                    builder.setIcon(R.drawable.ic_action_warning);
                    builder.setTitle("Delete");
                    builder.setMessage("Are you sure you want to delete selected " + plural + "?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            deleteSelectedLists(checkedItems);
                            mode.finish();
                        }
                    });
                    builder.setNegativeButton("No", null);
                    builder.create().show();
                    break;
                case R.id.selectAll:
                    for (int i = 0; i < getListAdapter().getCount(); i++) {
                        getListView().setItemChecked(i, true);
                    }
                    break;
                default:
                    break;
            }
            return true;
        }

        public void onDestroyActionMode(ActionMode mode) {
        }

        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            final int checkedCount = getListView().getCheckedItemCount();
            switch (checkedCount) {
                case 0:
                    mode.setSubtitle(null);
                    break;
                case 1:
                    mode.setSubtitle("One item selected");
                    break;
                default:
                    mode.setSubtitle("" + checkedCount + " items selected");
                    break;
            }
        }
    }

    private void loadList(String listname) {
        String defaultListID = getString(R.string.default_list_id);
        // Delete the entire Table Temp
        getContentResolver().delete(Contract.Temp.CONTENT_URI, null, null);
        // Delete remark associated with default list ID
        getContentResolver().delete(Contract.Remark.CONTENT_URI, Contract.Remark.COLUMN_NAME_PROFILE_ID + "=?", new String[]{defaultListID});
        // Run query on Table Profile with list name to retreive the list ID for next query.
        Cursor cProfile = getContentResolver().query(Contract.Profile.CONTENT_URI, new String[]{Contract.Profile.COLUMN_NAME_ID},
                Contract.Profile.COLUMN_NAME_NAME + "=?", new String[]{listname}, null);
        if (cProfile != null && cProfile.moveToFirst()) {
            int listID = cProfile.getInt(cProfile.getColumnIndex(Contract.Profile.COLUMN_NAME_ID));
            cProfile.close();
            // Copy item list to temporary table
            Cursor cCart = getContentResolver().query(Contract.Cart.CONTENT_URI, new String[]{Contract.Cart.COLUMN_NAME_NAME, Contract.Cart.COLUMN_NAME_SUBCATEGORY, Contract.Cart.COLUMN_NAME_QUANTITY,
                    Contract.Cart.COLUMN_NAME_SIZE, Contract.Cart.COLUMN_NAME_COLOR, Contract.Cart.COLUMN_NAME_PRICE, Contract.Cart.COLUMN_NAME_LABOR}, Contract.Cart.COLUMN_NAME_PROFILE_ID + "=?",
                    new String[]{Integer.toString(listID)}, null);
            if (cCart != null && cCart.moveToFirst()) {
                String name, subcategory, size, color;
                int quantity, price, labor;
                int i = 0;
                ContentValues[] values = new ContentValues[cCart.getCount()];
                do {
                    name = cCart.getString(cCart.getColumnIndex(Contract.Cart.COLUMN_NAME_NAME));
                    subcategory = cCart.getString(cCart.getColumnIndex(Contract.Cart.COLUMN_NAME_SUBCATEGORY));
                    quantity = cCart.getInt(cCart.getColumnIndex(Contract.Cart.COLUMN_NAME_QUANTITY));
                    size = cCart.getString(cCart.getColumnIndex(Contract.Cart.COLUMN_NAME_SIZE));
                    color = cCart.getString(cCart.getColumnIndex(Contract.Cart.COLUMN_NAME_COLOR));
                    price = cCart.getInt(cCart.getColumnIndex(Contract.Cart.COLUMN_NAME_PRICE));
                    labor = cCart.getInt(cCart.getColumnIndex(Contract.Cart.COLUMN_NAME_LABOR));
                    values[i] = new ContentValues();
                    values[i].put(Contract.Temp.COLUMN_NAME_NAME, name);
                    values[i].put(Contract.Temp.COLUMN_NAME_SUBCATEGORY, subcategory);
                    values[i].put(Contract.Temp.COLUMN_NAME_QUANTITY, quantity);
                    values[i].put(Contract.Temp.COLUMN_NAME_SIZE, size);
                    values[i].put(Contract.Temp.COLUMN_NAME_COLOR, color);
                    values[i].put(Contract.Temp.COLUMN_NAME_PRICE, price);
                    if (labor > 0) {
                        values[i].put(Contract.Temp.COLUMN_NAME_LABOR, labor);
                    }
                    i++;
                } while (cCart.moveToNext());
                cCart.close();
                getContentResolver().bulkInsert(Contract.Temp.CONTENT_URI, values);
            }
            // Copy remark to default list
            Cursor cRemark = getContentResolver().query(Contract.Remark.CONTENT_URI, null, Contract.Remark.COLUMN_NAME_PROFILE_ID + "=?", new String[]{Integer.toString(listID)}, null);
            if (cRemark != null && cRemark.moveToFirst()) {
                String itemName, remark;
                int i = 0;
                ContentValues[] values = new ContentValues[cRemark.getCount()];
                do {
                    itemName = cRemark.getString(cRemark.getColumnIndex(Contract.Remark.COLUMN_NAME_ITEM_NAME));
                    remark = cRemark.getString(cRemark.getColumnIndex(Contract.Remark.COLUMN_NAME_REMARK));
                    values[i] = new ContentValues();
                    values[i].put(Contract.Remark.COLUMN_NAME_PROFILE_ID, Integer.parseInt(defaultListID));
                    values[i].put(Contract.Remark.COLUMN_NAME_ITEM_NAME, itemName);
                    values[i].put(Contract.Remark.COLUMN_NAME_REMARK, remark);
                    i++;
                } while (cRemark.moveToNext());
                cRemark.close();
                getContentResolver().bulkInsert(Contract.Remark.CONTENT_URI, values);
            }
            // Update the list name and list ID in preference
            pref.edit().putString(getString(R.string.list_name), listname).apply();
            pref.edit().putInt(getString(R.string.list_id), listID).apply();
        }
    }

    private void deleteSelectedLists(SparseBooleanArray checkedItems) {
        if (checkedItems != null) {
            String whereName = null;
            String whereID = null;
            String listName, listID, currentListName;
            for (int i = 0; i < checkedItems.size(); i++) {
                // Returns profile name from the adapter.
                listName = getListAdapter().getItem(checkedItems.keyAt(i)).toString();
                currentListName = pref.getString(getString(R.string.list_name), getString(R.string.default_list_name));
                // If one of the selected profile name is equal to current preference profile name then update it to default value.
                if (listName.equals(currentListName)) {
                    int defaultListID = 0;
                    pref.edit().putString(getString(R.string.list_name), getString(R.string.default_list_name)).apply();
                    pref.edit().putInt(getString(R.string.list_id), defaultListID).apply();
                }
                // Build a string of profile names for WHERE statement to delete rows in Table Profile
                if (i == 0) {
                    whereName = "'" + listName + "'";
                } else {
                    whereName = whereName + ",'" + listName + "'";
                }
                // Query Table Profile for list ID.
                Cursor cProfile = getContentResolver().query(Contract.Profile.CONTENT_URI, new String[]{Contract.Profile.COLUMN_NAME_ID},
                        Contract.Profile.COLUMN_NAME_NAME + "=?", new String[]{listName}, null);
                // Build a string of profile IDs for WHERE statement to delete rows in Table Cart associated with those profile IDs.
                if (cProfile != null && cProfile.moveToFirst()) {
                    listID = String.valueOf(cProfile.getInt(cProfile.getColumnIndex(Contract.Profile.COLUMN_NAME_ID)));
                    cProfile.close();
                    if (i == 0) {
                        whereID = "'" + listID + "'";
                    } else {
                        whereID = whereID + ",'" + listID + "'";
                    }
                }
            }
            getContentResolver().delete(Contract.Profile.CONTENT_URI, Contract.Profile.COLUMN_NAME_NAME + " IN (" + whereName + ")", null);
            getContentResolver().delete(Contract.Remark.CONTENT_URI, Contract.Remark.COLUMN_NAME_PROFILE_ID + " IN (" + whereID + ")", null);
            getContentResolver().delete(Contract.Cart.CONTENT_URI, Contract.Cart.COLUMN_NAME_PROFILE_ID + " IN (" + whereID + ")", null);
        }
    }
}
