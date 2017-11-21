package com.example.androidthingsdemo.driver2801;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.androidthingsdemo.BoardDefaults;

import java.io.IOException;
import java.util.Random;


public class SimpleLedActivity extends Activity {

    private static final String TAG = RainbowLedActivity.class.getSimpleName();

    private Ws2801 mLedstrip;
    private int[] colors;
    int counter = 0;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            counter++;
            try {
//                if (counter%3==0) {
//                    mLedstrip.write(getFullGreen());
//                } else if (counter % 2 == 0) {
//                    mLedstrip.write(getFullRed());
//                } else {
                    mLedstrip.write(getRundam());
//                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            handler.sendEmptyMessageDelayed(0, 500);
        }
    };
    private Random random;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            mLedstrip = Ws2801.create(BoardDefaults.getDefaultSPI(), Ws2801.Mode.RGB);
            colors = new int[]{Color.parseColor("#ffff00"), Color.parseColor("#00ff00"),
                    Color.parseColor("#0000ff"), Color.parseColor("#ffff00"), Color.parseColor("#00ff00"),
                    Color.parseColor("#0000ff"), Color.parseColor("#ffff00"), Color.parseColor("#00ff00"),
                    Color.parseColor("#0000ff"), Color.parseColor("#ffff00"), Color.parseColor("#00ff00"),
                    Color.parseColor("#0000ff"), Color.parseColor("#ffff00"), Color.parseColor("#00ff00"),
                    Color.parseColor("#0000ff"), Color.parseColor("#ffff00"), Color.parseColor("#00ff00"),
                    Color.parseColor("#0000ff"), Color.parseColor("#ffff00"), Color.parseColor("#00ff00"),
                    Color.parseColor("#0000ff"), Color.parseColor("#ffff00"), Color.parseColor("#00ff00"),
                    Color.parseColor("#0000ff"), Color.parseColor("#ffff00"), Color.parseColor("#00ff00"),
                    Color.parseColor("#0000ff"), Color.parseColor("#ffff00"), Color.parseColor("#00ff00"),
                    Color.parseColor("#0000ff"), Color.parseColor("#ffff00"), Color.parseColor("#00ff00"),
                    Color.parseColor("#0000ff"), Color.parseColor("#ffff00"), Color.parseColor("#00ff00"),
                    Color.parseColor("#0000ff"), Color.parseColor("#ffff00"), Color.parseColor("#00ff00"),
                    Color.parseColor("#0000ff"), Color.parseColor("#ffff00"), Color.parseColor("#00ff00"),
                    Color.parseColor("#0000ff")};
            mLedstrip.write(colors);
            random = new Random();
            Log.d(TAG, "Done!");
        } catch (IOException e) {
            Log.e(TAG, "Error initializing LED strip", e);
        }

        handler.sendEmptyMessageDelayed(0, 500);
    }


    public int[] getFullGreen() {
        colors = new int[33];
        for (int j=0;j<33;j++) {
            colors[j] =  Color.parseColor("#00ff00");
        }
        return colors;
    }

    public int[] getFullRed() {
        colors = new int[33];
        for (int j=0;j<33;j++) {
            colors[j] =  Color.parseColor("#ff8981");
        }
        return colors;
    }

    public int[] getRundam() {
        for (int j=0;j<33;j++) {
//            colors = new int[33];
            int red = random.nextInt(220) + 20; // 0 ----255    220
            int green = random.nextInt(220) + 20;
            int blue = random.nextInt(220) + 20; // 20---239
            colors[j] =  Color.rgb(red, green, blue);
        }
        return colors;
    }


    @Override
    public void onDestroy() {
        try {
            mLedstrip.close();
        } catch (Exception e) {
            Log.e(TAG, "Exception closing LED strip", e);
        }
        super.onDestroy();
    }

}

