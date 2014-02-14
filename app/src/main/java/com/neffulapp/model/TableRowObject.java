package com.neffulapp.model;

import java.util.List;

public class TableRowObject {

    private List<String> sizes = null;
    private List<String> colors = null;
    private int price = 0;
    private int labor = 0;

    public TableRowObject(List<String> sizes, List<String> colors, int price, int labor) {
        this.sizes = sizes;
        this.colors = colors;
        this.price = price;
        this.labor = labor;
    }

    public List<String> getSizes() {
        return sizes;
    }

    public List<String> getColors() {
        return colors;
    }

    public int getPrice() {
        return price;
    }

    public int getLabor() {
        return labor;
    }
}
