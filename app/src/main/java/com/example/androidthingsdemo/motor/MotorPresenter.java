package com.example.androidthingsdemo.motor;

import com.example.androidthingsdemo.Constants;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

/**
 * Created by 瑜哥 on 2017/11/4.
 * 操作五线四相电动机的对象
 */

public class MotorPresenter {

    private Gpio gpio3;
    private Gpio gpio4;
    private Gpio gpio2;
    private Gpio gpio1;
    char[] F_Rotation={0x03,0x06,0b1100,0b1001};//正转表格
    char[] B_Rotation={0b1001,0b1100,0x06,0x03};//反转表格

    PeripheralManagerService service;

    public MotorPresenter() {
        service = new PeripheralManagerService();
        try {
            gpio1 = service.openGpio(Constants.GPIO1);
            gpio2 = service.openGpio(Constants.GPIO2);
            gpio3 = service.openGpio(Constants.GPIO3);
            gpio4 = service.openGpio(Constants.GPIO4);

            gpio1.setActiveType(Gpio.ACTIVE_LOW);
            gpio1.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            gpio2.setActiveType(Gpio.ACTIVE_LOW);
            gpio2.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            gpio3.setActiveType(Gpio.ACTIVE_LOW);
            gpio3.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            gpio4.setActiveType(Gpio.ACTIVE_LOW);
            gpio4.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 正转
     */
    public void frontRotate() {
        for (int i=0;i<100;i++) {
            try {
                sendData(F_Rotation);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    /**
     * 反转
     */
    public void backRotate() {
        for (int i=0;i<100;i++) {
            try {
                sendData(B_Rotation);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 发送数据
     */
    private void sendData(char[] data) throws IOException, InterruptedException {
        for (char c :data) {
            switch (c) {
                case 3:
                    gpio1.setValue(false);
                    gpio2.setValue(false);
                    gpio3.setValue(true);
                    gpio4.setValue(true);
                    break;
                case 6:
                    gpio1.setValue(true);
                    gpio2.setValue(false);
                    gpio3.setValue(false);
                    gpio4.setValue(true);
                    break;
                case 12:
                    gpio1.setValue(true);
                    gpio2.setValue(true);
                    gpio3.setValue(false);
                    gpio4.setValue(false);
                    break;
                case 9:
                    gpio1.setValue(false);
                    gpio2.setValue(true);
                    gpio3.setValue(true);
                    gpio4.setValue(false);
                    break;
            }
            Thread.sleep(5);
        }
    }


}
