package com.neffulapp.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.neffulapp.R;
import com.neffulapp.model.Contract;

public class BundleListAdapter extends CursorAdapter {

    private Context mContext;
    private LayoutInflater layoutInflater;

    public BundleListAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        this.mContext = context;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = layoutInflater.inflate(R.layout.bundle_row, parent, false);
        view.setTag(new ViewHolder(view));
        return view;
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        String size, color, fullString;
        String extra;
        int qty, price, labor;

        qty = cursor.getInt(cursor.getColumnIndex(Contract.Temp.COLUMN_NAME_QUANTITY));
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

        fullString = qty + size + color + price + extra;

        holder.itemDetail.setText(fullString);

        holder.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = cursor.getInt(cursor.getColumnIndex(Contract.Temp.COLUMN_NAME_ID));
                mContext.getContentResolver().delete(Contract.Temp.CONTENT_URI, Contract.Temp.COLUMN_NAME_ID + "=?", new String[]{Integer.toString(id)});
            }
        });
    }

    static class ViewHolder {

        TextView itemDetail;
        ImageView removeButton;

        ViewHolder(View v) {
            itemDetail = (TextView) v.findViewById(R.id.itemTxt);
            removeButton = (ImageView) v.findViewById(R.id.removeBtn);
        }
    }
}
