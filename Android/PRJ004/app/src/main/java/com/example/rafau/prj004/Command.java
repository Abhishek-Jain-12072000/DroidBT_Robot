package com.example.rafau.prj004;

/**
 * Created by rafal on 2016-03-18.
 */
public class Command {

    public static String motor(int right, int left, int max) {
        if (right > 50) {
            right = 50;
        }
        if (right < -50) {
            right = -50;
        }
        if (left > 50) {
            left = 50;
        }
        if (left < -50) {
            left = -50;
        }
        return ("motor," + right + "," + left + "," + max);
    }

    public static String motor(int right, int left) {
        if (right > 50) {
            right = 50;
        } else if (right < -50) {
            right = -50;
        }
        if (left > 50) {
            left = 50;
        } else if (left < -50) {
            left = -50;
        }
        return ("motor," + right + "," + left);
    }

    public static String line() {
        return ("line");
    }

    public static String ping(int horiz, int vert) {
        if (horiz > 180)  //ograniczenie z góry wielkośći wejsciowej
            horiz = 180;
        else if (horiz < 0) //ograniczenie z dołu wielkośći wejsciowej
            horiz = 0;

        if (vert > 180)
            vert = 180;
        else if (vert < 0)
            vert = 0;

        return ("ping," + horiz + "," + vert);  //gotowa komenda
    }

    public static String measure() {
        return ("measure");
    }

    public static String radar() {
        return ("radar");
    }

    public static String arm(int val) {
        if (val > 100)
            val = 100;
        return ("arm," + val);
    }

    public static String trim(int rightTrim, int leftTrim) {
        return ("trim," + rightTrim + "," + leftTrim);
    }

    public static String regPD(int Kp, int Kd) {
        return "regPD," + Kp + "," + Kd;
    }

    public static String trim() {
        return ("trim");
    }

    public static String regPD() {
        return ("regPD");
    }

    public static byte crc8_ccitt_update(byte inCrc, byte inData) {
        byte data = (byte) (inCrc ^ inData);

        for (int i = 0; i < 8; i++) {
            if ((data & 0x80) != 0) {
                data <<= 1;
                data ^= 0x07;
            } else {
                data <<= 1;
            }
        }
        return data;
    }

    public static byte[] countAndAddCRC(String command) {
        String in = command + "@_";
        byte buffer[] = (in).getBytes();
        byte crc = 0;
        int i;
        for (i = 0; i < buffer.length; i++) {
            if (buffer[i] != 64) {
                crc = crc8_ccitt_update(crc, buffer[i]);
            } else {
                buffer[i] = crc;
                buffer[i + 1] = 10;
                break;
            }
        }
        String out = new String(buffer);
        return buffer;
    }

}

