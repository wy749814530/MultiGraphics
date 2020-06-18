package com.views.graphics;

import java.util.List;


/**
 * Created by Administrator on 2020/6/17 0017.
 */

public class DottedLineUtil {
    /**
     * 获取两点之间的距离
     *
     * @param x1
     * @param x2
     * @param y1
     * @param y2
     * @return
     */
    public static double distzj(float x1, float x2, float y1, float y2) {
        double dis = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
        return dis;
    }

    /**
     * 判断点是否在多边形内
     *
     * @param point 检测点
     * @param pts   多边形的顶点
     * @return 点在多边形内返回true, 否则返回false
     */
    public static boolean IsPtInPoly(PointBean point, List<PointBean> pts) {

        int N = pts.size();
        boolean boundOrVertex = true; //如果点位于多边形的顶点或边上，也算做点在多边形内，直接返回true
        int intersectCount = 0;//cross points count of x
        double precision = 2e-10; //浮点类型计算时候与0比较时候的容差
        PointBean p1, p2;//neighbour bound vertices
        PointBean p = point; //当前点

        p1 = pts.get(0);//left vertex
        for (int i = 1; i <= N; ++i) {//check all rays
            if (p.equals(p1)) {
                return boundOrVertex;//p is an vertex
            }

            p2 = pts.get(i % N);//right vertex
            if (p.getX() < Math.min(p1.getX(), p2.getX()) || p.getX() > Math.max(p1.getX(), p2.getX())) {//ray is outside of our interests
                p1 = p2;
                continue;//next ray left point
            }

            if (p.getX() > Math.min(p1.getX(), p2.getX()) && p.getX() < Math.max(p1.getX(), p2.getX())) {//ray is crossing over by the algorithm (common part of)
                if (p.getY() <= Math.max(p1.getY(), p2.getY())) {//x is before of ray
                    if (p1.getY() == p2.getX() && p.getY() >= Math.min(p1.getY(), p2.getY())) {//overlies on a horizontal ray
                        return boundOrVertex;
                    }

                    if (p1.getY() == p2.getY()) {//ray is vertical
                        if (p1.getY() == p.getY()) {//overlies on a vertical ray
                            return boundOrVertex;
                        } else {//before ray
                            ++intersectCount;
                        }
                    } else {//cross point on the left side
                        double xinters = (p.getX() - p1.getX()) * (p2.getY() - p1.getY()) / (p2.getX() - p1.getX()) + p1.getY();//cross point of y
                        if (Math.abs(p.getY() - xinters) < precision) {//overlies on a ray
                            return boundOrVertex;
                        }

                        if (p.getY() < xinters) {//before ray
                            ++intersectCount;
                        }
                    }
                }
            } else {//special case when ray is crossing through the vertex
                if (p.getX() == p2.getX() && p.getY() <= p2.getY()) {//p crossing over p2
                    PointBean p3 = pts.get((i + 1) % N); //next vertex
                    if (p.getX() >= Math.min(p1.getX(), p3.getX()) && p.getX() <= Math.max(p1.getX(), p3.getX())) {//p.x lies between p1.x & p3.x
                        ++intersectCount;
                    } else {
                        intersectCount += 2;
                    }
                }
            }
            p1 = p2;//next ray left point
        }

        if (intersectCount % 2 == 0) {//偶数在多边形外
            return false;
        } else { //奇数在多边形内
            return true;
        }
    }

    /**
     * 判断是否有相交线
     *
     * @param p1
     * @param p2
     * @param p3
     * @param p4
     * @return
     */
    public static boolean detectIntersect(PointBean p1, PointBean p2, PointBean p3, PointBean p4) {
        double line_x, line_y; //交点
        if ((Math.abs(p1.getX() - p2.getX()) < 1e-6) && (Math.abs(p3.getX() - p4.getX()) < 1e-6)) {
            return false;
        } else if ((Math.abs(p1.getX() - p2.getX()) < 1e-6)) //如果直线段p1p2垂直与y轴
        {
            if (between(p1.getX(), p3.getX(), p4.getX())) {
                double k = (p4.getY() - p3.getY()) / (p4.getX() - p3.getX());
                line_x = p1.getX();
                line_y = k * (line_x - p3.getX()) + p3.getY();
                if (between(line_y, p1.getY(), p2.getY())) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else if ((Math.abs(p3.getX() - p4.getX()) < 1e-6)) //如果直线段p3p4垂直与y轴
        {
            if (between(p3.getX(), p1.getX(), p2.getX())) {
                double k = (p2.getY() - p1.getY()) / (p2.getX() - p1.getX());
                line_x = p3.getX();
                line_y = k * (line_x - p2.getX()) + p2.getY();

                if (between(line_y, p3.getY(), p4.getY())) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            double k1 = (p2.getY() - p1.getY()) / (p2.getX() - p1.getX());
            double k2 = (p4.getY() - p3.getY()) / (p4.getX() - p3.getX());

            if (Math.abs(k1 - k2) < 1e-6) {
                return false;
            } else {
                line_x = ((p3.getY() - p1.getY()) - (k2 * p3.getX() - k1 * p1.getX())) / (k1 - k2);
                line_y = k1 * (line_x - p1.getX()) + p1.getY();
            }

            if (between(line_x, p1.getX(), p2.getX()) && between(line_x, p3.getX(), p4.getX())) {
                return true;
            } else {
                return false;
            }
        }
    }

    public static boolean between(double a, double X0, double X1) {
        double temp1 = a - X0;
        double temp2 = a - X1;
        if ((temp1 < 1e-8 && temp2 > -1e-8) || (temp2 < 1e-6 && temp1 > -1e-8)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断点是否在线上
     *
     * @param Q
     * @param p1
     * @param p2
     * @param tolerance
     * @return
     */
    public static boolean pointOnline(PointBean Q, PointBean p1, PointBean p2, int tolerance) {

        float maxx = p1.getX() > p2.getX() ? p1.getX() : p2.getX();    //矩形的右边长
        float minx = p1.getX() > p2.getX() ? p2.getX() : p1.getX();     //矩形的左边长
        float maxy = p1.getY() > p2.getY() ? p1.getY() : p2.getY();    //矩形的上边长
        float miny = p1.getY() > p2.getY() ? p2.getY() : p1.getY();     //矩形的下边长

        if (p1.getX() == p2.getX()) {
            // 竖向线
            if (Q.getY() >= miny && Q.getY() <= maxy && Math.abs(p1.getX() - Q.getX()) < tolerance) {
                return true;
            } else {
                return false;
            }
        } else if (p1.getY() == p2.getY()) {
            // 横向线
            if (Q.getX() >= minx && Q.getX() <= maxx && Math.abs(p1.getY() - Q.getY()) < tolerance) {
                return true;
            } else {
                return false;
            }
        }

        return false;
    }
}
