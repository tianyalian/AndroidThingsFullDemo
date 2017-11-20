package com.example.androidthingsdemo;

/**
 * Created by 瑜哥 on 2017/11/4.
 */

public class HexUtil {
    /**
     * 把int 转为8位二进制字节数组
     * @param a
     * @return
     */
    public static byte[] parseInt2byte(int a) {
        byte[] chs = new byte[8];
        for(int i = 0; i < 8; i++) {
            chs[8 - 1 - i] = (byte) ((a >> i) & 1);
        }
        return chs;
    }

    /**
     * byte 转为8位二进制字节数组
     * @param a
     * @return
     */
    public static byte[] parsebyte2bin(byte a) {
        byte[] chs = new byte[8];
        for(int i = 0; i < 8; i++) {
            chs[8 - 1 - i] = (byte) ((a >> i) & 1);
        }
        return chs;
    }
}
