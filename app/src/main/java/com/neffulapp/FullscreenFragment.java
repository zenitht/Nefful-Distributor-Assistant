package com.neffulapp;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.neffulapp.helper.BitmapProcessor;

public class FullscreenFragment extends Fragment {

    private ImageView productPhoto;

    public static FullscreenFragment newInstance(String photo) {
        FullscreenFragment f = new FullscreenFragment();
        Bundle args = new Bundle();
        args.putString("Photo", photo);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        productPhoto = new ImageView(getActivity());
        return productPhoto;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();
        Resources res = activity.getResources();
        String photo = getArguments().getString("Photo");
        int resId = res.getIdentifier(photo, "drawable", activity.getPackageName());

        if (resId == 0) {
            String path = getActivity().getFilesDir().getPath() + "/" + photo + ".png";
            BitmapProcessor bitmapProcessor = new BitmapProcessor(activity);
            bitmapProcessor.initTask(path, productPhoto, false);
        } else {
            productPhoto.setImageResource(resId);
        }
    }
}
