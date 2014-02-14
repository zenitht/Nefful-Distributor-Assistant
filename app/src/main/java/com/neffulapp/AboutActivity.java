package com.neffulapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class AboutActivity extends Activity {

    private static final String URL_DONATIONS = "http://neffulapp.net76.net/donations";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView versionTxt = (TextView) findViewById(R.id.version);
        TextView licenseTxt = (TextView) findViewById(R.id.license);
        TextView librariesTxt = (TextView) findViewById(R.id.libraries_content);

        librariesTxt.setMovementMethod(LinkMovementMethod.getInstance());
        versionTxt.setMovementMethod(LinkMovementMethod.getInstance());
        licenseTxt.setMovementMethod(LinkMovementMethod.getInstance());

        ImageButton donateBtn = (ImageButton) findViewById(R.id.btn_donate);
        donateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent donateIntent = new Intent(getApplicationContext(), WebViewActivity.class);
                donateIntent.putExtra(getPackageName() + ".WebView", URL_DONATIONS);
                donateIntent.putExtra("Title", getString(R.string.title_activity_webview_donation));
                startActivity(donateIntent);
            }
        });
    }
}
