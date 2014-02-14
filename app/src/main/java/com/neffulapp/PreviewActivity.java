package com.neffulapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.neffulapp.adapter.PreviewListAdapter;
import com.neffulapp.model.Contract;
import com.neffulapp.model.PreviewItemObject;

import net.sf.andpdf.pdfviewer.PdfViewerActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PreviewActivity extends ListActivity implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    // Constants
    private static final int LOADER_TEMP = 10;
    private static final int LOADER_REMARK = 20;
    private static final int LOADER_PRODUCT = 30;
    private static final int LOADER_ORDER_TOTAL = 40;
    private static final int DIALOG_SAVE = 1;
    private static final int DIALOG_SAVE_AS = 2;
    private static final int DIALOG_REMARK = 3;
    private static final int DIALOG_PRINT_DETAIL = 4;
    private static final int EDT_LIMIT_NAME = 20;
    private static final int EDT_LIMIT_REMARK = 35;
    private static final String ITEM_NAME = "Item Name";
    private static final String ITEM_REMARK = "Item Remark";
    private static final String ITEM_POSITION = "Item Position";
    // Member variables
    private PreviewListAdapter adapter;
    private List<PreviewItemObject> adapterList = new ArrayList<PreviewItemObject>();
    private HashMap<String, PreviewItemObject> unsortedMap = new HashMap<String, PreviewItemObject>();
    private SharedPreferences pref;
    private Toast toast;
    private Activity myActivity = this;
    private ProgressDialog progressDialog;
    private TextView orderTotal;
    private TextView itemListName;
    private List<String> subcategoryList = new ArrayList<String>();
    private DecimalFormat formatter = new DecimalFormat("#,###");
    private int position = 0;
    private int y = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        itemListName = (TextView) findViewById(R.id.item_list_name);
        orderTotal = (TextView) findViewById(R.id.preview_order_total);
        orderTotal.setOnClickListener(this);
        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        adapter = new PreviewListAdapter(this, R.layout.preview_item_row, adapterList);
        setListAdapter(adapter);
        retrieveSubcategoriesFromPref();
        getLoaderManager().initLoader(LOADER_TEMP, null, this);
        getLoaderManager().initLoader(LOADER_ORDER_TOTAL, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setItemListName();
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        if (state != null) {
            position = state.getInt("Position");
            y = state.getInt("Y");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.preview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.save:
                String profileName = pref.getString(getString(R.string.list_name), getString(R.string.default_list_name));
                if (profileName.equals(getString(R.string.default_list_name))) {
                    createDialog(DIALOG_SAVE_AS, null);
                } else {
                    createDialog(DIALOG_SAVE, null);
                }
                return true;
            case R.id.generatePdf:
                createDialog(DIALOG_PRINT_DETAIL, null);
                return true;
            case R.id.load:
                startSavedProfileActivity();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                unsortedMap.clear();
                adapterList.clear();
            }
        }
    }

    @Override
    protected void onListItemClick(ListView parent, View v, int position, long id) {
        super.onListItemClick(parent, v, position, id);
        String itemName = adapterList.get(position).getName();
        Bundle bundle = new Bundle();
        bundle.putString(ITEM_NAME, itemName);
        bundle.putInt(ITEM_POSITION, position);
        if (adapterList.get(position).getRemark() != null) {
            bundle.putString(ITEM_REMARK, adapterList.get(position).getRemark());
        }
        createDialog(DIALOG_REMARK, bundle);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.preview_order_total) {
            orderTotal.setFocusable(!orderTotal.isFocusable());
            orderTotal.setSelected(orderTotal.isFocusable());
        }
    }

    private void setItemListName() {
        String itemListName = pref.getString(getString(R.string.list_name), getString(R.string.default_list_name));
        if (itemListName.equals(getString(R.string.default_list_name))) {
            this.itemListName.setText("");
        } else {
            this.itemListName.setText("~" + itemListName);
        }
    }

    private void createDialog(int flag, final Bundle bundle) {
        String title;
        final AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        switch (flag) {
            case DIALOG_SAVE:
                title = getString(R.string.dialog_list_exist_title);
                final TextView confirmationTxt = new TextView(this);
                confirmationTxt.setText(getString(R.string.dialog_list_exist_body));
                confirmationTxt.setPadding(20, 20, 20, 20);

                builder.setTitle(title)
                        .setView(confirmationTxt)
                        .setPositiveButton(R.string.save_as, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                createDialog(DIALOG_SAVE_AS, null);
                            }
                        })
                        .setNeutralButton(R.string.save, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Delete all rows that are associated with that profile ID from Table Cart.
                                getContentResolver().delete(Contract.Cart.CONTENT_URI, Contract.Cart.COLUMN_NAME_PROFILE_ID + "=?", new String[]{Integer.toString(getListID())});
                                // Method to copy all rows from Table Temp to Table Cart.
                                persistItemList();
                                // Notify user that profile has been saved.
                                showToast(myActivity, "Saved");
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null);

                dialog = builder.create();
                dialog.show();
                break;
            case DIALOG_SAVE_AS:
                title = "Enter Name";
                final EditText profileNameEdt = new EditText(this);
                profileNameEdt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(EDT_LIMIT_NAME)});
                profileNameEdt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

                builder.setTitle(title)
                        .setView(profileNameEdt)
                        .setPositiveButton(R.string.save, null)
                        .setNegativeButton(android.R.string.cancel, null);

                dialog = builder.create();
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {

                    @Override
                    public void onShow(final DialogInterface dialogInterface) {
                        Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        b.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                String sProfileName = profileNameEdt.getText().toString();
                                if (sProfileName.matches("")) {
                                    showToast(myActivity, "Please enter a name.");
                                } else {
                                    // Check if the profile already exists in the database.
                                    if (isProfileExist(sProfileName)) {
                                        showToast(myActivity, "List name exists. Please enter another name.");
                                    } else {
                                        // If the profile does not exist, then insert the new profile to database.
                                        int newProfileID = insertNewProfile(sProfileName);
                                        // Update the preference's profile name and profile ID.
                                        pref.edit().putString(getString(R.string.list_name), sProfileName).apply();
                                        pref.edit().putInt(getString(R.string.list_id), newProfileID).apply();
                                        setItemListName();
                                        // Method to copy all rows from Table Temp to Table Cart.
                                        persistItemList();
                                        // Persist temporary remark
                                        persistRemark();
                                        // Dismiss the dialog.
                                        dialog.dismiss();
                                        // Notify user that profile has been saved.
                                        showToast(myActivity, "Saved");
                                    }
                                }
                            }
                        });
                    }
                });
                profileNameEdt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                        }
                    }
                });
                setHideSoftKeyboardListener(dialog);
                dialog.show();
                break;
            case DIALOG_PRINT_DETAIL:
                title = "Enter Details";
                final EditText nameEdt = new EditText(this);
                nameEdt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(EDT_LIMIT_NAME)});
                nameEdt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                nameEdt.setHint("Enter Name");

                builder.setTitle(title)
                        .setView(nameEdt)
                        .setPositiveButton(android.R.string.ok, null)
                        .setNegativeButton(android.R.string.cancel, null);

                dialog = builder.create();
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {

                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        b.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                final String name = nameEdt.getText().toString();
                                progressDialog = ProgressDialog.show(myActivity, "Please wait ...", "Generating Document ...", true);
                                final Handler handler = new Handler();
                                new Thread(new Runnable() {

                                    @Override
                                    public void run() {
                                        // Generate Pdf file
                                        Bundle bundle = new Bundle();
                                        bundle.putParcelableArrayList("PreviewItemObject", (ArrayList<? extends Parcelable>) adapterList);
                                        Pdf pdf = new Pdf(getApplicationContext(), name, bundle);
                                        final File file = pdf.getFile();
                                        handler.post(new Runnable() {

                                            @Override
                                            public void run() {
                                                progressDialog.dismiss();
                                                // Start PdfActivity
                                                Intent pdfViewerIntent = new Intent(PreviewActivity.this, PdfActivity.class);
                                                pdfViewerIntent.putExtra(PdfViewerActivity.EXTRA_PDFFILENAME, file.getAbsolutePath());
                                                pdfViewerIntent.putExtra(PdfActivity.EXTRA_PDFURI, Uri.fromFile(file).toString());
                                                startActivity(pdfViewerIntent);
                                            }
                                        });
                                    }
                                }).start();
                                dialog.dismiss();
                            }
                        });
                    }
                });
                nameEdt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                        }
                    }
                });
                setHideSoftKeyboardListener(dialog);
                dialog.show();
                break;

            case DIALOG_REMARK:
                title = "Remark";
                final EditText remarkEdt = new EditText(this);
                remarkEdt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(EDT_LIMIT_REMARK)});

                if (bundle.getString(ITEM_REMARK) != null) {
                    remarkEdt.setText(bundle.getString(ITEM_REMARK));
                    remarkEdt.selectAll();
                }

                builder.setTitle(title)
                        .setView(remarkEdt)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Update adapter
                                int position = bundle.getInt(ITEM_POSITION);
                                final String remark = remarkEdt.getText().toString();
                                adapterList.get(position).setRemark(remark);
                                adapter.notifyDataSetChanged();
                                // Update database in new thread
                                new Thread(new Runnable() {

                                    @Override
                                    public void run() {
                                        String defaultListID = getString(R.string.default_list_id);
                                        String itemName = bundle.getString(ITEM_NAME);
                                        // Prepare values for insertion or update
                                        ContentValues values = new ContentValues();
                                        values.put(Contract.Remark.COLUMN_NAME_PROFILE_ID, defaultListID);
                                        values.put(Contract.Remark.COLUMN_NAME_ITEM_NAME, itemName);
                                        values.put(Contract.Remark.COLUMN_NAME_REMARK, remark);
                                        // Check if remark already exist for the selected item
                                        Cursor cRemark = getContentResolver().query(Contract.Remark.CONTENT_URI, null, Contract.Remark.COLUMN_NAME_PROFILE_ID + "=? AND " + Contract.Remark.COLUMN_NAME_ITEM_NAME + "=?",
                                                new String[]{defaultListID, itemName}, null);
                                        if (cRemark != null && cRemark.moveToFirst()) {
                                            // If remark already exist, update or delete remark if user input is empty
                                            if (!remark.isEmpty()) {
                                                getContentResolver().update(Contract.Remark.CONTENT_URI, values, Contract.Remark.COLUMN_NAME_PROFILE_ID + "=? AND " + Contract.Remark.COLUMN_NAME_ITEM_NAME + "=?",
                                                        new String[]{defaultListID, itemName});
                                            } else {
                                                getContentResolver().delete(Contract.Remark.CONTENT_URI, Contract.Remark.COLUMN_NAME_PROFILE_ID + "=? AND " + Contract.Remark.COLUMN_NAME_ITEM_NAME + "=?",
                                                        new String[]{defaultListID, itemName});
                                            }
                                        } else {
                                            // If remark not already exist, insert remark
                                            getContentResolver().insert(Contract.Remark.CONTENT_URI, values);
                                        }
                                    }
                                }).start();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null);

                dialog = builder.create();
                remarkEdt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                        }
                    }
                });
                setHideSoftKeyboardListener(dialog);
                dialog.show();
                break;

            default:
                break;
        }
    }

    private void setHideSoftKeyboardListener(AlertDialog dialog) {
        dialog.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            }
        });
    }

    private void startSavedProfileActivity() {
        Intent intent = new Intent(getApplicationContext(), SavedProfileActivity.class);
        startActivityForResult(intent, 1);
    }

    private boolean isProfileExist(String name) {
        Cursor cProfile = getContentResolver().query(Contract.Profile.CONTENT_URI, null, Contract.Profile.COLUMN_NAME_NAME + "=?",
                new String[]{name}, null);
        cProfile.close();
        return (cProfile.getCount() > 0);
    }

    private int insertNewProfile(String name) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        String dateTime = dateFormat.format(date);
        ContentValues values = new ContentValues();
        values.put(Contract.Profile.COLUMN_NAME_NAME, name);
        values.put(Contract.Profile.COLUMN_NAME_CREATED_AT, dateTime);
        Uri result = getContentResolver().insert(Contract.Profile.CONTENT_URI, values);
        String idSegment = result.getPathSegments().get(Contract.Profile.PATH_PROFILE_PATH_POSITION);
        return Integer.parseInt(idSegment);
    }

    private void persistItemList() {
        // Run query to return all rows from Table Temp
        Cursor cTemp = getContentResolver().query(Contract.Temp.CONTENT_URI, null, null, null, null);
        // Copy every column of every row to ContentValues.
        if (cTemp != null && cTemp.moveToFirst()) {
            String name, subcategory, size, color;
            int quantity, price, labor;
            int i = 0;
            ContentValues[] values = new ContentValues[cTemp.getCount()];
            do {
                name = getStr(cTemp, Contract.Temp.COLUMN_NAME_NAME);
                subcategory = getStr(cTemp, Contract.Temp.COLUMN_NAME_SUBCATEGORY);
                quantity = getInt(cTemp, Contract.Temp.COLUMN_NAME_QUANTITY);
                size = getStr(cTemp, Contract.Temp.COLUMN_NAME_SIZE);
                color = getStr(cTemp, Contract.Temp.COLUMN_NAME_COLOR);
                price = getInt(cTemp, Contract.Temp.COLUMN_NAME_PRICE);
                labor = getInt(cTemp, Contract.Temp.COLUMN_NAME_LABOR);
                values[i] = new ContentValues();
                values[i].put(Contract.Cart.COLUMN_NAME_PROFILE_ID, getListID());
                values[i].put(Contract.Cart.COLUMN_NAME_NAME, name);
                values[i].put(Contract.Cart.COLUMN_NAME_SUBCATEGORY, subcategory);
                values[i].put(Contract.Cart.COLUMN_NAME_QUANTITY, quantity);
                values[i].put(Contract.Cart.COLUMN_NAME_SIZE, size);
                values[i].put(Contract.Cart.COLUMN_NAME_COLOR, color);
                values[i].put(Contract.Cart.COLUMN_NAME_PRICE, price);
                if (labor > 0) {
                    values[i].put(Contract.Cart.COLUMN_NAME_LABOR, labor);
                }
                i++;
            } while (cTemp.moveToNext());
            cTemp.close();
            getContentResolver().bulkInsert(Contract.Cart.CONTENT_URI, values);
        }
    }

    private void persistRemark() {
        String defaultListID = getString(R.string.default_list_id);
        String currentListID = Integer.toString(getListID());
        Cursor cRemark = getContentResolver().query(Contract.Remark.CONTENT_URI, new String[]{Contract.Remark.COLUMN_NAME_ITEM_NAME, Contract.Remark.COLUMN_NAME_REMARK},
                Contract.Remark.COLUMN_NAME_PROFILE_ID + "=?", new String[]{defaultListID}, null);
        int i = 0;
        ContentValues[] values = new ContentValues[cRemark.getCount()];
        if (cRemark != null && cRemark.moveToFirst()) {
            String itemName, remark;
            do {
                itemName = getStr(cRemark, Contract.Remark.COLUMN_NAME_ITEM_NAME);
                remark = getStr(cRemark, Contract.Remark.COLUMN_NAME_REMARK);
                values[i] = new ContentValues();
                values[i].put(Contract.Remark.COLUMN_NAME_PROFILE_ID, currentListID);
                values[i].put(Contract.Remark.COLUMN_NAME_ITEM_NAME, itemName);
                values[i].put(Contract.Remark.COLUMN_NAME_REMARK, remark);
                i++;
            } while (cRemark.moveToNext());
            cRemark.close();
            getContentResolver().bulkInsert(Contract.Remark.CONTENT_URI, values);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = null;
        String[] projection = null;
        String selection = null;
        String[] selectionArgs = null;
        switch (id) {
            case LOADER_TEMP:
                uri = Contract.Temp.CONTENT_URI;
                break;
            case LOADER_REMARK:
                String defaultListID = getString(R.string.default_list_id);
                uri = Contract.Remark.CONTENT_URI;
                projection = new String[]{Contract.Remark.COLUMN_NAME_ITEM_NAME, Contract.Remark.COLUMN_NAME_REMARK};
                selection = Contract.Remark.COLUMN_NAME_PROFILE_ID + "=?";
                selectionArgs = new String[]{defaultListID};
                break;
            case LOADER_PRODUCT:
                String questionMarks = null;
                for (int i = 0; i < args.getStringArray(ITEM_NAME).length; i++) {
                    if (i == 0) {
                        questionMarks = "?";
                    } else {
                        questionMarks = questionMarks + ",?";
                    }
                }
                uri = Contract.Product.CONTENT_URI;
                projection = new String[]{Contract.Product.COLUMN_NAME_NAME, Contract.Product.COLUMN_NAME_SUBCATEGORY,
                        Contract.Product.COLUMN_NAME_CODE, Contract.Product.COLUMN_NAME_PHOTO};
                selection = Contract.Product.COLUMN_NAME_NAME + " IN (" + questionMarks + ")";
                selectionArgs = args.getStringArray(ITEM_NAME);
                break;
            case LOADER_ORDER_TOTAL:
                uri = Contract.Temp.CONTENT_URI;
                projection = new String[]{Contract.Temp.COLUMN_NAME_PRICE, Contract.Temp.COLUMN_NAME_LABOR};
                break;
            default:
                break;
        }
        return new CursorLoader(this, uri, projection, selection, selectionArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case LOADER_TEMP:
                String name, size, color, fullString;
                String extra;
                int quantity, price, labor = 0;
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        name = getStr(cursor, Contract.Temp.COLUMN_NAME_NAME);
                        quantity = cursor.getInt(cursor.getColumnIndex(Contract.Temp.COLUMN_NAME_QUANTITY));
                        size = cursor.getString(cursor.getColumnIndex(Contract.Temp.COLUMN_NAME_SIZE));
                        color = cursor.getString(cursor.getColumnIndex(Contract.Temp.COLUMN_NAME_COLOR));
                        price = cursor.getInt(cursor.getColumnIndex(Contract.Temp.COLUMN_NAME_PRICE));
                        if (size.equals("N/A")) {
                            size = "-";
                        } else {
                            size = "-" + size + "-";
                        }
                        if (color.equals("N/A")) {
                            color = "";
                        } else {
                            color = color.replace(" ", ".");
                            color = color + "-";
                        }
                        if (!cursor.isNull(cursor.getColumnIndex(Contract.Temp.COLUMN_NAME_LABOR))) {
                            labor = cursor.getInt(cursor.getColumnIndex(Contract.Temp.COLUMN_NAME_LABOR));
                            extra = "+" + Integer.toString(labor);
                        } else {
                            extra = "";
                        }
                        fullString = quantity + size + color + price + extra;
                        addToMap(name);
                        unsortedMap.get(name).setSubtotal(unsortedMap.get(name).getSubtotal() + price);
                        unsortedMap.get(name).setLabor(unsortedMap.get(name).getLabor() + labor);
                        unsortedMap.get(name).addToList(fullString);
                    } while (cursor.moveToNext());
                }
                String[] itemName = unsortedMap.keySet().toArray(new String[unsortedMap.keySet().size()]);
                Bundle bundle = new Bundle();
                bundle.putStringArray(ITEM_NAME, itemName);
                getLoaderManager().restartLoader(LOADER_PRODUCT, bundle, this);
                getLoaderManager().restartLoader(LOADER_REMARK, null, this);
                break;
            case LOADER_REMARK:
                if (cursor != null && cursor.moveToFirst() && !unsortedMap.isEmpty()) {
                    do {
                        String key = getStr(cursor, Contract.Remark.COLUMN_NAME_ITEM_NAME);
                        String remark = getStr(cursor, Contract.Remark.COLUMN_NAME_REMARK);
                        unsortedMap.get(key).setRemark(remark);
                    } while (cursor.moveToNext());
                }
                break;
            case LOADER_PRODUCT:
                String sItemName, subcategory, photo, code;
                Map<String, Integer> unsorted = new HashMap<String, Integer>();
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        sItemName = getStr(cursor, Contract.Product.COLUMN_NAME_NAME);
                        subcategory = getStr(cursor, Contract.Product.COLUMN_NAME_SUBCATEGORY);
                        code = getStr(cursor, Contract.Product.COLUMN_NAME_CODE);
                        photo = getStr(cursor, Contract.Product.COLUMN_NAME_PHOTO);
                        this.unsortedMap.get(sItemName).setPhoto(photo);
                        this.unsortedMap.get(sItemName).setCode(code);
                        this.unsortedMap.get(sItemName).setName(sItemName);
                        unsorted.put(sItemName, subcategoryList.indexOf(subcategory));
                    } while (cursor.moveToNext());
                    Map<String, Integer> sortedMap = sortByValue(unsorted);
                    for (String key : sortedMap.keySet()) {
                        PreviewItemObject value = this.unsortedMap.get(key);
                        adapterList.add(value);
                    }
                }
                adapter.notifyDataSetChanged();
                if (position != 0 && y != 0) {
                    getListView().post(new Runnable() {
                        @Override
                        public void run() {
                            getListView().setSelectionFromTop(position, y);
                        }
                    });
                }
                break;
            case LOADER_ORDER_TOTAL:
                long total = 0;
                long laborCost = 0;
                long rowPrice;
                long rowLabor;
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        rowPrice = cursor.getInt(cursor.getColumnIndex(Contract.Temp.COLUMN_NAME_PRICE));
                        rowLabor = cursor.getInt(cursor.getColumnIndex(Contract.Temp.COLUMN_NAME_LABOR));
                        total = total + rowPrice;
                        laborCost = laborCost + rowLabor;
                    } while (cursor.moveToNext());
                }
                long disc = (long) (total * 0.2);
                long grandTotal = (long) (total * 0.8);
                long totalPayable = grandTotal + laborCost;
                if (laborCost == 0) {
                    orderTotal
                            .setText(getString(R.string.order_total) + " " + formatter.format(total) + "    " + getString(R.string.member_disc) + " " + formatter
                                    .format(disc) + "    " + getString(R.string.grand_total) + " " + formatter.format(grandTotal));
                } else {
                    orderTotal
                            .setText(getString(R.string.order_total) + " " + formatter.format(total) + "    " + getString(R.string.member_disc) + " " + formatter
                                    .format(disc) + "    " + getString(R.string.grand_total) + " " + formatter.format(grandTotal) + " + " + formatter
                                    .format(laborCost) + " = " + formatter.format(totalPayable));
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int firstVisiblePosition = getListView().getFirstVisiblePosition();
        View view = getListView().getChildAt(0);
        int y = (view == null) ? 0 : view.getTop();
        outState.putInt("Position", firstVisiblePosition);
        outState.putInt("Y", y);
    }

    // Utility methods
    private void showToast(Context context, CharSequence text) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        toast.show();
    }

    private String getStr(Cursor cursor, String columnName) {
        return cursor.getString(cursor.getColumnIndex(columnName));
    }

    private int getInt(Cursor cursor, String columnName) {
        return cursor.getInt(cursor.getColumnIndex(columnName));
    }

    private void retrieveSubcategoriesFromPref() {
        try {
            JSONArray mSubcategoryArray = new JSONArray(pref.getString(getString(R.string.all_subcategories), ""));
            if (mSubcategoryArray != null) {
                for (int i = 0; i < mSubcategoryArray.length(); i++) {
                    subcategoryList.add(mSubcategoryArray.get(i).toString());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void addToMap(String key) {
        if (!unsortedMap.containsKey(key)) {
            unsortedMap.put(key, new PreviewItemObject());
        }
    }

    private static Map<String, Integer> sortByValue(Map<String, Integer> unsortedMap) {
        List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(unsortedMap.entrySet());
        // sort list based on comparator
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {

            public int compare(Map.Entry<String, Integer> object1, Map.Entry<String, Integer> object2) {
                return (object1.getValue()).compareTo(object2.getValue());
            }
        });
        // Put sorted list into map again, LinkedHashMap make sure order in which keys were inserted.
        Map<String, Integer> result = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private int getListID() {
        return pref.getInt(getString(R.string.list_id), 0);
    }
}
