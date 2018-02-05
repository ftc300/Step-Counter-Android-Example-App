package com.carl.test;

/**
 * @ 创建者:   CoderChen
 * @ 时间:     2018/1/8
 * @ 描述:
 */


public class Axis {
    public float x;
    public float y;
    public float z;

    public Axis(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public String toString() {
        return x + "," + y + "," + z;
    }
}
