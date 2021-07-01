package utils.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
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
    byte[] iv = getIVFromByteArray(buff);
    byte[] buffWithNoIV = removeIVFromByteArray(buff);

    gcmCipher.init(Cipher.DECRYPT_MODE, Config.serverSeaKey, new IvParameterSpec(iv));

    return gcmCipher.doFinal(buffWithNoIV);
  }

  public static byte[] encrypt(byte[] buff) throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException, InvalidAlgorithmParameterException {
    byte[] iv = RandomHelper.getBytes(SEA_IV_SIZE);

    gcmCipher.init(Cipher.ENCRYPT_MODE, Config.serverSeaKey, new IvParameterSpec(iv));

    return org.bouncycastle.util.Arrays.concatenate(gcmCipher.doFinal(buff), iv);
  }

  public static byte[] ecbHash(byte[] buff) throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException {
    ecbCipher.init(Cipher.ENCRYPT_MODE, Config.serverSeaKey);

    return hash(ecbCipher.doFinal(buff));
  }

  public static Key generateKey(String keyString) {
    return new SecretKeySpec(hashString(keyString), SEA_ALG);
  }

  public static String encryptUserName(String user) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
    BigInteger bigInteger = new BigInteger(org.bouncycastle.util.Arrays.concatenate(new byte[]{0}, CryptoHelper.ecbHash(user.getBytes(StandardCharsets.UTF_8))));

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


}
