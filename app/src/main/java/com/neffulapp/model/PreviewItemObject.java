package com.neffulapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;


public class PreviewItemObject implements Parcelable {

    private String photo = null;
    private String code = null;
    private String name = null;
    private String remark = null;
    private int subtotal = 0;
    private int labor = 0;
    private List<String> stackList = new ArrayList<String>();

    public PreviewItemObject() {
    }

    private PreviewItemObject(Parcel in) {
        code = in.readString();
        name = in.readString();
        remark = in.readString();
        labor = in.readInt();
        subtotal = in.readInt();
        in.readStringList(stackList);
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(int subtotal) {
        this.subtotal = subtotal;
    }

    public int getLabor() {
        return labor;
    }

    public void setLabor(int labor) {
        this.labor = labor;
    }

    public List<String> getStackList() {
        return stackList;
    }

    public void addToList(String text) {
        stackList.add(text);
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(code);
        dest.writeString(name);
        dest.writeString(remark);
        dest.writeInt(labor);
        dest.writeInt(subtotal);
        dest.writeStringList(stackList);
    }

    public static final Parcelable.Creator<PreviewItemObject> CREATOR = new Parcelable.Creator<PreviewItemObject>() {

        public PreviewItemObject createFromParcel(Parcel in) {
            return new PreviewItemObject(in);
        }

        public PreviewItemObject[] newArray(int size) {
            return new PreviewItemObject[size];
        }
    };
}