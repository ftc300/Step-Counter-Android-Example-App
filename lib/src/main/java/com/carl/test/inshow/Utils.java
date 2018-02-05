package com.carl.test.inshow;

import com.carl.test.oxford.DataPoint;

import java.util.ArrayList;

/**
 * Created by chendong on 2018/2/5.
 */

public class Utils {
//    public final static int FILTER_LENGTH = 13;
    public final static int FILTER_LENGTH = 2;
//    public final static float FILTER_STD = 0.35f;
    public final static float FILTER_STD = 0.8f;
//    public static final int WINDOW_SIZE = 35;
    public static final int WINDOW_SIZE = 9;
//    public static final float THRESHOLD = 1.2f;
    public static final float THRESHOLD = 1.2f;
    public static final int TIME_THRESHOLD_MIN = 4;
    public static final int TIME_THRESHOLD_MAX = 25;

    public static ArrayList<Float> generateCoefficients() {
        // Create a window of the correct size.
        ArrayList<Float> coeff = new ArrayList<Float>();
        for (int i = 0; i < FILTER_LENGTH; i++) {
            float value = (float) Math.pow(Math.E, -0.5 * Math.pow((i - (FILTER_LENGTH - 1) / 2) / (FILTER_STD * (FILTER_LENGTH - 1) / 2), 2));
            coeff.add(value);
        }
        return coeff;
    }

    public static  float scorePeak(ArrayList<com.carl.test.inshow.DataPoint> data) {
        int midpoint = (int) data.size() / 2;
        float diffLeft = 0f;
        float diffRight = 0f;

        for(int i = 0; i < midpoint; i++) {
            diffLeft += data.get(midpoint).getMagnitude() - data.get(i).getMagnitude();
        }

        for (int j = midpoint + 1; j < data.size(); j++) {
            diffRight += data.get(midpoint).getMagnitude() - data.get(j).getMagnitude();
        }

        return (diffRight + diffLeft) / (WINDOW_SIZE - 1);
    }

}
