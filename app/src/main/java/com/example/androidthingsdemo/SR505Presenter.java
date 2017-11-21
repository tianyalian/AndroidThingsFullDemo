package com.example.androidthingsdemo;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

/**
 * Created by 瑜哥 on 2017/11/5.
 * 人体红外感应开关
 */

public class SR505Presenter {
    static SR505Presenter sr505Presenter;

    private PeripheralManagerService service;
    private Gpio gpio_505;
    private Gpio gpio_led;
    private GpioCallback callback;

    public static SR505Presenter getInstance() {
        if (sr505Presenter == null) {
            sr505Presenter = new SR505Presenter();
        }
        return sr505Presenter;
    }

    /**
     * 开始检测
     */
    public void detect() {
        service = new PeripheralManagerService();
        try {
            gpio_505 = service.openGpio(Constants.GPIO2);
            callback = new GpioCallback() {
                @Override
                public boolean onGpioEdge(Gpio gpio) {
                    try {
                        if (gpio.getValue()) {
                            gpio_led.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
                        } else {
                            gpio_led.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return super.onGpioEdge(gpio);
                }
            };
            gpio_505.registerGpioCallback(callback);
            gpio_505.setDirection(Gpio.DIRECTION_IN);
            gpio_505.setEdgeTriggerType(Gpio.EDGE_BOTH);
            gpio_505.setActiveType(Gpio.ACTIVE_HIGH);
            gpio_led = service.openGpio(Constants.GPIO3);
            gpio_led.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 关闭检测
     */
    public void releaseResource() {
        if (gpio_505 != null && callback != null) {
            gpio_505.unregisterGpioCallback(callback);
        }
    }

}
