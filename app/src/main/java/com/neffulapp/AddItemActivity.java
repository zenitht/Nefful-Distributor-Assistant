package com.neffulapp;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.neffulapp.helper.BitmapProcessor;
import com.neffulapp.model.Contract;
import com.neffulapp.model.TableRowObject;
import com.neffulapp.view.MultiSelectSpinner;
import com.neffulapp.view.RoundedImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AddItemActivity extends FragmentActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_CATEGORY = 1;
    private static final int LOADER_SUBCATEGORY = 2;
    private static final int LOADER_REFERENCE = 3;
    private static final int LOADER_PRICEDBYATT = 4;
    private static final int PICK_IMAGE = 100;
    private static final int CATEGORY_SPINNER = R.id.add_extra_category;
    private static final int SUBCATEGORY_SPINNER = R.id.add_extra_subcategory;
    private static final int PHOTO_IMAGEVIEW = R.id.photo_preview;
    private static final int PRICE_EDITTEXT = R.id.add_extra_price;
    private static final int LABOR_EDITTEXT = R.id.add_extra_labor;
    private static final int CHOOSE_PHOTO_BUTTON = R.id.add_extra_choose_photo;
    private static final int ADD_ROW_BUTTON = R.id.add_extra_add;
    private static final int TABLE_LAYOUT = R.id.add_extra_table;
    // Member variables
    private TextView productCodeEdt;
    private TextView productNameEdt;
    private Spinner categorySpn;
    private Spinner subcategorySpn;
    private MultiSelectSpinner sizesSpn;
    private MultiSelectSpinner colorsSpn;
    private EditText priceEdt;
    private EditText laborEdt;
    private RoundedImageView photoImg;
    private Button addRowBtn;
    private Button choosePhotoBtn;
    private ProgressBar progressBar;
    private TableLayout tableLyt;
    private HashMap<String, List<String>> categoryMap = new HashMap<String, List<String>>();
    private List<String> categoryList = new ArrayList<String>();
    private List<String> subcategoryList = new ArrayList<String>();
    private List<String> sizeList = new ArrayList<String>();
    private List<String> colorList = new ArrayList<String>();
    private ArrayAdapter<String> categoryAdapter = null;
    private ArrayAdapter<String> subcategoryAdapter = null;
    private String selectedCategory = null;
    private String selectedSubCategory = null;
    private Toast toast = null;
    private String filePath = null;
    private List<TableRowObject> rowList = new ArrayList<TableRowObject>();
    private BitmapProcessor bitmapProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        productCodeEdt = (EditText) findViewById(R.id.add_extra_code);
        productNameEdt = (EditText) findViewById(R.id.add_extra_name);
        categorySpn = (Spinner) findViewById(CATEGORY_SPINNER);
        subcategorySpn = (Spinner) findViewById(SUBCATEGORY_SPINNER);
        photoImg = (RoundedImageView) findViewById(PHOTO_IMAGEVIEW);
        sizesSpn = (MultiSelectSpinner) findViewById(R.id.add_extra_sizes);
        colorsSpn = (MultiSelectSpinner) findViewById(R.id.add_extra_colors);
        priceEdt = (EditText) findViewById(PRICE_EDITTEXT);
        laborEdt = (EditText) findViewById(LABOR_EDITTEXT);
        choosePhotoBtn = (Button) findViewById(CHOOSE_PHOTO_BUTTON);
        addRowBtn = (Button) findViewById(ADD_ROW_BUTTON);
        tableLyt = (TableLayout) findViewById(TABLE_LAYOUT);
        progressBar = (ProgressBar) findViewById(R.id.progressbar_add);

        categorySpn.setOnItemSelectedListener(this);
        subcategorySpn.setOnItemSelectedListener(this);
        choosePhotoBtn.setOnClickListener(this);
        addRowBtn.setOnClickListener(this);

        sizesSpn.setSingleChoiceItems(new String[]{getString(R.string.size_free), getString(R.string.not_applicable)});
        colorsSpn.setSingleChoiceItems(new String[]{getString(R.string.not_applicable)});

        categoryAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, categoryList);
        subcategoryAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, subcategoryList);

        categorySpn.setAdapter(categoryAdapter);
        subcategorySpn.setAdapter(subcategoryAdapter);

        bitmapProcessor = new BitmapProcessor(this);

        getSupportLoaderManager().initLoader(LOADER_CATEGORY, null, this);
        getSupportLoaderManager().initLoader(LOADER_SUBCATEGORY, null, this);
        getSupportLoaderManager().initLoader(LOADER_REFERENCE, null, this);
        getSupportLoaderManager().initLoader(LOADER_PRICEDBYATT, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_item, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.clear:
                clearAllFields();
                return true;
            case R.id.done:
                showAlertDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE) {
            if (resultCode == RESULT_OK) {
                Uri selectedImageUri = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImageUri, filePathColumn, null, null, null);

                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    filePath = cursor.getString(columnIndex);
                    cursor.close();
                    bitmapProcessor.initTask(filePath, photoImg, true);
                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case ADD_ROW_BUTTON:
                if (priceEdt.getText().toString().isEmpty()) {
                    showToast("Price field cannot be empty");
                } else {
                    int price = Integer.parseInt(priceEdt.getText().toString());
                    int labor = 0;
                    if (!laborEdt.getText().toString().isEmpty()) {
                        labor = Integer.parseInt(laborEdt.getText().toString());
                    }
                    TableRowObject row = new TableRowObject(sizesSpn.getSelectedStrings(), colorsSpn.getSelectedStrings(), price, labor);
                    rowList.add(row);
                    addRow();
                    resetPartialWidgets();
                    // Hide soft keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(priceEdt.getWindowToken(), 0);
                }
                break;
            case CHOOSE_PHOTO_BUTTON:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                break;
        }

    }

    private void resetPartialWidgets() {
        Arrays.fill(sizesSpn.getSelections(), false);
        Arrays.fill(colorsSpn.getSelections(), false);
        sizesSpn.getProxyAdapter().clear();
        colorsSpn.getProxyAdapter().clear();
        sizesSpn.getProxyAdapter().add(getString(R.string.select_size));
        colorsSpn.getProxyAdapter().add(getString(R.string.select_color));
        priceEdt.setText("");
        laborEdt.setText("");
        priceEdt.clearFocus();
        laborEdt.clearFocus();
    }

    private void clearAllFields() {
        productCodeEdt.setText("");
        productNameEdt.setText("");
        categorySpn.setSelection(0);
        subcategorySpn.setSelection(0);
        Arrays.fill(sizesSpn.getSelections(), false);
        Arrays.fill(colorsSpn.getSelections(), false);
        sizesSpn.getProxyAdapter().clear();
        colorsSpn.getProxyAdapter().clear();
        sizesSpn.getProxyAdapter().add(getString(R.string.select_size));
        colorsSpn.getProxyAdapter().add(getString(R.string.select_color));
        priceEdt.setText("");
        laborEdt.setText("");
        priceEdt.clearFocus();
        laborEdt.clearFocus();
        tableLyt.removeViews(1, tableLyt.getChildCount() - 1);
        photoImg.setImageResource(R.drawable.placeholder_image);
        rowList.clear();
    }

    private void addRow() {
        TableRow row = new TableRow(this);
        row.setWeightSum(1);

        String selectedSizes = sizesSpn.buildSelectedItemString();
        String selectedColors = colorsSpn.buildSelectedItemString();
        String price = priceEdt.getText().toString();
        String labor = laborEdt.getText().toString();

        final TextView sizeCell = createCell(selectedSizes);
        final TextView colorCell = createCell(selectedColors);
        final TextView priceCell = createCell(price);
        final TextView laborCell = createCell(labor);

        final List<Integer> lineCountList = new ArrayList<Integer>();
        sizeCell.post(new Runnable() {
            @Override
            public void run() {
                lineCountList.add(sizeCell.getLineCount());
            }
        });

        colorCell.post(new Runnable() {
            @Override
            public void run() {
                lineCountList.add(colorCell.getLineCount());
            }
        });

        priceCell.post(new Runnable() {
            @Override
            public void run() {
                lineCountList.add(priceCell.getLineCount());
            }
        });

        laborCell.post(new Runnable() {
            @Override
            public void run() {
                lineCountList.add(laborCell.getLineCount());
            }
        });

        sizeCell.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.3f));
        colorCell.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.45f));
        priceCell.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.15f));
        laborCell.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.1f));

        row.addView(sizeCell);
        row.addView(colorCell);
        row.addView(priceCell);
        row.addView(laborCell);

        row.post(new Runnable() {
            @Override
            public void run() {
                int lines = Collections.max(lineCountList);
                sizeCell.setLines(lines);
                colorCell.setLines(lines);
                priceCell.setLines(lines);
                laborCell.setLines(lines);
            }
        });

        tableLyt.addView(row);
    }

    private TextView createCell(String text) {
        TextView cell = new TextView(this);
        cell.setBackgroundResource(R.drawable.full_cell_border);
        cell.setText(text);
        cell.setPadding(5, 5, 5, 5);
        return cell;
    }

    private void showAlertDialog() {
        boolean isProductExists = false;
        String name = productNameEdt.getText().toString().trim();

        Cursor cProduct = getContentResolver().query(Contract.Product.CONTENT_URI, null, Contract.Product.COLUMN_NAME_NAME + "=?", new String[]{name}, null);
        if (cProduct.moveToFirst()) {
            isProductExists = true;
        }

        if (!productCodeEdt.getText().toString().isEmpty() && !productNameEdt.getText().toString().isEmpty() && tableLyt.getChildCount() > 1 && !isProductExists) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to insert this item ?")
                    .setPositiveButton("Yes", confirmListener)
                    .setNegativeButton("No", null)
                    .show();
        } else {
            if (isProductExists) {
                showToast("Item already exists.");
            } else {
                showToast("Please fill in all item details.");
            }
        }
    }

    private DialogInterface.OnClickListener confirmListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int which) {
            String code = productCodeEdt.getText().toString().trim();
            String name = productNameEdt.getText().toString().trim();
            String category = selectedCategory;
            String subcategory = selectedSubCategory;
            String photo;
            if (filePath != null) {
                photo = name.replaceAll(" ", "_").toLowerCase();
            } else {
                photo = "";
            }

            // Prepare column values for Table Product
            ContentValues productValues = new ContentValues();
            productValues.put(Contract.Product.COLUMN_NAME_CODE, code);
            productValues.put(Contract.Product.COLUMN_NAME_NAME, name);
            productValues.put(Contract.Product.COLUMN_NAME_CATEGORY, category);
            productValues.put(Contract.Product.COLUMN_NAME_SUBCATEGORY, subcategory);
            productValues.put(Contract.Product.COLUMN_NAME_PHOTO, photo);

            // Insert new row to Table Product and get row ID
            Uri productUri = getContentResolver().insert(Contract.Product.CONTENT_URI, productValues);
            String productID = productUri.getLastPathSegment();
            int iProductID = Integer.parseInt(productID);

            // Join size list from all rows without duplicate values
            Set<String> sizeSet = new LinkedHashSet<String>();
            for (TableRowObject rowObject : rowList) {
                sizeSet.addAll(rowObject.getSizes());
            }

            // if no size has been entered by user, assign N/A to sizeSet
            String notApplicable = getString(R.string.not_applicable);
            if (sizeSet.isEmpty()) {
                sizeSet.add(notApplicable);
            }

            // Map size to size group
            HashMap<String, String> sizeMap = new LinkedHashMap<String, String>();
            for (String size : sizeSet) {
                Cursor cRef = getContentResolver().query(Contract.Reference.CONTENT_URI, new String[]{Contract.Reference.COLUMN_NAME_SIZE_GROUP},
                        Contract.Reference.COLUMN_NAME_SIZE + "=?", new String[]{size}, null);

                if (cRef != null && cRef.moveToFirst()) {
                    String sizeGroup = cRef.getString(cRef.getColumnIndex(Contract.Reference.COLUMN_NAME_SIZE_GROUP));
                    sizeMap.put(sizeGroup, size);
                }
            }

            // Iterate size and insert values to Table Size and Table PricedByAtt
            ContentValues sizeValues;
            for (Map.Entry<String, String> size : sizeMap.entrySet()) {
                // Prepare values to be inserted to Table Size
                sizeValues = new ContentValues();
                sizeValues.put(Contract.Size.COLUMN_NAME_PRODUCT_ID, iProductID);
                sizeValues.put(Contract.Size.COLUMN_NAME_SIZE_GROUP, size.getKey());

                // Insert new row to Table Size and get the row ID
                Uri sizeUri = getContentResolver().insert(Contract.Size.CONTENT_URI, sizeValues);
                String sizeID = sizeUri.getLastPathSegment();
                int iSizeID = Integer.parseInt(sizeID);

                // Iterate user entered data to determine colors associated with current size and enter to Table PricedByAtt
                ContentValues pricedbyattValues;
                List<ContentValues> pricedbyattValuesList = new ArrayList<ContentValues>();

                for (TableRowObject rowObject : rowList) {
                    if (size.getValue().equals(notApplicable) || rowObject.getSizes().contains(size.getValue())) {
                        if (!rowObject.getColors().isEmpty()) {
                            for (String color : rowObject.getColors()) {
                                pricedbyattValues = new ContentValues();
                                pricedbyattValues.put(Contract.PricedByAtt.COLUMN_NAME_SIZE_ID, iSizeID);
                                pricedbyattValues.put(Contract.PricedByAtt.COLUMN_NAME_COLOR, color);
                                pricedbyattValues.put(Contract.PricedByAtt.COLUMN_NAME_PRICE, rowObject.getPrice());
                                if (rowObject.getLabor() > 0) {
                                    pricedbyattValues.put(Contract.PricedByAtt.COLUMN_NAME_LABOR, rowObject.getLabor());
                                }
                                pricedbyattValuesList.add(pricedbyattValues);
                            }
                        } else {
                            pricedbyattValues = new ContentValues();
                            pricedbyattValues.put(Contract.PricedByAtt.COLUMN_NAME_SIZE_ID, iSizeID);
                            pricedbyattValues.put(Contract.PricedByAtt.COLUMN_NAME_COLOR, getString(R.string.not_applicable));
                            pricedbyattValues.put(Contract.PricedByAtt.COLUMN_NAME_PRICE, rowObject.getPrice());
                            if (rowObject.getLabor() > 0) {
                                pricedbyattValues.put(Contract.PricedByAtt.COLUMN_NAME_LABOR, rowObject.getLabor());
                            }
                            pricedbyattValuesList.add(pricedbyattValues);
                        }
                    }
                }
                ContentValues[] pricedbyattValuesArray = pricedbyattValuesList.toArray(new ContentValues[pricedbyattValuesList.size()]);
                getContentResolver().bulkInsert(Contract.PricedByAtt.CONTENT_URI, pricedbyattValuesArray);
            }

            if (filePath != null) {
                bitmapProcessor.initTask(filePath, photo, false);
                progressBar.setVisibility(View.VISIBLE);
            }

            clearAllFields();
        }
    };

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case CATEGORY_SPINNER:
                selectedCategory = parent.getItemAtPosition(position).toString();
                if (!subcategoryList.isEmpty()) {
                    List<String> list = categoryMap.get(selectedCategory);
                    subcategoryList.clear();
                    subcategoryList.addAll(list);
                    subcategoryAdapter.notifyDataSetChanged();
                    subcategorySpn.setAdapter(subcategoryAdapter);
                }
                break;
            case SUBCATEGORY_SPINNER:
                selectedSubCategory = parent.getItemAtPosition(position).toString();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        Uri uri = null;
        String[] projection = null;
        String selection = null;
        String[] selectionArgs = null;
        switch (id) {
            case LOADER_CATEGORY:
                uri = Contract.Product.DISTINCT_CONTENT_URI;
                projection = new String[]{Contract.Product.COLUMN_NAME_CATEGORY};
                break;
            case LOADER_SUBCATEGORY:
                uri = Contract.Product.DISTINCT_CONTENT_URI;
                projection = new String[]{Contract.Product.COLUMN_NAME_CATEGORY, Contract.Product.COLUMN_NAME_SUBCATEGORY};
                break;
            case LOADER_REFERENCE:
                uri = Contract.Reference.CONTENT_URI;
                projection = new String[]{Contract.Reference.COLUMN_NAME_SIZE};
                break;
            case LOADER_PRICEDBYATT:
                uri = Contract.PricedByAtt.DISTINCT_CONTENT_URI;
                projection = new String[]{Contract.PricedByAtt.COLUMN_NAME_COLOR};
                break;
            default:
                break;
        }
        return new CursorLoader(this, uri, projection, selection, selectionArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case LOADER_CATEGORY:
                if (cursor != null && cursor.moveToFirst()) {
                    String category;
                    do {
                        category = cursor.getString(cursor.getColumnIndex(Contract.Product.COLUMN_NAME_CATEGORY));
                        categoryList.add(category);
                    } while (cursor.moveToNext());
                    categoryAdapter.notifyDataSetChanged();
                }
                break;
            case LOADER_SUBCATEGORY:
                if (cursor != null && cursor.moveToFirst()) {
                    String category, subcategory;
                    String firstCategory = cursor.getString(cursor.getColumnIndex(Contract.Product.COLUMN_NAME_CATEGORY));
                    do {
                        category = cursor.getString(cursor.getColumnIndex(Contract.Product.COLUMN_NAME_CATEGORY));
                        subcategory = cursor.getString(cursor.getColumnIndex(Contract.Product.COLUMN_NAME_SUBCATEGORY));
                        addToMap(category, subcategory);
                    } while (cursor.moveToNext());

                    List<String> list = categoryMap.get(firstCategory);
                    subcategoryList.addAll(list);
                    subcategoryAdapter.notifyDataSetChanged();
                }
                break;
            case LOADER_REFERENCE:
                if (cursor != null && cursor.moveToFirst()) {
                    String size;
                    do {
                        size = cursor.getString(cursor.getColumnIndex(Contract.Reference.COLUMN_NAME_SIZE));
                        sizeList.add(size);
                    } while (cursor.moveToNext());
                    sizesSpn.setItems(sizeList);
                }
                break;
            case LOADER_PRICEDBYATT:
                if (cursor != null && cursor.moveToFirst()) {
                    String color;
                    do {
                        color = cursor.getString(cursor.getColumnIndex(Contract.PricedByAtt.COLUMN_NAME_COLOR));
                        colorList.add(color);
                    } while (cursor.moveToNext());
                    colorsSpn.setItems(colorList);
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    // Utility methods
    private void showToast(CharSequence text) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        toast.show();
    }

    public void addToMap(String key, String value) {
        if (!categoryMap.containsKey(key)) {
            categoryMap.put(key, new ArrayList<String>());
        }
        categoryMap.get(key).add(value);
    }

    public void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }
}
