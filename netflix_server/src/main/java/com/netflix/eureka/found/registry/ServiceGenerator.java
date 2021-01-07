package com.netflix.eureka.found.registry;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.eureka.security.RsaKeyHelper;

public class ServiceGenerator {
	private static Logger log = LoggerFactory.getLogger(ServiceGenerator.class);
	
	private final SecretProperties properties;
	
	private KeyPair keyPair;
	
	public ServiceGenerator(SecretProperties properties) {
		this.properties = properties;
	}
	
	public void init() {
		KeyPairGenerator keyPairGenerator = RsaKeyHelper.init(properties.getSeed());
		if(keyPairGenerator != null) {
			this.keyPair = keyPairGenerator.genKeyPair();
		}
	}
	
	public String sign(String data) {
		try {
			return RsaKeyHelper.sign(data, keyPair);
		} catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | SignatureException e) {
			log.error(e.getMessage());
		}
		return null;
	}
	
	public byte[] verify(String data) {
		try {
			return RsaKeyHelper.verify(data, keyPair);
		} catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | SignatureException e) {
			log.error(e.getMessage());
		} 
		return null;
	}
}
