package com.views.multigraphics;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.views.graphics.MultiGraphicsView;
import com.views.graphics.PointBean;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    MultiGraphicsView graphicsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        graphicsView = findViewById(R.id.graphicsView);
        graphicsView.setOnDelClickListener(new MultiGraphicsView.OnDelClickListener() {
            @Override
            public void onDelClicked() {
                Toast.makeText(MainActivity.this, "删除图形", Toast.LENGTH_LONG).show();
                graphicsView.delCurrentGraphics();
            }

            @Override
            public void onMiniArea() {
                Toast.makeText(MainActivity.this, "到达最小区域", Toast.LENGTH_LONG).show();
            }
        });

    }

    public void addAction(View view) {
        List<MultiGraphicsView.GraphicsObj> objs = graphicsView.getGraphics();
        if (objs.size() == 0) {
            addSuijiPoint();
        } else {
            graphicsView.addNewArea();
        }

    }

    public void complateAction(View view) {
        graphicsView.setDottedLine(false);
    }

    public void addSuijiPoint() {
        ArrayList<PointBean> paintingArea = new ArrayList<>();
        paintingArea.add(new PointBean(100f, 100f, 0));
        paintingArea.add(new PointBean(340f, 120f, 1));
        paintingArea.add(new PointBean(320f, 320f, 2));
        paintingArea.add(new PointBean(370f, 380f, 3));
        paintingArea.add(new PointBean(90f, 330f, 4));

        MultiGraphicsView.GraphicsObj currentArea = new MultiGraphicsView.GraphicsObj();
        currentArea.setAraa(paintingArea, true);

        Log.i("MainActivity", "addSuijiPoint : " + new Gson().toJson(currentArea));
        graphicsView.addAreaBeans(currentArea);
    }
}
