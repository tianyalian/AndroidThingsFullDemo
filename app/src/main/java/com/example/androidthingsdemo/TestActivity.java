package com.example.androidthingsdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.androidthingsdemo.ds18b20.DS18B20Presenter;
import com.example.androidthingsdemo.motor.MotorPresenter;

/**
 * Created by 瑜哥 on 2017/10/11.
 */

public class TestActivity extends Activity implements View.OnClickListener {


    private DS18B20Presenter presenter;
    private MotorPresenter motorPresenter;
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
        seekBar_freq.setOnClickListener(this);
        tv_pwm = (TextView) findViewById(R.id.tv_pwm);
        tv_pwm.setOnClickListener(this);
        seekBar_pwm = (SeekBar) findViewById(R.id.seekBar_pwm);
        seekBar_pwm.setOnClickListener(this);
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
                if (motorPresenter == null)
                    motorPresenter = new MotorPresenter();
                motorPresenter.frontRotate();
                break;
            case R.id.back_rotate:
                if (motorPresenter == null)
                    motorPresenter = new MotorPresenter();
                motorPresenter.backRotate();
                break;
            case R.id.detected_people:
                break;
            case R.id.btn_rainb:
                break;
            case R.id.btn_random:
                break;
            case R.id.smoke:
                break;
            case R.id.btn_1206:
                break;
            case R.id.btn_clear:
                break;
        }
    }


}
