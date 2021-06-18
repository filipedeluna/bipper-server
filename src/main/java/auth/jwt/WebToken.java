package auth.jwt;

import com.google.gson.Gson;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.spec.SecretKeySpec;
import javax.management.relation.Role;
import java.security.Key;
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

  public String toJson() {
    return gson.toJson(this);
  }

  public WebToken fromJson(String webToken) {
    return gson.fromJson(webToken, WebToken.class);
  }

  public String getSubject() {
    return subject;
  }

  public Date getExpiration() {
    return expiration;
  }

  public Role getRole() {
    return role;
  }

  public boolean checkExpired() {
    return new Date().before(expiration);
  }
}