package com.example.androidthingsdemo.screen1206;

import android.util.Log;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

/**
 * Created by 瑜哥 on 2017/11/14.
 */

public class LcdPcf8574 implements AutoCloseable {

    // commands
    private static final int LCD_CLEARDISPLAY = 0x01;
    private static final int LCD_RETURNHOME = 0x02;
    private static final int LCD_ENTRYMODESET = 0x04;
    private static final int LCD_DISPLAYCONTROL = 0x08;
    private static final int LCD_CURSORSHIFT = 0x10;
    private static final int LCD_FUNCTIONSET = 0x20;
    private static final int LCD_SETCGRAMADDR = 0x40;
    private static final int LCD_SETDDRAMADDR = 0x80;

    // flags for display entry mode
    private static final int LCD_ENTRYRIGHT = 0x00;
    private static final int LCD_ENTRYLEFT = 0x02;
    private static final int LCD_ENTRYSHIFTINCREMENT = 0x01;
    private static final int LCD_ENTRYSHIFTDECREMENT = 0x00;

    // flags for display on/off control
    private static final int LCD_DISPLAYON = 0x04;
    private static final int LCD_DISPLAYOFF = 0x00;
    private static final int LCD_CURSORON = 0x02;
    private static final int LCD_CURSOROFF = 0x00;
    private static final int LCD_BLINKON = 0x01;
    private static final int LCD_BLINKOFF = 0x00;

    // flags for display/cursor shift
    private static final int LCD_DISPLAYMOVE = 0x08;
    private static final int LCD_CURSORMOVE = 0x00;
    private static final int LCD_MOVERIGHT = 0x04;
    private static final int LCD_MOVELEFT = 0x00;

    // flags for function set
    private static final int LCD_8BITMODE = 0x10;
    private static final int LCD_4BITMODE = 0x00;
    private static final int LCD_2LINE = 0x08;
    private static final int LCD_1LINE = 0x00;
    private static final int LCD_5x10DOTS = 0x04;
    private static final int LCD_5x8DOTS = 0x00;

    private static final int LOW = 0x0;

    // These are Bit-Masks for the special signals and background light
    private static final int PCF_RS = 0x01;
    private static final int PCF_RW = 0x02;
    private static final int PCF_EN = 0x04;
    private static final int PCF_BACKLIGHT = 0x08;

    // Definitions on how the PCF8574 is connected to the LCD
    // These are Bit-Masks for the special signals and Background
    private static final int RSMODE_CMD = 0;
    private static final int RSMODE_DATA = 1;

    private boolean backlight; // use backlight

    private byte displayFunction; // lines and dots mode
    private byte displayControl; // cursor, display, blink flags
    private byte displayMode; // left2right, autoscroll

    private int numLines; // The number of rows the display supports.

    private I2cDevice device;

    public LcdPcf8574(String i2cName, int i2cAddress) throws IOException {
        PeripheralManagerService manager = new PeripheralManagerService();
        device = manager.openI2cDevice(i2cName, i2cAddress);
    }

    @Override
    public void close() throws Exception {
        if (device != null) {
            try {
                device.close();
            } finally {
                device = null;
            }
        }
    }

    public void begin(int cols, int rows) throws IOException {
        begin(cols, rows, LCD_5x8DOTS);
    }

    public void begin(int cols, int rows, int charsize) throws IOException {
        // cols ignored !
        numLines = rows;

        displayFunction = 0;

        if (rows > 1) {
            displayFunction |= LCD_2LINE;
        }

        // for some 1 line displays you can select a 10 pixel high font
        if ((charsize != 0) && (rows == 1)) {
            displayFunction |= LCD_5x10DOTS;
        }

        // SEE PAGE 45/46 FOR INITIALIZATION SPECIFICATION!
        // according to datasheet, we need at least 40ms after power rises above 2.7V
        // before sending commands.

        // initializing th display
        write2Wire((byte) 0x00, LOW, false);
        delayMicroseconds(50000);

        // put the LCD into 4 bit mode according to the hitachi HD44780 datasheet figure 26, pg 47
        sendNibble((byte) 0x03, RSMODE_CMD);
        delayMicroseconds(4500);
        sendNibble((byte) 0x03, RSMODE_CMD);
        delayMicroseconds(4500);
        sendNibble((byte) 0x03, RSMODE_CMD);
        delayMicroseconds(150);
        // finally, set to 4-bit interface
        sendNibble((byte) 0x02, RSMODE_CMD);

        // finally, set # lines, font size, etc.
        command(LCD_FUNCTIONSET | displayFunction);

        // turn the display on with no cursor or blinking default
        displayControl = LCD_DISPLAYON | LCD_CURSOROFF | LCD_BLINKOFF;
        display();

        // clear it off
        clear();

        // Initialize to default text direction (for romance languages)
        displayMode = LCD_ENTRYLEFT | LCD_ENTRYSHIFTDECREMENT;
        // set the entry mode
        command(LCD_ENTRYMODESET | displayMode);
    }

    public void clear() throws IOException {
        command(LCD_CLEARDISPLAY);  // clear display, set cursor position to zero
        delayMicroseconds(2000); // this command takes a long time!
    }

    public void home() throws IOException {
        command(LCD_RETURNHOME);  // set cursor position to zero
        delayMicroseconds(2000); // this command takes a long time!
    }

    // Set the cursor to a new position.
    public void setCursor(int col, int row) throws IOException {
        int row_offsets[] = {0x00, 0x40, 0x14, 0x54};
        if (row >= numLines) {
            row = numLines - 1;    // we count rows starting w/0
        }

        command(LCD_SETDDRAMADDR | (col + row_offsets[row]));
    }

    // Turn the display on/off (quickly)
    public void noDisplay() throws IOException {
        displayControl &= ~LCD_DISPLAYON;
        command(LCD_DISPLAYCONTROL | displayControl);
    }

    public void display() throws IOException {
        displayControl |= LCD_DISPLAYON;
        command(LCD_DISPLAYCONTROL | displayControl);
    }

    // Turn on and off the blinking cursor
    public void noBlink() throws IOException {
        displayControl &= ~LCD_BLINKON;
        command(LCD_DISPLAYCONTROL | displayControl);
    }

    public void blink() throws IOException {
        displayControl |= LCD_BLINKON;
        command(LCD_DISPLAYCONTROL | displayControl);
    }

    // Turns the underline cursor on/off
    public void noCursor() throws IOException {
        displayControl &= ~LCD_CURSORON;
        command(LCD_DISPLAYCONTROL | displayControl);
    }

    public void cursor() throws IOException {
        displayControl |= LCD_CURSORON;
        command(LCD_DISPLAYCONTROL | displayControl);
    }

    // These commands scroll the display without changing the RAM
    public void scrollDisplayLeft() throws IOException {
        command(LCD_CURSORSHIFT | LCD_DISPLAYMOVE | LCD_MOVELEFT);
    }

    public void scrollDisplayRight() throws IOException {
        command(LCD_CURSORSHIFT | LCD_DISPLAYMOVE | LCD_MOVERIGHT);
    }

    // This is for text that flows Left to Right
    public void leftToRight() throws IOException {
        displayMode |= LCD_ENTRYLEFT;
        command(LCD_ENTRYMODESET | displayMode);
    }

    // This is for text that flows Right to Left
    public void rightToLeft() throws IOException {
        displayMode &= ~LCD_ENTRYLEFT;
        command(LCD_ENTRYMODESET | displayMode);
    }

    // This will 'right justify' text from the cursor
    public void autoscroll() throws IOException {
        displayMode |= LCD_ENTRYSHIFTINCREMENT;
        command(LCD_ENTRYMODESET | displayMode);
    }

    // This will 'left justify' text from the cursor
    public void noAutoscroll() throws IOException {
        displayMode &= ~LCD_ENTRYSHIFTINCREMENT;
        command(LCD_ENTRYMODESET | displayMode);
    }

    // Setting the brightness of the background display light.
    // The backlight can be switched on and off.
    // The current brightness is stored in the private backlight variable to have it available for further data transfers.
    public void setBacklight(boolean enable) throws IOException {
        backlight = enable;
        // send no data but set the background-pin right;
        write2Wire((byte) 0x00, RSMODE_DATA, false);
    }

    // Allows us to fill the first 8 CGRAM locations with custom characters
    public void createChar(int location, int[] charmap) throws IOException {
        location &= 0x7; // we only have 8 locations 0-7
        command(LCD_SETCGRAMADDR | (location << 3));
        for (int i = 0; i < 8; i++) {
            write(charmap[i]);
        }
    }

    /* The write function is needed for derivation from the Print class. */
    public void write(int value) throws IOException {
        send(value, RSMODE_DATA);
    }

    public void print(String message) throws IOException {
        for (int i = 0; i < message.length(); i++) {
            write(message.charAt(i));
        }
    }

    // low level functions
    private void command(int value) throws IOException {
        send(value, RSMODE_CMD);
    }

    // write either command or data
    private void send(int value, int mode) throws IOException {
        // separate the 4 value-nibbles
        byte valueLo = (byte) (value & 0x0F);
        byte valueHi = (byte) (value >> 4 & 0x0F);

        sendNibble(valueHi, mode);
        sendNibble(valueLo, mode);
    }

    // write a nibble / halfByte with handshake
    private void sendNibble(byte halfByte, int mode) throws IOException {
        write2Wire(halfByte, mode, true);
        delayMicroseconds(1);    // enable pulse must be >450ns
        write2Wire(halfByte, mode, false);
        delayMicroseconds(37); // commands need > 37us to settle
    }

    // private function to change the PCF8674 pins to the given value
    private void write2Wire(byte halfByte, int mode, boolean enable) throws IOException {
        // map the given values to the hardware of the I2C schema
        byte i2cData = (byte) (halfByte << 4);
        if (mode > 0) i2cData |= PCF_RS;
        // PCF_RW is never used.
        if (enable) i2cData |= PCF_EN;
        if (backlight) i2cData |= PCF_BACKLIGHT;

        device.write(new byte[]{i2cData}, 1);
    }

    private void delayMicroseconds(long microseconds) {
        try {
            Thread.sleep(Math.max(1, Math.round(0.001d * microseconds)));
        } catch (InterruptedException e) {
            Log.e(LcdPcf8574.class.getSimpleName(), "Sleep error", e);
        }
    }
}