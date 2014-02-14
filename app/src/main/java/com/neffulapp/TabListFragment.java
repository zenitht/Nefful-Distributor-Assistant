package com.neffulapp;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;

import com.neffulapp.adapter.ItemListAdapter;
import com.neffulapp.model.CatalogueItemObject;
import com.neffulapp.model.Contract;

import java.util.ArrayList;
import java.util.List;

public class TabListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, ItemListAdapter.ItemListAdapterEventListener {

    // Constants
    private static final int LOADER_PRODUCT = 1;
    private static final int LOADER_TEMP = 2;
    // Member variables
    private ItemListAdapter adapter;
    private long lastClickTime = 0;
    private List<CatalogueItemObject> itemList = new ArrayList<CatalogueItemObject>();
    private String pageTitle;
    private int position = 0;
    private int y = 0;

    public static TabListFragment newInstance(String title) {
        TabListFragment f = new TabListFragment();
        Bundle args = new Bundle();
        args.putString("PageTitle", title);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            position = savedInstanceState.getInt("Position");
            y = savedInstanceState.getInt("Y");
        }
        pageTitle = getArguments().getString("PageTitle");
        adapter = new ItemListAdapter(getActivity(), R.layout.item_row, itemList, this);
        setListAdapter(adapter);
        getLoaderManager().restartLoader(LOADER_PRODUCT, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (position != 0 && y != 0) {
            getListView().postDelayed(new Runnable() {
                @Override
                public void run() {
                    getListView().setSelectionFromTop(position, y);
                }
            }, 300);
        }
    }

    public ItemListAdapter getAdapter() {


        return adapter;
    }

    @Override
    public void onListItemClick(ListView parent, View view, int position, long id) {
        super.onListItemClick(parent, view, position, id);
        if (SystemClock.elapsedRealtime() - lastClickTime < 500) {
            return;
        }
        lastClickTime = SystemClock.elapsedRealtime();
        String itemName = itemList.get(position).getName();
        DialogFragment newFragment = ItemDialogFragment.newInstance(itemName, pageTitle);
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getFragmentManager(), itemName);
        // Prevent image from reloading while dialog fragment is active
        adapter.isLoadImage = false;
    }

    @Override
    public void onDestroyView() {
        // Delete the data as well when the view is being destroyed
        itemList.clear();
        // Save the ListView scroll position before view is destroyed
        this.position = getListView().getFirstVisiblePosition();
        View view = getListView().getChildAt(0);
        this.y = (view == null) ? 0 : view.getTop();
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (isVisible()) {
            this.position = getListView().getFirstVisiblePosition();
            View view = getListView().getChildAt(0);
            this.y = (view == null) ? 0 : view.getTop();
            outState.putInt("Position", this.position);
            outState.putInt("Y", this.y);
        } else {
            outState.putInt("Position", this.position);
            outState.putInt("Y", this.y);
        }
    }

    @Override
    public void onPhotoClicked(int pos) {
        Bundle bundle = new Bundle();
        bundle.putString(getString(R.string.subcategory), pageTitle);
        bundle.putInt(getString(R.string.position), pos);
        Intent intent = new Intent(getActivity().getApplicationContext(), FullscreenActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = null;
        String[] projection = null;
        String selection = null;
        String[] selectionArgs = null;
        switch (id) {
            case LOADER_PRODUCT:
                uri = Contract.Product.CONTENT_URI;
                projection = new String[]{Contract.Product.COLUMN_NAME_CODE, Contract.Product.COLUMN_NAME_NAME, Contract.Product.COLUMN_NAME_PHOTO};
                selection = Contract.Product.COLUMN_NAME_SUBCATEGORY + " = ?";
                selectionArgs = new String[]{pageTitle};
                break;
            case LOADER_TEMP:
                uri = Contract.Temp.CONTENT_URI;
                projection = new String[]{Contract.Temp.COLUMN_NAME_NAME, Contract.Temp.COLUMN_NAME_SUBCATEGORY, Contract.Temp.COLUMN_NAME_QUANTITY, Contract.Temp.COLUMN_NAME_SIZE,
                        Contract.Temp.COLUMN_NAME_COLOR, Contract.Temp.COLUMN_NAME_PRICE, Contract.Temp.COLUMN_NAME_LABOR};
                break;
        }
        return new CursorLoader(this.getActivity(), uri, projection, selection, selectionArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case LOADER_PRODUCT:
                if (cursor != null && cursor.moveToFirst()) {
                    String itemCode, itemName, itemPhoto;
                    do {
                        itemCode = cursor.getString(cursor.getColumnIndex(Contract.Product.COLUMN_NAME_CODE));
                        itemName = cursor.getString(cursor.getColumnIndex(Contract.Product.COLUMN_NAME_NAME));
                        itemPhoto = cursor.getString(cursor.getColumnIndex(Contract.Product.COLUMN_NAME_PHOTO));
                        CatalogueItemObject item = new CatalogueItemObject(itemCode, itemName, itemPhoto);
                        itemList.add(item);
                    } while (cursor.moveToNext());
                    getLoaderManager().restartLoader(LOADER_TEMP, null, this);
                }
                break;
            case LOADER_TEMP:
                if (cursor != null && cursor.moveToFirst()) {
                    String itemName, subcategory, size, color, fullString;
                    String extra;
                    int quantity, price, labor;
                    // Make sure stackList is empty before adding.
                    for (CatalogueItemObject item : itemList) {
                        item.getStackList().clear();
                    }
                    do {
                        itemName = cursor.getString(cursor.getColumnIndex(Contract.Temp.COLUMN_NAME_NAME));
                        subcategory = cursor.getString(cursor.getColumnIndex(Contract.Temp.COLUMN_NAME_SUBCATEGORY));
                        for (CatalogueItemObject item : itemList) {
                            if (item.getName().equals(itemName) && pageTitle.equals(subcategory)) {
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
                                    color = color + "-" ;
                                }
                                if (!cursor.isNull(cursor.getColumnIndex(Contract.Temp.COLUMN_NAME_LABOR))) {
                                    labor = cursor.getInt(cursor.getColumnIndex(Contract.Temp.COLUMN_NAME_LABOR));
                                    extra = "+" + Integer.toString(labor);
                                } else {
                                    extra = "";
                                }
                                fullString = quantity + size + color + price + extra;
                                item.getStackList().add(fullString);
                            }
                        }
                    } while (cursor.moveToNext());
                } else {
                    for (CatalogueItemObject item : itemList) {
                        item.getStackList().clear();
                    }
                }
                adapter.notifyDataSetChanged();
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
