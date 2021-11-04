package me.shetj.sdk.ffmepg.demo;

import android.graphics.PointF;
import android.graphics.Rect;

public class DataBean {

    public Rect mRect; // 其他类
    public PointF[] mPoints; // 其它类数组
    public Inner mInner; // 静态内部类

    public int mID; // 整型
    public float mScore; // 浮点型
    public byte[] mData; // 基本类型数组
    public int[][] mDoubleDimenArray; // 二维数组

    public static class Inner {
        public String mMessage; // 字符串
    }
}
