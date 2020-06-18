package com.views.multigraphics;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.views.graphics.MultiGraphicsView;

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
        graphicsView.addNewArea();
    }

    public void complateAction(View view) {
        graphicsView.setDottedLine(false);
    }
}
