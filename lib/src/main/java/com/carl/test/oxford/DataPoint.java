package com.carl.test.oxford;

/**
 * Created by Jamie Brynes on 1/22/2017.
 */

public class DataPoint {

    private float time;
    private float magnitude;


    // Toggle for special End-Of-Stream flag.
    private boolean eos;

    public DataPoint(long time, float magnitude) {
        this.time = (float) time;
        this.magnitude = magnitude;
        eos = false;
    }

    public DataPoint(float time, float magnitude) {
        this.time = time;
        this.magnitude = magnitude;
    }

    public DataPoint(float time, float magnitude ,boolean eos) {
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

    public float getTime() {
        return time;
    }

    public void setTime(float time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return time + "," + magnitude + "," + eos;
    }
}
