package com.example.androidthingsdemo.ds18b20;

import com.example.androidthingsdemo.Constants;
import com.example.androidthingsdemo.HexUtil;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

import static com.google.android.things.pio.Gpio.EDGE_RISING;

/**
 * Created by 瑜哥 on 2017/11/4.
 * 18b20 操作的presenter
 */

public class DS18B20Presenter {

    PeripheralManagerService service ;
    Gpio gpio;
    private GpioCallback callback;


    public DS18B20Presenter() {
        this.service = new PeripheralManagerService();
        try {
            gpio = service.openGpio(Constants.GPIO1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        callback = new GpioCallback() {
            @Override
            public boolean onGpioEdge(Gpio gpio) {
                try {
                    if (i >= 0) {
                        if (gpio.getValue()) {
                            data[i] = 0;

                        } else {
                            data[i] = 1;
                        }
                        i--;
                    } else {
                        parseTemp(data);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return super.onGpioEdge(gpio);
            }
        };
    }

    /**
     * 获取当前温度
     * @return float,保留两位小数的温度值
     */
    public String getCurrentTemp() {
        String temp = "0.0";
        convertTemp();
        readTemp();
        return temp;
    }


    /**
     * 1、主机先作个复位操作
     * 2、主机再写跳过ROM的操作(CCH)命令，
     *  3、然后主机接着写个转换温度的操作命令，后面释放总线至少一秒，
     */
    private void convertTemp() {
        try {
            if (shakeHands()) {
                //写跳过ROM的操作(CCH)命令
                snedData(0xCC);
                //  主机发出转换温度的指令(44H)
                snedData(0x44);
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取RAM内的温度数据。
     1、主机发出复位操作并接收DS18B20的应答(存在)脉冲。
     2、主机发出跳过对ROM操作的命令(CCH)。
     3、主机发出读取RAM的命令(BEH)，
     */
    private void readTemp() {
        try {
            if (shakeHands()) {
                //写跳过ROM的操作(CCH)命令
                snedData(0xCC);
                //  主机发出读取RAM的命令(BEH)
                snedData(0xBE);
                receiveData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * 把获取的二进制转换为可读温度
     */
    private void parseTemp(byte[] data) {
        int front=0,last = 0;
        for (int i=0;i<12;i++) {
            if (i < 4) {
                last += (int) Math.pow(2, i - 4);
            } else {
                front+= (int) Math.pow(2, i - 4);
            }
        }
        String temp = front + "." + last;
    }

    /**
     * 发送二进制数
     * @param b
     */
    private void snedData(int b) {
        int period = 100000;//单位纳秒
        try {
            byte[]  bytes = HexUtil.parseInt2byte(b);
            for (int i = 0;i<8;i++) {
                if (bytes[7-i] == 0) {
                    gpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
                } else {
                    gpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
                }
                Thread.sleep(0,period);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 发握手信息
     * 每次读写前对 DS18B20 进行复位初始化。复位要求主 CPU 将数据线下拉 500us ，然后释放，
     * DS18B20 收到信号后等待 16us~60us 左右，然后发出60us~240us 的存在低脉冲，主 CPU 收到此信号后表示复位成功。
     * @return true 握手成功
     */
    private boolean shakeHands() {
        boolean result = false;
        try {
            //设置引脚为输入信号
            gpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            //将数据线下拉 500us
            Thread.sleep(0,500000);
            //然后释放总线变为高电平
            gpio.setValue(false);
            //设置有效模式为低电平有效
            gpio.setActiveType(Gpio.ACTIVE_LOW);
            gpio.registerGpioCallback(callback);
            gpio.setDirection(Gpio.DIRECTION_IN);
//           gpio.setEdgeTriggerType(Gpio.EDGE_FALLING);  .ServiceSpecificException
            Thread.sleep(0,60000);
            //检测到低电平
            result = gpio.getValue();
            if (result) {
                Thread.sleep(0,180000);
                gpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 接受从机发来的数据
     * @return
     */
    byte[] data = new byte[16];
                    int i = 15;
    private void receiveData() {
        try {
            gpio.setDirection(Gpio.DIRECTION_IN);
            gpio.setActiveType(Gpio.ACTIVE_LOW);
            gpio.setEdgeTriggerType(EDGE_RISING);
            i = 15;
            gpio.registerGpioCallback(callback);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
