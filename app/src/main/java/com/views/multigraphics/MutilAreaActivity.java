package com.views.multigraphics;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.views.graphics.MultiGraphicsView;
import com.views.graphics.PointBean;

import java.util.ArrayList;
import java.util.List;

public class MutilAreaActivity extends AppCompatActivity {
    MultiGraphicsView graphicsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mutil_area);

        graphicsView = findViewById(R.id.graphicsView);
        graphicsView.setOnDelClickListener(new MultiGraphicsView.OnDelClickListener() {
            @Override
            public void onDelClicked() {
                Toast.makeText(MutilAreaActivity.this, "删除图形", Toast.LENGTH_LONG).show();
                graphicsView.delCurrentGraphics();
            }

            @Override
            public void onMiniArea() {
                Toast.makeText(MutilAreaActivity.this, "到达最小区域", Toast.LENGTH_LONG).show();
            }
        });

        initData();
    }

    private float screenWidth, screenHeight;
    private float xRatio = 1, yRatio = 1;
    private float height = 1080, width = 1920;

    private void resetAreaSize() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenHeight = dm.heightPixels;
        screenWidth = dm.widthPixels;

        xRatio = screenWidth / width;
        yRatio = screenHeight / height;
    }

    private void initData() {
        resetAreaSize();
        String dataString = "{\"Region\":[[{\"x\":258,\"y\":159},{\"x\":898,\"y\":159},{\"x\":898,\"y\":519},{\"x\":258,\"y\":519}],[{\"x\":1019,\"y\":157},{\"x\":1659,\"y\":157},{\"x\":1659,\"y\":517},{\"x\":1019,\"y\":517}],[{\"x\":212,\"y\":668},{\"x\":852,\"y\":668},{\"x\":852,\"y\":1028},{\"x\":212,\"y\":1028}],[{\"x\":998,\"y\":603},{\"x\":1638,\"y\":603},{\"x\":1638,\"y\":963},{\"x\":998,\"y\":963}]],\"Enable\":true,\"RegionType\":1}";
        PointListBean pointListBean = new Gson().fromJson(dataString, PointListBean.class);
        if (pointListBean != null) {
            List<MultiGraphicsView.GraphicsObj> graphicsObjs = new ArrayList<>();

            for (List<PointListBean.RegionBean> pointBeans : pointListBean.getRegion()) {
                MultiGraphicsView.GraphicsObj graphicsObj = new MultiGraphicsView.GraphicsObj(graphicsObjs.size());
                ArrayList<PointBean> mPointBeans = new ArrayList<>();
                for (int i = 0; i < pointBeans.size(); i++) {
                    PointListBean.RegionBean pointBean = pointBeans.get(i);
                    PointBean point = new PointBean(pointBean.getX() * xRatio, pointBean.getY() * yRatio, i);
                    mPointBeans.add(point);
                }
                // 这里 第二个参数必须为false
                graphicsObj.setAraa(mPointBeans, false);
                graphicsObjs.add(graphicsObj);
            }


            graphicsView.setAreaBeans(graphicsObjs);
        }
        graphicsView.setEnabled(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                graphicsView.setEnabled(true);
            }
        }, 3 * 1000);
    }

    public void addAction(View view) {
//        List<MultiGraphicsView.GraphicsObj> objs = graphicsView.getGraphics();
//        if (objs.size() == 0) {
//            addSuijiPoint();
//        } else {
//            graphicsView.addNewArea();
//        }
        graphicsView.addNewArea();
    }

    public void complateAction(View view) {
        graphicsView.getGraphics();
        graphicsView.complete();
    }

    public void addSuijiPoint() {
        ArrayList<PointBean> paintingArea = new ArrayList<>();
        paintingArea.add(new PointBean(100f, 100f, 0));
        paintingArea.add(new PointBean(340f, 120f, 1));
        paintingArea.add(new PointBean(320f, 320f, 2));
        paintingArea.add(new PointBean(370f, 380f, 3));
        paintingArea.add(new PointBean(90f, 330f, 4));

        MultiGraphicsView.GraphicsObj currentArea = new MultiGraphicsView.GraphicsObj(graphicsView.getGraphics().size());
        currentArea.setAraa(paintingArea, false);

        Log.i("MainActivity", "addSuijiPoint : " + new Gson().toJson(currentArea));
        graphicsView.addAreaBeans(currentArea);
    }


}
