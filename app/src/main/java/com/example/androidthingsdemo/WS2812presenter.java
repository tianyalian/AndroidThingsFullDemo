package com.example.androidthingsdemo;

import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.SpiDevice;

import java.io.IOException;

/**
 * Created by 瑜哥 on 2017/10/8.
 * 2812 操作的presenter代理
 */

public class WS2812presenter {


    private static final String TAG = "WS2812presenter";
    private  SpiDevice mDevice ;
    public  byte[] byte0ne = new byte[16];
    public  byte[] byteZero = new byte[14];
    private final PeripheralManagerService service;

    public  void initbyte() {
        for (int i = 0;i<4;i++) {
            byteZero[i] = (byte) 0xff;
        }
        for (int i = 0;i<9;i++) {
            byte0ne[i] =(byte) 0xff;
        }
    }

    public WS2812presenter(PeripheralManagerService service) {
        this.service = service;
        try {
            mDevice = service.openSpiDevice("SPI0.0");
            mDevice.setBitsPerWord(8);//12
//            mDevice.setFrequency(100000000);
            mDevice.setFrequency(1);
            mDevice.setMode(SpiDevice.MODE0);
            mDevice.setBitJustification(false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        initbyte();
    }

    /**
     * 发送颜色数据
     * @param bytes 转化后的byte
     */
    public void sendData(byte[] bytes) {
        if (mDevice != null) {
            try {
                mDevice.write(bytes,bytes.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 垃圾回收
     */
    public void recycleDevice() {
        if (mDevice != null) {
            try {
                mDevice.close();
                mDevice = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 改变灯的颜色
     * @param red
     * @param green
     * @param blue
     */
    public void changeColor(int red,int green,int blue) {
        //1把int 转换成为 8位二进制byte[]数组封装
        byte[] bytes_red = parseInt2byte(red);
        byte[] bytes_green = parseInt2byte(green);
        byte[] bytes_blue = parseInt2byte(blue);
        //2把byte[]数组中的数据进行转换 0:4个1,8个0  1:6个0,6个1
        bytes_red = convertData(bytes_red);
        bytes_green = convertData(bytes_green);
        bytes_blue = convertData(bytes_blue);
        // 3以grb顺序的数组组成一个完整的byte[]数据
        byte[] bytes = byteMerger(byteMerger(bytes_green, bytes_red), bytes_blue);
//        sendData(bytes);
        sendData(new byte[]{(byte) 0xff,0,0,(byte) 0xff});
    }

    /**
     * 把int 转为8位二进制字节数组
     * @param a
     * @return
     */
    public  byte[] parseInt2byte(int a) {
        byte[] chs = new byte[8];
        for(int i = 0; i < 8; i++) {
            chs[8 - 1 - i] = (byte) ((a >> i) & 1);
        }
        return chs;
    }


    /**
     * 把byte字节中的0或1转换为ws2812需要的0和1
     *  ws2812 0:35个1,80个0  1:70个0,60个1
     * @return
     */
    public byte[] convertData(byte[] bytes) {
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
        }
        return b;
    }

    /**
     * 字节数组拼接
     * @param byte_1
     * @param byte_2
     * @return
     */
    public  byte[] byteMerger(byte[] byte_1, byte[] byte_2){
        byte[] byte_3 = new byte[byte_1.length+byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }
}
