package com.zeroxn.bbs.core.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.zeroxn.bbs.core.common.JwtService;
import com.zeroxn.bbs.web.service.UserService;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;

import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashSet;
import java.util.Set;

/**
 * @Author: lisang
 * @DateTime: 2023-10-13 13:17:02
 * @Description: Spring Security和Jwt配置类
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfigurations {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .build();
    }

    @Bean
    public WebSecurityCustomizer ignoringCustomizer() {
        return web -> web.ignoring().requestMatchers("/user/login", "/swagger-ui/**");
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource(JwtProperties properties, ResourceLoader resourceLoader) {
        KeyPair keyPair;
        if (properties.getRandomKey()) {
            keyPair = generateRsaKey();
        }else {
            keyPair = RsaKeyReader.keyPair(resourceLoader, properties.getPublicKeyPath(), properties.getPrivateKeyPath());
        }
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID("1111111111")
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
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

    static class RsaKeyReader {
        static KeyPair keyPair(ResourceLoader resourceLoader, String publicPath, String privatePath) {
            try {
                String publicKeyValue = new String(resourceLoader.getResource(publicPath).getInputStream().readAllBytes());
                String privateKeyValue = new String(resourceLoader.getResource(privatePath).getInputStream().readAllBytes());
                KeyFactory factory = KeyFactory.getInstance("RSA");
                PublicKey publicKey = getPublicKey(factory, publicKeyValue);
                PrivateKey privateKey = getPrivateKey(factory, privateKeyValue);
                return new KeyPair(publicKey, privateKey);
            }catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
        private static PublicKey getPublicKey(KeyFactory factory, String keyValue) throws Exception {
            keyValue = keyValue.replaceAll("-----BEGIN PUBLIC KEY-----\n", "");
            keyValue = keyValue.replaceAll("-----END PUBLIC KEY-----", "");
            byte[] decodeBytes = Base64.decodeBase64(keyValue);
            return factory.generatePublic(new X509EncodedKeySpec(decodeBytes));
        }
        private static PrivateKey getPrivateKey(KeyFactory factory, String keyValue) throws Exception {
            keyValue = keyValue.replaceAll("-----BEGIN RSA PRIVATE KEY-----\n", "");
            keyValue = keyValue.replaceAll("-----END RSA PRIVATE KEY-----", "");
            byte[] decodeBytes = Base64.decodeBase64(keyValue);
            return factory.generatePrivate(new PKCS8EncodedKeySpec(decodeBytes));
        }
    }

    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource, UserService userService) {
        Set<JWSAlgorithm> jwsAlgs = new HashSet<>();
        jwsAlgs.addAll(JWSAlgorithm.Family.RSA);
        jwsAlgs.addAll(JWSAlgorithm.Family.EC);
        jwsAlgs.addAll(JWSAlgorithm.Family.HMAC_SHA);
        ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        JWSKeySelector<SecurityContext> jwsKeySelector = new JWSVerificationKeySelector<>(jwsAlgs, jwkSource);
        jwtProcessor.setJWSKeySelector(jwsKeySelector);
        jwtProcessor.setJWTClaimsSetVerifier((claims, context) -> {});
        NimbusJwtDecoder jwtDecoder = new NimbusJwtDecoder(jwtProcessor);
        jwtDecoder.setJwtValidator(new UserTokenValidator(userService));
        return jwtDecoder;
    }

    @Bean
    public JwtService jwtService(JwtEncoder encoder, JwtProperties properties, ObjectMapper objectMapper) {
        return new JwtService(encoder, properties, objectMapper);
    }
}
