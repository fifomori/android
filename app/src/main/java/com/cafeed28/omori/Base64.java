package com.cafeed28.omori;

// http://www.java2s.com/Code/Java/Development-Class/OneofthefastestimplementationoftheBase64encodingJakartaandothersareslower.htm
public class Base64 {
    private static final char[] BASE64_CHAR = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
            'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd',
            'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
            'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
            'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', '+', '/'
    };
    private static final char BASE64_PAD = '=';

    public static String encode(byte[] data) {
        int len = data.length;
        if (len == 0) {
            return "";
        }
        char[] out = new char[(len / 3 << 2) + 4];
        int rindex = 0;
        int windex = 0;
        int rest = len;
        while (rest >= 3) {
            int i = ((data[rindex] & 0xff) << 16)
                    + ((data[rindex + 1] & 0xff) << 8)
                    + (data[rindex + 2] & 0xff);
            out[windex++] = BASE64_CHAR[i >> 18];
            out[windex++] = BASE64_CHAR[(i >> 12) & 0x3f];
            out[windex++] = BASE64_CHAR[(i >> 6) & 0x3f];
            out[windex++] = BASE64_CHAR[i & 0x3f];
            rindex += 3;
            rest -= 3;
        }
        if (rest == 1) {
            int i = data[rindex] & 0xff;
            out[windex++] = BASE64_CHAR[i >> 2];
            out[windex++] = BASE64_CHAR[(i << 4) & 0x3f];
            out[windex++] = BASE64_PAD;
            out[windex++] = BASE64_PAD;
        } else if (rest == 2) {
            int i = ((data[rindex] & 0xff) << 8) + (data[rindex + 1] & 0xff);
            out[windex++] = BASE64_CHAR[i >> 10];
            out[windex++] = BASE64_CHAR[(i >> 4) & 0x3f];
            out[windex++] = BASE64_CHAR[(i << 2) & 0x3f];
            out[windex++] = BASE64_PAD;
        }
        return new String(out, 0, windex);
    }
}