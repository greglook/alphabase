package alphabase.codec;


/**
 * Utility code providing efficient hex codec operations in Clojure.
 */
public class Base16 {

    private static final char[] ALPHABET = "0123456789ABCDEF".toCharArray();


    /**
     * Static utility class doesn't need constructed instances.
     */
    private Base16() {}


    /**
     * Encode a byte array to a hexadecimal string.
     *
     * @param data  array of bytes to encode
     * @return encoded string
     */
    public static String encode(byte[] data) {
        char[] chars = new char[2 * data.length];
        for (int i = 0; i < data.length; i++) {
            byte val = data[i];
            int off = 2*i;
            chars[off] = ALPHABET[(val & 0xF0) >>> 4];
            chars[off+1] = ALPHABET[val & 0x0F];
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
    private static int decodeNybble(String hex, int idx) {
        char c = hex.charAt(idx);
        if ('0' <= c && c <= '9') {
            return c - '0';
        } else if ('a' <= c && c <= 'f') {
            return (c - 'a') + 10;
        } else if ('A' <= c && c <= 'F') {
            return (c - 'A') + 10;
        } else {
            throw new IllegalArgumentException("Character '" + c + "' at index " + idx + " is not a valid hexadecimal digit");
        }
    }


    /**
     * Decode a hexadecimal string to a byte array.
     *
     * @param string  string of encoded characters
     * @return byte array data
     */
    public static byte[] decode(String string) {
        int length = string.length() / 2;
        byte[] data = new byte[length];
        for (int i = 0; i < length; i++) {
            int offset = 2*i;
            int octet = (decodeNybble(string, offset) << 4) | decodeNybble(string, offset + 1);
            data[i] = (byte)octet;
        }
        return data;
    }

}
