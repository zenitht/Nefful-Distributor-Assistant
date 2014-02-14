package com.neffulapp;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.neffulapp.model.Contract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DeleteItemActivity extends FragmentActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_PRODUCT = 1;
    private static final int SPINNER_CATEGORY = R.id.delete_category;
    private static final int SPINNER_SUBCATEGORY = R.id.delete_subcategory;
    private static final int BUTTON_CONTINUE = R.id.continue_button;
    private static final int DIALOG_DELETE = 1;
    private static final int DIALOG_CONFIRM = 2;
    // Member variables
    private Map<String, List<String>> categoryMap = new LinkedHashMap<String, List<String>>();
    private Spinner categorySpn;
    private Spinner subcategorySpn;
    private Button continueBtn;
    private SpinnerAdapter categoryAdapter;
    private SpinnerAdapter subcategoryAdapter;
    private List<String> categoryList = new ArrayList<String>();
    private List<String> subcategoryList = new ArrayList<String>();
    private String selectedCategory = null;
    private String selectedSubCategory = null;
    private AlertDialog dialog = null;
    private String[] items = null;
    private List<String> selectedItems = new ArrayList<String>();
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_item);
        categorySpn = (Spinner) findViewById(SPINNER_CATEGORY);
        subcategorySpn = (Spinner) findViewById(SPINNER_SUBCATEGORY);
        continueBtn = (Button) findViewById(BUTTON_CONTINUE);

        categorySpn.setOnItemSelectedListener(this);
        subcategorySpn.setOnItemSelectedListener(this);
        continueBtn.setOnClickListener(this);

        categoryAdapter = new SpinnerAdapter(this, R.layout.spinner_row, categoryList);
        subcategoryAdapter = new SpinnerAdapter(this, R.layout.spinner_row, subcategoryList);

        categorySpn.setAdapter(categoryAdapter);
        subcategorySpn.setAdapter(subcategoryAdapter);

        getSupportLoaderManager().initLoader(LOADER_PRODUCT, null, this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
        switch (parent.getId()) {
            case SPINNER_CATEGORY:
                selectedCategory = parent.getItemAtPosition(position).toString();
                subcategoryList.clear();
                subcategoryList.addAll(categoryMap.get(selectedCategory));
                subcategoryAdapter.notifyDataSetChanged();
                subcategorySpn.setAdapter(subcategoryAdapter);
                break;
            case SPINNER_SUBCATEGORY:
                selectedSubCategory = parent.getItemAtPosition(position).toString();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case BUTTON_CONTINUE:
                DialogAsyncTask task = new DialogAsyncTask();
                task.execute();
                break;
        }
    }

    private void createAlertDialog(int id, String[] items) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        switch (id) {
            case DIALOG_DELETE:
                boolean[] checked = new boolean[items.length];
                Arrays.fill(checked, false);
                builder.setMultiChoiceItems(items, checked, null);
                builder.setPositiveButton(R.string.delete, onDeleteListener);
                break;
            case DIALOG_CONFIRM:
                builder.setIcon(R.drawable.ic_action_warning);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure you want to delete " + Arrays.toString(items) + "?");
                builder.setPositiveButton("Yes", onConfirmListener);
                break;
        }

        builder.setNegativeButton("No", null);

        dialog = builder.create();
        dialog.show();
    }

    private DialogInterface.OnClickListener onDeleteListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int which) {
            selectedItems.clear();
            int itemCount = dialog.getListView().getCount();
            SparseBooleanArray checked = dialog.getListView().getCheckedItemPositions();
            for (int i = 0; i < itemCount; i++) {
                if (checked.get(i)) {
                    selectedItems.add(dialog.getListView().getAdapter().getItem(i).toString());
                }
            }

            if (!selectedItems.isEmpty()) {
                String[] selectedItemsArr = selectedItems.toArray(new String[selectedItems.size()]);
                createAlertDialog(DIALOG_CONFIRM, selectedItemsArr);
            } else {
                showToast(DeleteItemActivity.this, "Please select at least one item to delete.");
            }
        }
    };

    private DialogInterface.OnClickListener onConfirmListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // Delete checked items
                    for (String item : selectedItems) {
                        int productID;
                        String sProductID = null;
                        Cursor cProduct = getContentResolver().query(Contract.Product.CONTENT_URI, new String[]{Contract.Product.COLUMN_NAME_ID, Contract.Product.COLUMN_NAME_PHOTO}, Contract.Product.COLUMN_NAME_NAME + "=?", new String[]{item}, null);
                        if (cProduct != null && cProduct.moveToFirst()) {
                            productID = cProduct.getInt(cProduct.getColumnIndex(Contract.Product.COLUMN_NAME_ID));
                            sProductID = Integer.toString(productID);
                            // Delete photo if exists.
                            String photo = cProduct.getString(cProduct.getColumnIndex(Contract.Product.COLUMN_NAME_PHOTO));
                            if (!photo.isEmpty()) {
                                getApplicationContext().deleteFile(photo + ".png");
                            }
                        }

                        int sizeID;
                        String sSizeID = null;
                        Cursor cSize = getContentResolver().query(Contract.Size.CONTENT_URI, new String[]{Contract.Size.COLUMN_NAME_ID}, Contract.Size.COLUMN_NAME_PRODUCT_ID + "=?", new String[]{sProductID}, null);
                        if (cSize != null && cSize.moveToFirst()) {
                            sizeID = cSize.getInt(cSize.getColumnIndex(Contract.Size.COLUMN_NAME_ID));
                            sSizeID = Integer.toString(sizeID);
                        }

                        int rowDeleted = getContentResolver().delete(Contract.Product.CONTENT_URI, Contract.Product.COLUMN_NAME_NAME + "=?", new String[]{item});
                        getContentResolver().delete(Contract.Size.CONTENT_URI, Contract.Size.COLUMN_NAME_PRODUCT_ID + "=?", new String[]{sProductID});
                        getContentResolver().delete(Contract.PricedByAtt.CONTENT_URI, Contract.PricedByAtt.COLUMN_NAME_SIZE_ID + "=?", new String[]{sSizeID});

                        if (rowDeleted > 0) {
                            DeleteItemActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showToast(DeleteItemActivity.this, "Deleted");
                                }
                            });
                        } else {
                            DeleteItemActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showToast(DeleteItemActivity.this, "Delete operation was not successful.");
                                }
                            });
                        }
                    }
                }
            }).start();
        }
    };

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        Uri uri = null;
        String[] projection = null;
        String selection = null;
        String[] selectionArgs = null;
        switch (id) {
            case LOADER_PRODUCT:
                uri = Contract.Product.DISTINCT_CONTENT_URI;
                projection = new String[]{Contract.Product.COLUMN_NAME_CATEGORY, Contract.Product.COLUMN_NAME_SUBCATEGORY};
                break;
        }
        return new CursorLoader(this, uri, projection, selection, selectionArgs, null);
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor cursor) {
        switch (loader.getId()) {
            case LOADER_PRODUCT:
                if (cursor != null && cursor.moveToFirst()) {
                    String category, subcategory;
                    do {
                        category = cursor.getString(cursor.getColumnIndex(Contract.Product.COLUMN_NAME_CATEGORY));
                        subcategory = cursor.getString(cursor.getColumnIndex(Contract.Product.COLUMN_NAME_SUBCATEGORY));
                        addToMap(category, subcategory);
                    } while (cursor.moveToNext());
                    categoryList.addAll(categoryMap.keySet());
                    categoryAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
    }

    public class SpinnerAdapter extends ArrayAdapter<String> {
        private Context context;
        private int resource;
        private List<String> list;
        private LayoutInflater layoutInflater;

        public SpinnerAdapter(Context context, int resource, List<String> list) {
            super(context, resource, list);
            this.context = context;
            this.resource = resource;
            this.list = list;
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        public View getCustomView(int position, View convertView, ViewGroup parent) {
            View row = layoutInflater.inflate(resource, parent, false);

            TextView text = (TextView) row.findViewById(R.id.spinner_text);
            text.setText(list.get(position));

            return row;
        }
    }

    public class DialogAsyncTask extends AsyncTask<Void, Void, Cursor> {

        @Override
        protected Cursor doInBackground(Void... voids) {
            Uri uri = Contract.Product.CONTENT_URI;
            String[] projection = new String[]{Contract.Product.COLUMN_NAME_NAME};
            String selection = Contract.Product.COLUMN_NAME_CATEGORY + "=? AND " + Contract.Product.COLUMN_NAME_SUBCATEGORY + "=?";
            String[] selectionArgs = new String[]{selectedCategory, selectedSubCategory};
            return getContentResolver().query(uri, projection, selection, selectionArgs, null);
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            if (cursor != null && cursor.moveToFirst()) {
                int i = 0;
                String productName;
                items = new String[cursor.getCount()];
                do {
                    productName = cursor.getString(cursor.getColumnIndex(Contract.Product.COLUMN_NAME_NAME));
                    items[i] = productName;
                    i++;
                } while (cursor.moveToNext());
                createAlertDialog(DIALOG_DELETE, items);
            } else {
                showToast(DeleteItemActivity.this, "No item found!");
            }
        }
    }

    // Utility methods
    public void addToMap(String key, String value) {
        if (!categoryMap.containsKey(key)) {
            categoryMap.put(key, new ArrayList<String>());
        }
        categoryMap.get(key).add(value);
    }

    private void showToast(Context context, CharSequence text) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        toast.show();
    }
}
