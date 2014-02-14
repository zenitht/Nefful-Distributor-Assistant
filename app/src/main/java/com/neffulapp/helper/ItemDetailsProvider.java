package com.neffulapp.helper;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.neffulapp.model.Contract;

import java.io.IOException;

public class ItemDetailsProvider extends ContentProvider {

    // Used for debugging and logging
    private static final String TAG = "ItemDetailsProvider";
    // Handle to a new DatabaseHelper.
    private DatabaseHelper dbHelper;
    /*
     * Constants used by the Uri matcher to choose an action based on the pattern of the incoming URI
     */
    /**
     * **************************************************************************
     * Table Products
     * ***************************************************************************
     */
    private static final int PRODUCT = 1;
    private static final int PRODUCT_ID = 2;
    private static final int DISTINCT_PRODUCT = 3;
    /**
     * **************************************************************************
     * Table Size
     * ***************************************************************************
     */
    private static final int SIZE = 4;
    private static final int SIZE_ID = 5;
    private static final int DISTINCT_SIZE = 6;
    /**
     * **************************************************************************
     * Table Reference
     * ***************************************************************************
     */
    private static final int REFERENCE = 7;
    private static final int REFERENCE_ID = 8;
    private static final int DISTINCT_REFERENCE = 9;
    /**
     * **************************************************************************
     * Table PricedByAtt
     * ***************************************************************************
     */
    private static final int PRICEDBYATT = 10;
    private static final int PRICEDBYATT_ID = 11;
    private static final int DISTINCT_PRICEDBYATT = 12;
    /**
     * **************************************************************************
     * Table Cart
     * ***************************************************************************
     */
    private static final int CART = 13;
    private static final int CART_ID = 14;
    private static final int DISTINCT_CART = 15;
    /**
     * **************************************************************************
     * Table Temp
     * ***************************************************************************
     */
    private static final int TEMP = 16;
    private static final int TEMP_ID = 17;
    private static final int DISTINCT_TEMP = 18;
    /**
     * **************************************************************************
     * Table Profile
     * ***************************************************************************
     */
    private static final int PROFILE = 19;
    private static final int PROFILE_ID = 20;
    private static final int DISTINCT_PROFILE = 21;
    /**
     * **************************************************************************
     * Table Remark
     * ***************************************************************************
     */
    private static final int REMARK = 22;
    private static final int REMARK_ID = 23;
    private static final int DISTINCT_REMARK = 24;
    /**
     * A UriMatcher instance
     */
    private static final UriMatcher sUriMatcher;

    /**
     * A block that instantiates and sets static objects
     */
    static {
        // Creates and initializes the URI matcher
        // Create a new instance
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        /*********************
         * Table Products
         *********************/
        // Add a pattern that routes URIs terminated with "products" to a PRODUCTS operation
        sUriMatcher.addURI(Contract.AUTHORITY, "product", PRODUCT);
        // Add a pattern that routes URIs terminated with "product" plus an integer
        // to a product ID operation
        sUriMatcher.addURI(Contract.AUTHORITY, "product/#", PRODUCT_ID);
        // Add a pattern that routes URIs terminated with "distinctproducts" to a DISTINCT_PRODUCTS operation
        sUriMatcher.addURI(Contract.AUTHORITY, "distinctproduct", DISTINCT_PRODUCT);
        /*********************
         * Table Size
         *********************/
        // Add a pattern that routes URIs terminated with "products" to a PRODUCTS operation
        sUriMatcher.addURI(Contract.AUTHORITY, "size", SIZE);
        // Add a pattern that routes URIs terminated with "product" plus an integer
        // to a product ID operation
        sUriMatcher.addURI(Contract.AUTHORITY, "size/#", SIZE_ID);
        // Add a pattern that routes URIs terminated with "distinctproducts" to a DISTINCT_PRODUCTS operation
        sUriMatcher.addURI(Contract.AUTHORITY, "distinctsize", DISTINCT_SIZE);
        /*********************
         * Table Reference
         **********************/
        // Add a pattern that routes URIs terminated with "products" to a PRODUCTS operation
        sUriMatcher.addURI(Contract.AUTHORITY, "reference", REFERENCE);
        // Add a pattern that routes URIs terminated with "product" plus an integer
        // to a product ID operation
        sUriMatcher.addURI(Contract.AUTHORITY, "reference/#", REFERENCE_ID);
        // Add a pattern that routes URIs terminated with "distinctproducts" to a DISTINCT_PRODUCTS operation
        sUriMatcher.addURI(Contract.AUTHORITY, "distinctreference", DISTINCT_REFERENCE);
        /*********************
         * Table PricedByAtt
         **********************/
        // Add a pattern that routes URIs terminated with "products" to a PRODUCTS operation
        sUriMatcher.addURI(Contract.AUTHORITY, "pricedbyatt", PRICEDBYATT);
        // Add a pattern that routes URIs terminated with "product" plus an integer
        // to a product ID operation
        sUriMatcher.addURI(Contract.AUTHORITY, "pricedbyatt/#", PRICEDBYATT_ID);
        // Add a pattern that routes URIs terminated with "distinctproducts" to a DISTINCT_PRODUCTS operation
        sUriMatcher.addURI(Contract.AUTHORITY, "distinctpricedbyatt", DISTINCT_PRICEDBYATT);
        /*********************
         * Table Cart
         **********************/
        // Add a pattern that routes URIs terminated with "products" to a PRODUCTS operation
        sUriMatcher.addURI(Contract.AUTHORITY, "cart", CART);
        // Add a pattern that routes URIs terminated with "product" plus an integer
        // to a product ID operation
        sUriMatcher.addURI(Contract.AUTHORITY, "cart/#", CART_ID);
        // Add a pattern that routes URIs terminated with "distinctproducts" to a DISTINCT_PRODUCTS operation
        sUriMatcher.addURI(Contract.AUTHORITY, "distinctcart", DISTINCT_CART);
        /*********************
         * Table Temp
         **********************/
        // Add a pattern that routes URIs terminated with "products" to a PRODUCTS operation
        sUriMatcher.addURI(Contract.AUTHORITY, "temp", TEMP);
        // Add a pattern that routes URIs terminated with "product" plus an integer
        // to a product ID operation
        sUriMatcher.addURI(Contract.AUTHORITY, "temp/#", TEMP_ID);
        // Add a pattern that routes URIs terminated with "distinctproducts" to a DISTINCT_PRODUCTS operation
        sUriMatcher.addURI(Contract.AUTHORITY, "distincttemp", DISTINCT_TEMP);
        /*********************
         * Table Profile
         **********************/
        // Add a pattern that routes URIs terminated with "products" to a PRODUCTS operation
        sUriMatcher.addURI(Contract.AUTHORITY, "profile", PROFILE);
        // Add a pattern that routes URIs terminated with "product" plus an integer
        // to a product ID operation
        sUriMatcher.addURI(Contract.AUTHORITY, "profile/#", PROFILE_ID);
        // Add a pattern that routes URIs terminated with "distinctproducts" to a DISTINCT_PRODUCTS operation
        sUriMatcher.addURI(Contract.AUTHORITY, "distinctprofile", DISTINCT_PROFILE);
        /*********************
         * Table Remark
         **********************/
        // Add a pattern that routes URIs terminated with "products" to a PRODUCTS operation
        sUriMatcher.addURI(Contract.AUTHORITY, "remark", REMARK);
        // Add a pattern that routes URIs terminated with "product" plus an integer
        // to a product ID operation
        sUriMatcher.addURI(Contract.AUTHORITY, "remark/#", REMARK_ID);
        // Add a pattern that routes URIs terminated with "distinctproducts" to a DISTINCT_PRODUCTS operation
        sUriMatcher.addURI(Contract.AUTHORITY, "distinctremark", DISTINCT_REMARK);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        try {
            dbHelper.createDataBase();
        } catch (IOException ioe) {
            Log.e(TAG, ioe.toString() + "  Unable to create database");
            throw new Error("Unable to create database");
        }
        return (dbHelper != null);
    }

    @Override
    public String getType(Uri uri) {
        /**
         * Chooses the MIME type based on the incoming URI pattern
         */
        switch (sUriMatcher.match(uri)) {
            // If the pattern is for products, returns the general content type.
            case PRODUCT:
                return Contract.Product.CONTENT_TYPE;
            // If the pattern is for product IDs, returns the product ID content type.
            case PRODUCT_ID:
                return Contract.Product.CONTENT_ITEM_TYPE;
            // If the pattern is for products, returns the general content type.
            case SIZE:
                return Contract.Size.CONTENT_TYPE;
            // If the pattern is for products, returns the general content type.
            case SIZE_ID:
                return Contract.Size.CONTENT_ITEM_TYPE;
            // If the pattern is for products, returns the general content type.
            case REFERENCE:
                return Contract.Reference.CONTENT_TYPE;
            // If the pattern is for products, returns the general content type.
            case REFERENCE_ID:
                return Contract.Reference.CONTENT_ITEM_TYPE;
            // If the pattern is for products, returns the general content type.
            case PRICEDBYATT:
                return Contract.PricedByAtt.CONTENT_TYPE;
            // If the pattern is for products, returns the general content type.
            case PRICEDBYATT_ID:
                return Contract.PricedByAtt.CONTENT_ITEM_TYPE;
            // If the pattern is for products, returns the general content type.
            case CART:
                return Contract.Cart.CONTENT_TYPE;
            // If the pattern is for products, returns the general content type.
            case CART_ID:
                return Contract.Cart.CONTENT_ITEM_TYPE;
            // If the pattern is for products, returns the general content type.
            case TEMP:
                return Contract.Temp.CONTENT_TYPE;
            // If the pattern is for products, returns the general content type.
            case TEMP_ID:
                return Contract.Temp.CONTENT_ITEM_TYPE;
            // If the pattern is for products, returns the general content type.
            case PROFILE:
                return Contract.Profile.CONTENT_TYPE;
            // If the pattern is for products, returns the general content type.
            case PROFILE_ID:
                return Contract.Profile.CONTENT_ITEM_TYPE;
            // If the URI pattern doesn't match any permitted patterns, throws an exception.
            case REMARK:
                return Contract.Remark.CONTENT_TYPE;
            // If the pattern is for products, returns the general content type.
            case REMARK_ID:
                return Contract.Remark.CONTENT_ITEM_TYPE;
            // If the URI pattern doesn't match any permitted patterns, throws an exception.
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri result = null;
        long id;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Validate the requested Uri
        switch (sUriMatcher.match(uri)) {
            case PRODUCT:
                id = db.insert(Contract.Product.TABLE_NAME, null, values);
                break;
            case SIZE:
                id = db.insert(Contract.Size.TABLE_NAME, null, values);
                break;
            case CART:
                id = db.insert(Contract.Cart.TABLE_NAME, null, values);
                break;
            case TEMP:
                id = db.insert(Contract.Temp.TABLE_NAME, null, values);
                break;
            case PROFILE:
                id = db.insert(Contract.Profile.TABLE_NAME, null, values);
                break;
            case REMARK:
                id = db.insert(Contract.Remark.TABLE_NAME, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        if (id > 0) {
            // Return a URI to the newly created row on success
            switch (sUriMatcher.match(uri)) {
                case PRODUCT:
                    result = ContentUris.withAppendedId(Contract.Product.CONTENT_ID_URI_BASE, id);
                    break;
                case SIZE:
                    result = ContentUris.withAppendedId(Contract.Size.CONTENT_ID_URI_BASE, id);
                    break;
                case CART:
                    result = ContentUris.withAppendedId(Contract.Cart.CONTENT_ID_URI_BASE, id);
                    break;
                case TEMP:
                    result = ContentUris.withAppendedId(Contract.Temp.CONTENT_ID_URI_BASE, id);
                    break;
                case PROFILE:
                    result = ContentUris.withAppendedId(Contract.Profile.CONTENT_ID_URI_BASE, id);
                    break;
                case REMARK:
                    result = ContentUris.withAppendedId(Contract.Remark.CONTENT_ID_URI_BASE, id);
                    break;
                default:
                    break;
            }
            // Notify the Context's ContentResolver of the change
            getContext().getContentResolver().notifyChange(result, null);
        }
        return result;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        int rowAdded = 0;
        String table;
        switch (sUriMatcher.match(uri)) {
            case PRICEDBYATT:
                table = Contract.PricedByAtt.TABLE_NAME;
                break;
            case CART:
                table = Contract.Cart.TABLE_NAME;
                break;
            case TEMP:
                table = Contract.Temp.TABLE_NAME;
                break;
            case REMARK:
                table = Contract.Remark.TABLE_NAME;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (ContentValues cv : values) {
                long newID = db.insertOrThrow(table, null, cv);
                if (newID <= 0) {
                    throw new SQLException("Failed to insert row into " + uri);
                }
            }
            db.setTransactionSuccessful();
            getContext().getContentResolver().notifyChange(uri, null);
            rowAdded = values.length;
        } finally {
            db.endTransaction();
        }
        return rowAdded;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        String segment, selectionClause;
        switch (sUriMatcher.match(uri)) {
            case PRODUCT:
                count = db.delete(Contract.Product.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                segment = uri.getLastPathSegment();
                selectionClause = Contract.Product.COLUMN_NAME_ID + "=" + segment + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
                count = db.delete(Contract.Product.TABLE_NAME, selectionClause, selectionArgs);
                break;
            case SIZE:
                count = db.delete(Contract.Size.TABLE_NAME, selection, selectionArgs);
                break;
            case SIZE_ID:
                segment = uri.getLastPathSegment();
                selectionClause = Contract.Size.COLUMN_NAME_ID + "=" + segment + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
                count = db.delete(Contract.Size.TABLE_NAME, selectionClause, selectionArgs);
                break;
            case PRICEDBYATT:
                count = db.delete(Contract.PricedByAtt.TABLE_NAME, selection, selectionArgs);
                break;
            case PRICEDBYATT_ID:
                segment = uri.getLastPathSegment();
                selectionClause = Contract.PricedByAtt.COLUMN_NAME_ID + "=" + segment + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
                count = db.delete(Contract.PricedByAtt.TABLE_NAME, selectionClause, selectionArgs);
                break;
            case CART:
                count = db.delete(Contract.Cart.TABLE_NAME, selection, selectionArgs);
                break;
            case CART_ID:
                segment = uri.getLastPathSegment();
                selectionClause = Contract.Cart.COLUMN_NAME_ID + "=" + segment + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
                count = db.delete(Contract.Cart.TABLE_NAME, selectionClause, selectionArgs);
                break;
            case TEMP:
                count = db.delete(Contract.Temp.TABLE_NAME, selection, selectionArgs);
                break;
            case TEMP_ID:
                segment = uri.getLastPathSegment();
                selectionClause = Contract.Temp.COLUMN_NAME_ID + "=" + segment + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
                count = db.delete(Contract.Temp.TABLE_NAME, selectionClause, selectionArgs);
                break;
            case PROFILE:
                count = db.delete(Contract.Profile.TABLE_NAME, selection, selectionArgs);
                break;
            case PROFILE_ID:
                segment = uri.getLastPathSegment();
                selectionClause = Contract.Profile.COLUMN_NAME_ID + "=" + segment + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
                count = db.delete(Contract.Profile.TABLE_NAME, selectionClause, selectionArgs);
                break;
            case REMARK:
                count = db.delete(Contract.Remark.TABLE_NAME, selection, selectionArgs);
                break;
            case REMARK_ID:
                segment = uri.getLastPathSegment();
                selectionClause = Contract.Remark.COLUMN_NAME_ID + "=" + segment + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
                count = db.delete(Contract.Remark.TABLE_NAME, selectionClause, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        if (count > 0) {
            // Notify the Context's ContentResolver of the change
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Constructs a new query builder and sets its table name
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        boolean useDistinct = false;
        /**
         * Choose the projection and adjust the "where" clause based on URI pattern-matching.
         */
        switch (sUriMatcher.match(uri)) {
            /*********************
             * Table Products
             *********************/
            case PRODUCT:
                qb.setTables(Contract.Product.TABLE_NAME);
                break;
            case PRODUCT_ID:
                qb.setTables(Contract.Product.TABLE_NAME);
                qb.appendWhere(Contract.Product._ID + "=" + uri.getPathSegments().get(Contract.Product.PRODUCT_ID_PATH_POSITION));
                break;
            case DISTINCT_PRODUCT:
                qb.setTables(Contract.Product.TABLE_NAME);
                useDistinct = true;
                break;
            /*********************
             * Table Size
             *********************/
            case SIZE:
                qb.setTables(Contract.Size.TABLE_NAME);
                break;
            case SIZE_ID:
                qb.setTables(Contract.Size.TABLE_NAME);
                qb.appendWhere(Contract.Size._ID + "=" + uri.getPathSegments().get(Contract.Size.SIZE_ID_PATH_POSITION));
                break;
            case DISTINCT_SIZE:
                qb.setTables(Contract.Size.TABLE_NAME);
                useDistinct = true;
                break;
            /*********************
             * Table Reference
             *********************/
            case REFERENCE:
                qb.setTables(Contract.Reference.TABLE_NAME);
                break;
            case REFERENCE_ID:
                qb.setTables(Contract.Reference.TABLE_NAME);
                qb.appendWhere(Contract.Reference._ID + "=" + uri.getPathSegments().get(Contract.Reference.REFERENCE_ID_PATH_POSITION));
                break;
            case DISTINCT_REFERENCE:
                qb.setTables(Contract.Reference.TABLE_NAME);
                useDistinct = true;
                break;
            /*********************
             * Table PricedByAtt
             *********************/
            case PRICEDBYATT:
                qb.setTables(Contract.PricedByAtt.TABLE_NAME);
                break;
            case PRICEDBYATT_ID:
                qb.setTables(Contract.PricedByAtt.TABLE_NAME);
                qb.appendWhere(Contract.PricedByAtt._ID + "=" + uri.getPathSegments().get(Contract.PricedByAtt.PRICEDBYATT_ID_PATH_POSITION));
                break;
            case DISTINCT_PRICEDBYATT:
                qb.setTables(Contract.PricedByAtt.TABLE_NAME);
                useDistinct = true;
                break;
            /*********************
             * Table Cart
             *********************/
            case CART:
                qb.setTables(Contract.Cart.TABLE_NAME);
                break;
            case CART_ID:
                qb.setTables(Contract.Cart.TABLE_NAME);
                qb.appendWhere(Contract.Cart._ID + "=" + uri.getPathSegments().get(Contract.Cart.PATH_CART_PATH_POSITION));
                break;
            case DISTINCT_CART:
                qb.setTables(Contract.Cart.TABLE_NAME);
                useDistinct = true;
                break;
            /*********************
             * Table Temp
             *********************/
            case TEMP:
                qb.setTables(Contract.Temp.TABLE_NAME);
                break;
            case TEMP_ID:
                qb.setTables(Contract.Temp.TABLE_NAME);
                qb.appendWhere(Contract.Temp._ID + "=" + uri.getPathSegments().get(Contract.Temp.PATH_TEMP_PATH_POSITION));
                break;
            case DISTINCT_TEMP:
                qb.setTables(Contract.Temp.TABLE_NAME);
                useDistinct = true;
                break;
            /*********************
             * Table Profile
             *********************/
            case PROFILE:
                qb.setTables(Contract.Profile.TABLE_NAME);
                break;
            case PROFILE_ID:
                qb.setTables(Contract.Profile.TABLE_NAME);
                qb.appendWhere(Contract.Profile._ID + "=" + uri.getPathSegments().get(Contract.Profile.PATH_PROFILE_PATH_POSITION));
                break;
            case DISTINCT_PROFILE:
                qb.setTables(Contract.Profile.TABLE_NAME);
                useDistinct = true;
                break;
            /*********************
             * Table Remark
             *********************/
            case REMARK:
                qb.setTables(Contract.Remark.TABLE_NAME);
                break;
            case REMARK_ID:
                qb.setTables(Contract.Remark.TABLE_NAME);
                qb.appendWhere(Contract.Remark._ID + "=" + uri.getPathSegments().get(Contract.Remark.PATH_REMARK_PATH_POSITION));
                break;
            case DISTINCT_REMARK:
                qb.setTables(Contract.Remark.TABLE_NAME);
                useDistinct = true;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        String orderBy;
        // If no sort order is specified, uses the default
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = null;
        } else {
            // otherwise, uses the incoming sort order
            orderBy = sortOrder;
        }
        // Opens the database object in "read" mode, since no writes need to be done.
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        qb.setDistinct(useDistinct);
    /*
	 * Performs the query. If no problems occur trying to read the database, then a Cursor object is returned; otherwise, the cursor variable contains null. If no records were selected, then the
	 * Cursor object is empty, and Cursor.getCount() returns 0.
	 */
        Cursor c = qb.query(db, // The database to query
                projection, // The columns to return from the query
                selection, // The columns for the where clause
                selectionArgs, // The values for the where clause
                null, // don't group the rows
                null, // don't filter by row groups
                orderBy // The sort order
        );
        // Tells the Cursor what URI to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsUpdated;
        switch (sUriMatcher.match(uri)) {
            case REMARK:
                rowsUpdated = db.update(Contract.Remark.TABLE_NAME, values, selection, selectionArgs);
                break;
            case REMARK_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update(Contract.Remark.TABLE_NAME, values, Contract.Remark.COLUMN_NAME_ID + "=" + id, null);
                } else {
                    rowsUpdated = db.update(Contract.Remark.TABLE_NAME, values, Contract.Remark.COLUMN_NAME_ID + "=" + id + " and " + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }
}
