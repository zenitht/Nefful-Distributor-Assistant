/*
 * Copyright (C) 2012 Kris Wong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.neffulapp.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.text.InputFilter;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.neffulapp.R;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * A Spinner view that does not dismiss the dialog displayed when the control is "dropped down"
 * and the user presses it. This allows for the selection of more than one option.
 */
public class MultiSelectSpinner extends Spinner implements OnMultiChoiceClickListener {
    private Context context;
    private String[] item = null;
    private boolean[] selection = null;
    private int id = 0;
    private ArrayAdapter<String> proxyAdapter = null;
    private String[] singleChoiceItems = null;
    private AlertDialog alertDialog = null;

    public MultiSelectSpinner(Context context) {
        super(context);
        this.context = context;
        proxyAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item);
        super.setAdapter(proxyAdapter);
    }

    public MultiSelectSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        id = this.getId();
        proxyAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item);
        super.setAdapter(proxyAdapter);
    }

    @Override
    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        if (singleChoiceItems != null && isChecked) {
            if (Arrays.asList(singleChoiceItems).contains(item[which])) {
                Arrays.fill(selection, false);
                for (int i = 0; i < selection.length; i++) {
                    alertDialog.getListView().setItemChecked(i, false);
                }
            } else {
                for (String singleChoiceItem : singleChoiceItems) {
                    if (Arrays.asList(item).contains(singleChoiceItem)) {
                        int index = Arrays.asList(item).indexOf(singleChoiceItem);
                        selection[index] = false;
                        alertDialog.getListView().setItemChecked(index, false);
                    }
                }
            }
        }
        if (selection != null && which < selection.length) {
            selection[which] = isChecked;
            proxyAdapter.clear();

            if (buildSelectedItemString().isEmpty()) {
                switch (id) {
                    case R.id.add_extra_sizes:
                        proxyAdapter.add("[Select Size]");
                        break;
                    case R.id.add_extra_colors:
                        proxyAdapter.add("[Select Color]");
                        break;
                }
            } else {
                proxyAdapter.add(buildSelectedItemString());
            }
            setSelection(0);
        } else {
            throw new IllegalArgumentException("Argument 'which' is out of bounds.");
        }
    }

    @Override
    public boolean performClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMultiChoiceItems(item, selection, this);

        if (id == R.id.add_extra_colors) {
            LinearLayout vlinearLyt = new LinearLayout(context);
            vlinearLyt.setOrientation(LinearLayout.VERTICAL);

            View divider = new View(context);
            divider.setBackgroundResource(android.R.drawable.divider_horizontal_bright);
            divider.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 3));

            LinearLayout hlinearLyt = new LinearLayout(context);
            hlinearLyt.setOrientation(LinearLayout.HORIZONTAL);
            hlinearLyt.setWeightSum(1);

            final EditText customColorEdt = new EditText(context);
            customColorEdt.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.7f));
            customColorEdt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});
            customColorEdt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            customColorEdt.setHint("New Color");

            Button addBtn = new Button(context);
            addBtn.setText("+");
            addBtn.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.3f));
            addBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    String newColor = customColorEdt.getText().toString().trim();
                    customColorEdt.setText("");
                    // Add new color to item array
                    String[] tempItem = new String[item.length + 1];
                    System.arraycopy(item, 0, tempItem, 0, selection.length);
                    tempItem[tempItem.length - 1] = newColor;
                    item = null;
                    item = tempItem.clone();

                    // Increase the size of selection array
                    boolean[] temp = new boolean[selection.length + 1];
                    System.arraycopy(selection, 0, temp, 0, selection.length);
                    temp[temp.length - 1] = true;
                    selection = null;
                    selection = temp.clone();

                    proxyAdapter.clear();
                    proxyAdapter.add(buildSelectedItemString());
                    setSelection(0);
                }
            });

            hlinearLyt.addView(customColorEdt);
            hlinearLyt.addView(addBtn);

            vlinearLyt.addView(divider);
            vlinearLyt.addView(hlinearLyt);

            builder.setView(vlinearLyt);
        }

        alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getWindow().setLayout(500, 1100);
        return true;
    }

    @Override
    public void setAdapter(SpinnerAdapter adapter) {
        throw new RuntimeException("setAdapter is not supported by MultiSelectSpinner.");
    }

    public void setItems(String[] items) {
        item = items;
        selection = new boolean[item.length];

        Arrays.fill(selection, false);
    }

    public void setItems(List<String> items) {
        items.remove("N/A");
        items.add(0, "N/A");
        item = items.toArray(new String[items.size()]);
        selection = new boolean[item.length];

        switch (id) {
            case R.id.add_extra_sizes:
                proxyAdapter.add("[Select Size]");
                break;
            case R.id.add_extra_colors:
                proxyAdapter.add("[Select Color]");
                break;
        }

        Arrays.fill(selection, false);
    }

    public void setSelection(String[] selection) {
        for (String sel : selection) {
            for (int j = 0; j < item.length; ++j) {
                if (item[j].equals(sel)) {
                    this.selection[j] = true;
                }
            }
        }
    }

    public void setSelection(List<String> selection) {
        for (String sel : selection) {
            for (int j = 0; j < item.length; ++j) {
                if (item[j].equals(sel)) {
                    this.selection[j] = true;
                }
            }
        }
    }

    public void setSelection(int[] selectedIndicies) {
        for (int index : selectedIndicies) {
            if (index >= 0 && index < selection.length) {
                selection[index] = true;
            } else {
                throw new IllegalArgumentException("Index " + index + " is out of bounds.");
            }
        }
    }

    public List<String> getSelectedStrings() {
        List<String> selection = new LinkedList<String>();
        for (int i = 0; i < item.length; ++i) {
            if (this.selection[i]) {
                selection.add(item[i]);
            }
        }
        return selection;
    }

    public List<Integer> getSelectedIndicies() {
        List<Integer> selection = new LinkedList<Integer>();
        for (int i = 0; i < item.length; ++i) {
            if (this.selection[i]) {
                selection.add(i);
            }
        }
        return selection;
    }

    public String buildSelectedItemString() {
        StringBuilder sb = new StringBuilder();
        boolean foundOne = false;

        for (int i = 0; i < item.length; ++i) {
            if (selection[i]) {
                if (foundOne) {
                    sb.append(", ");
                }
                foundOne = true;

                sb.append(item[i]);
            }
        }

        return sb.toString();
    }

    public void setSingleChoiceItems(String[] items) {
        this.singleChoiceItems = items;
    }

    public boolean[] getSelections() {
        return selection;
    }

    public ArrayAdapter<String> getProxyAdapter() {
        return proxyAdapter;
    }
}
