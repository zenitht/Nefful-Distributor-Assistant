package com.neffulapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;

import com.neffulapp.adapter.BundleListAdapter;
import com.neffulapp.model.Contract;

import java.util.ArrayList;
import java.util.List;

public class ItemDialogFragment extends DialogFragment implements View.OnClickListener, OnItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {

    // Constants
    private static final int LOADER_TEMP = 111;
    private static final int SPINNER_NUM = R.id.spinner1;
    private static final int SPINNER_SIZE = R.id.spinner2;
    private static final int SPINNER_COLOR = R.id.spinner3;
    private static final int MAX_NUMBER_OF_BUNDLE = 9;
    private static final int MAX_QUANTITY_PER_BUNDLE = 10;
    private static final String NOT_APPLICABLE = "N/A";
    // Member variables
    private View rootView;
    private BundleListAdapter adapter;
    private Spinner spinnerSize;
    private Spinner spinnerColor;
    private ArrayAdapter<String> spinnerColorAdapter;
    private long lastClickTime = 0;
    private List<String> numberList = new ArrayList<String>();
    private List<String> sizeList = new ArrayList<String>();
    private List<String> colorList = new ArrayList<String>();
    private int selectedQty;
    private String selectedSize;
    private String selectedColor;
    private String productName;
    private String subcategory;


    public static ItemDialogFragment newInstance(String productName, String subcategory) {
        ItemDialogFragment f = new ItemDialogFragment();
        // supply name input as an argument.
        Bundle args = new Bundle();
        args.putString("Product Name", productName);
        args.putString("Subcategory", subcategory);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        productName = getArguments().getString("Product Name");
        subcategory = getArguments().getString("Subcategory");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater vInflater = getActivity().getLayoutInflater();
        rootView = vInflater.inflate(R.layout.item_dialog, null);
        ImageView add = (ImageView) rootView.findViewById(R.id.addButton);
        LinearLayout linearLayout = (LinearLayout) rootView.findViewById(R.id.listviewlayout);

        AlertDialog.Builder aBuilder = new AlertDialog.Builder(getActivity());
        aBuilder.setView(rootView);
        add.setOnClickListener(this);
        setSpinners();
        ListView bundleList = new ListView(getActivity());
        linearLayout.addView(bundleList);
        adapter = new BundleListAdapter(getActivity(), null, false);
        bundleList.setAdapter(adapter);
        getLoaderManager().initLoader(LOADER_TEMP, null, this);
        return aBuilder.create();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.addButton:
                if (SystemClock.elapsedRealtime() - lastClickTime < 300) {
                    return;
                }
                lastClickTime = SystemClock.elapsedRealtime();
                if (adapter.getCount() < MAX_NUMBER_OF_BUNDLE) {
                    int price = getBundlePrice();
                    int labor = getBundleLaborCost();
                    ContentValues value = new ContentValues();
                    value.put(Contract.Temp.COLUMN_NAME_NAME, productName);
                    value.put(Contract.Temp.COLUMN_NAME_SUBCATEGORY, subcategory);
                    value.put(Contract.Temp.COLUMN_NAME_QUANTITY, selectedQty);
                    value.put(Contract.Temp.COLUMN_NAME_SIZE, selectedSize);
                    value.put(Contract.Temp.COLUMN_NAME_COLOR, selectedColor);
                    value.put(Contract.Temp.COLUMN_NAME_PRICE, price);
                    if (labor > 0) {
                        value.put(Contract.Temp.COLUMN_NAME_LABOR, labor);
                    }
                    getActivity().getContentResolver().insert(Contract.Temp.CONTENT_URI, value);
                }
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        switch (parent.getId()) {
            case SPINNER_NUM:
                selectedQty = pos + 1;
                break;
            case SPINNER_SIZE:
                selectedSize = parent.getItemAtPosition(pos).toString();
                if (selectedSize.equals("FREE") || selectedSize.equals("N/A")) {
                    spinnerSize.setEnabled(false);
                }
                List<String> colors = getColorList();
                colorList.clear();
                colorList.addAll(colors);
                spinnerColorAdapter.notifyDataSetChanged();
                if (colors.get(0).equals(NOT_APPLICABLE) || colors.size() == 1) {
                    spinnerColor.setEnabled(false);
                } else {
                    spinnerColor.setEnabled(true);
                    spinnerColor.setAdapter(spinnerColorAdapter);
                }
                break;
            case SPINNER_COLOR:
                selectedColor = parent.getItemAtPosition(pos).toString();
                break;
            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }

    private void setSpinners() {
        Spinner spinnerNum = (Spinner) rootView.findViewById(SPINNER_NUM);
        spinnerSize = (Spinner) rootView.findViewById(SPINNER_SIZE);
        spinnerColor = (Spinner) rootView.findViewById(SPINNER_COLOR);

        // Sets quantity spinner
        numberList.addAll(getNumberList());
        spinnerNum.setOnItemSelectedListener(this);
        ArrayAdapter<String> spinnerNumAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, numberList);
        spinnerNumAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNum.setAdapter(spinnerNumAdapter);

        // Sets size spinner
        List<String> sizes = getSizeList();
        sizeList.addAll(sizes);
        spinnerSize.setOnItemSelectedListener(this);
        ArrayAdapter<String> spinnerSizeAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, sizeList);
        spinnerSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSize.setAdapter(spinnerSizeAdapter);

        // Sets color spinner
        spinnerColor.setOnItemSelectedListener(this);
        spinnerColorAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, colorList);
        spinnerColorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerColor.setAdapter(spinnerColorAdapter);
    }

    private List<String> getNumberList() {
        List<String> list = new ArrayList<String>();
        for (int i = 1; i <= MAX_QUANTITY_PER_BUNDLE; i++) {
            list.add(Integer.toString(i));
        }
        return list;
    }

    private List<String> getSizeList() {
        List<String> list;
        String productID = getProductID();
        List<String> sizeGroups = getSizeGroups(productID);
        list = getSizes(sizeGroups);
        return list;
    }

    private List<String> getColorList() {
        List<String> list;
        String sizeGroup = getSizeGroup(selectedSize);
        String sizeID = getSizeID(sizeGroup);
        list = getColors(sizeID);
        return list;
    }

    private String getProductID() {
        String productID = null;
        Cursor cProducts = getActivity().getContentResolver().query(Contract.Product.CONTENT_URI, new String[]{Contract.Product.COLUMN_NAME_ID},
                Contract.Product.COLUMN_NAME_NAME + " =?", new String[]{productName}, null);
        if (cProducts != null && cProducts.moveToFirst()) {
            productID = getStr(cProducts, Contract.Product.COLUMN_NAME_ID);
            cProducts.close();
        }
        return productID;
    }

    private String getSizeGroup(String size) {
        String sizeGroup = null;
        Cursor cSize = getActivity().getContentResolver().query(Contract.Reference.CONTENT_URI,
                new String[]{Contract.Reference.COLUMN_NAME_SIZE_GROUP}, Contract.Reference.COLUMN_NAME_SIZE + " =?", new String[]{size}, null);
        if (cSize != null && cSize.moveToFirst()) {
            sizeGroup = getStr(cSize, Contract.Reference.COLUMN_NAME_SIZE_GROUP);
            cSize.close();
        }
        return sizeGroup;
    }

    private List<String> getSizeGroups(String productID) {
        List<String> sizeGroups = new ArrayList<String>();
        Cursor cSize = getActivity().getContentResolver().query(Contract.Size.CONTENT_URI, new String[]{Contract.Size.COLUMN_NAME_SIZE_GROUP},
                Contract.Size.COLUMN_NAME_PRODUCT_ID + " =?", new String[]{productID}, null);
        if (cSize != null && cSize.moveToFirst()) {
            do {
                String sizeGroup = getStr(cSize, Contract.Size.COLUMN_NAME_SIZE_GROUP);
                sizeGroups.add(sizeGroup);
            } while (cSize.moveToNext());
            cSize.close();
        }
        return sizeGroups;
    }

    private List<String> getSizes(List<String> sizeGroups) {
        List<String> sizes = new ArrayList<String>();
        // Build a string from sizeGroups element.
        String whereIn = null;
        for (int i = 0; i < sizeGroups.size(); i++) {
            if (i == 0) {
                whereIn = "'" + sizeGroups.get(i) + "'";
            } else {
                whereIn = whereIn + ",'" + sizeGroups.get(i) + "'";
            }
        }
        Cursor cReference = getActivity().getContentResolver()
                .query(Contract.Reference.CONTENT_URI, new String[]{Contract.Reference.COLUMN_NAME_SIZE},
                        Contract.Reference.COLUMN_NAME_SIZE_GROUP + " IN (" + whereIn + ")", null, null);
        if (cReference != null && cReference.moveToFirst()) {
            do {
                String size = getStr(cReference, Contract.Reference.COLUMN_NAME_SIZE);
                sizes.add(size);
            } while (cReference.moveToNext());
            cReference.close();
        }
        return sizes;
    }

    private String getSizeID(String sizeGroup) {
        String sizeID = null;
        Cursor cSize = getActivity().getContentResolver().query(Contract.Size.CONTENT_URI, new String[]{Contract.Size.COLUMN_NAME_ID},
                Contract.Size.COLUMN_NAME_PRODUCT_ID + "=? AND " + Contract.Size.COLUMN_NAME_SIZE_GROUP + "=?",
                new String[]{getProductID(), sizeGroup}, null);
        if (cSize != null && cSize.moveToFirst()) {
            sizeID = getStr(cSize, Contract.Reference.COLUMN_NAME_ID);
            cSize.close();
        }
        return sizeID;
    }

    private List<String> getColors(String sizeID) {
        List<String> colors = new ArrayList<String>();
        Cursor cPricedByAtt = getActivity().getContentResolver().query(Contract.PricedByAtt.CONTENT_URI,
                new String[]{Contract.PricedByAtt.COLUMN_NAME_COLOR}, Contract.PricedByAtt.COLUMN_NAME_SIZE_ID + "=?", new String[]{sizeID},
                null);
        if (cPricedByAtt != null && cPricedByAtt.moveToFirst()) {
            do {
                String color = getStr(cPricedByAtt, Contract.PricedByAtt.COLUMN_NAME_COLOR);
                colors.add(color);
            } while (cPricedByAtt.moveToNext());
            cPricedByAtt.close();
        }
        return colors;
    }

    public int getBundlePrice() {
        int bundlePrice = 0;
        String sizeGroup = getSizeGroup(selectedSize);
        String sizeID = getSizeID(sizeGroup);
        Cursor cPricedByAtt = getActivity().getContentResolver().query(Contract.PricedByAtt.CONTENT_URI, new String[]{Contract.PricedByAtt.COLUMN_NAME_PRICE},
                Contract.PricedByAtt.COLUMN_NAME_SIZE_ID + " = ? AND " + Contract.PricedByAtt.COLUMN_NAME_COLOR + " =?", new String[]{sizeID, selectedColor}, null);
        if (cPricedByAtt != null && cPricedByAtt.moveToFirst()) {
            int price = cPricedByAtt.getInt(cPricedByAtt.getColumnIndex(Contract.PricedByAtt.COLUMN_NAME_PRICE));
            bundlePrice = price * selectedQty;
            cPricedByAtt.close();
        }
        return bundlePrice;
    }

    public int getBundleLaborCost() {
        int bundleLaborCost = 0;
        String sizeGroup = getSizeGroup(selectedSize);
        String sizeID = getSizeID(sizeGroup);
        String selection = Contract.PricedByAtt.COLUMN_NAME_SIZE_ID + " = ? AND " + Contract.PricedByAtt.COLUMN_NAME_COLOR + " =?";
        String[] selectionArgs = new String[]{sizeID, selectedColor};
        // Run query.
        Cursor cPricedByAtt = getActivity().getContentResolver().query(Contract.PricedByAtt.CONTENT_URI,
                new String[]{Contract.PricedByAtt.COLUMN_NAME_LABOR}, selection, selectionArgs, null);
        // Retrieve result.
        if (cPricedByAtt != null && cPricedByAtt.moveToFirst()) {
            int laborCost = 0;
            if (!cPricedByAtt.isNull(cPricedByAtt.getColumnIndex(Contract.Temp.COLUMN_NAME_LABOR))) {
                laborCost = cPricedByAtt.getInt(cPricedByAtt.getColumnIndex(Contract.PricedByAtt.COLUMN_NAME_LABOR));
            }
            bundleLaborCost = laborCost * selectedQty;
            cPricedByAtt.close();
        }
        return bundleLaborCost;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = Contract.Temp.CONTENT_URI;
        String[] projection = {Contract.Temp.COLUMN_NAME_ID, Contract.Temp.COLUMN_NAME_SUBCATEGORY, Contract.Temp.COLUMN_NAME_QUANTITY, Contract.Temp.COLUMN_NAME_SIZE,
                Contract.Temp.COLUMN_NAME_COLOR, Contract.Temp.COLUMN_NAME_PRICE, Contract.Temp.COLUMN_NAME_LABOR};
        String selection = Contract.Temp.COLUMN_NAME_NAME + "=? AND " + Contract.Temp.COLUMN_NAME_SUBCATEGORY + "=?";
        String[] selectionArgs = {productName, subcategory};
        return new CursorLoader(getActivity(), uri, projection, selection, selectionArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Fragment fragment = getTargetFragment();
        ((TabListFragment) fragment).getAdapter().isLoadImage = true;
    }

    // Utility methods
    private String getStr(Cursor cursor, String columnName) {
        if (cursor.getType(cursor.getColumnIndex(columnName)) == Cursor.FIELD_TYPE_INTEGER) {
            int i = cursor.getInt(cursor.getColumnIndex(columnName));
            return String.valueOf(i);
        } else {
            return cursor.getString(cursor.getColumnIndex(columnName));
        }
    }
}