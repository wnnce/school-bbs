package com.zeroxn.bbs.core.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeroxn.bbs.core.config.security.JwtProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Instant;
import java.util.Set;

/**
 * @Author: lisang
 * @DateTime: 2023-10-13 13:55:26
 * @Description: Jwt服务层
 */
public class JwtService {
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);
    private final JwtProperties properties;
    private final JwtEncoder encoder;
    private final ObjectMapper objectMapper;

    public JwtService(JwtEncoder encoder, JwtProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.encoder = encoder;
        this.objectMapper = objectMapper;
    }

    /**
     * 根据提供的信息生成Token
     * @param userId 用户ID
     * @param subject 开放的用户信息
     * @param scope 用户包含的权限
     * @return 返回生成的Token
     */
    public String generateToken(String userId, Object subject, Set<String> scope) {
        long expireTime = (System.currentTimeMillis() + properties.getExpireTime().toMillis()) / 1000;
        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .id(userId)
                .issuedAt(Instant.now())
                .notBefore(Instant.now())
                .expiresAt(Instant.ofEpochSecond(expireTime))
                .audience(properties.getAudience())
                .subject(subjectToString(subject))
                .issuer(properties.getIssuer())
                .claim("scope", scope)
                .build();
        JwtEncoderParameters parameters = JwtEncoderParameters.from(claimsSet);
        logger.info("生成用户Token成功，userId：{}，权限：{}", userId, scope);
        return encoder.encode(parameters).getTokenValue();
    }

    /**
     * Object类型的Subject转String 使用Jackson序列化
     * @param subject Token中包含的信息
     * @return 返回序列化后的字符串
     */
    private String subjectToString(Object subject) {
        String value = null;
        try {
            value = objectMapper.writeValueAsString(subject);
        }catch (JsonProcessingException ex) {
            logger.error("序列化Subject错误，错误信息：{}", ex.getMessage());
            value = subject.toString();
        }
        return value;
    }

    /**
     * 通过泛型参数，将Token中的信息转换为指定类型的对象
     * @param jwt Token解析成功后生成的Jwt
     * @param clazz 类型参数
     * @return 返回信息反序列化后的对象
     * @param <T> 泛型参数
     */
    public <T> T jwtSubjectToObject(Jwt jwt, Class<T> clazz) {
        T value = null;
        if (jwt.getSubject() != null && !jwt.getSubject().isEmpty()) {
            try {
                value = objectMapper.readValue(jwt.getSubject(), clazz);
            }catch (JsonProcessingException ex) {
                logger.error("反序列化Subject失败，错误信息：{}", ex.getMessage());
            }
        }
        return value;
    }
}