package com.ricequant.rqboot.lang;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * @author Kain
 */
public class EncryptionHelper {

  public static final String ALGORITHM = "DES";

  public static Key toKey(String keyStr) {
    try {
      byte[] keyBytes = Base64.getDecoder().decode(keyStr);
      SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
      return keyFactory.generateSecret(new DESKeySpec(keyBytes));
    }
    catch (Exception e) {
      throw new RuntimeException("Unable to convert to key", e);
    }
  }

  public static byte[] decrypt(byte[] data, String keyStr) {
    try {
      Key key = toKey(keyStr);
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      cipher.init(Cipher.DECRYPT_MODE, key);
      return cipher.doFinal(data);
    }
    catch (Exception e) {
      throw new RuntimeException("Unable to decrypt", e);
    }
  }

  public static byte[] encrypt(byte[] data, String keyStr) {
    try {
      Key key = toKey(keyStr);
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      cipher.init(Cipher.ENCRYPT_MODE, key);
      return cipher.doFinal(data);
    }
    catch (Exception e) {
      throw new RuntimeException("Unable to encrypt", e);
    }
  }

  public static String genKey() {
    try {
      KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
      byte[] keyBytes = keyGenerator.generateKey().getEncoded();
      return Base64.getEncoder().encodeToString(keyBytes);
    }
    catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Unable to generate key", e);
    }
  }

  public static void main(String[] args) {
    System.out.println(genKey());
  }
}
