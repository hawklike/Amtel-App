package cz.prague.cvut.fit.steuejan.amtelapp.business.helpers;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import cz.prague.cvut.fit.steuejan.amtelapp.App;
import cz.prague.cvut.fit.steuejan.amtelapp.R;

public class AESCrypt
{
    private static final String ALGORITHM = "AES";
    private static final String KEY = App.Companion.getContext().getString(R.string.aes_key);

    public static String encrypt(String value) throws Exception
    {
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(AESCrypt.ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte [] encryptedByteValue = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeToString(encryptedByteValue, Base64.DEFAULT);
    }

    public static String decrypt(String value) throws Exception
    {
        System.out.println("decrypt: " + value);
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(AESCrypt.ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedValue64 = Base64.decode(value, Base64.DEFAULT);
        byte [] decryptedByteValue = cipher.doFinal(decryptedValue64);
        return new String(decryptedByteValue, StandardCharsets.UTF_8);
    }

    private static Key generateKey()
    {
        return new SecretKeySpec(AESCrypt.KEY.getBytes(), AESCrypt.ALGORITHM);
    }
}
