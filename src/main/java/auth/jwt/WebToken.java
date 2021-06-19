package auth.jwt;

import auth.Role;
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

public class WebToken {
  private static final transient Duration MAX_TOKEN_DURATION = Duration.ofMinutes(5);
  private static final transient Gson gson = new Gson();

  private String subject;
  private Role role;
  private Date expiration;

  public WebToken() {
  }

  public WebToken(String subject, Role role) {
    this.subject = subject;
    this.role = role;
    expiration = Date.from(Instant.now().plus(MAX_TOKEN_DURATION));
  }

  public String getSubject() {
    return subject;
  }

  public Date getExpiration() {
    return expiration;
  }

  public boolean isAdmin() {
    return role == Role.ADMIN;
  }

  public boolean checkExpired() {
    return new Date().before(expiration);
  }

  public String encrypt() throws InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
    String stringifiedToken = gson.toJson(this, WebToken.class);

    return Base64.toBase64String(CryptoHelper.encrypt(stringifiedToken.getBytes()));
  }

  public static WebToken decrypt(String base64EncryptedToken) throws InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, CustomException, InvalidKeyException {
    byte[] decryptedTokenBytes = CryptoHelper.decrypt(Base64.decode(base64EncryptedToken));

    return gson.fromJson(new String(decryptedTokenBytes, StandardCharsets.UTF_8), WebToken.class);
  }
}