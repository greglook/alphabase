package alphabase.codec;


import java.math.BigInteger;


/**
 * Utility code providing efficient radix codec operations in Clojure.
 */
public class Radix {

    /**
     * Static utility class doesn't need constructed instances.
     */
    private Radix() {}


    /**
     * Encode a byte array to a string using a character alphabet.
     *
     * @param alphabet  string of alphabet characters
     * @param data      array of byte data
     * @return encoded string
     */
    public static String encode(String alphabet, byte[] data) {
        int base = alphabet.length();

        // Calculate roughly how many bytes we think we'll need to encode the
        // data, by dividing the bits in the data array by a lower bound on the
        // number of bits we can encode per output character in the alphabet.
        int bitsPerChar = 31 - Integer.numberOfLeadingZeros(alphabet.length());
        int estimatedLength = (8 * data.length)/bitsPerChar + 8;

        StringBuilder buf = new StringBuilder(estimatedLength);

        // Count the number of leading zeros in the array.
        int leadingZeros = 0;
        for (int i = 0; i < data.length; i++) {
            if (data[i] == 0) {
                leadingZeros++;
            } else {
                break;
            }
        }

        // Encode leading zeros with repeated first character digits.
        String zeros = alphabet.substring(0, 1).repeat(leadingZeros);

        // If the entire array is zeros, short-circuit.
        if (leadingZeros == data.length) {
            return zeros;
        }

        // Do big-integer division to calculate the radix tokens.
        BigInteger bigBase = BigInteger.valueOf((long)base);
        BigInteger n = new BigInteger(1, data);

        while (n.compareTo(bigBase) >= 0) {
            BigInteger digit = n.mod(bigBase);
            buf.append(alphabet.charAt(digit.intValue()));
            n = n.subtract(digit).divide(bigBase);
        }

        // Append final token, then leading zeros.
        buf.append(alphabet.charAt(n.intValue()));
        buf.append(zeros);

        return buf.reverse().toString();
    }


    /**
     * Decode a string into a byte array using a character alphabet.
     *
     * @param alphabet  string of alphabet characters
     * @param string    encoded string data
     * @return decoded byte array
     */
    public static byte[] decode(String alphabet, String string) {
        int base = alphabet.length();
        int strLen = string.length();

        // Count the number of leading 'zeros' in the string.
        int leadingZeros = 0;
        char zeroDigit = alphabet.charAt(0);
        for (int i = 0; i < strLen; i++) {
            if (string.charAt(i) == zeroDigit) {
                leadingZeros++;
            } else {
                break;
            }
        }

        // If the string is all zero digits, return an array of zeros.
        if (leadingZeros == strLen) {
            return new byte[leadingZeros];
        }

        // For each digit, starting with the least significant (rightmost)
        // multiply by the base to the power of that digit's position.
        BigInteger bigBase = BigInteger.valueOf((long)base);
        BigInteger n = BigInteger.ZERO;
        BigInteger p = BigInteger.ONE;
        for (int i = strLen - 1; i >= leadingZeros; i--) {
            char digit = string.charAt(i);
            int v = alphabet.indexOf(digit);

            if (v < 0) {
                throw new IllegalArgumentException("Character '" + digit + "' at index " + i + " is not a valid digit");
            }

            BigInteger bigDigit = BigInteger.valueOf((long)v);
            n = n.add(bigDigit.multiply(p));
            p = p.multiply(bigBase);
        }

        // Convert bigint into a byte array.
        byte[] bigBytes = n.toByteArray();
        boolean skipByte = (bigBytes.length > 1) && (bigBytes[0] == 0) && (bigBytes[1] < 0);

        // If there are no leading zeros and we don't need to skip a byte,
        // return directly.
        if ((leadingZeros == 0) && !skipByte) {
            return bigBytes;
        }

        // Otherwise, copy results into new byte array to return.
        int skip = (skipByte ? 1 : 0);
        byte[] output = new byte[leadingZeros + bigBytes.length - skip];
        System.arraycopy(bigBytes, skip, output, leadingZeros, bigBytes.length - skip);

        return output;
    }

}
