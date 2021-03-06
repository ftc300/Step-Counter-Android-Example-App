package com.carl.test.inshow;

/**
 * Created by Jamie Brynes on 1/22/2017.
 */

public class DataPoint {

    private int time;
    private float magnitude;


    // Toggle for special End-Of-Stream flag.
    private boolean eos;

    public DataPoint(int time, float magnitude) {
        this.time = time;
        this.magnitude = magnitude;
        eos = false;
    }

    public DataPoint(int time, float magnitude , boolean eos) {
        this.time = time;
        this.magnitude = magnitude;
        this.eos = eos;
    }

    public void setEos(boolean val) {
        eos = val;
    }

    public boolean getEos() {
        return eos;
    }

    public float getMagnitude() {
        return magnitude;
    }

    public void setMagnitude(float magnitude) {
        this.magnitude = magnitude;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return time + "," + magnitude + "," + eos;
    }
}
