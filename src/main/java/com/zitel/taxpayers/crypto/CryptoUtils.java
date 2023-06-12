package com.zitel.taxpayers.crypto;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.text.Collator;
import java.util.*;

public class CryptoUtils {

    private final static ObjectMapper mapper = new ObjectMapper();

    public static String encrypt(String text, PublicKey publicKey) throws NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException,
            IllegalBlockSizeException, InvalidKeyException {
        Cipher encryptCipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING");
        encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] secretMessageBytes = text.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedMessageBytes = encryptCipher.doFinal(secretMessageBytes);
        return Base64.getEncoder().encodeToString(encryptedMessageBytes);
    }

    public static String getSignedText(String text, String algorithm, PrivateKey privateKey) throws Exception {

        byte[] data = text.getBytes("UTF8");
        Signature sig = Signature.getInstance(algorithm == null ? "SHA256WITHRSA" : algorithm);
        sig.initSign(privateKey);
        sig.update(data);
        byte[] signatureBytes = sig.sign();
        return Base64.getEncoder().encodeToString(signatureBytes);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String normalJson(Object object, Map<String, Object> header) {
        if (object == null && header == null)
            return null;
        Map<String, Object> map = null;
        if (object != null) {
            if (object instanceof String) {
                try {
                    object = mapper.readValue((String) object, Object.class);
                } catch (IOException e) {
                    throw new RuntimeException(e.getMessage());
                }
            }
            if (object instanceof Collection) {
                PacketsWrapper packetsWrapper = new PacketsWrapper((Collection) object);
                map = mapper.convertValue(packetsWrapper, Map.class);
            } else {
                map = mapper.convertValue(object, Map.class);
            }
        }
        if (map == null && header != null) {
            map = header;
        }
        if (map != null && header != null) {
            for (Map.Entry<String, Object> entry : header.entrySet()) {
                map.put(entry.getKey(), entry.getValue());
            }
        }
        Map<String, Object> result = new HashMap<>();
        flatMap(result, null, map);
        StringBuilder sb = new StringBuilder();
        List<String> keys = new ArrayList<>(result.keySet());
        Collections.sort(keys, Collator.getInstance(Locale.ENGLISH));
        for (String key : keys) {
            String textValue;
            Object value = result.get(key);
            if (value != null) {
                textValue = value.toString();
                if (textValue == null || textValue.equals("")) {
                    textValue = "#";
                } else {
                    textValue = textValue.replaceAll("#", "##");
                }
            } else {
                textValue = "#";
            }
            sb.append(textValue).append('#');
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }

    private static String getKey(String rootKey, String myKey) {
        if (rootKey != null) {
            return rootKey + "." + myKey;
        } else {
            return myKey;
        }
    }

    private static void flatMap(Map<String, Object> result, String rootKey,
                                Object input) {
        if (input instanceof Collection) {
            Collection list = (Collection) input;
            int i = 0;
            for (Object e : list) {
                String key = getKey(rootKey, "E" + i++);
                flatMap(result, key, e);
            }
        } else if (input instanceof Map) {
            Map<String, Object> map = (Map) input;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                flatMap(result, getKey(rootKey, entry.getKey()), entry.getValue());
            }
        } else {
            result.put(rootKey, input);
        }
    }

    private static class PacketsWrapper {
        private Collection packets;

        public PacketsWrapper() {
        }

        public PacketsWrapper(Collection packets) {
            this.packets = packets;
        }

        public Collection getPackets() {
            return packets;
        }

        public void setPackets(Collection packets) {
            this.packets = packets;
        }
    }

    public static SecretKey getAESKey(int keySize) throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(keySize, SecureRandom.getInstanceStrong());
        return keyGen.generateKey();
    }

    public static byte[] getRandomNonce(int byteSize) {
        byte[] nonce = new byte[byteSize];
        new SecureRandom().nextBytes(nonce);
        return nonce;
    }

    public static byte[] encrypt(byte[] pText, SecretKey secret, byte[] iv)
            throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secret, new GCMParameterSpec(128, iv));
        return cipher.doFinal(pText);
    }

    public static byte[] xorBody(byte[] a, byte[] b) {
        int aLen = a.length;

        int chunkSize = 32;

        if (aLen <= chunkSize) {
            return xor(a, b);
        }

        int outer = aLen / chunkSize;
        byte[] result = new byte[aLen];

        for (int i = 0; i < outer; i++) {
            byte[] tmp = new byte[chunkSize];
            for (int j = 0; j < chunkSize; j++) {
                tmp[j] = a[(i * chunkSize) + j];
            }
            byte[] resultTmp = xor(tmp, b);
            for (int j = 0; j < chunkSize; j++) {
                result[(i * chunkSize) + j] = resultTmp[j];
            }
        }
        int remain = aLen - (outer * chunkSize);
        byte[] lastPart = new byte[remain];
        for (int i = 0; i < remain; i++) {
            lastPart[i] = a[(outer * chunkSize) + i];
        }
        byte[] resultTmp = xor(lastPart, b);
        for (int i = 0; i < remain; i++) {
            result[(outer * chunkSize) + i] = resultTmp[i];
        }
        return result;
    }

    public static byte[] xor(byte[] a, byte[] b) {
        int aLen = a.length;
        int bLen = b.length;
        int min = 0;
        int size = aLen > bLen ? aLen : bLen;
        byte[] c = new byte[size];
        if (size == aLen) {
            min = bLen;
            System.arraycopy(a, min, c, min, size - min);
        } else {
            min = aLen;
            System.arraycopy(b, min, c, min, size - min);
        }
        for (int i = 0; i < min; i++) {
            c[i] = (byte) (a[i] ^ b[i]);
        }
        return c;
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

}