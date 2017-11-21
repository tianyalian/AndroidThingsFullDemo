package com.example.androidthingsdemo.pwmspeaker;

import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.Pwm;

import java.io.IOException;

/**
 * Created by 瑜哥 on 2017/11/21.
 */

public class PWMPresenter {
    static PWMPresenter pwmPresenter;
    private   Pwm device;
    private float frequency = 50.0f ,duty = 8000f;

    public static PWMPresenter getInstance() {
        if (pwmPresenter == null) {
            pwmPresenter = new PWMPresenter();
        }
        return pwmPresenter;
    }

    public PWMPresenter() {
        try {
        PeripheralManagerService pioService = new PeripheralManagerService();
            device = pioService.openPwm("PWM1");
            device.setPwmDutyCycle(50.0);
            device.setPwmFrequencyHz(frequency);
            device.setEnabled(true);
        } catch (IOException|RuntimeException e) {

        }
    }

    public void setFrequency(float frequency) {
        try {
            device.setPwmFrequencyHz(frequency);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setDuty(float duty) {
        try {
            device.setPwmDutyCycle(duty);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void releaseResource()  {
        if (device!= null) {
            try {
                device.setEnabled(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
