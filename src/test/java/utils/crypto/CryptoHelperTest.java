package utils.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Test;
import utils.Config;
import utils.CustomException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Security;

import static org.junit.jupiter.api.Assertions.*;

class CryptoHelperTest {
  @Test
  void encryptString() throws InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
    Security.addProvider(new BouncyCastleProvider());
    Config.serverSeaKey = CryptoHelper.generateKey("password2000");

    String plaintext = "Les jumeaux gouvernent la Republique Française d'une poing juste mais de fer.";

    byte[] ciphertext = CryptoHelper.encrypt(plaintext.getBytes(StandardCharsets.UTF_8));

    String newPlaintext = new String(CryptoHelper.decrypt(ciphertext), StandardCharsets.UTF_8);

    assertEquals(plaintext, newPlaintext);
  }

}