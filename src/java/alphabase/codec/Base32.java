package alphabase.codec;


/**
 * Utility code providing efficient base32 codec operations in Clojure.
 */
public class Base32 {

    private static final char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();


    /**
     * Static utility class doesn't need constructed instances.
     */
    private Base32() {}


    /**
     * Encode a byte array to a base32 string.
     *
     * @param data  array of bytes to encode
     * @return encoded string
     */
    public static String encode(byte[] data) {
        char[] chars = new char[(int)Math.ceil(1.6*data.length)];
        int charIdx = 0;

        // Each character digit of base32 encodes five bits, so work in chunks
        // of five bytes which encode 40 bits into eight digits.
        int dataIdx = 0;
        while (dataIdx + 4 < data.length) {
            long n = 0;

            // Take next five bytes and pack into a long.
            for (int i = 0; i < 5; i++) {
                n = (n << 8) | (data[dataIdx] & 0xFF);
                dataIdx++;
            }

            // Unpack and encode eight digits from the long. Note that the
            // digits are assigned working backwards from the LSB.
            for (int offset = 7; offset >= 0; offset--) {
                chars[charIdx+offset] = ALPHABET[(int)(n & 0x1F)];
                n = n >>> 5;
            }

            charIdx += 8;
        }

        // Handle any remaining bytes that are not an even multiple of five.
        if (dataIdx < data.length) {
            int remnant = data.length - dataIdx;
            int padding = 5 - ((8 * remnant) % 5);
            int digitsLeft = ((8 * remnant) + padding)/5;
            long n = 0;

            // Pack remnant bytes into the long.
            for (; dataIdx < data.length; dataIdx++) {
                n = (n << 8) | (data[dataIdx] & 0xFF);
            }

            // Right-pad with zero bits to make the total evenly divisible.
            n = n << padding;

            // Finish digit encoding.
            for (int offset = digitsLeft - 1; offset >= 0; offset--) {
                chars[charIdx+offset] = ALPHABET[(int)(n & 0x1F)];
                n = n >>> 5;
            }

            charIdx += digitsLeft;
        }

        return new String(chars, 0, charIdx);
    }


    /**
     * Decode a base32 string to a byte array.
     *
     * @param string  string of encoded characters
     * @return byte array data
     */
    public static byte[] decode(String string) {
        // TODO: implement
        return null;
    }

}
