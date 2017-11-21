package com.example.androidthingsdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.androidthingsdemo.driver2801.Ws2801;
import com.example.androidthingsdemo.ds18b20.DS18B20Presenter;
import com.example.androidthingsdemo.motor.MotorPresenter;
import com.example.androidthingsdemo.pm25.PM25Presenter;
import com.example.androidthingsdemo.pwmspeaker.PWMPresenter;
import com.example.androidthingsdemo.screen1206.Presenter1206;

/**
 * Created by 瑜哥 on 2017/10/11.
 */

public class TestActivity extends Activity implements View.OnClickListener {

    private DS18B20Presenter presenter;
    private TextView tv_freq;
    private SeekBar seekBar_freq;
    private TextView tv_pwm;
    private SeekBar seekBar_pwm;
    private Button front_rotate;
    private Button back_rotate;
    private Button detected_people;
    private Button btn_rainb;
    private Button btn_random;
    private TextView tv_text;
    private Button smoke;
    private Button btn_1206;
    private Button btn_clear;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        tv_freq = (TextView) findViewById(R.id.tv_freq);
        tv_freq.setOnClickListener(this);
        seekBar_freq = (SeekBar) findViewById(R.id.seekBar_freq);
        seekBar_freq.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                tv_freq.setText(seekBar.getProgress()+"HZ");
                if (seekBar.getProgress() == 0) {
                    PWMPresenter.getInstance().releaseResource();
                } else {
                  PWMPresenter.getInstance().setFrequency(seekBar.getProgress());
                }
            }
        });
        tv_pwm = (TextView) findViewById(R.id.tv_pwm);
        tv_pwm.setOnClickListener(this);
        seekBar_pwm = (SeekBar) findViewById(R.id.seekBar_pwm);
        seekBar_pwm.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                tv_pwm.setText(seekBar.getProgress()+"%");
                PWMPresenter.getInstance().setDuty(seekBar.getProgress());
            }
        });
        front_rotate = (Button) findViewById(R.id.front_rotate);
        front_rotate.setOnClickListener(this);
        back_rotate = (Button) findViewById(R.id.back_rotate);
        back_rotate.setOnClickListener(this);
        detected_people = (Button) findViewById(R.id.detected_people);
        detected_people.setOnClickListener(this);
        btn_rainb = (Button) findViewById(R.id.btn_rainb);
        btn_rainb.setOnClickListener(this);
        btn_random = (Button) findViewById(R.id.btn_random);
        btn_random.setOnClickListener(this);
        tv_text = (TextView) findViewById(R.id.tv_text);
        tv_text.setOnClickListener(this);
        smoke = (Button) findViewById(R.id.smoke);
        smoke.setOnClickListener(this);
        btn_1206 = (Button) findViewById(R.id.btn_1206);
        btn_1206.setOnClickListener(this);
        btn_clear = (Button) findViewById(R.id.btn_clear);
        btn_clear.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.front_rotate:
                MotorPresenter.getInstance().frontRotate();
                break;
            case R.id.back_rotate:
                MotorPresenter.getInstance().backRotate();
                break;
            case R.id.detected_people:
                SR505Presenter.getInstance().detect();
                break;
            case R.id.btn_rainb:

                break;
            case R.id.btn_random:
                Ws2801.getInstance().startFlash();
                break;
           case R.id.smoke:
                PM25Presenter.getInstance().setOnUartListener(new PM25Presenter.onUartReceiveData() {
                    @Override
                    public void onReceive(String data) {
                        tv_text.setText(data);
                    }
                });
               PM25Presenter.getInstance().readData();
                break;
            case R.id.btn_1206:
                Presenter1206.getInstance().startTest();
                break;
            case R.id.btn_clear:
                releaseSource();
                break;
        }
    }

    /**
     * 释放所有资源
     */
    private void releaseSource() {
        MotorPresenter.getInstance().releaseSource();
        SR505Presenter.getInstance().releaseResource();
        Ws2801.getInstance().close();
        Presenter1206.getInstance().releaseSource();
        PM25Presenter.getInstance().releaseSource();
    }

}
