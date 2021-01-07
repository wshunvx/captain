package com.netflix.eureka.security;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.alibaba.csp.sentinel.log.jul.Level;


public class RsaKeyHelper {
	private static Logger log = Logger.getLogger(RsaKeyHelper.class.getName());
	
	private static final String ENC = "UTF-8";
	private static final String ALG = "RSA/ECB/PKCS1Padding"; 

    /**
     * 获取公钥
     *
     * @param publicKey
     * @return
     * @throws Exception
     */
    public PublicKey getPublicKey(byte[] publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKey);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    /**
     * 获取密钥
     *
     * @param privateKey
     * @return
     * @throws InvalidKeySpecException 
     * @throws Exception
     */
    public PrivateKey getPrivateKey(byte[] privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKey);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    /**
     * 生存rsa公钥
     *
     * @param password
     * @throws IOException
     */
    public static byte[] generatePublicKey(String password) throws IOException {
    	KeyPairGenerator keyPairGenerator = init(password);
        KeyPair keyPair = keyPairGenerator.genKeyPair();
        return keyPair.getPublic().getEncoded();
    }

    /**
     * 生存rsa公钥
     *
     * @param password
     * @throws IOException
     */
    public static byte[] generatePrivateKey(String password) throws IOException {
    	KeyPairGenerator keyPairGenerator = init(password);
        KeyPair keyPair = keyPairGenerator.genKeyPair();
        return keyPair.getPrivate().getEncoded();
    }

    public static KeyPair generateKey(String password) throws IOException {
    	KeyPairGenerator keyPairGenerator = init(password);
        return keyPairGenerator.genKeyPair();
    }

	public static String toHexString(byte[] b) {
		Encoder encoder = Base64.getEncoder();
		return encoder.encodeToString(b);
    }
	
	public static byte[] toHexBytes(String data) {
		Decoder decoder = Base64.getDecoder();
		return decoder.decode(data);
    }
	
	public static String sign(String data,  KeyPair keyPair) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyPair.getPublic().getEncoded());
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
        return encryptAESEncrypt(data, publicKey);
	}
	
	public static byte[] verify(String data, KeyPair keyPair) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
		PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyPair.getPrivate().getEncoded());
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        return decryptAESEncrypt(data, privateKey);
	}

	public static final KeyPairGenerator init(String password) {
		KeyPairGenerator keyPairGenerator = null;
		try {
			keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			SecureRandom secureRandom = new SecureRandom(password.getBytes());
	        keyPairGenerator.initialize(1024, secureRandom);
		} catch (NoSuchAlgorithmException e) {
			log.log(Level.ERROR, e.getMessage());
		}
        return keyPairGenerator;
	}

	/**
	 * bluefis encrypt
	 * @param value
	 * @return
	 */
	public static String encryptAESEncrypt(String data, PublicKey publicKey){
		try {
			Cipher aesCipher = Cipher.getInstance(ALG);
			aesCipher.init(Cipher.ENCRYPT_MODE, publicKey);
			
			byte[] test = aesCipher.doFinal(data.getBytes(ENC));
			return toHexString(test);
		} catch (UnsupportedEncodingException 
				| NoSuchAlgorithmException 
				| NoSuchPaddingException 
				| InvalidKeyException 
				| IllegalBlockSizeException 
				| BadPaddingException e) {
			log.log(Level.ERROR, e.getMessage());
		}
		return null;
	}

	/**
	 * bluefis decrypt
	 * @param body
	 * @return
	 */
	public static byte[] decryptAESEncrypt(String data, PrivateKey privateKey) {
		try {
			Cipher aesCipher = Cipher.getInstance(ALG);
			aesCipher.init(Cipher.DECRYPT_MODE, privateKey);
			
			byte[] test = toHexBytes(data);
			return aesCipher.doFinal(test);
		} catch (NoSuchAlgorithmException 
				| NoSuchPaddingException 
				| InvalidKeyException 
				| IllegalBlockSizeException 
				| BadPaddingException e) {
			log.log(Level.ERROR, e.getMessage());
		}
		return null;
	}
	
    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
    	String body = "{\"id\": \"1000100120000001\", \"hostname\": \"10.10.10.104\"}";
    	String password = "y2paX#huGp2&vPez54UMRVNyXvlbzT3/b80hLdpsS9%AOSrb4dK";
    	
        KeyPairGenerator keyPairGenerator = init(password);
        KeyPair keyPair = keyPairGenerator.genKeyPair();
        
        String result = sign(body, keyPair);
        System.out.println("Rsa sign : " + result);
        
        byte[] decrypt = verify(result, keyPair);
        System.out.println("Rsa verify : " + new String(decrypt));
    }
}
