package utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for hashing operations.
 *
 * This class provides methods for generating cryptographic hashes, such as SHA-1,
 * which can be used for data integrity verification and unique identifier generation.
 * It encapsulates common hashing functionality in an easy-to-use interface.
 */
public class Hasher {

    /**
     * Generates a SHA-1 hash for the given input.
     *
     * @param input The string to be hashed.
     * @return The hexadecimal representation of the hash.
     */
    public static String computeSHA1(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");

            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    // Pad with a leading zero if necessary
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 algorithm not found", e);
        }
    }
}