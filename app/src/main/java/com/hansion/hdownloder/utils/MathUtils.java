package com.hansion.hdownloder.utils;

import android.graphics.Point;

/**
 * Created by Hansion on 2016/6/20.
 */
public class MathUtils {
    /**
     * 求斜率对应的斜角等级（0~18）
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    public static int bevel(int x1, int y1, int x2, int y2) {
        int bevel = 9;

        if (x1 == x2) {
            bevel = 9;
        } else {
            int k = (y2 - y1) / (x2 - x1);
            if (k > 0 && k <= 0.6) {     //0到30
                bevel = 1;
            } else if (k > 0.6 && k <= 1.7) { //30到60
                bevel = 4;
            } else if (k > 1.7 && k <= 5.7) { //60到90
                bevel = 7;
            } else if (k > 5.7 || k < -5.7) { //90
                bevel = 9;
            } else if (k >= -0.6 && k < 0) {  //150到180
                bevel = 16;
            } else if (k >= -1.7 && k < -0.6) { //120到150
                bevel = 13;
            } else if (k >= -5.7 && k < -1.7) {
                bevel = 10;
            } else if (k == 0) {
                if (x1 > x2) {
                    bevel = 0;
                } else {
                    bevel = 18;
                }
            } else {
                bevel = 10000;
            }
        }
        return bevel;
    }

    /**
     * 两点之间的距离
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    public static double distance(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    /**
     * 根据斜率求角度
     *
     * @return 0到360
     */
    public static int angle(int x1, int y1, int x2, int y2) {
        int angle = (int) (180 + 180 * (Math.atan2(y2 - y1, x2 - x1) / Math.PI));
        return angle;
    }

    public static float evalute(float fraction, float start, float end) {
        return start + (end - start) * fraction;
    }


    /**
     *  文件大小换算
     * @param 字节大小
     */
    private static final String UNIT_B = "B";
    private static final String UNIT_KB = "KB";
    private static final String UNIT_MB = "MB";
    private static final String UNIT_GB = "GB";
    private static final String UNIT_TB = "TB";
    private static final int UNIT_INTERVAL = 1024;
    private static final double ROUNDING_OFF = 0.005;
    private static final int DECIMAL_NUMBER = 100;
    public static String sizeToString(long size) {
        String unit = UNIT_B;
        if (size < DECIMAL_NUMBER) {
            return Long.toString(size) + " " + unit;
        }

        unit = UNIT_KB;
        double sizeDouble = (double) size / (double) UNIT_INTERVAL;
        if (sizeDouble > UNIT_INTERVAL) {
            sizeDouble = (double) sizeDouble / (double) UNIT_INTERVAL;
            unit = UNIT_MB;
        }
        if (sizeDouble > UNIT_INTERVAL) {
            sizeDouble = (double) sizeDouble / (double) UNIT_INTERVAL;
            unit = UNIT_GB;
        }
        if (sizeDouble > UNIT_INTERVAL) {
            sizeDouble = (double) sizeDouble / (double) UNIT_INTERVAL;
            unit = UNIT_TB;
        }

        //  0.test005 用来舍入.精确到小数点后两位
        long sizeInt = (long) ((sizeDouble + ROUNDING_OFF) * DECIMAL_NUMBER);
        double formatedSize = ((double) sizeInt) / DECIMAL_NUMBER;

        if (formatedSize == 0) {
            return "0" + " " + unit;
        } else {
            return Double.toString(formatedSize) + " " + unit;
        }
    }

    /**
     * 获取线段上某个点的坐标，长度为a.x - cutRadius
     *  @param a 点
     * @param b 点B
     * @param cutRadius 截断距离
     * @return 截断点
     */
    public static Point getBorderPoint(Point a, Point b, int cutRadius) {
        float radian = getRadian(a, b);
        return new Point(a.x + (int)(cutRadius * Math.cos(radian)), a.x + (int)(cutRadius * Math.sin(radian)));
    }

    //获取水平线夹角弧度
    public static float getRadian (Point a, Point b) {
        float lenA = b.x-a.x;
        float lenB = b.y-a.y;
        float lenC = (float) Math.sqrt(lenA*lenA+lenB*lenB);
        float ang = (float) Math.acos(lenA/lenC);
        ang = ang * (b.y < a.y ? -1 : 1);
        return ang;
    }
}
