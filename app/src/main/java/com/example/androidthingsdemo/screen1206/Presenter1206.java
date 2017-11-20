package com.example.androidthingsdemo.screen1206;

import android.util.Log;

import com.example.androidthingsdemo.TestActivity;

import java.io.IOException;

/**
 * Created by 瑜哥 on 2017/11/11.
 * 1206 液晶显示器
 */

public class Presenter1206 {
    private static final String TAG = TestActivity.class.getSimpleName();

    private static final String I2C_NAME = "I2C1";
    private static final int I2C_ADDRESS = 0x27;
    private LcdPcf8574 lcd;


    public Presenter1206() {
        try {
            lcd = new LcdPcf8574(I2C_NAME, I2C_ADDRESS);
            lcd.begin(16, 2);
            lcd.setBacklight(true);
        } catch (IOException e) {
            Log.e(TAG, "Error initializing LCD", e);
        }
        startTest();
    }

    private void startTest() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        lcd.setBacklight(true);
                        lcd.home();
                        lcd.clear();
                        lcd.print("Hello LCD");
                        int[] heart = {0b00000, 0b01010, 0b11111, 0b11111, 0b11111, 0b01110, 0b00100, 0b00000};
                        lcd.createChar(0, heart);
                        lcd.setCursor(10, 0);
                        lcd.write(0); // write :heart: custom character

                        delay(1000);
                        lcd.setBacklight(false);
                        delay(400);
                        lcd.setBacklight(true);
                        delay(2000);

                        lcd.clear();
                        lcd.print("Cursor On");
                        lcd.cursor();
                        delay(2000);

                        lcd.clear();
                        lcd.print("Cursor Blink");
                        lcd.blink();
                        delay(2000);

                        lcd.clear();
                        lcd.print("Cursor OFF");
                        lcd.noBlink();
                        lcd.noCursor();
                        delay(2000);

                        lcd.clear();
                        lcd.print("Display Off");
                        lcd.noDisplay();
                        delay(2000);

                        lcd.clear();
                        lcd.print("Display On");
                        lcd.display();
                        delay(2000);

                        lcd.clear();
                        lcd.setCursor(0, 0);
                        lcd.print("*** first line.");
                        lcd.setCursor(0, 1);
                        lcd.print("*** second line.");
                        delay(2000);

                        lcd.scrollDisplayLeft();
                        delay(2000);

                        lcd.scrollDisplayLeft();
                        delay(2000);

                        lcd.scrollDisplayLeft();
                        delay(2000);

                        lcd.scrollDisplayRight();
                        delay(2000);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private void delay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Log.e(TAG, "Sleep error", e);
        }
    }


}
