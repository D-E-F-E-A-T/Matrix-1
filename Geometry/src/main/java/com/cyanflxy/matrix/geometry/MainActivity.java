package com.cyanflxy.matrix.geometry;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.cyanflxy.matrix.geometry.coordinate.Coordinate2D;

public class MainActivity extends Activity implements View.OnClickListener {

    private Coordinate2D coordinate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);

        coordinate = (Coordinate2D) findViewById(R.id.coordinate);

        findViewById(R.id.scale_large).setOnClickListener(this);
        findViewById(R.id.scale_small).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scale_large:
                coordinate.setNextScaleLarge();
                break;
            case R.id.scale_small:
                coordinate.setNextScaleSmall();
                break;
            default:
                break;
        }
    }
}
