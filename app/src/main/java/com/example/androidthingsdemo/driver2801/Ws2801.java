package com.example.androidthingsdemo.driver2801;

import android.graphics.Color;
import android.util.Log;

import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.SpiDevice;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static android.content.ContentValues.TAG;


public class Ws2801 implements AutoCloseable {

    public static Ws2801 ws2801;
    private float hue = 0.0f;
    private float increment = 0.002f;
    private static final int NUM_LEDS = 33;
    private final int[] mLedColors = new int[NUM_LEDS];

    private static final int SPI_BPW = 8;
    private static final int SPI_FREQUENCY = 1_000_000;
    private static final int SPI_MODE = SpiDevice.MODE0;
    private static final int WS2801_PACKET_LENGTH = 3;
    private Disposable subscribe;

    public static Ws2801 getInstance() {
        if (ws2801 == null) {
            try {
                ws2801 = Ws2801.create("SPI0.0", Mode.RBG);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ws2801;
    }
    /**
     * Color ordering for the RGB LED messages; the most common modes are BGR and RGB.
     */
    public enum Mode {
        RGB,
        RBG,
        GRB,
        GBR,
        BRG,
        BGR
    }

    public enum Direction {
        NORMAL,
        REVERSED,
    }

    private final SpiDevice device;
    private final ColorUnpacker colorUnpacker;

    private final Direction direction;


    public static Ws2801 create(String spiBusPort) throws IOException {
        return create(spiBusPort, Mode.RGB);
    }


    public static Ws2801 create(String spiBusPort, Mode ledMode) throws IOException {
        return create(spiBusPort, ledMode, Direction.NORMAL);
    }


    public static Ws2801 create(String spiBusPort, Mode ledMode, Direction direction) throws IOException {
        PeripheralManagerService pioService = new PeripheralManagerService();
        try {
            return new Ws2801(pioService.openSpiDevice(spiBusPort), new ColorUnpacker(ledMode), direction);
        } catch (IOException e) {
            throw new IOException("Unable to open SPI device in bus port " + spiBusPort, e);
        }
    }

    Ws2801(SpiDevice device, ColorUnpacker colorUnpacker, Direction direction) throws IOException {
        this.device = device;
        this.colorUnpacker = colorUnpacker;
        this.direction = direction;
        configure(device);
    }

    private static void configure(SpiDevice device) throws IOException {
        device.setFrequency(SPI_FREQUENCY);
        device.setMode(SPI_MODE);
        device.setBitsPerWord(SPI_BPW);
    }


    public void write(int[] colors) throws IOException {
        byte[] ledData = new byte[WS2801_PACKET_LENGTH * colors.length];

        for (int i = 0; i < colors.length; i++) {
            int outputPosition = i * WS2801_PACKET_LENGTH;
            int di = direction == Direction.NORMAL ? i : colors.length - i - 1;
            System.arraycopy(colorUnpacker.unpack(colors[di]), 0, ledData, outputPosition, WS2801_PACKET_LENGTH);
        }

        device.write(ledData, ledData.length);
    }

    private int hue2Rgb() {
        float l = 0.5f; // luminosity?
        float s = 0.8f; // saturation?

        float q = l < 0.5f ? l * (1 + s) : l + s - l * s;
        float p = 2f * l - q;

        int r = (int) (convert(p, q, hue + (1 / 3f)) * 255);
        int g = (int) (convert(p, q, hue) * 255);
        int b = (int) (convert(p, q, hue - (1 / 3f)) * 255);

        return Color.rgb(r, g, b);
    }

    private float convert(float p, float q, float t) {
        if (t < 0) t += 1f;
        if (t > 1) t -= 1f;
        if (t < 1 / 6f) return p + (q - p) * 6f * t;
        if (t < 1 / 2f) return q;
        if (t < 2 / 3f) return p + (q - p) * (2 / 3f - t) * 6f;
        return p;
    }

    /**
     * 开始颜色变化
     */
    public void startFlash() {

        subscribe = Observable.interval(20, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        try {
                            Arrays.fill(mLedColors, hue2Rgb()); // all LEDs will have the same color
                            write(mLedColors);

                            hue += increment;
                            if (hue >= 1.0f || hue <= 0.0f) {
                                hue = Math.max(0.0f, Math.min(hue, 1.0f));
                                increment = -increment;
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Error while writing to LED strip", e);
                        }
                    }
                });

    }


    /**
     * Releases the SPI interface.
     */
    @Override
    public void close(){
        try {
            device.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (subscribe!=null) {
         subscribe.dispose();
        }
    }

}
