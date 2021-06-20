package utils.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import utils.Config;
import utils.CustomException;
import utils.CustomLogger;
import utils.CustomRuntimeException;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Arrays;

public final class CryptoHelper {
  private static final CustomLogger logger = new CustomLogger(CryptoHelper.class);

  private static final String SEA_ALG = "AES";
  private static final String SEA_MODE = "GCM";
  private static final String SEA_PAD = "NoPadding";
  private static final int SEA_KEY_SIZE = 32;
  private static final int SEA_IV_SIZE = 12;

  private static final String SEA_SPEC = SEA_ALG + "/" + SEA_MODE + "/" + SEA_PAD;
  private static final Cipher cipher;

  private static final MessageDigest messageDigest;

  static {
    Security.addProvider(new BouncyCastleProvider());

    try {
      cipher = Cipher.getInstance(SEA_SPEC, Config.PROVIDER);
      messageDigest = MessageDigest.getInstance("SHA3-256", Config.PROVIDER);
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
    return new SecretKeySpec(hashString(keyString), SEA_ALG);
  }

  public static String hashToHexString(String string) {
    BigInteger bigInteger = new BigInteger(joinByteArrays(new byte[]{0}, messageDigest.digest(string.getBytes(StandardCharsets.UTF_8))));
    return bigInteger.toString(16).toUpperCase();
  }

  // UTILS ----------------------------------------------------------------------------------
  private static byte[] hash(byte[] bytes) {
    return messageDigest.digest(bytes);
  }

  private static byte[] hashString(String string) {
    return messageDigest.digest(string.getBytes(StandardCharsets.UTF_8));
  }

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
