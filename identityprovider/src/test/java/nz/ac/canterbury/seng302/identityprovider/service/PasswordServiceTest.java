package nz.ac.canterbury.seng302.identityprovider.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.jupiter.api.Assertions.*;

class PasswordServiceTest {

    PasswordService service;

    @BeforeEach
    void setUp() {
        service = new PasswordService();
    }

    @Test
    void testHashesSame() throws NoSuchAlgorithmException, InvalidKeySpecException {

        String salt = service.getNewSalt();
        String hash1 = service.getHash("testpassword123", salt);
        String hash2 = service.getHash("testpassword123", salt);

        assertEquals(hash1, hash2);

    }

    @Test
    void testHashesDifferent() throws NoSuchAlgorithmException, InvalidKeySpecException {

        String salt = service.getNewSalt();
        String hash1 = service.getHash("testpassword123", salt);
        String hash2 = service.getHash("differentpassword123", salt);

        assertNotEquals(hash1, hash2);

    }

    @Test
    void testSaltsRandom() throws NoSuchAlgorithmException {

        String salt1 = service.getNewSalt();
        String salt2 = service.getNewSalt();

        assertNotEquals(salt1, salt2);

    }

}