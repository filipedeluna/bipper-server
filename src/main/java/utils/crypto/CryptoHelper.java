package utils.crypto;

import utils.Config;
import utils.CustomRuntimeException;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.logging.Logger;

public class CryptoHelper {
  private static final Logger logger = Config.getLogger(CryptoHelper.class);

  private static final String SEA_ALG = "AES";
  private static final String SEA_MODE = "GCM";
  private static final String SEA_PAD = "NoPadding";
  private static final int SEA_KEY_SIZE = 32;
  private static final int SEA_IV_SIZE = 12;

  private static final String SEA_SPEC = SEA_ALG + "/" + SEA_MODE + "/" + SEA_PAD;
  private static final Cipher cipher;

  private static final MessageDigest messageDigest;

  static {
    try {
      cipher = Cipher.getInstance(SEA_SPEC, Config.PROVIDER);
      messageDigest = MessageDigest.getInstance("SHA256", Config.PROVIDER);
    } catch (NoSuchAlgorithmException e) {
      throw new CustomRuntimeException(logger, "Algorithm " + SEA_SPEC + " was not found.");
    } catch (NoSuchProviderException e) {
      throw new CustomRuntimeException(logger, "Provider " + Config.PROVIDER + " was not found.");
    } catch (NoSuchPaddingException e) {
      throw new CustomRuntimeException(logger, "Padding " + SEA_PAD + " was not found.");
    }
  }

  public static byte[] decrypt(byte[] buff, Key key, byte[] iv) throws BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeyException {
    IvParameterSpec ivSpec = new IvParameterSpec(iv);

    cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);

    return cipher.doFinal(buff);
  }

  public static byte[] encrypt(byte[] buff, Key key, byte[] iv) throws BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeyException {
    IvParameterSpec ivSpec = new IvParameterSpec(iv);

    cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);

    return cipher.doFinal(buff);
  }

  public static String decryptToString(byte[] buff, Key key, byte[] iv) throws BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeyException {
    IvParameterSpec ivSpec = new IvParameterSpec(iv);

    cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);

    return new String(decrypt(buff, key, iv), StandardCharsets.UTF_8);
  }

  public static byte[] encryptString(String string, Key key, byte[] iv) throws BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeyException {
    return encrypt(string.getBytes(StandardCharsets.UTF_8), key, iv);
  }

  public static byte[] generateIV() {
    return RandomHelper.getBytes(SEA_IV_SIZE);
  }

  public static Key generateKey(String keyString) {
    return new SecretKeySpec(messageDigest.digest(keyString.getBytes(StandardCharsets.UTF_8)), SEA_ALG);
  }
}
