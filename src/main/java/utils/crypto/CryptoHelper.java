package utils.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import utils.Config;
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

  private static final int SEA_IV_SIZE = 12;

  private static final String SEA_ALG = "AES";
  private static final String ECB_SPEC = "AES/ECB/PKCS5Padding";
  private static final String GCM_SPEC = "AES/GCM/NoPadding";
  private static final Cipher gcmCipher;
  private static final Cipher ecbCipher;

  private static final MessageDigest messageDigest;

  static {
    Security.addProvider(new BouncyCastleProvider());

    try {
      gcmCipher = Cipher.getInstance(GCM_SPEC, Config.PROVIDER);
      ecbCipher = Cipher.getInstance(ECB_SPEC, Config.PROVIDER);
      messageDigest = MessageDigest.getInstance("SHA3-256", Config.PROVIDER);
    } catch (NoSuchAlgorithmException e) {
      throw new CustomRuntimeException(logger, "Algorithm was not found.");
    } catch (NoSuchProviderException e) {
      throw new CustomRuntimeException(logger, "Provider " + Config.PROVIDER + " was not found.");
    } catch (NoSuchPaddingException e) {
      throw new CustomRuntimeException(logger, "Padding was not found.");
    }
  }

  public static byte[] decrypt(byte[] buff) throws BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeyException {
    gcmCipher.init(Cipher.DECRYPT_MODE, Config.serverSeaKey, new IvParameterSpec(getIVFromByteArray(buff)));

    return gcmCipher.doFinal(removeIVFromByteArray(buff));
  }

  public static byte[] encrypt(byte[] buff) throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException, InvalidAlgorithmParameterException {
    byte[] iv = RandomHelper.getBytes(SEA_IV_SIZE);
    gcmCipher.init(Cipher.ENCRYPT_MODE, Config.serverSeaKey, new IvParameterSpec(iv));

    return joinByteArrays(gcmCipher.doFinal(buff), iv);
  }

  public static byte[] ecbHash(byte[] buff) throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException {
    ecbCipher.init(Cipher.ENCRYPT_MODE, Config.serverSeaKey);

    return hash(ecbCipher.doFinal(buff));
  }

  public static Key generateKey(String keyString) {
    return new SecretKeySpec(hashString(keyString), SEA_ALG);
  }

  public static String encryptUserName(String user) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
    BigInteger bigInteger = new BigInteger(joinByteArrays(new byte[]{0}, CryptoHelper.ecbHash(user.getBytes(StandardCharsets.UTF_8))));
    return bigInteger.toString(16).toUpperCase();
  }

  // UTILS ----------------------------------------------------------------------------------
  private static byte[] hash(byte[] bytes) {
    return messageDigest.digest(bytes);
  }

  private static byte[] hashString(String string) {
    return messageDigest.digest(string.getBytes(StandardCharsets.UTF_8));
  }

  private static byte[] getIVFromByteArray(byte[] array) throws BadPaddingException {
    if (array.length <= SEA_IV_SIZE)
      throw new BadPaddingException("Invalid byte array size");

    return Arrays.copyOfRange(array, array.length - SEA_IV_SIZE, array.length);
  }

  private static byte[] removeIVFromByteArray(byte[] array) throws BadPaddingException {
    if (array.length <= SEA_IV_SIZE)
      throw new BadPaddingException("Invalid byte array size");

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
