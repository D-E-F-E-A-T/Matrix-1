package com.cyanflxy.matrix.geometry;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.cyanflxy.matrix.geometry.coordinate.Coordinate2D;

public class MainActivity extends Activity implements View.OnClickListener,
        Coordinate2D.OnScaleStateChangeListener {

    private Coordinate2D coordinate;

    private View lockCoordinateView;
    private View scaleLargeView;
    private View scaleSmallView;
    private View dashGridShowView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        coordinate = (Coordinate2D) findViewById(R.id.coordinate);
        coordinate.setOnScaleStateChangeListener(this);

        lockCoordinateView = findViewById(R.id.lock_coordinate);
        scaleLargeView = findViewById(R.id.scale_large);
        scaleSmallView = findViewById(R.id.scale_small);
        dashGridShowView = findViewById(R.id.dash_grid);

        lockCoordinateView.setOnClickListener(this);
        scaleLargeView.setOnClickListener(this);
        scaleSmallView.setOnClickListener(this);
        dashGridShowView.setOnClickListener(this);

        findViewById(R.id.restore_original).setOnClickListener(this);
        findViewById(R.id.restore_scale).setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        lockCoordinateView.setSelected(coordinate.isCoordinateLocked());
        scaleLargeView.setEnabled(coordinate.canScaleLarge());
        scaleSmallView.setEnabled(coordinate.canScaleSmall());
        dashGridShowView.setSelected(coordinate.isDashGridVisible());
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.lock_coordinate: {
                boolean b = v.isSelected();
                v.setSelected(!b);
                coordinate.setCoordinateLock(!b);
            }
            break;
            case R.id.restore_original:
                coordinate.restoreOriginal();
                break;
            case R.id.restore_scale:
                coordinate.restoreScale();
                break;
            case R.id.scale_large:
                coordinate.setNextScaleLarge();
                break;
            case R.id.scale_small:
                coordinate.setNextScaleSmall();
                break;
            case R.id.dash_grid: {
                boolean b = v.isSelected();
                v.setSelected(!b);
                coordinate.setDashGridVisible(!b);
            }
            break;
            default:
                break;
        }
    }

    @Override
    public void onScaleLargeStateChange(boolean enable) {
        scaleLargeView.setEnabled(enable);
    }

    @Override
    public void onScaleSmallStateChange(boolean enable) {
        scaleSmallView.setEnabled(enable);
    }
}
