package com.views.graphics;

/**
 * Created by Administrator on 2020/6/17 0017.
 */

public class PointBean {
    private float x;
    private float y;
    private int position;
    private boolean current;

    public PointBean() {
    }

    public PointBean(float x, float y, int position) {
        this.x = x;
        this.y = y;
        this.position = position;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }

    public boolean isCurrent() {
        return current;
    }

    @Override
    public String toString() {
        return "{\"x\":" + x + ",\"y\":" + y + ",\"position\":" + position + "}";
    }
}
