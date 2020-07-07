package com.views.multigraphics;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.views.graphics.GraphicsView;
import com.views.graphics.MultiGraphicsView;
import com.views.graphics.PointBean;

import java.util.ArrayList;
import java.util.List;

public class AreaActivity extends AppCompatActivity {
    GraphicsView graphicsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area);

        graphicsView = findViewById(R.id.graphicsView);
    }


    public void complateAction(View view) {
        graphicsView.getPointBeans();
        graphicsView.setDottedLine(false);
    }

    public void delAction(View view) {
        graphicsView.delPoint();
    }

    public void cleanAction(View view) {
        graphicsView.clearGraphics();
    }
}
