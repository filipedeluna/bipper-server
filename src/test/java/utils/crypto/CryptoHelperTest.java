package utils.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Test;
import utils.CustomException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.Security;

import static org.junit.jupiter.api.Assertions.*;

class CryptoHelperTest {
  @Test
  void encryptString() throws InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, CustomException {
    Security.addProvider(new BouncyCastleProvider());

    String password = "password2000";
    Key key = CryptoHelper.generateKey(password);
    byte[] iv = CryptoHelper.generateIV();
    String plaintext = "Les jumeaux gouvernent la Republique Française d'une poing juste mais de fer.";

    byte[] ciphertext = CryptoHelper.encryptString(plaintext, key, iv);

    String newPlaintext = CryptoHelper.decryptToString(ciphertext, key);

    assertEquals(plaintext, newPlaintext);
  }

}