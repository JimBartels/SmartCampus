package com.inc.bb.smartcampus;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import java.util.regex.Pattern;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/* class contains useful password utilities such as encryption and verification tools
 *
 * How to use?
 * First off, get a salt value of desired length, 30 should do fine
 * Then, generatedSecurePassword using the salt value and plain text password
 * Store the salt value and encrypted password in the database
 * (DO NOT store the plain text password)
 *
 * How to verify password?
 * Extract the salt value and encrypted password from the database
 * call verifyUserPassword() on plain text password, encrypted password and salt value
 *
 */
public class PasswordUtils {

    private static final Random RANDOM = new SecureRandom();
    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!@#$%^&*_+-=";
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;

    // to be used by validPassword() method
    private static final Pattern[] inputRegexes = new Pattern[4];
    static {
        inputRegexes[0] = Pattern.compile(".*[A-Z].*");
        inputRegexes[1] = Pattern.compile(".*[a-z].*");
        inputRegexes[2] = Pattern.compile(".*\\d.*");
        inputRegexes[3] = Pattern.compile(".*[`~!@#$%^&*()\\-_=+\\\\|\\[{\\]};:'\",<.>/?].*");
    }

    /**
     * Determines if a password has valid format
     *
     * @param input - the password to verify
     * Pre-condition: input must be 8 more digits
     * @return if a password verifies 4 conditions:
     * 1. contains an upper case letter
     * 2. contains a lower case letter
     * 3. conatains a digit
     * 4. contains a special character
     */
    private static boolean validPassword(String input) {
        if (input.length() < 8) {
            return false;
        }

        boolean inputMatches = true;
        for (Pattern inputRegex : inputRegexes) {
            if (!inputRegex.matcher(input).matches()) {
                inputMatches = false;
            }
        }
        return inputMatches;
    }

    /**
     * Generates a salt value of desired length
     *
     * @param length - the desired length of salt value; we can use length 30
     * @return salt value of a desired length
     */
    public static String generateSalt(int length) {
        StringBuilder saltValue = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            saltValue.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return new String(saltValue);
    }

    /**
     * Hashes the password string using the salt value
     * (used by generateSecurePassword() method, unlikely to be called directly)
     *
     * @param password - plain text password as an array of characters
     * @param salt - desired salt value
     * @return hashed password
     */
    public static byte[] hash(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        Arrays.fill(password, Character.MIN_VALUE);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new AssertionError("Error while hashing a password: " + e.getMessage(), e);
        } finally {
            spec.clearPassword();
        }
    }

    /**
     * Returns an encrypted string generated from the user password
     *
     * @param password - the password inputted by the user in plain text
     * @param salt - salt value to be calculated before
     * Pre-condition: password must be valid as determined by validPassword() function
     * @return encrypted password
     */
    public static String generateEncryptedPassword(String password, String salt) {

        // exception is thrown if password is not valid,
        // must be caught and treated somehow perhaps by displaying the message
        if (!validPassword(password)) {
            throw new IllegalArgumentException("Password does not meet required format: \n" +
                    "1. Must contain an upper case letter \n" +
                    "2. Must contain a lower case letter \n" +
                    "3. Must have a digit (0-9) \n" +
                    "4. Must contain a special symbol.");
        }

        String returnValue = null;
        byte[] securePassword = hash(password.toCharArray(), salt.getBytes());

        // the following functions require API 26, so something we need to look into later
        returnValue = Base64.getEncoder().encodeToString(securePassword);
        return returnValue;
    }

    /**
     * Verify that inputted password equals the real password of a user
     *
     * @param providedPassword - password inputted by user
     * @param securedPassword - encrypted password extracted from database
     * @param salt - salt value extracted from database
     * @return if inputted password equals the real password of a user
     */
    public static boolean verifyUserPassword(String providedPassword,
                                             String securedPassword, String salt) {
        boolean verify = false;

        // Generate New secure password with the same salt
        String test = generateEncryptedPassword(providedPassword, salt);

        // Check if two passwords are equal
        verify = test.equals(securedPassword);

        return verify;
    }

}
