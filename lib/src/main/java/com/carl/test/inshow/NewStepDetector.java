package com.carl.test.inshow;

import com.carl.test.Axis;

import java.util.ArrayList;
import java.util.List;

import static com.carl.test.inshow.Utils.FILTER_LENGTH;
import static com.carl.test.inshow.Utils.THRESHOLD;
import static com.carl.test.inshow.Utils.TIME_THRESHOLD_MAX;
import static com.carl.test.inshow.Utils.TIME_THRESHOLD_MIN;
import static com.carl.test.inshow.Utils.WINDOW_SIZE;
import static com.carl.test.inshow.Utils.scorePeak;

/**
 * Created by chendong on 2018/2/5.
 */

public class NewStepDetector {
    private List<Axis> rawSource = new ArrayList<>();
    private List<com.carl.test.inshow.DataPoint> rawData = new ArrayList<>();
    private List<com.carl.test.inshow.DataPoint> ppData = new ArrayList<>();
    private List<com.carl.test.inshow.DataPoint> smoothData = new ArrayList<>();
    private List<com.carl.test.inshow.DataPoint> peakScoreData = new ArrayList<>();
    private List<com.carl.test.inshow.DataPoint> peakData = new ArrayList<>();
    private String csv;
    //存放三轴数据
    private float[] oriValues = new float[3];
    //当前传感器的值
    private float g = 0;
    private com.carl.test.inshow.DataPoint dp;


    public NewStepDetector(List<Axis> rawSource, String csv) {
        this.rawSource = rawSource;
        this.csv = csv;
    }

    public void start() {
        for (int i = 0; i < rawSource.size(); i++) {
            oriValues[0] = rawSource.get(i).x;
            oriValues[1] = rawSource.get(i).y;
            oriValues[2] = rawSource.get(i).z;
            g = (float) Math.sqrt(oriValues[0] * oriValues[0] + oriValues[1] * oriValues[1] + oriValues[2] * oriValues[2]);
            rawData.add(new com.carl.test.inshow.DataPoint(i, g / 100));
        }
        rawData.add(new DataPoint(rawSource.size(), 0, true));
        process();
    }

    private void process() {
        //ignore linearInterpolate
        ppData = rawData;
        smoothFilter();
    }

    private void smoothFilter() {
//        boolean active = true;
//        ArrayList<com.carl.test.inshow.DataPoint> window = new ArrayList<>();
//        boolean first = true;
//        while (active) {
//            if (!ppData.isEmpty()) {
//                dp = ppData.remove(0);
//                if (first) {
//                    log("smoothFilter first : " + dp.toString());
//                    first = false;
//                }
//            }
//            if (dp != null) {
//                // Special handling for final data point.
//                if (dp.getEos()) {
//                    smoothData.add(dp);
//                    scoreStage();
//                    active = false;
//                    continue;
//                }
//                window.add(dp);
//                if (window.size() == FILTER_LENGTH) {
//                    float sum = 0;
//                    for (int i = 0; i < FILTER_LENGTH; i++) {
//                        sum += window.get(i).getMagnitude() * Utils.generateCoefficients().get(i);
//                    }
//                    com.carl.test.inshow.DataPoint new_dp = new com.carl.test.inshow.DataPoint(window.get(FILTER_LENGTH / 2).getTime(), sum);
//                    smoothData.add(new_dp);
//                    window.remove(0);
//                }
//                dp = null;
//            }
//        }

        //
        smoothData = ppData;
        scoreStage();
    }


    private void scoreStage() {
        boolean active = true;
        ArrayList<com.carl.test.inshow.DataPoint> window = new ArrayList<>();
        boolean first = true;
        while (active) {
            if (!smoothData.isEmpty()) {
                dp = smoothData.remove(0);
                if (first) {
                    log("scoreStage first:" + dp.toString());
                    first = false;
                }
            }
            if (dp != null) {
                // Special handling for final data point.
                if (dp.getEos()) {
                    peakScoreData.add(dp);
                    detectionStage();
                    active = false;
                    continue;
                }
                window.add(dp);
                if (window.size() == WINDOW_SIZE) {
                    // Calculate score and append to the output window.
                    float score = scorePeak(window);
                    peakScoreData.add(new com.carl.test.inshow.DataPoint(window.get(WINDOW_SIZE / 2).getTime(), score));
                    // Pop out the oldest point.
                    window.remove(0);
                }
                dp = null;
            }
        }
    }

    private void detectionStage() {
        boolean active = true;
        int count = 0;
        float mean = 0f;
        float std = 0f;
        boolean first = true;
        while (active) {
            if (!peakScoreData.isEmpty()) {
                dp = peakScoreData.remove(0);
                if (first) {
                    log("detectionStage first:" + dp.toString());
                    first = false;
                }
            }
            if (dp != null) {
                // Special handing for end of stream.
                if (dp.getEos()) {
                    peakData.add(dp);
                    postStage();
                    active = false;
                    continue;
                }
                // Update calculations of std and mean.
                count++;
                float o_mean = mean;
                switch (count) {
                    case 1:
                        mean = dp.getMagnitude();
                        std = 0f;
                        break;
                    case 2:
                        mean = (mean + dp.getMagnitude()) / 2;
                        std = (float) Math.sqrt(Math.pow(dp.getMagnitude() - mean, 2) + Math.pow(o_mean - mean, 2)) / 2;
                        break;
                    default:
                        mean = (dp.getMagnitude() + (count - 1) * mean) / count;
                        std = (float) Math.sqrt(((count - 2) * Math.pow(std, 2) / (count - 1)) + Math.pow(o_mean - mean, 2) + Math.pow(dp.getMagnitude() - mean, 2) / count);
                }
                // Once we have enough data points to have a reasonable mean/standard deviation, start detecting
                if (count > 15) {
                    if ((dp.getMagnitude() - mean) > std * THRESHOLD) {
                        // This is a peak
                        peakData.add(new com.carl.test.inshow.DataPoint(dp.getTime(), dp.getMagnitude()));
                    }
                }
                dp = null;
            }
        }
    }

    private void postStage() {
        boolean active = true;
        int count = 0;
        com.carl.test.inshow.DataPoint current = null;
        boolean first = true;
        while (active) {
            if (!peakData.isEmpty()) {
                dp = peakData.remove(0);
                if (first) {
                    log("postStage first:" + dp.toString());
                    first = false;
                }
            }
            if (dp != null) {
                if (dp.getEos()) {
                    active = false;
                    System.out.println(csv + ":" + count);
                    continue;
                }
                // First point handler.
                if (current == null) {
                    current = dp;
                } else {
                    // If the time difference exceeds the threshold, we have a confirmed step
                    if ((dp.getTime() - current.getTime()) >= TIME_THRESHOLD_MIN && (dp.getTime() - current.getTime()) <=TIME_THRESHOLD_MAX) {
                        current = dp;
                        count++;
                    } else {
                        // Keep the point with the largest magnitude.
                        if (dp.getMagnitude() > current.getMagnitude()) {
                            current = dp;
                        }
                    }
                }
                dp = null;
            }
        }
    }


    private void log(String s) {
        boolean b = false;
        if(b) {
            System.out.println(s);
        }
    }

    private void log(String tag, List<DataPoint> list) {
        int len = list.size();
        log(tag + ": size = " + len);
        for (int i = 0; i < len; i++) {
            log(list.get(i).toString());
        }
    }


}
