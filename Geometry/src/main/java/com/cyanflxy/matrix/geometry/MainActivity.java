package com.cyanflxy.matrix.geometry;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.Window;

import com.cyanflxy.matrix.geometry.coordinate.Coordinate2D;
import com.cyanflxy.matrix.mathematics.line.Line;

public class MainActivity extends Activity implements View.OnClickListener,
        Coordinate2D.OnScaleStateChangeListener {

    private static final String STATE_COORDINATE = "coordinate";

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

        if (savedInstanceState != null) {
            Parcelable object = savedInstanceState.getParcelable(STATE_COORDINATE);
            if (object != null) {
                coordinate.setCoordinateState(object);
            }
        }

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

        Line l = Line.createByStandard(3, -1, 3);
        coordinate.addFunction(l);
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean lock = Settings.isCoordinateLock();
        lockCoordinateView.setSelected(lock);
        coordinate.setCoordinateLock(lock);

        boolean grid = Settings.isShowDashGrid();
        dashGridShowView.setSelected(grid);
        coordinate.setDashGridVisible(grid);

        scaleLargeView.setEnabled(coordinate.canScaleLarge());
        scaleSmallView.setEnabled(coordinate.canScaleSmall());
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.lock_coordinate:
                boolean lock = !v.isSelected();
                v.setSelected(lock);
                Settings.setCoordinateLock(lock);
                coordinate.setCoordinateLock(lock);
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
            case R.id.dash_grid:
                boolean grid = !v.isSelected();
                v.setSelected(grid);
                Settings.setShowDashGrid(grid);
                coordinate.setDashGridVisible(grid);
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_COORDINATE, coordinate.getCoordinateState());
    }

}
