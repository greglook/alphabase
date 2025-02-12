package alphabase.codec;


import java.util.Arrays;


/**
 * Utility code providing efficient base32 codec operations in Clojure.
 */
public class Base32 {

    private static final char[] RFC_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();
    private static final char[] HEX_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUV".toCharArray();

    private static final int[] RFC_LOOKUP;
    private static final int[] HEX_LOOKUP;

    static {
        RFC_LOOKUP = new int[91];
        Arrays.fill(RFC_LOOKUP, -1);

        HEX_LOOKUP = new int[91];
        Arrays.fill(HEX_LOOKUP, -1);

        for (int i = 0; i < 32; i++) {
            RFC_LOOKUP[(int)RFC_ALPHABET[i]] = i;
            HEX_LOOKUP[(int)HEX_ALPHABET[i]] = i;
        }
    }


    /**
     * Static utility class doesn't need constructed instances.
     */
    private Base32() {}


    /**
     * Encode a byte array to a base32 string.
     *
     * @param data        array of bytes to encode
     * @param inHex       whether to use the hex alphabet instead of RFC 4648
     * @param withPadding whether to add padding characters to the result
     * @return encoded string
     */
    public static String encode(byte[] data, boolean inHex, boolean withPadding) {
        int dataIdx = 0;
        int dataLen = data.length;
        int digitsLen = (int)Math.ceil(1.6*dataLen);
        int paddingLen = (withPadding && (digitsLen % 8) > 0) ? (8 - (digitsLen % 8)) : 0;
        char[] chars = new char[digitsLen + paddingLen];
        int charIdx = 0;

        char[] alphabet = inHex ? HEX_ALPHABET : RFC_ALPHABET;

        // Each character digit of base32 encodes five bits, so work in chunks
        // of five bytes which encode 40 bits into eight digits.
        while (dataIdx < dataLen) {
            int chunkLen = Math.min(5, dataLen - dataIdx);
            int outputLen = (chunkLen == 5) ? 8 : (int)Math.ceil(1.6*chunkLen);

            long n = 0;

            // Take next chunk of bytes and pack into a long.
            for (int i = 0; i < chunkLen; i++) {
                n = (n << 8) | (data[dataIdx] & 0xFF);
                dataIdx++;
            }

            // Right-pad with zero bits to make total evenly divisible.
            if (chunkLen != 5) {
                int padding = 5 - ((8 * chunkLen) % 5);
                n = n << padding;
            }

            // Unpack and encode digits from the long. Note that the digits are
            // assigned working backwards from the LSB.
            for (int offset = outputLen - 1; offset >= 0; offset--) {
                chars[charIdx+offset] = alphabet[(int)(n & 0x1F)];
                n = n >>> 5;
            }

            charIdx += outputLen;
        }

        // Write padding characters if set.
        for (int i = 0; i < paddingLen; i++) {
            chars[charIdx] = '=';
            charIdx++;
        }

        // Double-check that we got the expected amount of data.
        if (charIdx != chars.length) {
            throw new IllegalStateException("Expected to encode " + dataLen + " byte array into "
                    + chars.length + " characters, but only got " + charIdx);
        }

        //return new String(chars, 0, charIdx);
        return new String(chars);
    }


    /**
     * Decode a base32 string to a byte array.
     *
     * @param string  string of encoded characters
     * @param inHex   whether to use the hex alphabet instead of RFC 4648
     * @return byte array data
     */
    public static byte[] decode(String string, boolean inHex) {
        int[] lookup = inHex ? HEX_LOOKUP : RFC_LOOKUP;
        int charLen = string.length();
        int charIdx = 0;

        // Look for padding chars at the end of the string.
        int padLen = 0;
        for (int i = charLen - 1; i >= 0; i--) {
            if (string.charAt(i) == '=') {
                padLen++;
            } else {
                break;
            }
        }

        charLen -= padLen;

        // Allocate output binary data array.
        byte[] data = new byte[(int)Math.floor(charLen*0.625)];
        int dataIdx = 0;

        // Each character digit of base32 encodes five bits, so work in chunks
        // of eight digits which decode 40 bits into five bytes.
        while (charIdx < charLen) {
            int chunkLen = Math.min(8, charLen - charIdx);
            long n = 0;

            // Take next chunk of digits and pack their bit values into a long.
            for (int i = 0; i < chunkLen; i++) {
                char digit = string.charAt(charIdx);
                int d = (int)Character.toUpperCase(digit);
                int v = (d < lookup.length) ? lookup[d] : -1;

                if (v < 0) {
                    throw new IllegalArgumentException("Character '" + digit + "' is not a valid Base32 digit");
                }

                n = (n << 5) | (v & 0xFF);
                charIdx++;
            }

            // Determine number of decoded bytes.
            int outputLen = (chunkLen == 8) ? 5 : (int)Math.floor(chunkLen*0.625);

            // Unpad right zero bits if needed.
            if (outputLen != 5) {
                int padding = 5 - ((8 * outputLen) % 5);
                n = n >>> padding;
            }

            // Unpack and decode bytes from the long. Note that the
            // bytes are assigned working backwards from the LSB.
            for (int offset = outputLen - 1; offset >= 0; offset--) {
                data[dataIdx+offset] = (byte)(n & 0xFF);
                n = n >>> 8;
            }

            dataIdx += outputLen;
        }

        // Double-check that we got the expected amount of data.
        if (dataIdx != data.length) {
            throw new IllegalStateException("Expected to decode " + charLen + " digit string into "
                    + data.length + " bytes, but only got " + dataIdx);
        }

        return data;
    }

}
