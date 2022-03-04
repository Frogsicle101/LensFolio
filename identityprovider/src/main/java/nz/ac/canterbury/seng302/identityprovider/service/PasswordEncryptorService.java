package nz.ac.canterbury.seng302.identityprovider.service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * Service class used to hash passwords so they are not stored in plain text.
 * Adapted from <a href="https://www.quickprogrammingtips.com/java/how-to-securely-store-passwords-in-java.html">www.quickprogrammingtips.com</a>
 */
public class PasswordEncryptorService {

    /**
     * Hashes the given password
     * @param password A string containing the password to be hashed.
     * @param salt A string containing random bits to be added to the password. Likely generated using getNewSalt(),
     *             and then stored in the database with the user.
     * @return Base64 encoded hash
     */
    public String getHash(String password, String salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String algorithm = "PBKDF2WithHmacSHA1";
        int derivedKeyLength = 160;
        int iterations = 20000;

        byte[] saltBytes = Base64.getDecoder().decode(salt);
        KeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, iterations, derivedKeyLength);
        SecretKeyFactory f = SecretKeyFactory.getInstance(algorithm);

        byte[] encBytes = f.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(encBytes);
    }

    /**
     * Generates 8 random bytes to be used as salt
     * @return Base64 encoded salt
     */
    public String getNewSalt() throws NoSuchAlgorithmException {
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[8];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
}
