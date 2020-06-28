package com.views.graphics;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import java.util.List;


/**
 * Created by Administrator on 2020/3/10 0010.
 */

public class MultiGraphicsView extends View implements View.OnTouchListener {
    String TAG = MultiGraphicsView.class.getSimpleName();
    private TOUCH_ACTION mAction = TOUCH_ACTION.NULL;

    public enum TOUCH_ACTION {
        DRAG_POINT,
        DRAG_GRAPHICS,
        DRAG_LINE,
        NULL,
    }

    private ORIENTATION orientation = ORIENTATION.NULL;

    public enum ORIENTATION {
        HORIZONTAL,
        VERTICAL,
        NULL
    }


    Paint mSelectPaint;         // 选中区域内画笔
    Paint mSelectLinePaint;    // 选中区域外边框画笔
    int mSelectPaintColor = 0xff13c6ed;
    int mGeneralPaintColor = 0xff13c6ed;


    Paint mPaintLine;    // 底层网格线
    int mPaintLineColor = 0xff999999;

    Paint mCurrentPointPaint; //
    int mCurrentPointColor = 0xffB0E11E;


    private int spacing = 30;
    private int widthPixels;
    private int heightPixels;
    private int PRECISION = 100;              // 点容错区间
    private int MAX_AREA_COUNT = 4;          // 最多显示多少个点
    private boolean mDottedLine = false;    // 是否显示虚线
    private boolean hadIntersect = false;   // 是否有交叉线
    private int delImageResourceId = 0;
    private boolean mShowPoint;
    private boolean mShowTable;

    private OnDelClickListener Mlistener;
    //最大图形框
    ArrayList<PointBean> MaxArea = new ArrayList<>();
    // 矩形区域位置
    List<GraphicsObj> mAreas = new ArrayList<>();
    // 图形区域点
    GraphicsObj currentArea;
    // 当点在线上时，一下是当前线上的点
    PointBean currentP1, currentP2;
    PointBean currentQ;

    public MultiGraphicsView(Context context) {
        super(context);
        initPoint(context, null);
    }

    public MultiGraphicsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPoint(context, attrs);
    }

    public MultiGraphicsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPoint(context, attrs);
    }

    public void setOnDelClickListener(OnDelClickListener listener) {
        this.Mlistener = listener;
    }

    private void initPoint(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MultiGraphicsView);
            mSelectPaintColor = typedArray.getColor(R.styleable.MultiGraphicsView_multi_selectColor, mSelectPaintColor);
            mGeneralPaintColor = typedArray.getColor(R.styleable.MultiGraphicsView_multi_generalColor, mGeneralPaintColor);
            delImageResourceId = typedArray.getResourceId(R.styleable.MultiGraphicsView_multi_delImage, delImageResourceId);
            PRECISION = typedArray.getDimensionPixelSize(R.styleable.MultiGraphicsView_multi_miniSpacing, PRECISION);
            mShowPoint = typedArray.getBoolean(R.styleable.MultiGraphicsView_multi_showPoint, false);
            mShowTable = typedArray.getBoolean(R.styleable.MultiGraphicsView_multi_showTable, false);
            MAX_AREA_COUNT = typedArray.getInteger(R.styleable.MultiGraphicsView_multi_max, 4);
        }

        mSelectPaint = new Paint();
        mSelectPaint.setStrokeWidth(2);
        mSelectPaint.setAntiAlias(true);
        mSelectPaint.setStyle(Paint.Style.FILL);
        mSelectPaint.setAlpha(50);
        mSelectPaint.setColor(mGeneralPaintColor);
        mSelectPaint.setStrokeWidth(2);//为画笔设置粗细
        mSelectPaint.setStrokeJoin(Paint.Join.ROUND);
        mSelectPaint.setStrokeCap(Paint.Cap.ROUND);


        mSelectLinePaint = new Paint();
        mSelectLinePaint.setStrokeWidth(2);
        mSelectLinePaint.setAntiAlias(true);
        mSelectLinePaint.setStyle(Paint.Style.STROKE);
        mSelectLinePaint.setColor(mGeneralPaintColor);
        mSelectLinePaint.setStrokeWidth(2);//为画笔设置粗细
        mSelectLinePaint.setStrokeJoin(Paint.Join.ROUND);
        mSelectLinePaint.setStrokeCap(Paint.Cap.ROUND);

        mPaintLine = new Paint();
        mPaintLine.setStrokeWidth(0.5f);
        mPaintLine.setAntiAlias(true);
        mPaintLine.setAlpha(30);
        mPaintLine.setStyle(Paint.Style.FILL);
        mPaintLine.setColor(mPaintLineColor);


        mCurrentPointPaint = new Paint();
        mCurrentPointPaint.setStrokeWidth(2);
        mCurrentPointPaint.setAntiAlias(true);
        mCurrentPointPaint.setTextSize(25);
        mCurrentPointPaint.setStyle(Paint.Style.FILL);
        mCurrentPointPaint.setColor(mCurrentPointColor);


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

        if (mShowTable) {
            drawBgGridlines(canvas); // 画网格线
        }

        for (GraphicsObj area : mAreas) {
            Log.i(TAG, "== onDraw GraphicsObj:" + area.toString());
            if (area.isSelect) {
                mSelectPaint.setColor(mSelectPaintColor);
                mSelectLinePaint.setColor(mSelectPaintColor);
            } else {
                mSelectPaint.setColor(mGeneralPaintColor);
                mSelectLinePaint.setColor(mGeneralPaintColor);
            }
            drawCloseGraphics(canvas, area.getAraa());// 画封闭图形填充区域
            drawCloseLines(canvas, area.getAraa());// 画封闭图形外线
            drawDelPoint(canvas, area.getAraa());// 画对应点的数字框
        }
        // 画当前点位置
        if (mShowPoint) {
            drawCurrentPoint(canvas);
        }
    }

    /**
     * 画底部网格线
     *
     * @param canvas
     */
    private void drawBgGridlines(Canvas canvas) {
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
     * @param area
     */
    private void drawCloseGraphics(Canvas canvas, List<PointBean> area) {
        if (hadIntersect) {
            mSelectPaint.setColor(Color.RED);//为画笔设置颜色
        }
        mSelectPaint.setAlpha(50);
        PointBean fristPoint;
        Path path = new Path();
        for (int i = 0; i < area.size(); i++) {
            if (i == 0) {
                fristPoint = area.get(0);
                path.moveTo(fristPoint.getX(), fristPoint.getY());
            }
            path.lineTo(area.get(i).getX(), area.get(i).getY());//右下角
        }
        if (area.size() >= 3) {
            path.close();//闭合图形
        }
        canvas.drawPath(path, mSelectPaint);
    }

    /**
     * 画封闭图形外边框
     *
     * @param canvas
     * @param area
     */
    private void drawCloseLines(Canvas canvas, List<PointBean> area) {
        if (hadIntersect) {
            mSelectLinePaint.setColor(Color.RED);//为画笔设置颜色
        }

        PointBean fristPoint;
        Path path = new Path();
        PathEffect effects = new DashPathEffect(new float[]{18, 10, 18, 10}, 1);

        for (int i = 0; i < area.size(); i++) {
            if (i == 0) {
                fristPoint = area.get(0);
                path.moveTo(fristPoint.getX(), fristPoint.getY());
            }
            path.lineTo(area.get(i).getX(), area.get(i).getY());//右下角
        }

        if (mDottedLine) {
            mSelectLinePaint.setPathEffect(effects);
        } else {
            mSelectLinePaint.setPathEffect(null);
        }

        if (area.size() >= 3) {
            path.close();//闭合图形
        }

        canvas.drawPath(path, mSelectLinePaint);
    }

    /**
     * 画对应删除点
     *
     * @param canvas
     * @param area
     */
    private int delArea = 30;

    private void drawDelPoint(Canvas canvas, List<PointBean> area) {
        float minY = -100;
        int minPostion = 0;
        for (int i = 0; i < area.size(); i++) {
            PointBean point = area.get(i);
            if (minY == -100) {
                minPostion = i;
                minY = point.getY();
            } else {
                if (point.getY() <= minY) {
                    minPostion = i;
                    minY = point.getY();
                }
            }

        }
        if (area.size() >= 3) {
            PointBean point = area.get(minPostion);
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.del_icon);
            if (delImageResourceId != 0) {
                BitmapFactory.decodeResource(getResources(), delImageResourceId);
            }
            Bitmap dstbmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), null, true);
            float px = point.getX() - bitmap.getWidth() / 2;
            float py = point.getY() - bitmap.getHeight() / 2;
            delArea = bitmap.getWidth() / 2;
            canvas.drawBitmap(dstbmp, px, py, null);
        }
    }

    /**
     * 画当前点击点
     *
     * @param canvas
     */
    private void drawCurrentPoint(Canvas canvas) {
        if (currentQ != null) {
            canvas.drawLine(currentQ.getX() - 30, currentQ.getY(), currentQ.getX() + 30, currentQ.getY(), mCurrentPointPaint);
            canvas.drawLine(currentQ.getX(), currentQ.getY() - 30, currentQ.getX(), currentQ.getY() + 30, mCurrentPointPaint);
        }
    }


    int screenWidth = 0;
    int screenHeight = 0;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        screenWidth = w;
        screenHeight = h;

        MaxArea.clear();
        MaxArea.add(new PointBean(0, 0, 0));
        MaxArea.add(new PointBean(w, 0, 1));
        MaxArea.add(new PointBean(w, h, 2));
        MaxArea.add(new PointBean(0, h, 3));
    }


    PointBean mCurrentPoint = null;

    // 拖拽到新的点
    private void dragToNewPoint(float x, float y) {
        if (currentArea == null || currentArea.getAraa() == null || currentArea.getAraa().size() < 3) {
            return;
        }
        double lDis = 0;
        if (currentArea.getAraa().size() >= 3) {
            if (mCurrentPoint == null) {
                for (PointBean point : currentArea.getAraa()) {
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
                    mCurrentPoint = null;
                    return;
                }
            }

            if (currentArea.getAraa().size() == 4) {
                if (mCurrentPoint != null) {
                    if (mCurrentPoint.getPosition() == 0) {
                        double dis1 = DottedLineUtil.distzj(x, currentArea.getAraa().get(1).getX(), y, y);
                        double dis2 = DottedLineUtil.distzj(x, x, y, currentArea.getAraa().get(3).getY());
                        if (dis1 < 100 || dis2 < 100) {
                            if (Mlistener != null) {
                                Mlistener.onMiniArea();
                            }
                            return;
                        }
                        currentArea.getAraa().get(0).setX(x);
                        currentArea.getAraa().get(0).setY(y);
                        currentArea.getAraa().get(1).setY(y);
                        currentArea.getAraa().get(3).setX(x);
                    } else if (mCurrentPoint.getPosition() == 1) {
                        double dis1 = DottedLineUtil.distzj(x, currentArea.getAraa().get(0).getX(), y, y);
                        double dis2 = DottedLineUtil.distzj(x, x, y, currentArea.getAraa().get(2).getY());
                        if (dis1 < 100 || dis2 < 100) {
                            if (Mlistener != null) {
                                Mlistener.onMiniArea();
                            }
                            return;
                        }

                        currentArea.getAraa().get(0).setY(y);
                        currentArea.getAraa().get(1).setX(x);
                        currentArea.getAraa().get(1).setY(y);
                        currentArea.getAraa().get(2).setX(x);
                    } else if (mCurrentPoint.getPosition() == 2) {
                        double dis1 = DottedLineUtil.distzj(x, x, currentArea.getAraa().get(1).getY(), y);
                        double dis2 = DottedLineUtil.distzj(x, currentArea.getAraa().get(3).getX(), y, y);
                        if (dis1 < 100 || dis2 < 100) {
                            if (Mlistener != null) {
                                Mlistener.onMiniArea();
                            }
                            return;
                        }

                        currentArea.getAraa().get(1).setX(x);
                        currentArea.getAraa().get(2).setX(x);
                        currentArea.getAraa().get(2).setY(y);
                        currentArea.getAraa().get(3).setY(y);
                    } else if (mCurrentPoint.getPosition() == 3) {
                        double dis1 = DottedLineUtil.distzj(x, x, currentArea.getAraa().get(0).getY(), y);
                        double dis2 = DottedLineUtil.distzj(x, currentArea.getAraa().get(2).getX(), y, y);
                        if (dis1 < 100 || dis2 < 100) {
                            if (Mlistener != null) {
                                Mlistener.onMiniArea();
                            }
                            return;
                        }

                        currentArea.getAraa().get(0).setX(x);
                        currentArea.getAraa().get(3).setX(x);
                        currentArea.getAraa().get(3).setY(y);
                        currentArea.getAraa().get(2).setY(y);
                    }
                }
            } else {
                currentArea.getAraa().get(mCurrentPoint.getPosition()).setX(x);
                currentArea.getAraa().get(mCurrentPoint.getPosition()).setY(y);
            }
            hadIntersect = checkIntersect();
            mDottedLine = true;
            invalidate();
        }
    }

    // 拖拽图形
    private void dragGraphics2(float currentX, float currentY) {
        if (currentArea == null || currentArea.getAraa() == null) {
            return;
        }
        float moveX = 0;
        float moveY = 0;
        if (downX != currentX) {
            moveX = currentX - downX;
        }
        if (downY != currentY) {
            moveY = currentY - downY;
        }
        for (int i = 0; i < currentArea.getAraa().size(); i++) {
            PointBean point = currentArea.getAraa().get(i);
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
        for (int i = 0; i < currentArea.getAraa().size(); i++) {
            PointBean point = currentArea.getAraa().get(i);
            currentArea.getAraa().get(i).setX(point.getX() + moveX);
            currentArea.getAraa().get(i).setY(point.getY() + moveY);
        }

        mDottedLine = true;
        invalidate();
    }

    /**
     * 判断是否在可画点的区域内
     *
     * @param currentX
     * @param currentY
     * @return
     */
    private boolean isInPaintingArea(float currentX, float currentY) {
        if (MaxArea.size() < 3) {
            return false;
        }
        boolean inArea = DottedLineUtil.IsPtInPoly(new PointBean(currentX, currentY, 0), MaxArea);
        return inArea;
    }

    /**
     * 判断是否在图形之内
     *
     * @param currentX
     * @param currentY
     * @return
     */
    private boolean isInGraphicsArea(float currentX, float currentY) {
        if (currentArea == null || currentArea.getAraa() == null || currentArea.getAraa().size() < 3) {
            return false;
        }
        boolean inArea = DottedLineUtil.IsPtInPoly(new PointBean(currentX, currentY, 0), currentArea.getAraa());
        return inArea;
    }

    /**
     * 判断点是否在线上
     *
     * @param currentX
     * @param currentY
     * @return
     */
    private boolean pointIsOnline(float currentX, float currentY) {
        if (currentArea == null || currentArea.getAraa() == null || currentArea.getAraa().size() < 3) {
            return false;
        }
        orientation = ORIENTATION.NULL;
        currentP1 = null;
        currentP2 = null;
        for (int i = 0; i < currentArea.getAraa().size(); i++) {
            boolean isOnLine = false;
            PointBean p1, p2;
            if (i < currentArea.getAraa().size() - 1) {
                p1 = currentArea.getAraa().get(i);
                p2 = currentArea.getAraa().get(i + 1);
                isOnLine = DottedLineUtil.pointOnline(new PointBean(currentX, currentY, 0), p1, p2, 40);
            } else {
                p1 = currentArea.getAraa().get(currentArea.getAraa().size() - 1);
                p2 = currentArea.getAraa().get(0);
                isOnLine = DottedLineUtil.pointOnline(new PointBean(currentX, currentY, 0), p1, p2, 40);
            }
            if (isOnLine) {
                currentP1 = p1;
                currentP2 = p2;
                if (currentP1.getX() == currentP2.getX()) {
                    orientation = ORIENTATION.HORIZONTAL;
                } else {
                    orientation = ORIENTATION.VERTICAL;
                }
                return true;
            } else {

            }
        }
        return false;
    }

    /**
     * 是否在某点的区域内
     *
     * @param currentX
     * @param currentY
     * @return
     */
    private boolean isInPointArea(float currentX, float currentY) {
        if (currentArea == null || currentArea.getAraa() == null || currentArea.getAraa().size() < 3) {
            return false;
        }
        PointBean pointBean = null;
        double lDis = 0;
        for (PointBean point : currentArea.getAraa()) {
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

        if (lDis > 0 && lDis < PRECISION && pointBean != null) {
            return true;
        }
        return false;
    }

    /**
     * 是否在点击事件区域
     *
     * @param x
     * @param y
     * @return
     */
    private boolean isInClickArea(float x, float y) {
        if (currentArea == null || currentArea.getAraa() == null || currentArea.getAraa().size() < 3) {
            return false;
        }

        PointBean pointBean = currentArea.getClickPoint();
        if (x <= (pointBean.getX() + delArea) && x >= (pointBean.getX() - delArea) && y <= (pointBean.getY() + delArea) && y >= (pointBean.getY() - delArea)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 检测是否有相交线
     *
     * @return
     */
    public boolean checkIntersect() {
        if (currentArea == null || currentArea.getAraa() == null) {
            return false;
        }
        if (currentArea.getAraa().size() > 3) {
            for (int i = 0; i < currentArea.getAraa().size(); i++) {
                LineSegmentBean lineSegment = new LineSegmentBean();
                if (i == currentArea.getAraa().size() - 1) {
                    lineSegment.setP1(currentArea.getAraa().get(i));
                    lineSegment.setP2(currentArea.getAraa().get(0));
                } else {
                    lineSegment.setP1(currentArea.getAraa().get(i));
                    lineSegment.setP2(currentArea.getAraa().get(i + 1));
                }

                for (int j = i + 2; j < currentArea.getAraa().size(); j++) {
                    LineSegmentBean lineSegment2 = null;
                    if (j == currentArea.getAraa().size() - 1) {
                        if (i != 0) {
                            lineSegment2 = new LineSegmentBean();
                            lineSegment2.setP1(currentArea.getAraa().get(j));
                            lineSegment2.setP2(currentArea.getAraa().get(0));
                        }
                    } else {
                        lineSegment2 = new LineSegmentBean();
                        lineSegment2.setP1(currentArea.getAraa().get(j));
                        lineSegment2.setP2(currentArea.getAraa().get(j + 1));
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

    float downX = 0, downY = 0;
    boolean inClickArea = false;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            //TODO 轮训所有图形，自行判断当前手势是针对哪个图形的操作
            selectArea(event.getX(), event.getY());
            // 在点上
            boolean inPoint = isInPointArea(event.getX(), event.getY());
            // 在图形内
            boolean inArea = isInGraphicsArea(event.getX(), event.getY());
            //点是否在线上
            boolean isOnline = pointIsOnline(event.getX(), event.getY());
            // 是否在点击事件区域
            inClickArea = isInClickArea(event.getX(), event.getY());

            if (inPoint) {
                mAction = TOUCH_ACTION.DRAG_POINT;
            } else if (isOnline) {
                mAction = TOUCH_ACTION.DRAG_LINE;
            } else if (inArea) {
                mAction = TOUCH_ACTION.DRAG_GRAPHICS;
            } else {
                // 不在点上，不在图形内，也不再线上。 啥也不干
                mAction = TOUCH_ACTION.NULL;
            }
            downX = event.getX();
            downY = event.getY();
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            float currentX = event.getX();
            float currentY = event.getY();
            currentQ = new PointBean(currentX, currentY, 0);
            if (Math.abs(currentX - downX) > 10 || Math.abs(currentY - downY) > 10) {
                inClickArea = false;
                if (mAction == TOUCH_ACTION.DRAG_POINT) {
                    boolean inArea = isInPaintingArea(currentX, currentY);
                    if (inArea) {
                        dragToNewPoint(event.getX(), event.getY());
                    } else {
                        return true;
                    }
                } else if (mAction == TOUCH_ACTION.DRAG_GRAPHICS) {
                    dragGraphics2(currentX, currentY);
                } else if (mAction == TOUCH_ACTION.DRAG_LINE) {
                    dragLine(currentX, currentY);
                }
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            mAction = TOUCH_ACTION.NULL;
            orientation = ORIENTATION.NULL;
            currentP1 = null;
            currentP2 = null;
            currentQ = null;
            mCurrentPoint = null;
            if (inClickArea) {
                if (Mlistener != null) {
                    Mlistener.onDelClicked();
                }
                inClickArea = false;
            }
        }
        invalidate();
        return true;
    }

    /**
     * 拖动线移动
     *
     * @param currentX
     * @param currentY
     */
    private void dragLine(float currentX, float currentY) {
        if (currentArea == null || currentArea.getAraa() == null) {
            return;
        }
        if (currentP1 != null && currentP2 != null) {
            int p1Position = currentP1.getPosition();
            int p2Position = currentP2.getPosition();
            for (int i = 0; i < currentArea.getAraa().size(); i++) {
                PointBean pointBean = currentArea.getAraa().get(i);
                if (pointBean.getPosition() == p1Position || pointBean.getPosition() == p2Position) {
                    PointBean distance1 = null;
                    if (orientation == ORIENTATION.HORIZONTAL || currentP1.getX() == currentP2.getX()) {
                        if (p1Position == 0 && p2Position == 3) {
                            distance1 = currentArea.getAraa().get(1);
                        } else if (p1Position == 1 && p2Position == 2) {
                            distance1 = currentArea.getAraa().get(0);
                        } else if (p1Position == 2 && p2Position == 1) {
                            distance1 = currentArea.getAraa().get(3);
                        } else if (p1Position == 3 && p2Position == 0) {
                            distance1 = currentArea.getAraa().get(2);
                        }
                        if (distance1 != null) {
                            double dis1 = DottedLineUtil.distzj(currentX, distance1.getX(), currentP1.getY(), distance1.getY());
                            if (dis1 < 100 && Mlistener != null) {
                                Mlistener.onMiniArea();
                                return;
                            }
                        }
                        currentArea.getAraa().get(i).setX(currentX);
                    }

                    if (orientation == ORIENTATION.VERTICAL || currentP1.getY() == currentP2.getY()) {
                        if (p1Position == 0 && p2Position == 1) {
                            distance1 = currentArea.getAraa().get(3);
                        } else if (p1Position == 1 && p2Position == 0) {
                            distance1 = currentArea.getAraa().get(2);

                        } else if (p1Position == 2 && p2Position == 3) {
                            distance1 = currentArea.getAraa().get(1);

                        } else if (p1Position == 3 && p2Position == 2) {
                            distance1 = currentArea.getAraa().get(0);
                        }
                        if (distance1 != null) {
                            double dis1 = DottedLineUtil.distzj(currentP1.getX(), distance1.getX(), currentY, distance1.getY());
                            if (dis1 < 100 && Mlistener != null) {
                                Mlistener.onMiniArea();
                                return;
                            }
                        }

                        currentArea.getAraa().get(i).setY(currentY);
                    }
                }
            }
            invalidate();
        }
    }

    /**
     * 选择图形
     *
     * @param x
     * @param y
     */
    private void selectArea(float x, float y) {
        for (int i = 0; i < mAreas.size(); i++) {
            GraphicsObj area = mAreas.get(i);
            boolean inArea = DottedLineUtil.IsPtInPoly(new PointBean(x, y, 0), area.getAraa());
            if (inArea) {
                mAreas.get(i).setSelect(true);
                currentArea = mAreas.get(i);
            }
        }
        // 多图形选中图形判定时，会因为先后顺序原因，导致选中状态赋值出现不唯一，所以用2个循环确保唯一性
        for (int i = 0; i < mAreas.size(); i++) {
            if (currentArea != null && currentArea.getId() == mAreas.get(i).getId()) {
                mAreas.get(i).setSelect(true);
            } else {
                mAreas.get(i).setSelect(false);
            }
        }
    }

    boolean mEnabled = false;

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
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

    public void delCurrentGraphics() {
        for (GraphicsObj obj : mAreas) {
            if (obj.isSelect) {
                mAreas.remove(obj);
                break;
            }
        }
        hadIntersect = false;
        invalidate();
    }

    //清除图形
    public void clearGraphics() {
        mAreas.clear();
        hadIntersect = false;
        invalidate();
    }

    /**
     * 设置区域数据
     *
     * @param graphicsObjs
     */
    public void setAreaBeans(List<GraphicsObj> graphicsObjs) {
        mAreas.clear();
        if (graphicsObjs != null && graphicsObjs.size() != 0) {
            mAreas.addAll(graphicsObjs);
            currentArea = mAreas.get(0);
        }
        checkIntersect();
        invalidate();
    }

    /**
     * 添加一个图形区域
     *
     * @param graphicsObjs
     */
    public void addAreaBeans(GraphicsObj graphicsObjs) {
        if (graphicsObjs == null) {
            return;
        }
        mAreas.add(graphicsObjs);
        mDottedLine = true;
        checkIntersect();
        invalidate();
    }


    public List<GraphicsObj> getGraphics() {
        return mAreas;
    }

    /**
     * 添加新区域
     */
    public boolean addNewArea() {
        if (mAreas.size() >= MAX_AREA_COUNT) {
            return false;
        }
        int defaultWidth = getWidth() / 3;
        int defaultHeight = getHeight() / 3;
        int left = (getWidth() - defaultWidth) / 2;
        int top = (getHeight() - defaultHeight) / 2;
        int right = left + defaultWidth;
        int bottom = top + defaultHeight;

        ArrayList<PointBean> paintingArea = new ArrayList<>();
        paintingArea.add(new PointBean(left, top, 0));
        paintingArea.add(new PointBean(right, top, 1));
        paintingArea.add(new PointBean(right, bottom, 2));
        paintingArea.add(new PointBean(left, bottom, 3));

        for (int i = 0; i < mAreas.size(); i++) {
            mAreas.get(i).setSelect(false);
        }
        currentArea = new GraphicsObj();
        currentArea.setAraa(paintingArea, true);
        mAreas.add(currentArea);
        mDottedLine = true;
        invalidate();
        return true;
    }

    public interface OnDelClickListener {
        void onDelClicked();

        void onMiniArea();
    }

    public static class GraphicsObj {
        private ArrayList<PointBean> mAraa = new ArrayList<>();
        private PointBean clickPoint;
        private boolean isSelect;
        private long id;

        public GraphicsObj() {
            id = System.currentTimeMillis();
        }

        public void setAraa(ArrayList<PointBean> araa, boolean isSelect) {
            this.mAraa.clear();
//            if (araa.size() > 4) {
//                this.mAraa.addAll(araa.subList(0, 4));
//            } else {
            this.mAraa.addAll(araa);
//            }
            this.isSelect = isSelect;
        }

        public ArrayList<PointBean> getAraa() {
            return mAraa;
        }

        public PointBean getClickPoint() {
            float minY = -100;
            int minPostion = 0;
            for (int i = 0; i < mAraa.size(); i++) {
                PointBean point = mAraa.get(i);
                if (minY == -100) {
                    minPostion = i;
                    minY = point.getY();
                } else {
                    if (point.getY() <= minY) {
                        minPostion = i;
                        minY = point.getY();
                    }
                }
            }

            float px = mAraa.get(minPostion).getX();
            float py = mAraa.get(minPostion).getY();
            clickPoint = new PointBean(px, py, 0);
            return clickPoint;
        }

        public void setSelect(boolean select) {
            this.isSelect = select;
        }

        public long getId() {
            return id;
        }

        @Override
        public String toString() {
            return "{\"clickPoint\":" + getClickPoint().toString() + ",\"isSelect\":" + isSelect + ",\"id\":" + id + "}";
        }
    }

}
