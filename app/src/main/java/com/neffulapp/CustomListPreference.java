package com.neffulapp;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

import com.neffulapp.adapter.StableArrayAdapter;
import com.neffulapp.view.DynamicListView;

import org.json.JSONArray;

import java.util.ArrayList;

public class CustomListPreference extends DialogPreference {

    private Context context;
    private DynamicListView listView;
    private StableArrayAdapter adapter;
    private ArrayList<String> list = new ArrayList<String>();

    public CustomListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    @Override
    protected View onCreateDialogView() {
        adapter = new StableArrayAdapter(context, android.R.layout.simple_list_item_1, list);
        listView = new DynamicListView(context);
        return listView;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        listView.setList(list);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            JSONArray jsonArray = new JSONArray(list);
            persistString(jsonArray.toString());
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return super.onGetDefaultValue(a, index);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            try {
                JSONArray jsonArray = new JSONArray(getPersistedString(""));
                for (int i = 0; i < jsonArray.length(); i++) {
                    list.add(jsonArray.getString(i));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
