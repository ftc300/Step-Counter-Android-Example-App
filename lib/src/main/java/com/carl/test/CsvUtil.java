package com.carl.test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @ 创建者:   CoderChen
 * @ 时间:     2018/1/8
 * @ 描述:
 */


public class CsvUtil {
    public static void main(String[] args) {
//       List<Axis> axises =  readCsvFile("D:\\AnalysisCountStep\\analysis-statistic\\data\\cd_quick130.csv", "gb2312", ",");
//        for (Axis a: axises) {
//        System.out.println(a.toString());
//        }
    }

    public static List<Axis> readCsvFile(String csvFilePath, String fileEncoder, String separtor) {
        List<Axis> ret = new ArrayList<>();
        int lineNum = 0;
        InputStreamReader fr = null;
        BufferedReader br = null;
        try {
            fr = new InputStreamReader(new FileInputStream(csvFilePath), fileEncoder);
            br = new BufferedReader(fr);
            String rec = null;
            String[] argsArr = null;
            while ((rec = br.readLine()) != null) {
                if (rec != null) {
                    argsArr = rec.split(separtor);
                    if (lineNum != 0) {
                        Axis axis = new Axis(Float.valueOf(argsArr[0]), Float.valueOf(argsArr[1]), Float.valueOf(argsArr[2]));
                        ret.add(axis);
                    }
                    lineNum++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fr != null)
                    fr.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            try {
                if (br != null)
                    br.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return  ret;
    }
}
