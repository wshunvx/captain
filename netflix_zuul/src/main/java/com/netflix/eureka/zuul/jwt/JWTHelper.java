package com.netflix.eureka.zuul.jwt;

import java.util.Date;

import org.joda.time.DateTime;

import com.netflix.eureka.http.constants.ZuulConstant;
import com.netflix.eureka.http.jwt.IJWTInfo;
import com.netflix.eureka.security.RsaKeyHelper;
import com.netflix.eureka.zuul.utils.StringHelper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JWTHelper {
    private static RsaKeyHelper rsaKeyHelper = new RsaKeyHelper();
    
    /**
     * 密钥加密token
     *
     * @param jwtInfo
     * @param priKey
     * @param expire
     * @return
     * @throws Exception
     */
    public static String generateToken(String subject, byte[] priKey, int expire) throws Exception {
        return Jwts.builder()
                .setSubject(subject)
                .setExpiration(DateTime.now().plusSeconds(expire).toDate())
                .signWith(SignatureAlgorithm.RS256, rsaKeyHelper.getPrivateKey(priKey))
                .compact();
    }

    /**
     * 密钥加密token
     *
     * @param jwtInfo
     * @param priKey
     * @param expire
     * @return
     * @throws Exception
     */
    public static String generateToken(IJWTInfo jwtInfo, byte[] priKey, int expire) throws Exception {
        return generateToken(jwtInfo, priKey, DateTime.now().plusSeconds(expire).toDate());
    }
    
    /**
     * 
     * @param jwtInfo
     * @param priKey
     * @param expire
     * @return
     * @throws Exception
     */
    public static String generateToken(IJWTInfo jwtInfo, byte priKey[], Date expire) throws Exception {
        return Jwts.builder()
                .setSubject(jwtInfo.getUniqueName())
                .claim(ZuulConstant.JWT_KEY_ID, jwtInfo.getId())
                .claim(ZuulConstant.JWT_KEY_METADATA, jwtInfo.getMetadata())
                .setExpiration(expire)
                .signWith(SignatureAlgorithm.RS256, rsaKeyHelper.getPrivateKey(priKey))
                .compact();
    }

    /**
     * 公钥解析token
     *
     * @param token
     * @return
     * @throws Exception
     */
    public static Jws<Claims> parserToken(String token, byte[] pubKey) throws Exception {
        return Jwts.parser().setSigningKey(rsaKeyHelper.getPublicKey(pubKey)).parseClaimsJws(token);
    }
    
    /**
     * 获取token中的用户信息
     *
     * @param token
     * @param pubKey
     * @return
     * @throws Exception
     */
    public static IJWTInfo getInfoFromToken(String token, byte[] pubKey) throws Exception {
        Jws<Claims> claimsJws = parserToken(token, pubKey);
        Claims body = claimsJws.getBody();
        return new JWTInfo(body.getSubject(), 
        		StringHelper.getObjectValue(body.get(ZuulConstant.JWT_KEY_ID)), 
        		StringHelper.getObjectList(body.get(ZuulConstant.JWT_KEY_METADATA))
        		);
    }
}
