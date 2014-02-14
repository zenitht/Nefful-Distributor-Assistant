package com.neffulapp.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.neffulapp.R;
import com.neffulapp.helper.BitmapProcessor;
import com.neffulapp.model.CatalogueItemObject;

import java.util.List;

public class ItemListAdapter extends ArrayAdapter<CatalogueItemObject> {

    private static final int MAX_LENGTH = 18;
    private Context context;
    private Resources resources;
    private LayoutInflater layoutInflater;
    private int layoutResourceId;
    private ItemListAdapterEventListener listener;
    private List<CatalogueItemObject> item;
    private long lastClickTime = 0;
    private BitmapProcessor bitmapProcessor;
    public boolean isLoadImage = true;

    public ItemListAdapter(Context context, int layoutResourceId, List<CatalogueItemObject> item, ItemListAdapterEventListener listener) {
        super(context, layoutResourceId, item);
        this.context = context;
        this.resources = context.getResources();
        this.layoutResourceId = layoutResourceId;
        this.listener = listener;
        this.item = item;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        bitmapProcessor = new BitmapProcessor(context);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder;
        if (row == null) {
            row = layoutInflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder(row);
            if (row != null) {
                row.setTag(holder);
            }
        } else {
            holder = (ViewHolder) row.getTag();
        }

        String code = item.get(position).getCode();
        String name = item.get(position).getName();

        holder.productCodeTxt.setText(code);
        holder.productNameTxt.setText(name);

        if (isLoadImage) {
            String path = null;
            String photo = item.get(position).getPhoto();
            int resId = resources.getIdentifier(photo, "drawable", context.getPackageName());
            if (resId == 0) {
                path = context.getFilesDir().getPath() + "/" + photo + ".png";
            }
            bitmapProcessor.loadBitmap(resId, path, holder.photoImg);
        }

        holder.photoImg.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - lastClickTime < 500) {
                    return;
                }
                lastClickTime = SystemClock.elapsedRealtime();
                listener.onPhotoClicked(position);
            }
        });

        holder.tableLyt.removeAllViews();

        if (!item.get(position).getStackList().isEmpty()) {
            int maxLength = 0;
            for (String string : item.get(position).getStackList()) {
                if (string.length() > maxLength) {
                    maxLength = string.length();
                }
            }

            int i = 0;
            int columnPerRow = (maxLength < MAX_LENGTH) ? 3 : 2;
            TableRow tableRow = null;

            for (String bundle : item.get(position).getStackList()) {
                if (i % columnPerRow == 0) {
                    tableRow = new TableRow(context);
                    holder.tableLyt.addView(tableRow);
                }

                TextView txt = new TextView(context);
                txt.setText(bundle);
                txt.setTextSize(10);
                txt.setPadding(10, 0, 10, 0);
                txt.setTextColor(context.getResources().getColor(R.color.text_purple));
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

        ViewHolder(View v) {
            photoImg = (ImageView) v.findViewById(R.id.productPhoto);
            productCodeTxt = (TextView) v.findViewById(R.id.productCode);
            productNameTxt = (TextView) v.findViewById(R.id.productName);
            tableLyt = (TableLayout) v.findViewById(R.id.row_table);
        }
    }

    public interface ItemListAdapterEventListener {

        public void onPhotoClicked(int position);
    }
}
