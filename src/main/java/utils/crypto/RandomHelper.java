package utils.crypto;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Base64.Encoder;

public final class RandomHelper {
  private static final Encoder stringifier = Base64.getEncoder().withoutPadding();
  private static final SecureRandom random = new SecureRandom();

  // Random Base64 string of size, cut because base64 adds characters
  public static String getString(int size) {
    byte[] randomBytes = getBytes(size);

    return stringifier.encodeToString(randomBytes).substring(0, size - 1);
  }

  public static byte[] getBytes(int size) {
    byte[] randomBytes = new byte[size];
    random.nextBytes(randomBytes);

    return randomBytes;
  }

  public static int getInt() {
    return random.nextInt();
  }

  public static float getFloat() {
    return random.nextFloat();
  }

  public static long getLong() {
    return random.nextLong();
  }
}
