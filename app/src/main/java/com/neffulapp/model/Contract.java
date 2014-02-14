package com.neffulapp.model;

import android.net.Uri;
import android.provider.BaseColumns;

public final class Contract {

    public static final String AUTHORITY = "com.neffulapp.provider.Main";
    /**
     * The scheme part for this provider's URI
     */
    private static final String SCHEME = "content://";

    // This class cannot be instantiated
    private Contract() {
    }

    /**
     * ********************************************************************************************************
     * Products table contract
     * *********************************************************************************************************
     */
    public static final class Product implements BaseColumns {

        // This class cannot be instantiated
        private Product() {
        }

        /**
         * The table name offered by this provider
         */
        public static final String TABLE_NAME = "product";
    /*
     * URI definitions
	 */
        /**
         * Path parts for the URIs
         */
        /**
         * Path part for the Products URI
         */
        private static final String PATH_PRODUCT = "/product";
        /**
         * Path part for the DISTINCT Products URI
         */
        private static final String PATH_PRODUCT_DISTINCT = "/distinctproduct";
        /**
         * Path part for the Product ID URI
         */
        private static final String PATH_PRODUCT_ID = "/product/";
        /**
         * 0-relative position of a note ID segment in the path part of a product ID URI
         */
        public static final int PRODUCT_ID_PATH_POSITION = 1;
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_PRODUCT);
        /**
         * The DISTINCT table URI
         */
        public static final Uri DISTINCT_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_PRODUCT_DISTINCT);
        /**
         * The content URI base for a single product. Callers must append a numeric product id to this Uri to retrieve a product
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_PRODUCT_ID);
        /**
         * The content URI match pattern for a single product, specified by its ID. Use this to match incoming URIs or to construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_PRODUCT_ID + "/#");
    /*
     * MIME type definitions
	 */
        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of products.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.nefcat.product";
        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single product.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.nefcat.product";
        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "modified DESC";
        /*
         * Column definitions
         */
        public static final String COLUMN_NAME_ID = "_id";
        public static final String COLUMN_NAME_CODE = "code";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_CATEGORY = "category";
        public static final String COLUMN_NAME_SUBCATEGORY = "subcategory";
        public static final String COLUMN_NAME_PHOTO = "photo";
    }

    /**
     * ********************************************************************************************************
     * Size table contract
     * *********************************************************************************************************
     */
    public static final class Size implements BaseColumns {

        // This class cannot be instantiated
        private Size() {
        }

        /**
         * The table name offered by this provider
         */
        public static final String TABLE_NAME = "size";
	/*
	 * URI definitions
	 */
        /**
         * Path parts for the URIs
         */
        /**
         * Path part for the Products URI
         */
        private static final String PATH_SIZE = "/size";
        /**
         * Path part for the DISTINCT Products URI
         */
        private static final String PATH_SIZE_DISTINCT = "/distinctsize";
        /**
         * Path part for the Product ID URI
         */
        private static final String PATH_SIZE_ID = "/size/";
        /**
         * 0-relative position of a note ID segment in the path part of a product ID URI
         */
        public static final int SIZE_ID_PATH_POSITION = 1;
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_SIZE);
        /**
         * The DISTINCT table URI
         */
        public static final Uri DISTINCT_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_SIZE_DISTINCT);
        /**
         * The content URI base for a single product. Callers must append a numeric product id to this Uri to retrieve a product
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_SIZE_ID);
        /**
         * The content URI match pattern for a single product, specified by its ID. Use this to match incoming URIs or to construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_SIZE_ID + "/#");
	/*
	 * MIME type definitions
	 */
        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of products.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.nefcat.size";
        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single product.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.nefcat.size";
        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "modified DESC";
        /*
         * Column definitions
         */
        public static final String COLUMN_NAME_ID = "_id";
        public static final String COLUMN_NAME_PRODUCT_ID = "product_id";
        public static final String COLUMN_NAME_SIZE_GROUP = "size_group";
    }

    /**
     * ********************************************************************************************************
     * SizeReference table contract
     * *********************************************************************************************************
     */
    public static final class Reference implements BaseColumns {

        // This class cannot be instantiated
        private Reference() {
        }

        /**
         * The table name offered by this provider
         */
        public static final String TABLE_NAME = "reference";
	/*
	 * URI definitions
	 */
        /**
         * Path parts for the URIs
         */
        /**
         * Path part for the Products URI
         */
        private static final String PATH_REFERENCE = "/reference";
        /**
         * Path part for the DISTINCT Products URI
         */
        private static final String PATH_REFERENCE_DISTINCT = "/distinctreference";
        /**
         * Path part for the Product ID URI
         */
        private static final String PATH_REFERENCE_ID = "/reference/";
        /**
         * 0-relative position of a note ID segment in the path part of a product ID URI
         */
        public static final int REFERENCE_ID_PATH_POSITION = 1;
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_REFERENCE);
        /**
         * The DISTINCT table URI
         */
        public static final Uri DISTINCT_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_REFERENCE_DISTINCT);
        /**
         * The content URI base for a single product. Callers must append a numeric product id to this Uri to retrieve a product
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_REFERENCE_ID);
        /**
         * The content URI match pattern for a single product, specified by its ID. Use this to match incoming URIs or to construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_REFERENCE_ID + "/#");
	/*
	 * MIME type definitions
	 */
        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of products.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.nefcat.reference";
        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single product.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.nefcat.reference";
        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "modified DESC";
        /*
         * Column definitions
         */
        public static final String COLUMN_NAME_ID = "_id";
        public static final String COLUMN_NAME_SIZE_GROUP = "size_group";
        public static final String COLUMN_NAME_SIZE = "size";
    }

    /**
     * ********************************************************************************************************
     * PricedByAtt table contract
     * *********************************************************************************************************
     */
    public static final class PricedByAtt implements BaseColumns {

        // This class cannot be instantiated
        private PricedByAtt() {
        }

        /**
         * The table name offered by this provider
         */
        public static final String TABLE_NAME = "pricedbyatt";
	/*
	 * URI definitions
	 */
        /**
         * Path parts for the URIs
         */
        /**
         * Path part for the Products URI
         */
        private static final String PATH_PRICEDBYATT = "/pricedbyatt";
        /**
         * Path part for the DISTINCT Products URI
         */
        private static final String PATH_PRICEDBYATT_DISTINCT = "/distinctpricedbyatt";
        /**
         * Path part for the Product ID URI
         */
        private static final String PATH_PRICEDBYATT_ID = "/pricedbyatt/";
        /**
         * 0-relative position of a note ID segment in the path part of a product ID URI
         */
        public static final int PRICEDBYATT_ID_PATH_POSITION = 1;
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_PRICEDBYATT);
        /**
         * The DISTINCT table URI
         */
        public static final Uri DISTINCT_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_PRICEDBYATT_DISTINCT);
        /**
         * The content URI base for a single product. Callers must append a numeric product id to this Uri to retrieve a product
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_PRICEDBYATT_ID);
        /**
         * The content URI match pattern for a single product, specified by its ID. Use this to match incoming URIs or to construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_PRICEDBYATT_ID + "/#");
	/*
	 * MIME type definitions
	 */
        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of products.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.nefcat.pricedbyatt";
        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single product.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.nefcat.pricedbyatt";
        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "modified DESC";
        /*
         * Column definitions
         */
        public static final String COLUMN_NAME_ID = "_id";
        public static final String COLUMN_NAME_SIZE_ID = "size_id";
        public static final String COLUMN_NAME_COLOR = "color";
        public static final String COLUMN_NAME_PRICE = "price";
        public static final String COLUMN_NAME_LABOR = "labor";
    }

    /**
     * ********************************************************************************************************
     * Table Cart contract
     * *********************************************************************************************************
     */
    public static final class Cart implements BaseColumns {

        // This class cannot be instantiated
        private Cart() {
        }

        /**
         * The table name offered by this provider
         */
        public static final String TABLE_NAME = "cart";
	/*
	 * URI definitions
	 */
        /**
         * Path parts for the URIs
         */
        /**
         * Path part for the Products URI
         */
        private static final String PATH_CART = "/cart";
        /**
         * Path part for the DISTINCT Products URI
         */
        private static final String PATH_CART_DISTINCT = "/distinctcart";
        /**
         * Path part for the Product ID URI
         */
        private static final String PATH_CART_ID = "/cart/";
        /**
         * 0-relative position of a note ID segment in the path part of a product ID URI
         */
        public static final int PATH_CART_PATH_POSITION = 1;
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_CART);
        /**
         * The DISTINCT table URI
         */
        public static final Uri DISTINCT_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_CART_DISTINCT);
        /**
         * The content URI base for a single product. Callers must append a numeric product id to this Uri to retrieve a product
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_CART_ID);
        /**
         * The content URI match pattern for a single product, specified by its ID. Use this to match incoming URIs or to construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_CART_ID + "/#");
	/*
	 * MIME type definitions
	 */
        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of products.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.nefcat.cart";
        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single product.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.nefcat.cart";
        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "modified DESC";
        /*
         * Column definitions
         */
        public static final String COLUMN_NAME_ID = "_id";
        public static final String COLUMN_NAME_PROFILE_ID = "profile_id";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_SUBCATEGORY = "subcategory";
        public static final String COLUMN_NAME_QUANTITY = "quantity";
        public static final String COLUMN_NAME_SIZE = "size";
        public static final String COLUMN_NAME_COLOR = "color";
        public static final String COLUMN_NAME_PRICE = "price";
        public static final String COLUMN_NAME_LABOR = "labor";
    }

    /**
     * ********************************************************************************************************
     * Table Temp contract
     * *********************************************************************************************************
     */
    public static final class Temp implements BaseColumns {

        // This class cannot be instantiated
        private Temp() {
        }

        /**
         * The table name offered by this provider
         */
        public static final String TABLE_NAME = "temp";
	/*
	 * URI definitions
	 */
        /**
         * Path parts for the URIs
         */
        /**
         * Path part for the Products URI
         */
        private static final String PATH_TEMP = "/temp";
        /**
         * Path part for the DISTINCT Products URI
         */
        private static final String PATH_TEMP_DISTINCT = "/distincttemp";
        /**
         * Path part for the Product ID URI
         */
        private static final String PATH_TEMP_ID = "/temp/";
        /**
         * 0-relative position of a note ID segment in the path part of a product ID URI
         */
        public static final int PATH_TEMP_PATH_POSITION = 1;
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_TEMP);
        /**
         * The DISTINCT table URI
         */
        public static final Uri DISTINCT_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_TEMP_DISTINCT);
        /**
         * The content URI base for a single product. Callers must append a numeric product id to this Uri to retrieve a product
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_TEMP_ID);
        /**
         * The content URI match pattern for a single product, specified by its ID. Use this to match incoming URIs or to construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_TEMP_ID + "/#");
	/*
	 * MIME type definitions
	 */
        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of products.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.nefcat.temp";
        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single product.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.nefcat.temp";
        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "modified DESC";
        /*
         * Column definitions
         */
        public static final String COLUMN_NAME_ID = "_id";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_SUBCATEGORY = "subcategory";
        public static final String COLUMN_NAME_QUANTITY = "quantity";
        public static final String COLUMN_NAME_SIZE = "size";
        public static final String COLUMN_NAME_COLOR = "color";
        public static final String COLUMN_NAME_PRICE = "price";
        public static final String COLUMN_NAME_LABOR = "labor";
    }

    /**
     * ********************************************************************************************************
     * Table Profile contract
     * *********************************************************************************************************
     */
    public static final class Profile implements BaseColumns {

        // This class cannot be instantiated
        private Profile() {
        }

        /**
         * The table name offered by this provider
         */
        public static final String TABLE_NAME = "profile";
	/*
	 * URI definitions
	 */
        /**
         * Path parts for the URIs
         */
        /**
         * Path part for the Products URI
         */
        private static final String PATH_PROFILE = "/profile";
        /**
         * Path part for the DISTINCT Products URI
         */
        private static final String PATH_PROFILE_DISTINCT = "/distinctprofile";
        /**
         * Path part for the Product ID URI
         */
        private static final String PATH_PROFILE_ID = "/profile/";
        /**
         * 0-relative position of a note ID segment in the path part of a product ID URI
         */
        public static final int PATH_PROFILE_PATH_POSITION = 1;
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_PROFILE);
        /**
         * The DISTINCT table URI
         */
        public static final Uri DISTINCT_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_PROFILE_DISTINCT);
        /**
         * The content URI base for a single product. Callers must append a numeric product id to this Uri to retrieve a product
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_PROFILE_ID);
        /**
         * The content URI match pattern for a single product, specified by its ID. Use this to match incoming URIs or to construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_PROFILE_ID + "/#");
	/*
	 * MIME type definitions
	 */
        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of products.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.nefcat.profile";
        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single product.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.nefcat.profile";
        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "modified DESC";
        /*
         * Column definitions
         */
        public static final String COLUMN_NAME_ID = "_id";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_CREATED_AT = "created_at";
    }

    /**
     * ********************************************************************************************************
     * Table Remark contract
     * *********************************************************************************************************
     */
    public static final class Remark implements BaseColumns {

        // This class cannot be instantiated
        private Remark() {
        }

        /**
         * The table name offered by this provider
         */
        public static final String TABLE_NAME = "remark";
	/*
	 * URI definitions
	 */
        /**
         * Path parts for the URIs
         */
        /**
         * Path part for the Products URI
         */
        private static final String PATH_REMARK = "/remark";
        /**
         * Path part for the DISTINCT Products URI
         */
        private static final String PATH_REMARK_DISTINCT = "/distinctremark";
        /**
         * Path part for the Product ID URI
         */
        private static final String PATH_REMARK_ID = "/remark/";
        /**
         * 0-relative position of a note ID segment in the path part of a product ID URI
         */
        public static final int PATH_REMARK_PATH_POSITION = 1;
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_REMARK);
        /**
         * The DISTINCT table URI
         */
        public static final Uri DISTINCT_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_REMARK_DISTINCT);
        /**
         * The content URI base for a single product. Callers must append a numeric product id to this Uri to retrieve a product
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_REMARK_ID);
        /**
         * The content URI match pattern for a single product, specified by its ID. Use this to match incoming URIs or to construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_REMARK_ID + "/#");
	/*
	 * MIME type definitions
	 */
        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of products.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.nefcat.remark";
        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single product.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.nefcat.remark";
        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "modified DESC";
        /*
         * Column definitions
         */
        public static final String COLUMN_NAME_ID = "_id";
        public static final String COLUMN_NAME_PROFILE_ID = "profile_id";
        public static final String COLUMN_NAME_ITEM_NAME = "item_name";
        public static final String COLUMN_NAME_REMARK = "remark";
    }
}
