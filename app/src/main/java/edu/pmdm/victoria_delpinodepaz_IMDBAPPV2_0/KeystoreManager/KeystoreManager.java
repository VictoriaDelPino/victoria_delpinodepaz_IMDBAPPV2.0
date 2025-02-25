package edu.pmdm.victoria_delpinodepaz_IMDBAPPV2_0.KeystoreManager;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.security.KeyStore;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;


/* Clase que gestiona la encriptación y desencriptación utilizando el Android KeyStore.
   Esta clase se encarga de generar o recuperar una clave secreta y de utilizarla para cifrar y descifrar datos
   mediante el algoritmo AES en modo GCM (Galois/Counter Mode) sin padding
*/
public class KeystoreManager {

    //Nombre del almacén de claves de Android
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
   // Identificador único para la clave dentro del KeyStore
    private static final String ALIAS = "MyKeyAlias";
    //Especifica el algoritmo, modo y padding para el cifrado
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    //Tamaño del vector de inicialización
    private static final int IV_SIZE = 12;
    // Tamaño de la etiqueta (en bits)
    private static final int TAG_SIZE = 128;

    // Obtiene o genera la clave secreta en el AndroidKeyStore
    private static SecretKey getSecretKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        keyStore.load(null);
        SecretKey secretKey = (SecretKey) keyStore.getKey(ALIAS, null);
        if (secretKey == null) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
            keyGenerator.init(
                    new KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            .build()
            );
            secretKey = keyGenerator.generateKey();
        }
        return secretKey;
    }

    // Encripta un texto plano y retorna una cadena Base64 que combina el IV y el ciphertext
    public static String encrypt(String plainText) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());
        byte[] iv = cipher.getIV();
        byte[] ciphertext = cipher.doFinal(plainText.getBytes("UTF-8"));
        byte[] combined = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);
        return Base64.encodeToString(combined, Base64.DEFAULT);
    }

    // Desencripta el texto encriptado (que contiene IV y ciphertext)
    public static String decrypt(String encryptedData) throws Exception {
        byte[] combined = Base64.decode(encryptedData, Base64.DEFAULT);
        byte[] iv = Arrays.copyOfRange(combined, 0, IV_SIZE);
        byte[] ciphertext = Arrays.copyOfRange(combined, IV_SIZE, combined.length);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec spec = new GCMParameterSpec(TAG_SIZE, iv);
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec);
        byte[] plainText = cipher.doFinal(ciphertext);
        return new String(plainText, "UTF-8");
    }
}

