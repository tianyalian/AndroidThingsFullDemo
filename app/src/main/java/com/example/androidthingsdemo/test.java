package com.example.androidthingsdemo;

/**
 * Created by 瑜哥 on 2017/10/9.
 */

public class test {

    public static  byte[] byte0ne = new byte[115];
    public static  byte[] byteZero = new byte[130];

    public static void initbyte() {
        for (int i = 0;i<35;i++) {
            byteZero[i] = 1;
        }
        for (int i = 0;i<70;i++) {
              byte0ne[i] = 1;
        }
   }

    public static void main(String[] args) {
        initbyte();
        changeColor(10,20,30);
    }

    public static byte[] byteMerger(byte[] byte_1, byte[] byte_2){
        byte[] byte_3 = new byte[byte_1.length+byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }

    public static byte[] parseInt2byte(int a) {
        byte[] chs = new byte[8];
        for(int i = 0; i < 8; i++) {
            chs[8 - 1 - i] = (byte) ((a >> i) & 1);
        }
        return chs;
    }

    public static byte[] convertData(byte[] bytes) {
        byte[] b = null;
        for (int i = 1 ;i<8;i++) {
            if (i < 2) {
                b = byteMerger(bytes[0]==0?byteZero:byte0ne, bytes[1]==0?byteZero:byte0ne);
            } else {
                if (bytes[i] == 0) {
                    b = byteMerger(b, byteZero);
                } else {
                    b =  byteMerger(b, byte0ne);
                }
            }
            System.out.println(b.toString());
        }
        return b;
    }

    public static void changeColor(int red, int green, int blue) {
        //1把int 转换成为 8位二进制byte[]数组封装
        byte[] bytes_red = parseInt2byte(red);
        byte[] bytes_green = parseInt2byte(green);
        byte[] bytes_blue = parseInt2byte(blue);
        //2把byte[]数组中的数据进行转换 0:35个1,80个0  1:70个0,60个1
        bytes_red = convertData(bytes_red);
        bytes_green = convertData(bytes_green);
        bytes_blue = convertData(bytes_blue);
        // 3以grb顺序的数组组成一个完整的byte[]数据
        byte[] bytes = byteMerger(byteMerger(bytes_green, bytes_red), bytes_blue);
        System.out.println(bytes.toString());
    }
}
