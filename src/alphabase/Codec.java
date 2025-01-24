package alphabase;


/**
 * Utility code providing efficient codec operations in Clojure.
 */
public class Codec {

    /**
     * Static utility class doesn't need constructed instances.
     */
    private Codec() {}


    ///// Hex Encoding /////

    private static final char[] HEX_ALPHABET = "0123456789abcdef".toCharArray();


    /**
     * Encode a byte array to a hex string.
     *
     * @param data  array of bytes to encode
     * @return hexadecimal string
     */
    public static String encodeHex(byte[] data) {
        char[] chars = new char[2 * data.length];
        for (int i = 0; i < data.length; i++) {
            byte val = data[i];
            int off = 2*i;
            chars[off] = HEX_ALPHABET[(val & 0xF0) >>> 4];
            chars[off+1] = HEX_ALPHABET[val & 0x0F];
        }
        return new String(chars);
    }


    /**
     * Read a nybble from a single hex character in a string.
     *
     * @param hex  string of hexadecimal characters
     * @param idx  index to read
     * @return numeric value representing the nybble
     * @throws IllegalArgumentException on error
     */
    private static int decodeHexNybble(String hex, int idx) {
        char c = hex.charAt(idx);
        if ('0' <= c && c <= '9') {
            return c - '0';
        } else if ('a' <= c && c <= 'f') {
            return (c - 'a') + 10;
        } else if ('A' <= c && c <= 'F') {
            return (c - 'A') + 10;
        } else {
            throw new IllegalArgumentException("Unrecognized character in hex string at index " + idx + ": " + c);
        }
    }


    /**
     * Decode a hex string to a byte array.
     *
     * @param hex  string of hexadecimal characters
     * @return byte array
     */
    public static byte[] decodeHex(String hex) {
        int length = hex.length() / 2;
        byte[] data = new byte[length];
        for (int i = 0; i < length; i++) {
            int offset = 2*i;
            int octet = (decodeHexNybble(hex, offset) << 4) | decodeHexNybble(hex, offset + 1);
            data[i] = (byte)octet;
        }
        return data;
    }

}
