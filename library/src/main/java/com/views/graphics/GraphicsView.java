package com.views.graphics;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;


/**
 * Created by Administrator on 2020/3/10 0010.
 */

public class GraphicsView extends View implements View.OnTouchListener {
    String TAG = GraphicsView.class.getSimpleName();

    public enum TOUCH_ACTION {
        ADD_POINT,
        DEL_POINT,
        DRAG_POINT,
        DRAG_GRAPHICS,
    }


    Paint mPaintText;     // 坐标点数字
    int mPaintTextColor = 0xff13c6ed; // 线与数字的颜色
    Paint mPaintPoint;    // 坐标点
    Paint mPaintBg;       // 当前选中的点
    int mPaintBgColor = 0xffffffff; // 选中点背景色
    Paint mPaintCurrent; // 当前选中的点
    Paint mPaintLine;    // 底层网格线
    int mPaintLineColor = 0xff999999; // 底层网格线颜色
    // 交叉线颜色
    int mCrosslineColor = Color.RED;

    private TOUCH_ACTION mAction = TOUCH_ACTION.ADD_POINT;
    private int spacing = 30;
    private int widthPixels;
    private int heightPixels;
    private int PRECISION = 100;
    private int MAX_POINT_COUNT = 8; // 最多显示多少个点
    private boolean selectedDragPoint = true;
    private boolean mDottedLine = true;// 是否显示虚线
    private boolean hadIntersect = false;
    private boolean mShowBgTable = true; //是否显示底层表格线
    // 矩形区域位置
    ArrayList<PointBean> paintingArea = new ArrayList<>();
    // 图形区域点
    ArrayList<PointBean> pointBeans = new ArrayList<>();

    public GraphicsView(Context context) {
        super(context);
        initPoint(context, null);
    }

    public GraphicsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPoint(context, attrs);
    }

    public GraphicsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPoint(context, attrs);
    }

    private void initPoint(Context context, AttributeSet attrs) {

        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.GraphicsView);
            mPaintTextColor = typedArray.getColor(R.styleable.GraphicsView_gv_lineColor, mPaintTextColor);
            mCrosslineColor = typedArray.getColor(R.styleable.GraphicsView_gv_crossLineColor, mCrosslineColor);
            mPaintLineColor = typedArray.getResourceId(R.styleable.GraphicsView_gv_tableLineColor, mPaintLineColor);
            MAX_POINT_COUNT = typedArray.getInteger(R.styleable.GraphicsView_gv_maxPoint, 8);
            mShowBgTable = typedArray.getBoolean(R.styleable.GraphicsView_gv_showTable, true);
        }

        mPaintText = new Paint();
        mPaintText.setStrokeWidth(2);
        mPaintText.setAntiAlias(true);
        mPaintText.setTextSize(25);
        mPaintText.setStyle(Paint.Style.FILL);
        mPaintText.setColor(mPaintTextColor);

        mPaintLine = new Paint();
        mPaintLine.setStrokeWidth(0.5f);
        mPaintLine.setAntiAlias(true);
        mPaintLine.setAlpha(30);
        mPaintLine.setStyle(Paint.Style.FILL);
        mPaintLine.setColor(mPaintLineColor);

        mPaintBg = new Paint();
        mPaintBg.setStrokeWidth(4);
        mPaintBg.setAntiAlias(true);
        mPaintBg.setTextSize(25);
        mPaintBg.setStyle(Paint.Style.FILL);
        mPaintBg.setColor(mPaintBgColor);


        mPaintPoint = new Paint();
        mPaintPoint.setStrokeWidth(4);
        mPaintPoint.setAntiAlias(true);
        mPaintPoint.setTextSize(25);
        mPaintPoint.setStyle(Paint.Style.STROKE);//设置空心
        mPaintPoint.setColor(mPaintTextColor);

        mPaintCurrent = new Paint();
        mPaintCurrent.setStrokeWidth(4);
        mPaintCurrent.setAntiAlias(true);
        mPaintCurrent.setTextSize(25);
        mPaintCurrent.setStyle(Paint.Style.FILL);//设置空心
        mPaintCurrent.setColor(mPaintTextColor);

        setOnTouchListener(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        widthPixels = getMeasuredWidth();
        heightPixels = getMeasuredHeight();
    }

    //重写绘图方法
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        if (mShowBgTable) {
            drawBgGridlines(canvas); // 画网格线
        }
        drawCloseGraphics(canvas);// 画封闭图形填充区域
        drawCloseLines(canvas);// 画封闭图形外线
        drawPoints(canvas);// 画对应点的数字框
    }


    /**
     * 画底部网格线
     *
     * @param canvas
     */
    public void drawBgGridlines(Canvas canvas) {
        int horizontalLine = heightPixels / spacing;
        for (int i = 0; i < horizontalLine; i++) {
            canvas.drawLine(0, i * spacing, widthPixels, i * spacing, mPaintLine);
        }

        int verticalLineCum = widthPixels / spacing;
        for (int i = 0; i < verticalLineCum; i++) {
            canvas.drawLine(i * spacing, 0, i * spacing, heightPixels, mPaintLine);
        }
    }

    /**
     * 画封闭图形
     *
     * @param canvas
     */
    private void drawCloseGraphics(Canvas canvas) {
        Paint paint = new Paint();//创建画笔
        paint.setAntiAlias(true);
        if (hadIntersect) {
            paint.setColor(mCrosslineColor);//为画笔设置颜色
        } else {
            paint.setColor(mPaintTextColor);//为画笔设置颜色
        }
        paint.setStrokeWidth(2);//为画笔设置粗细
        paint.setAlpha(50);
        //连接的外边缘以圆弧的方式相交
        paint.setStrokeJoin(Paint.Join.ROUND);
        //线条结束处绘制一个半圆
        paint.setStrokeCap(Paint.Cap.ROUND);

        PointBean fristPoint;
        Path path = new Path();

        for (int i = 0; i < pointBeans.size(); i++) {
            if (i == 0) {
                fristPoint = pointBeans.get(0);
                path.moveTo(fristPoint.getX(), fristPoint.getY());
            }
            path.lineTo(pointBeans.get(i).getX(), pointBeans.get(i).getY());//右下角
        }

        if (pointBeans.size() >= 3) {
            path.close();//闭合图形
        }
        canvas.drawPath(path, paint);
    }

    /**
     * 画封闭图形外边框
     *
     * @param canvas
     */
    private void drawCloseLines(Canvas canvas) {
        Paint paint = new Paint();//创建画笔
        paint.setAntiAlias(true);
        if (hadIntersect) {
            paint.setColor(mCrosslineColor);//为画笔设置颜色
        } else {
            paint.setColor(mPaintTextColor);//为画笔设置颜色
        }
        paint.setStrokeWidth(4);//为画笔设置粗细
        //paint.setAlpha(80);
        paint.setStyle(Paint.Style.STROKE);//设置空心
        //连接的外边缘以圆弧的方式相交
        paint.setStrokeJoin(Paint.Join.ROUND);
        //线条结束处绘制一个半圆
        paint.setStrokeCap(Paint.Cap.ROUND);

        PointBean fristPoint;
        Path path = new Path();
        PathEffect effects = new DashPathEffect(new float[]{18, 10, 18, 10}, 1);

        for (int i = 0; i < pointBeans.size(); i++) {
            if (i == 0) {
                fristPoint = pointBeans.get(0);
                path.moveTo(fristPoint.getX(), fristPoint.getY());
            }
            path.lineTo(pointBeans.get(i).getX(), pointBeans.get(i).getY());//右下角
        }

        if (mDottedLine) {
            paint.setPathEffect(effects);
        }

        if (pointBeans.size() >= 3) {
            path.close();//闭合图形
        }

        canvas.drawPath(path, paint);
    }

    /**
     * 画对应点数字框
     *
     * @param canvas
     */
    private void drawPoints(Canvas canvas) {
        for (int i = 0; i < pointBeans.size(); i++) {
            if (pointBeans.get(i).isCurrent()) {
                canvas.drawCircle(pointBeans.get(i).getX(), pointBeans.get(i).getY(), 20, mPaintCurrent);
            } else {
                canvas.drawCircle(pointBeans.get(i).getX(), pointBeans.get(i).getY(), 20, mPaintBg);
                canvas.drawCircle(pointBeans.get(i).getX(), pointBeans.get(i).getY(), 20, mPaintPoint);
            }

            if (pointBeans.get(i).isCurrent()) {
                mPaintText.setColor(mPaintBgColor);
                canvas.drawText("" + pointBeans.get(i).getPosition(), (float) (pointBeans.get(i).getX() - 6.5), (float) (pointBeans.get(i).getY() + 7.5), mPaintText);
            } else {
                mPaintText.setColor(mPaintTextColor);
                canvas.drawText("" + pointBeans.get(i).getPosition(), (float) (pointBeans.get(i).getX() - 6.5), (float) (pointBeans.get(i).getY() + 7.5), mPaintText);
            }
        }
    }

    int screenWidth = 0;
    int screenHeight = 0;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        screenWidth = w;
        screenHeight = h;

        paintingArea.clear();

        PointBean point1 = new PointBean(0, 0, 0);
        paintingArea.add(point1);

        PointBean point2 = new PointBean(w, 0, 1);
        paintingArea.add(point2);

        PointBean point3 = new PointBean(w, h, 2);
        paintingArea.add(point3);

        PointBean point4 = new PointBean(0, h, 3);
        paintingArea.add(point4);
    }

    //选中与手指最近的点
    public void selectPoint(float x, float y) {
        PointBean pointBean = null;
        double lDis = 0;
        for (PointBean point : pointBeans) {
            double dis = DottedLineUtil.distzj(x, point.getX(), y, point.getY());
            if (pointBean == null) {
                pointBean = point;
                lDis = dis;
            } else {
                if (dis < lDis) {
                    lDis = dis;
                    pointBean = point;
                }
            }
        }
        if (pointBeans.size() == 0 || lDis > PRECISION) {
            addNewPoint(x, y);
            return;
        }
        if (pointBean != null) {
            for (int i = 0; i < pointBeans.size(); i++) {
                if (pointBean.getPosition() == pointBeans.get(i).getPosition()) {
                    pointBeans.get(i).setCurrent(true);
                } else {
                    pointBeans.get(i).setCurrent(false);
                }
            }
        }
        mAction = TOUCH_ACTION.DRAG_POINT;
        invalidate();
    }

    // 添加新的点到图形
    public void addNewPoint(float x, float y) {
        if (pointBeans.size() >= MAX_POINT_COUNT) {
            return;
        }

        for (int i = 0; i < pointBeans.size(); i++) {
            pointBeans.get(i).setCurrent(false);
        }

        PointBean pointBean = new PointBean(x, y, pointBeans.size() + 1);
        pointBean.setCurrent(true);
//        pointBean.setPosition(pointBeans.size() + 1);
        pointBeans.add(pointBean);

        hadIntersect = checkIntersect();
        mDottedLine = true;
        invalidate();
    }

    PointBean mCurrentPoint = null;

    // 拖拽到新的点
    public void dragToNewPoint(float x, float y) {
        double lDis = 0;
        if (pointBeans.size() >= 3) {
            if (mCurrentPoint == null) {
                for (PointBean point : pointBeans) {
                    double dis = DottedLineUtil.distzj(x, point.getX(), y, point.getY());
                    if (mCurrentPoint == null) {
                        mCurrentPoint = point;
                        lDis = dis;
                    } else {
                        if (dis < lDis) {
                            lDis = dis;
                            mCurrentPoint = point;
                        }
                    }
                }

                if (lDis > PRECISION) {
                    selectedDragPoint = false;
                    mCurrentPoint = null;
                    return;
                }
            }

            if (mCurrentPoint != null) {
                for (int i = 0; i < pointBeans.size(); i++) {
                    PointBean point = pointBeans.get(i);
                    if (mCurrentPoint.getPosition() == point.getPosition()) {
                        pointBeans.get(i).setX(x);
                        pointBeans.get(i).setY(y);
                        pointBeans.get(i).setCurrent(true);
                    } else {
                        pointBeans.get(i).setCurrent(false);
                    }
                }
            }

            hadIntersect = checkIntersect();
            mDottedLine = true;
            invalidate();
        }
    }

    // 拖拽图形
    public void dragGraphics2(float currentX, float currentY) {
        float moveX = 0;
        float moveY = 0;
        if (downX != currentX) {
            moveX = currentX - downX;
        }
        if (downY != currentY) {
            moveY = currentY - downY;
        }
        for (int i = 0; i < pointBeans.size(); i++) {
            PointBean point = pointBeans.get(i);
            if ((point.getY() + moveY <= 0) || (point.getY() + moveY >= screenHeight)) {
                moveY = 0;
            }
            if ((point.getX() + moveX <= 0) || (point.getX() + moveX >= screenWidth)) {
                moveX = 0;
            }
        }
        downX = currentX;
        downY = currentY;

        if (moveX == 0 && moveY == 0) {
            return;
        }
        for (int i = 0; i < pointBeans.size(); i++) {
            PointBean point = pointBeans.get(i);
            pointBeans.get(i).setX(point.getX() + moveX);
            pointBeans.get(i).setY(point.getY() + moveY);
        }

        mDottedLine = true;
        invalidate();
    }

    // 拖拽图形移动
    public boolean dragGraphics(float moveX, float moveY) {
        for (int i = 0; i < pointBeans.size(); i++) {
            PointBean point = pointBeans.get(i);
            boolean isInPaint = isInPaintingArea(point.getX() + moveX, point.getY() + moveY);
            if (!isInPaint) {
                return false;
            }
        }

        for (int i = 0; i < pointBeans.size(); i++) {
            PointBean point = pointBeans.get(i);
            pointBeans.get(i).setX(point.getX() + moveX);
            pointBeans.get(i).setY(point.getY() + moveY);
        }

        mDottedLine = true;
        invalidate();
        return true;
    }

    /**
     * 判断是否在可画点的区域内
     *
     * @param currentX
     * @param currentY
     * @return
     */
    public boolean isInPaintingArea(float currentX, float currentY) {
        if (paintingArea.size() < 3) {
            return false;
        }
        PointBean pointBean = new PointBean(currentX, currentY, 0);
        boolean inArea = DottedLineUtil.IsPtInPoly(pointBean, paintingArea);
        return inArea;
    }

    /**
     * 判断是否在图形之内
     *
     * @param currentX
     * @param currentY
     * @return
     */
    public boolean isInGraphicsArea(float currentX, float currentY) {
        if (pointBeans.size() < 3) {
            return false;
        }
        PointBean pointBean = new PointBean(currentX, currentY, 0);
        pointBean.setX(currentX);
        pointBean.setY(currentY);
        boolean inArea = DottedLineUtil.IsPtInPoly(pointBean, pointBeans);
        return inArea;
    }

    /**
     * 是否在某点的区域内
     *
     * @param currentX
     * @param currentY
     * @return
     */
    public boolean isInPointArea(float currentX, float currentY) {
        if (pointBeans.size() == 0) {
            return false;
        }
        PointBean pointBean = null;
        double lDis = 0;
        for (PointBean point : pointBeans) {
            double dis = DottedLineUtil.distzj(currentX, point.getX(), currentY, point.getY());
            if (pointBean == null) {
                pointBean = point;
                lDis = dis;
            } else {
                if (dis < lDis) {
                    lDis = dis;
                    pointBean = point;
                }
            }
        }

        if (lDis > 0 && lDis < PRECISION) {
            return true;
        }
        return false;
    }

    /**
     * 检测是否有相交线
     *
     * @return
     */
    public boolean checkIntersect() {
        if (pointBeans.size() > 3) {
            for (int i = 0; i < pointBeans.size(); i++) {
                LineSegmentBean lineSegment = new LineSegmentBean();
                if (i == pointBeans.size() - 1) {
                    lineSegment.setP1(pointBeans.get(i));
                    lineSegment.setP2(pointBeans.get(0));
                } else {
                    lineSegment.setP1(pointBeans.get(i));
                    lineSegment.setP2(pointBeans.get(i + 1));
                }

                for (int j = i + 2; j < pointBeans.size(); j++) {
                    LineSegmentBean lineSegment2 = null;
                    if (j == pointBeans.size() - 1) {
                        if (i != 0) {
                            lineSegment2 = new LineSegmentBean();
                            lineSegment2.setP1(pointBeans.get(j));
                            lineSegment2.setP2(pointBeans.get(0));
                        }
                    } else {
                        lineSegment2 = new LineSegmentBean();
                        lineSegment2.setP1(pointBeans.get(j));
                        lineSegment2.setP2(pointBeans.get(j + 1));
                    }

                    if (lineSegment2 != null) {
                        boolean detctIntersect = DottedLineUtil.detectIntersect(lineSegment.getP1(), lineSegment.getP2(), lineSegment2.getP1(), lineSegment2.getP2());
                        if (detctIntersect) {
                            hadIntersect = true;
                            return true;
                        }
                    }
                }
            }
        }
        hadIntersect = false;
        return false;
    }

    /**
     * 是否使用虚线显示
     *
     * @param dottedLine
     */
    public void setDottedLine(boolean dottedLine) {
        mDottedLine = dottedLine;
        invalidate();
    }

    //清除图形
    public void clearGraphics() {
        pointBeans.clear();
        hadIntersect = false;
        invalidate();
    }

    //删除当前选中的点
    public void delPoint() {
        for (int i = 0; i < pointBeans.size(); i++) {
            if (pointBeans.get(i).isCurrent()) {
                pointBeans.remove(i);
                break;
            }
        }

        for (int i = 0; i < pointBeans.size(); i++) {
            pointBeans.get(i).setPosition(i + 1);
        }
        hadIntersect = checkIntersect();
        invalidate();
    }

    public void setPointBeans(ArrayList<PointBean> points) {
        pointBeans.clear();
        if (points != null && points.size() != 0) {
            pointBeans.addAll(points);
        }
        checkIntersect();
        invalidate();
    }

    public ArrayList<PointBean> getPointBeans() {
        return pointBeans;
    }

    float downX = 0, downY = 0;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            boolean inArea = isInGraphicsArea(event.getX(), event.getY());
            boolean isInPointArea = isInPointArea(event.getX(), event.getY());
            if (inArea && !isInPointArea) {
                mAction = TOUCH_ACTION.DRAG_GRAPHICS;
            } else {
                selectPoint(event.getX(), event.getY());
            }
            downX = event.getX();
            downY = event.getY();
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (mAction == TOUCH_ACTION.DRAG_POINT) {
                boolean inArea = isInPaintingArea(event.getX(), event.getY());
                Log.i(TAG, "inArea : " + inArea);
                if (selectedDragPoint && inArea) {
                    dragToNewPoint(event.getX(), event.getY());
                } else {
                    return true;
                }
            } else if (mAction == TOUCH_ACTION.DRAG_GRAPHICS) {
                float currentX = event.getX();
                float currentY = event.getY();
                dragGraphics2(currentX, currentY);
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            mAction = TOUCH_ACTION.DRAG_POINT;
            if (mAction == TOUCH_ACTION.DRAG_POINT) {
                mCurrentPoint = null;
                selectedDragPoint = true;
            }
        }
        return true;
    }

    boolean mEnabled = false;

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    public void setTouchAction(TOUCH_ACTION action) {
        mAction = action;
        if (mAction == TOUCH_ACTION.DEL_POINT) {
            for (int i = 0; i < pointBeans.size(); i++) {
                if (pointBeans.get(i).isCurrent()) {
                    pointBeans.remove(i);
                    break;
                }
            }

            for (int i = 0; i < pointBeans.size(); i++) {
                pointBeans.get(i).setPosition(i + 1);
            }
            hadIntersect = checkIntersect();
            invalidate();
        }
    }
}
