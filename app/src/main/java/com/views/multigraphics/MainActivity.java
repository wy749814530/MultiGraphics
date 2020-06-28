package com.views.multigraphics;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void gotoAreaActivity(View view) {
        startActivity(new Intent(this, AreaActivity.class));
    }

    public void gotoMutilAreaActivity(View view) {
        startActivity(new Intent(this, MutilAreaActivity.class));
    }
}
