package auth.jwt;

import auth.WebToken;
import com.google.gson.Gson;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.junit.jupiter.api.Test;
import utils.Config;
import utils.CustomException;
import utils.crypto.CryptoHelper;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Security;

import static org.junit.jupiter.api.Assertions.*;

class WebTokenTest {
  Gson gson = new Gson();

  @Test
  void cryptoTest() throws InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, CustomException {
    Security.addProvider(new BouncyCastleProvider());
    Config.serverSeaKey = CryptoHelper.generateKey("password2000");

    WebToken tokenAdmin = new WebToken("admin");

    byte[] encryptedTokenAdminBytes = CryptoHelper.encrypt(gson.toJson(tokenAdmin).getBytes(StandardCharsets.UTF_8));
    String base64EncryptedTokenAdmin = Base64.toBase64String(encryptedTokenAdminBytes);
    String decryptedTokenAdmin = new String(CryptoHelper.decrypt(Base64.decode(base64EncryptedTokenAdmin)), StandardCharsets.UTF_8);

    assertEquals(gson.toJson(WebToken.decrypt(tokenAdmin.encrypt())), decryptedTokenAdmin);
  }

  @Test
  void tokenTest() {
    WebToken tokenAdmin = new WebToken("admin");
    WebToken tokenUser = new WebToken("user");

    assertEquals("admin", tokenAdmin.getSubject());
    assertEquals("user", tokenUser.getSubject());
  }
}