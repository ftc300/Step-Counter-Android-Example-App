package com.carl.test.inshow;

import com.carl.test.Axis;

import java.util.ArrayList;
import java.util.List;

import static com.carl.test.CsvUtil.readCsvFile;

/**
 * @ 创建者:   CoderChen
 * @ 时间:     2018/1/8
 * @ 描述:
 */


public class Steptest {
    public static void main(String[] args) {
        String folderPath = "D:\\AnalysisCountStep\\analysis-statistic\\data\\";
        List<String> csv = new ArrayList<>();
        csv.add("cd_quick130");
        csv.add("cd_run101");
        csv.add("cd_slow150");
        csv.add("dk_quick114");
        csv.add("dk_run97");
        csv.add("dk_slow150");
        csv.add("jx_quick124");
        csv.add("jx_run130");
        csv.add("jx_slow186");
        csv.add("sx_quick127");
        csv.add("sx_ride0");
        csv.add("sx_run94");
        csv.add("sx_slow136");
        csv.add("wh_quick129");
        csv.add("wh_slow140");
        csv.add("xx_quick127");
        csv.add("xx_run118");
        csv.add("xx_slow150");
        csv.add("xy_quick113");
        csv.add("xy_run114");
        csv.add("xy_slow127");
        csv.add("yw_quick146");
        csv.add("yw_run126");
        csv.add("yw_slow154");
        for (String csvFile : csv) {
            List<Axis> list = readCsvFile(folderPath + csvFile + ".csv", "gb2312", ",");
            NewStepDetector stepCounter = new NewStepDetector(list,csvFile);
            stepCounter.start();
        }
    }
}
