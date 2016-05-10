package com.cyanflxy.matrix.mathematics.polygons;

import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * 多边形
 * <p/>
 * Created by CyanFlxy on 2015/12/15.
 */
public abstract class Polygon {

    public abstract Path getPath();

    public Path getPath(Rect region) {
        return getPath(region.left, region.top, region.right, region.bottom);
    }

    public Path getPath(RectF region) {
        return getPath(region.left, region.top, region.right, region.bottom);
    }

    public abstract Path getPath(int left, int top, int right, int bottom);

    public abstract Path getPath(float left, float top, float right, float bottom);

}
