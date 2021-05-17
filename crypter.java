package filecryptBase;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class crypter {
    public crypter(){ }

    //Generate a key for the AES-128 encryption (recommended parameters, may be adjusted depending on specifications)
    private static SecretKey generateKey(String password, byte[] iv) throws InvalidKeySpecException, NoSuchAlgorithmException {
        KeySpec kspec = new PBEKeySpec(password.toCharArray(), iv, 65536, 128);
        SecretKeyFactory skfactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] key = skfactory.generateSecret(kspec).getEncoded();

        return new SecretKeySpec(key, "AES");
    }

    public static byte[] encryptData(byte[] data, String password) throws Exception {
        byte[] iv = new byte[12];
        SecretKey sk = generateKey(password, iv);
        //the goal is to simply encrypt the bytes and no more, therefore no padding
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec paramspec = new GCMParameterSpec(128, iv);

        cipher.init(Cipher.ENCRYPT_MODE, sk, paramspec);

        byte[] encryptedData = cipher.doFinal(data);

        //TODO: implement dynamic nonce/iv size at a later date; Set at 12 bytes for now
        ByteBuffer finalEncryption = ByteBuffer.allocate(4 + iv.length + encryptedData.length);
        finalEncryption.putInt(iv.length);
        finalEncryption.put(iv);
        finalEncryption.put(encryptedData);

        return finalEncryption.array();
    }

    public static byte[] decryptData(byte[] encryptedData, String password) throws Exception {
        ByteBuffer bbuffer = ByteBuffer.wrap(encryptedData);
        int n = bbuffer.getInt();
        byte[] iv = new byte[n];
        bbuffer.get(iv);

        SecretKey sk = generateKey(password, iv);

        byte[] cipherBytes = new byte[bbuffer.remaining()];
        bbuffer.get(cipherBytes);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);

        cipher.init(Cipher.DECRYPT_MODE, sk, parameterSpec);
        return cipher.doFinal(cipherBytes);
    }
}