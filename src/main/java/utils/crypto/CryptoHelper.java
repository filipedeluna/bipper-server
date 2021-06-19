package utils.crypto;

import org.bouncycastle.util.encoders.Base64;
import utils.Config;
import utils.CustomException;
import utils.CustomRuntimeException;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Arrays;
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

  public static byte[] decrypt(byte[] buff) throws BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeyException, CustomException {
    cipher.init(Cipher.DECRYPT_MODE, Config.serverSeaKey, new IvParameterSpec(getIVFromByteArray(buff)));

    return cipher.doFinal(removeIVFromByteArray(buff));
  }

  public static byte[] encrypt(byte[] buff) throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException, InvalidAlgorithmParameterException {
    byte[] iv = RandomHelper.getBytes(SEA_IV_SIZE);
    cipher.init(Cipher.ENCRYPT_MODE, Config.serverSeaKey, new IvParameterSpec(iv));

    return joinByteArrays(cipher.doFinal(buff), iv);
  }

  public static Key generateKey(String keyString) {
    return new SecretKeySpec(messageDigest.digest(keyString.getBytes(StandardCharsets.UTF_8)), SEA_ALG);
  }

  // UTILS ----------------------------------------------------------------------------------
  private static byte[] getIVFromByteArray(byte[] array) throws CustomException {
    if (array.length <= SEA_IV_SIZE)
      throw new CustomException(logger, "Invalid byte array size");

    return Arrays.copyOfRange(array, array.length - SEA_IV_SIZE, array.length);
  }

  private static byte[] removeIVFromByteArray(byte[] array) throws CustomException {
    if (array.length <= SEA_IV_SIZE)
      throw new CustomException(logger, "Invalid byte array size");

    return Arrays.copyOfRange(array, 0, array.length - SEA_IV_SIZE);
  }

  private static byte[] joinByteArrays(byte[]... arrays) {
    byte[] finalArray = new byte[0];

    byte[] tempArray;
    for (byte[] array : arrays) {
      tempArray = new byte[finalArray.length + array.length];

      System.arraycopy(finalArray, 0, tempArray, 0, finalArray.length);
      System.arraycopy(array, 0, tempArray, finalArray.length, array.length);

      finalArray = tempArray;
    }

    return finalArray;
  }
}
