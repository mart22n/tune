package com.tune;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

/**
 * Created by mart22n on 23.09.2015.
 */
public class StartActivity extends Activity {
    private String tag;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hideTab();
        tag = getApplicationContext().getString(R.string.tag);
        setContentView(R.layout.activity_start);
        final ImageButton mikeButton = (ImageButton) findViewById(R.id.mikebutton);
        mikeButton.setOnClickListener(new ImageButton.OnClickListener() {
                                             public void onClick(View v) {
                                                 try {
                                                     Intent in = new Intent(v.getContext(), MainActivity.class);
                                                     startActivity(in);
                                                 } catch (Exception e) {
                                                     Log.i(tag, "Failed to launch MainActivity " + e.getMessage());
                                                 }
                                             }
                                         }
        );
    }

    private void hideTab() {
        // Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // set content view AFTER ABOVE sequence (to avoid crash)
    }
}
