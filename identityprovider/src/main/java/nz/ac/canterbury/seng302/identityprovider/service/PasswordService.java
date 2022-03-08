package nz.ac.canterbury.seng302.identityprovider.service;

import nz.ac.canterbury.seng302.identityprovider.User;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * Service class used to hash passwords so they are not stored in plain text.
 * Hashing details adapted from <a href="https://www.quickprogrammingtips.com/java/how-to-securely-store-passwords-in-java.html">www.quickprogrammingtips.com</a>
 */
public class PasswordService {

    public boolean passwordMatches(String password, User user) {
        return getHash(password, user.getSalt()).equals(user.getPwhash());
    }



    /**
     * Hashes the given password
     * @param password A string containing the password to be hashed.
     * @param salt A string containing random bits to be added to the password. Likely generated using getNewSalt(),
     *             and then stored in the database with the user.
     * @return Base64 encoded hash
     */
    public String getHash(String password, String salt) {
        String algorithm = "PBKDF2WithHmacSHA1";
        int derivedKeyLength = 160;
        int iterations = 20000;

        byte[] saltBytes = Base64.getDecoder().decode(salt);
        KeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, iterations, derivedKeyLength);

        byte[] encBytes;
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(algorithm);
            encBytes = factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            //
            throw new RuntimeException("Could not hash password: " + e.getMessage());
        }


        return Base64.getEncoder().encodeToString(encBytes);
    }

    /**
     * Generates 8 random bytes to be used as salt
     * @return Base64 encoded salt
     */
    public String getNewSalt() {
        SecureRandom random;

        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Could not get salt: " + e.getMessage());
        }

        byte[] salt = new byte[8];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
}
