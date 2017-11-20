package com.example.androidthingsdemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

import java.io.IOException;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    //LED闪烁时间间隔，单位毫秒
    private int interval_between_blinks_ms = 1000,red = 100,green = 100,blue = 100;

    //控制LED灯闪烁频率控件
    private SeekBar mSeekbar;
    //当前LED等闪烁频率
    private TextView mSeekbarValue;
    private Handler mHandler = new Handler();
    //Gpio接口对象
    private Gpio mLedGpio;
    private Switch switchs;
    private boolean  isBlink =false;
    private SeekBar blue_seekbar;
    private SeekBar green_seekbar;
    private SeekBar red_seekbar;
    private WS2812presenter presenter;
    private PeripheralManagerService service;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SpeechUtility.createUtility(this, SpeechConstant.APPID +"=59e74d3d");
        Log.i(TAG, "Starting BlinkActivity");
        setContentView(R.layout.activity_main);
        mSeekbarValue = (TextView) findViewById(R.id.seekBar_value);
        mSeekbar = (SeekBar) findViewById(R.id.seekBar);
        blue_seekbar = (SeekBar) findViewById(R.id.seekBar_blue);
        green_seekbar = (SeekBar) findViewById(R.id.seekBar_green);
        red_seekbar = (SeekBar) findViewById(R.id.seekBar_red);
         findViewById(R.id.btn_1).setOnClickListener(this);
        findViewById(R.id.btn_2).setOnClickListener(this);
        findViewById(R.id.btn_3).setOnClickListener(this);
        service = new PeripheralManagerService();
        presenter = new WS2812presenter(service);
        blue_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                blue = seekBar.getProgress();
                presenter.changeColor(red,green,blue);
            }
        });

        red_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                red = seekBar.getProgress();
                presenter.changeColor(red,green,blue);
            }
        });

         green_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                green = seekBar.getProgress();
                presenter.changeColor(red,green,blue);
            }
        });



        switchs = (Switch) findViewById(R.id.switch1);
        switchs.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isBlink = true;
                    mHandler.postDelayed(mBlinkRunnable, interval_between_blinks_ms);
                } else {
                    isBlink = false;
                }
            }
        });
        mSeekbar.setProgress(interval_between_blinks_ms);
        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mSeekbarValue.setText("LED between time:" + i + "ms");
                //通过SeekBar控件改变LED等闪烁频率
                interval_between_blinks_ms = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

//         使用指定引脚名称，PeripheralManagerService来打开一个连接到GPIO端口的LED连接

        try {
            String pinName = BoardDefaults.getGPIOForLED();
            mLedGpio = service.openGpio(pinName);
            //设置引脚为输出信号
            mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            Log.i(TAG, "Start blinking LED GPIO pin");

            //Post一个Runnable对象，在指定的时间间隔持续的改变GPIO接口的状态，使得LED等闪烁
            mHandler.post(mBlinkRunnable);
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
        PeripheralManagerService manager = new PeripheralManagerService();
        List<String> deviceList = manager.getSpiBusList();
        if (deviceList.isEmpty()) {
            Log.i(TAG, "No SPI bus available on this device.");
        } else {
            Log.i(TAG, "List of available devices: " + deviceList);
        }
//        try {
//        SpiDevice   mDevice = manager.openSpiDevice("SPI0.0");
//            mDevice.setBitsPerWord(8);
//            mDevice.setFrequency(10000000);
//            mDevice.setMode(SpiDevice.MODE0);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //从Handler中移除blink Runnable对象
        mHandler.removeCallbacks(mBlinkRunnable);
        Log.i(TAG, "Closing LED GPIO pin");
        try {
            //页面销毁，当应用程序不在需要GPIO连接的时候，关闭Gpio资源
            mLedGpio.close();
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        } finally {
            mLedGpio = null;
        }
        presenter.recycleDevice();
    }

    private Runnable mBlinkRunnable = new Runnable() {
        @Override
        public void run() {
            // 如果GPIO引脚已经关闭，则退出Runnable
            if (mLedGpio == null) {
                return;
            }
            try {
                if (isBlink) {
                    //使用setValue()方法传递getValue()相反的值来改变LED的状态；
                    boolean b = !mLedGpio.getValue();
                    Log.d(TAG, "start counter:" + System.currentTimeMillis());
                    mLedGpio.setValue(b);
                    Log.d(TAG, "end counter:" + System.currentTimeMillis());
                    mHandler.postDelayed(mBlinkRunnable, interval_between_blinks_ms);
                }

            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    };


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_1 :
                presenter.sendData(new byte[]{(byte)0xff});
                break;
            case R.id.btn_2 :
                presenter.sendData(new byte[]{0b01010101});

                break;
            case R.id.btn_3 :
                presenter.sendData(new byte[]{0x01});
                break;
        }
    }
}

