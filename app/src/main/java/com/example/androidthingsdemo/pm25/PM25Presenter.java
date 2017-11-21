package com.example.androidthingsdemo.pm25;

import android.text.TextUtils;
import android.util.Log;

import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.UartDevice;
import com.google.android.things.pio.UartDeviceCallback;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by 瑜哥 on 2017/11/11.
 * PM2.5 PM10传感器使用 UART通讯
 * 上电发送开机命令（AA 01 00 00 00 00 01 66 BB）风扇启动，
 * 发送读数命令（AA 02 00 00 00 00 01 67 BB）即可读取数据。
 * 关机命令（AA 03 00 00 00 00 01 68 BB）
 */

public class PM25Presenter {
    static PM25Presenter pm25Presenter;

    private static String TAG = "PM25Presenter",UART_NAME;

    byte[] open = {(byte) 0xAA, 1, 0, 0, 0, 0, 1, 0x66, (byte) 0xBB};
    byte[] read = {(byte) 0xAA, 2, 0, 0, 0, 0, 1, 0x67, (byte) 0xBB};
    byte[] close = {(byte) 0xAA, 3, 0, 0, 0, 0, 1, 0x68, (byte) 0xBB};
    private UartDevice uartDevice;

    public static PM25Presenter getInstance() {
        if (pm25Presenter==null) {
            pm25Presenter = new PM25Presenter();
        }
        return pm25Presenter;
    }

    private UartDeviceCallback callback = new UartDeviceCallback() {
        @Override
        public boolean onUartDeviceDataAvailable(UartDevice uart) {
            try {
                byte[] bytes = readDatas();
//                closeSenser();
                if (uartListener != null) {
                    String result = getPM25(bytes) + getPM10(bytes);
                    if (!TextUtils.isEmpty(result))
                    uartListener.onReceive(result);
                }
            } catch (IOException e) {
                Log.w(TAG, "Unable to access UART device", e);
            }
            // Continue listening for more interrupts
            return true;
        }

        @Override
        public void onUartDeviceError(UartDevice uart, int error) {
            super.onUartDeviceError(uart, error);
        }
    };

    public PM25Presenter() {
        PeripheralManagerService manager = new PeripheralManagerService();
        List<String> deviceList = manager.getUartDeviceList();
        if (deviceList.isEmpty()) {
            Log.i(TAG, "No UART port available on this device.");
        } else {
            UART_NAME = deviceList.get(0);
            Log.i(TAG, "List of available devices: " + deviceList);
        }
        try {
            uartDevice = manager.openUartDevice(UART_NAME);
            uartDevice.registerUartDeviceCallback(callback);
            uartDevice.setBaudrate(9600);
            uartDevice.setDataSize(8);
            uartDevice.setParity(UartDevice.PARITY_NONE);
            uartDevice.setStopBits(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 释放uart资源引用
     */
    public void releaseSource() {
        if (uartDevice != null) {
            try {
                uartDevice.close();
                uartDevice.unregisterUartDeviceCallback(callback);
                uartDevice = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 读数据
     * @return 获取的数据
     * @throws IOException
     */
    private byte[] readDatas() throws IOException {
        int maxCount = 9;
        byte[] buffer = new byte[maxCount];
        int count;
        while ((count = uartDevice.read(buffer, buffer.length)) < 9) {//********
            Log.d(TAG, "Read " + count + " bytes from peripheral");
        }
        return buffer;
    }

    /**
     * 打开传感器,读取数据
     */
    public void readData() {
        try {
            uartDevice.write(open, open.length);
//            Thread.sleep(1000);
            TimeUnit.SECONDS.sleep(4);
            uartDevice.write(read, read.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭传感器
     */
    private void closeSenser() throws IOException {
        uartDevice.write(close, close.length);
    }

    /**
     * 获取PM2.5指数
     * @return  2017年11月12日 08:25  PM2.5含量:20
     */
    private String getPM25(byte[] bytes){
        String result = "";
        //第四字节*256 +第五字节
        if (chekckSum(bytes)) {
        int i = (0xff&bytes[2]) * 256 + (0xff&bytes[3]);
            result = getFromatDate() + "PM2.5:" + i+"μg/m³";
        }
        return result;
    }


     /**
     * 获取PM10指数
     * @return  2017年11月12日 08:25  PM10含量:20
     */
     private String getPM10(byte[] bytes){
        String result = "";
        //第二字节*256 +第三字节
        if (chekckSum(bytes)) {
             int i = (0xff&bytes[2]) * 256 + (0xff&bytes[3]);
            result = getFromatDate() + "PM10:" + i+"μg/m³";
        }
        return result;
    }


    /**
     * 校验和
     * 字节6*256+字节7 = 剩余字节和
     * @return true 校验成功
     */
    private boolean chekckSum(byte[] bytes) {
        if (bytes[0] == 0 || bytes[8] == 0 ) {
            return false;
        } else {

      return   (0xff&bytes[6]) * 256 +(0xff&bytes[7]) == (0xff&bytes[0])+
                                                                (0xff&bytes[1])+
                                                                (0xff&bytes[2])+
                                                                (0xff&bytes[3])+
                                                                (0xff&bytes[4])+
                                                                (0xff&bytes[5])+
                                                                (0xff&bytes[8]);
        }
    }

    /**
     *
     * @return 2017年11月12日08:25
     */
    private String getFromatDate() {
        String result = "";
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss");
        result = format.format(new Date());
        return result;
    }

    /**
     * 监听数据回调的接口
     */
    public interface onUartReceiveData{
         void onReceive(String data);
    }

    onUartReceiveData uartListener;

    /**
     * 设置监听
     * @param listener
     */
    public void setOnUartListener(onUartReceiveData listener) {
        uartListener = listener;
    }
}
