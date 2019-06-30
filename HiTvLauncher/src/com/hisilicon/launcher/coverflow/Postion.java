
package com.hisilicon.launcher.coverflow;

/**
 * position info of image
 *
 * @author huyq
 */
public class Postion {
    // order of image
    private int mOrder;
    // x-axis offset
    private float x;
    // scale of image
    private float mScale;

    public Postion(float x, float scale, int order) {
        this.x = x;
        this.mScale = scale;
        this.mOrder = order;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Postion)) {
            return false;
        }
        Postion p = (Postion) o;
        if (p.getX() == x && p.getScale() == mScale) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return super.hashCode();
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getScale() {
        return mScale;
    }

    public void setScale(float scale) {
        this.mScale = scale;
    }

    public int getOrder() {
        return mOrder;
    }

    public void setOrder(int order) {
        this.mOrder = order;
    }

    @Override
    public String toString() {
        return "Postion [x=" + x + ", scale=" + mScale + ", order=" + mOrder
                + "]";
    }
}
