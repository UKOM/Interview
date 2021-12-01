package com.demo.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class FourPointView extends View {
    private static final String TAG = "FourPointView";

    private Paint pointPaint;
    private Paint linePaint;

    private Paint textPaint;

    public FourPointView(Context context) {
        this(context, null);
    }

    public FourPointView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        pointPaint = new Paint();
        pointPaint.setColor(Color.RED);
        pointPaint.setStyle(Paint.Style.FILL);

        linePaint = new Paint();
        linePaint.setColor(Color.GREEN);
        linePaint.setStrokeWidth(2);

        textPaint = new Paint();
        textPaint.setColor(Color.GRAY);
        textPaint.setStrokeWidth(3);
    }

    private List<PointF> generatePoints() {
        int maxX = getWidth();
        int maxY = getHeight();
        Random random = new Random();
        List<PointF> points = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            points.add(new PointF(maxX * random.nextFloat(), maxY * random.nextFloat()));
        }
        return points;
    }

    //判断两条线段是否相交
    private boolean isLineSegmentsIntersect(Pair<PointF, PointF> line1, Pair<PointF, PointF> line2) {
        PointF midVector = new PointF(line1.second.x - line1.first.x, line1.second.y - line1.first.y);
        PointF leftVector = new PointF(line2.first.x - line1.first.x, line2.first.y - line1.first.y);
        PointF rightVector = new PointF(line2.second.x - line1.first.x, line2.second.y - line1.first.y);

        float value1 = calculateVectorsProduct(midVector, leftVector);
        float value2 = calculateVectorsProduct(midVector, rightVector);
        return value1 * value2 < 0;
    }

    //判断一个点是否在另外三个点构成的三角形内
    private boolean isPointInTriangle(PointF point, List<PointF> triangle) {
        assert triangle.size() == 3;

        float lastValue = 0f;
        float value;

        PointF vector1 = new PointF();
        PointF vector2 = new PointF();

        for (int i = 0; i < 3; i++) {
            PointF start = triangle.get(i);
            PointF end = triangle.get((i == 2) ? 0 : (i + 1));

            vector1.x = end.x - start.x;
            vector1.y = end.y - start.y;
            vector2.x = point.x - start.x;
            vector2.y = point.y - start.y;

            value = calculateVectorsProduct(vector1, vector2);

            //向量叉乘不同号，则不在三角形内
            if (i != 0 && lastValue * value < 0) return false;

            lastValue = value;
        }

        return true;
    }

    //计算向量叉乘
    private float calculateVectorsProduct(PointF vector1, PointF vector2) {
        return vector1.x * vector2.y - vector1.y * vector2.x;
    }

    //获取线段中点坐标
    private PointF getMidpoint(Pair<PointF, PointF> line) {
        return getMidpoint(line.first, line.second);
    }

    private PointF getMidpoint(PointF first, PointF second) {
        return new PointF((first.x + second.x) / 2, (first.y + second.y) / 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        Log.i(TAG, "onDraw: ");
        List<PointF> points = generatePoints();
        for (int i = 0; i < 4; i++) {
            PointF point = points.get(i);
            canvas.drawCircle(point.x, point.y, 4, pointPaint);
            canvas.drawText(Integer.toString(i), point.x, point.y + 25, textPaint);
        }

        //判断是否凹四边形
        for (int i = 0; i < 3; i++) {
            ArrayList<PointF> triangle = new ArrayList<>(points);
            triangle.remove(i);
            PointF point = points.get(i);
            if (isPointInTriangle(point, triangle)) {
                PointF midpoint1 = getMidpoint(point, triangle.get(0));
                PointF midpoint2 = getMidpoint(point, triangle.get(1));
                PointF midpoint3 = getMidpoint(point, triangle.get(2));
                Log.d(TAG, "onDraw: 凹四边形");

//                canvas.drawLine(midpoint1.x, midpoint1.y, midpoint2.x, midpoint2.y, linePaint);
//                canvas.drawLine(midpoint1.x, midpoint1.y, midpoint3.x, midpoint3.y, linePaint);
                drawPointsAndExtendedLine(midpoint1, midpoint2, 100, canvas);
                drawPointsAndExtendedLine(midpoint1, midpoint3, 100, canvas);
                return;
            }
        }

        Pair<PointF, PointF> line1 = new Pair<>(points.get(0), points.get(1));
        Pair<PointF, PointF> line2 = new Pair<>(points.get(2), points.get(3));
        if (isLineSegmentsIntersect(line1, line2)) {
            Log.d(TAG, "onDraw: 01 与 23 相交");
            line1 = new Pair<>(points.get(0), points.get(2));
            line2 = new Pair<>(points.get(1), points.get(3));
        }
        drawPointsAndExtendedLine(getMidpoint(line1), getMidpoint(line2), 100, canvas);

        Pair<PointF, PointF> newLine1 = new Pair<>(line1.first, line2.first);
        Pair<PointF, PointF> newLine2 = new Pair<>(line1.second, line2.second);
        if (isLineSegmentsIntersect(newLine1, newLine2)) {
            Log.d(TAG, "onDraw: 线段1 与 线段2 的起点连线 与 终点连线相交");
            newLine1 = new Pair<>(line1.first, line2.second);
            newLine2 = new Pair<>(line1.second, line2.first);
        }
        drawPointsAndExtendedLine(getMidpoint(newLine1), getMidpoint(newLine2), 100, canvas);
    }

    //绘制点及连线的延长线
    private void drawPointsAndExtendedLine(PointF start, PointF end, int factor, Canvas canvas){
        canvas.drawCircle(start.x, start.y, 4, linePaint);
        canvas.drawCircle(end.x, end.y, 4, linePaint);

        float startExtendedX = (start.x - end.x) * factor + start.x;
        float startExtendedY = (start.y - end.y) * factor + start.y;

        float endExtendedX = (end.x - start.x) * factor + end.x;
        float endExtendedY = (end.y - start.y) * factor + end.y;

        canvas.drawLine(startExtendedX, startExtendedY, endExtendedX, endExtendedY, linePaint);
    }
}
