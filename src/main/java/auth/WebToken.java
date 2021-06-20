package auth;

import com.google.gson.Gson;
import org.bouncycastle.util.encoders.Base64;
import utils.CustomException;
import utils.crypto.CryptoHelper;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

public final class WebToken {
  private static final transient Duration MAX_TOKEN_DURATION = Duration.ofMinutes(5);
  private static final transient Gson gson = new Gson();

  private String subject;
  private Date expiration;

  public WebToken() {
  }

  public WebToken(String subject) {
    this.subject = subject;
    expiration = Date.from(Instant.now().plus(MAX_TOKEN_DURATION));
  }

  public String getUserID() {
    return subject;
  }

  public boolean isExpired() {
    return new Date().after(expiration);
  }

  public String encrypt() throws InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
    String stringifiedToken = gson.toJson(this, WebToken.class);

    return Base64.toBase64String(CryptoHelper.encrypt(stringifiedToken.getBytes()));
  }

  public static WebToken decrypt(String base64EncryptedToken) throws InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
    byte[] decryptedTokenBytes = CryptoHelper.decrypt(Base64.decode(base64EncryptedToken));

    return gson.fromJson(new String(decryptedTokenBytes, StandardCharsets.UTF_8), WebToken.class);
  }
}