package com.carl.test;

import java.util.ArrayList;
import java.util.List;

/**
 * @ 创建者:   CoderChen
 * @ 时间:     2018/1/8
 * @ 描述:
 */
class StepDetector {
    //存放三轴数据
    private float[] oriValues = new float[3];
    private final int ValueNum = 2;
    //用于存放计算阈值的波峰波谷差值
    private float[] tempValue = new float[ValueNum];
    int tempCount = 0;
    //是否上升的标志位
    private boolean isDirectionUp = false;
    //持续上升次数
    private int continueUpCount = 0;
    //上一点的持续上升的次数，为了记录波峰的上升次数
    private int continueUpFormerCount = 0;
    //上一点的状态，上升还是下降
    boolean lastStatus = false;
    //波峰值
    float peakOfWave = 0;
    //波谷值
    private float valleyOfWave = 0;
    //此次波峰的时间
    private long timeOfThisPeak = 0;
    //上次波峰的时间
    private long timeOfLastPeak = 0;
    //当前的时间
    private long timeOfNow = 0;
    //当前传感器的值
    private float gravityNew = 0;
    //上次传感器的值
    private float gravityOld = 0;
    //动态阈值需要动态的数据，这个值用于这些动态数据的阈值
    private final float InitialValue = (float) 130;
    //初始阈值
    private float ThreadValue = (float) 200;
    //波峰波谷时间差
//    int TimeInterval = 250;
    private float TimeIntervalMin = 5f;
    private float TimeIntervalMax = 50f;

    private List<Axis> mSrc = new ArrayList<>();
    private int step = 0;
    private long indexOfLastPeak = 0;
    private String csv;
    private boolean startCountFlag = true;

    //add 20180112 ==================================================
    int peakNum = 5;
    private float[] peakArray = new float[peakNum]; //波峰值
    private int peakArrCount = 0;
    private float peakVar = 100f; // 波峰方差
    private float[] peakVarArr = new float[peakNum];
    private float peakAvage = 0f;


    private void saveGpeakArray(float value) {
        if (peakArrCount < peakNum) {
            peakArray[peakArrCount] = value;
            peakArrCount++;
        } else {
            for (int i = 1; i < peakNum; i++) {
                peakArray[i - 1] = peakArray[i];
            }
            peakArray[peakNum - 1] = value;
        }
    }

    private float getPeakVar() {
        float ret = 100f;
        float sum = 0;
        for (int i = 0; i < peakNum; i++) {
            sum += peakArray[i];
        }
        peakAvage = sum / peakNum;
        if (peakAvage != 0) {
            sum = 0;
            for (int i = 0; i < peakNum; i++) {
                sum += Math.pow((peakArray[i] - peakAvage), 2);
            }
            ret = sum / peakNum;
        }
        peakVar = ret;
        return ret;
    }

    //=============================================


    public StepDetector(List<Axis> mSrc, String csv) {
        this.mSrc = mSrc;
        this.csv = csv;
    }

    public void onSensorChanged() {
        for (int i = 0; i < mSrc.size(); i++) {
            oriValues[0] = mSrc.get(i).x;
            oriValues[1] = mSrc.get(i).y;
            oriValues[2] = mSrc.get(i).z;
            gravityNew = (float) Math.sqrt(oriValues[0] * oriValues[0]
                    + oriValues[1] * oriValues[1] + oriValues[2] * oriValues[2]);
            detectorNewStep(gravityNew, i);
        }

    }


    /*
    * 检测步子，并开始计步
    * 1.传入sersor中的数据
    * 2.如果检测到了波峰，并且符合时间差以及阈值的条件，则判定为1步
    * 3.符合时间差条件，波峰波谷差值大于initialValue，则将该差值纳入阈值的计算中
    * */
    public void detectorNewStep(float values, int index) {
        if (gravityOld == 0) {
            gravityOld = values;
        } else {
            if (detectorPeak(values, gravityOld)) {
                timeOfLastPeak = timeOfThisPeak;
                timeOfNow = index;
                if (timeOfNow - timeOfLastPeak >= TimeIntervalMin
                        && (timeOfNow - timeOfLastPeak <= TimeIntervalMax)
                        && (peakOfWave - valleyOfWave >= ThreadValue)) {
                    timeOfThisPeak = timeOfNow;
                    /*
                     * 更新界面的处理，不涉及到算法
                     * 一般在通知更新界面之前，增加下面处理，为了处理无效运动：
                     * 1.连续记录10才开始计步
                     * 2.例如记录的9步用户停住超过3秒，则前面的记录失效，下次从头开始
                     * 3.连续记录了9步用户还在运动，之前的数据才有效
                     * */
//                    mStepListeners.countStep();
                    countStep(index);
                }
                if (timeOfNow - timeOfLastPeak >= TimeIntervalMin
                        && (peakOfWave - valleyOfWave >= InitialValue)) {
                    timeOfThisPeak = timeOfNow;
                    ThreadValue = peakValleyThread(peakOfWave - valleyOfWave);
//                    System.out.println("index:"+index+",ThreadValue:" + ThreadValue);
                }
            }
        }

        if (index == mSrc.size() - 1) {
            System.out.println(csv + ",Steps:" + step);
        }
        gravityOld = values;
    }

    public void countStep(int index) {
        if (index - this.indexOfLastPeak <= 80 || startCountFlag) {
            startCountFlag = false;
            indexOfLastPeak = index;
            if (this.step < 9) {
                this.step++;
            } else if (this.step == 9) {
                this.step++;
            } else {
                this.step++;
            }
        } else {//超时
            startCountFlag = true;
            this.step = 1;//为1,不是0
        }

    }

    /*
     * 检测波峰
     * 以下四个条件判断为波峰：
     * 1.目前点为下降的趋势：isDirectionUp为false
     * 2.之前的点为上升的趋势：lastStatus为true
     * 3.到波峰为止，持续上升大于等于2次
     * 4.波峰值大于20
     * 记录波谷值
     * 1.观察波形图，可以发现在出现步子的地方，波谷的下一个就是波峰，有比较明显的特征以及差值
     * 2.所以要记录每次的波谷值，为了和下次的波峰做对比
     * */
    public boolean detectorPeak(float newValue, float oldValue) {
        lastStatus = isDirectionUp;
        if (newValue >= oldValue) {
            isDirectionUp = true;
            continueUpCount++;
        } else {
            continueUpFormerCount = continueUpCount;
            continueUpCount = 0;
            isDirectionUp = false;
        }

        if (!isDirectionUp
                && lastStatus
                && (continueUpFormerCount >= 2 || oldValue >= 980)
                ) {
            peakOfWave = oldValue;
            saveGpeakArray(peakOfWave);
            getPeakVar();
            return true;
        } else if (!lastStatus && isDirectionUp) {
            valleyOfWave = oldValue;
            return false;
        } else {
            return false;
        }
    }

    /*
     * 阈值的计算
     * 1.通过波峰波谷的差值计算阈值
     * 2.记录4个值，存入tempValue[]数组中
     * 3.在将数组传入函数averageValue中计算阈值
     * */
    public float peakValleyThread(float value) {
        float tempThread = ThreadValue;
        if (tempCount < ValueNum) {
            tempValue[tempCount] = value;
            tempCount++;
        } else {
            tempThread = averageValue(tempValue, ValueNum);
            for (int i = 1; i < ValueNum; i++) {
                tempValue[i - 1] = tempValue[i];
            }
            tempValue[ValueNum - 1] = value;
        }
        return tempThread;

    }

    /*
     * 梯度化阈值
     * 1.计算数组的均值
     * 2.通过均值将阈值梯度化在一个范围里
     * */
    public float averageValue(float value[], int n) {
        float ave = 0;
        for (int i = 0; i < n; i++) {
            ave += value[i];
        }
        ave = ave / ValueNum;
        if (ave >= 800)
            ave = (float) 430;
        else if (ave >= 700 && ave < 800)
            ave = (float) 330;
        else if (ave >= 400 && ave < 700)
            ave = (float) 230;
        else if (ave >= 300 && ave < 400)
            ave = (float) 200;
        else {
            ave = (float) 130;
        }
        return ave;
    }
}
