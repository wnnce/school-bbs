package com.zeroxn.bbs.test;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import com.zeroxn.bbs.core.cache.MemoryCacheService;
import com.zeroxn.bbs.core.common.StudentAuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

/**
 * @Author: lisang
 * @DateTime: 2023-10-12 19:41:43
 * @Description:
 */

public class SchoolBbsTest {


    private StudentAuthService authService;
    @Test
    public void testMemoryCacheManager() {
        MemoryCacheService cacheManager = new MemoryCacheService();
        String message = "hello world";
        Map<Integer, String> map = Map.of(1, "a", 2, "b", 3, "b");
        cacheManager.setCache("map", map, Duration.ofHours(1));
        cacheManager.setCache("key", message, Duration.ofHours(1));
        String value = cacheManager.getCache("key", String.class);
        System.out.println(value);
        Map map1 = cacheManager.getCache("map", Map.class);
        System.out.println(map1);
    }

    @Test
    public void testGetStudentInfo() {
        String studentId = "1901615";
        boolean result = authService.sendStudentLoginCode(studentId);
        System.out.println(result);
        Scanner scanner = new Scanner(System.in);
        String code = scanner.nextLine();
        authService.getStudentInfo(studentId, code);
    }

    @Test
    public void testJwtEncode() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        ImmutableJWKSet<SecurityContext> jwkSet1 = new ImmutableJWKSet<>(jwkSet);
        NimbusJwtEncoder jwtEncoder = new NimbusJwtEncoder(jwkSet1);
        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .id("1")
                .issuedAt(Instant.now())
                .notBefore(Instant.now())
                .expiresAt(Instant.now())
                .audience(List.of("school_bbs"))
                .subject("hello world")
                .issuer("school_bbs")
                .build();
        JwtEncoderParameters parameters = JwtEncoderParameters.from(claimsSet);
        Jwt jwt = jwtEncoder.encode(parameters);
        System.out.println(jwt.getSubject());
        System.out.println(jwt.getTokenValue());
    }

    private static KeyPair generateRsaKey() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        }
        catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        return keyPair;
    }
}
