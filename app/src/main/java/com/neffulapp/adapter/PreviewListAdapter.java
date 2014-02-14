package com.neffulapp.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.neffulapp.R;
import com.neffulapp.helper.BitmapProcessor;
import com.neffulapp.model.PreviewItemObject;

import java.util.List;

public class PreviewListAdapter extends BaseAdapter {

    private static final int MAX_LENGTH = 18;
    private Context context;
    private Resources resources;
    private LayoutInflater layoutInflater;
    private int layoutResource;
    private List<PreviewItemObject> mList;
    private BitmapProcessor bitmapProcessor;

    public PreviewListAdapter(Context context, int resource, List<PreviewItemObject> list) {
        this.context = context;
        this.resources = context.getResources();
        this.layoutResource = resource;
        this.mList = list;
        this.bitmapProcessor = new BitmapProcessor(context);
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder;
        if (row == null) {
            row = layoutInflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder(row);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        String code = mList.get(position).getCode();
        String name = mList.get(position).getName();

        holder.productCodeTxt.setText(code);
        holder.productNameTxt.setText(name);

        String path = null;
        String photo = mList.get(position).getPhoto();
        int resId = resources.getIdentifier(photo, "drawable", context.getPackageName());
        if (resId == 0) {
            path = context.getFilesDir().getPath() + "/" + photo + ".png";
        }
        bitmapProcessor.loadBitmap(resId, path, holder.photoImg);

        if (mList.get(position).getRemark() != null && !mList.get(position).getRemark().isEmpty()) {
            holder.remarkTxt.setText("* " + mList.get(position).getRemark());
        } else {
            holder.remarkTxt.setText(null);
        }
        if (mList.get(position).getLabor() == 0) {
            holder.subtotalTxt.setText("[" + mList.get(position).getSubtotal() + "]");
        } else {
            holder.subtotalTxt.setText("[" + mList.get(position).getSubtotal() + "+" + mList.get(position).getLabor() + "]");
        }

        // Clear layout views
        holder.tableLyt.removeAllViews();

        // Set string from stack list to layout
        if (mList.get(position).getStackList() != null) {
            int maxLength = 0;

            for (String string : mList.get(position).getStackList()) {
                if (string.length() > maxLength) {
                    maxLength = string.length();
                }
            }

            int i = 0;
            int columnPerRow = (maxLength < MAX_LENGTH) ? 3 : 2;
            TableRow tableRow = null;

            for (String bundle : mList.get(position).getStackList()) {
                if (i % columnPerRow == 0) {
                    tableRow = new TableRow(context);
                    holder.tableLyt.addView(tableRow);
                }

                TextView txt = new TextView(context);
                txt.setText(bundle);
                txt.setTextSize(10);
                txt.setPadding(10, 0, 10, 0);
                txt.setTextColor(Color.parseColor("#750082"));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    txt.setBackground(context.getResources().getDrawable(R.drawable.text_background));
                } else {
                    txt.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.text_background));
                }

                TextView gap = new TextView(context);
                gap.setText("  ");

                tableRow.addView(txt);
                tableRow.addView(gap);

                i++;
            }
        }
        return row;
    }

    static class ViewHolder {

        ImageView photoImg;
        TextView productCodeTxt;
        TextView productNameTxt;
        TableLayout tableLyt;
        TextView remarkTxt;
        TextView subtotalTxt;

        ViewHolder(View v) {
            photoImg = (ImageView) v.findViewById(R.id.preview_productPhoto);
            productCodeTxt = (TextView) v.findViewById(R.id.productCode);
            productNameTxt = (TextView) v.findViewById(R.id.productName);
            tableLyt = (TableLayout) v.findViewById(R.id.row_table);
            remarkTxt = (TextView) v.findViewById(R.id.preview_remark);
            subtotalTxt = (TextView) v.findViewById(R.id.preview_subtotal);
        }
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public PreviewItemObject getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }
}
