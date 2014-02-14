package com.neffulapp.model;

import java.util.ArrayList;
import java.util.List;

public class CatalogueItemObject {

    String code = null;
    String name = null;
    String photo = null;
    List<String> stackList = new ArrayList<String>();

    public CatalogueItemObject(String code, String name, String photo) {
        this.code = code;
        this.name = name;
        this.photo = photo;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public List<String> getStackList() {
        return stackList;
    }

    public void setStackList(List<String> stackList) {
        this.stackList = stackList;
    }
}
