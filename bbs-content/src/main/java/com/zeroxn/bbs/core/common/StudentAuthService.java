package com.zeroxn.bbs.core.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeroxn.bbs.core.cache.CacheService;
import com.zeroxn.bbs.base.entity.Student;
import com.zeroxn.bbs.core.exception.ExceptionUtils;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

/**
 * @Author: lisang
 * @DateTime: 2023-10-12 21:00:23
 * @Description: 学生认证服务层
 */
@Component
public class StudentAuthService {
    /**
     * 获取学号关联手机号接口
     */
    private static final String GET_PHONE_URL = "http://szxy.cqtbi.edu.cn/cqdddt/bsdt!getPhone.action";
    /**
     * 发送验证码接口
     */
    private static final String SEND_CODE_URL = "http://szxy.cqtbi.edu.cn/cqdddt/bsdt!sendCheckCode.action";
    /**
     * 学校官网登录接口
     */
    private static final String LOGIN_URL = "http://szxy.cqtbi.edu.cn/cqdddt/bsdt!loginComputerPhoneCheck.action";
    /**
     * 获取AccessToken接口
     */
    private static final String TOKEN_URL = "http://lryw.cqtbi.edu.cn/bigData/bi/Oauth/ddSso";
    /**
     * 获取用户信息接口
     */
    private static final String USERINFO_URL = "http://lryw.cqtbi.edu.cn/bigData/dataCenter/arch/themesFront/getThemeDataObject";
    private static final String CACHE_PREFIX = "StudentAuth:";
    private static final Logger logger = LoggerFactory.getLogger(StudentAuthService.class);

    private final OkHttpClient client;
    private final CacheService cacheService;
    private final ObjectMapper objectMapper;
    public StudentAuthService(OkHttpClient client, CacheService cacheService, ObjectMapper objectMapper) {
        this.client = client;
        this.cacheService = cacheService;
        this.objectMapper = objectMapper;
    }

    /**
     * 请求学校官网接口获取手机验证码
     * @param studentId 学生学号
     * @return 返回是否获取成功
     */
    public boolean sendStudentLoginCode(String studentId) {
        RequestBody requestBody = new FormBody.Builder()
                .add("sfz", studentId)
                .build();
        Request phoneRequest = new Request.Builder()
                .url(GET_PHONE_URL)
                .post(requestBody)
                .build();
        String cookie = null;
        try{
            Response response = client.newCall(phoneRequest).execute();
            ExceptionUtils.isConditionThrowServer(!response.isSuccessful(), "发送验证码失败，请重试");
            PhoneResult phoneResult = objectMapper.readValue(response.body().string(), PhoneResult.class);
            if (phoneResult.success()) {
                cookie = response.header("Set-Cookie");
            }else {
                ExceptionUtils.throwRequestException(phoneResult.msg());
            }
        }catch (IOException ex) {
            logger.error("获取学号关联手机号请求失败, 错误信息：{}", ex.getMessage());
            ExceptionUtils.throwServerException("发送验证码失败，请重试");
        }
        Request codeRequest = new Request.Builder()
                .url(SEND_CODE_URL)
                .header("Cookie", cookie)
                .build();
        try {
            Response codeResponse = client.newCall(codeRequest).execute();
            if (codeResponse.isSuccessful()) {
                if (codeResponse.body().string().contains("true")) {
                    // 获取成功将cookie添加到缓存中，缓存设置3分钟内有效
                    cacheService.setCache(CACHE_PREFIX + studentId, cookie, Duration.ofMinutes(3));
                    return true;
                }
            }
        }catch (IOException ex) {
            logger.error("发送验证码失败，错误信息：{}", ex.getMessage());
        }
        return false;
    }

    /**
     * 通过学号和验证码从学习官网拿到学生信息
     * @param studentId 学生学号
     * @param validationCode 学校发送的验证码
     * @return 返回学生信息
     */
    public Student getStudentInfo(String studentId, String validationCode) {
        String cacheKey = CACHE_PREFIX + studentId;
        String cookie = cacheService.getCache(cacheKey, String.class);
        // 通过学号判断缓存的Cookie是否存在或者已经失效
        if (cookie == null || cookie.isEmpty()){
            ExceptionUtils.throwRequestException("验证码不存在或者已失效");
        }
        RequestBody loginBody = new FormBody.Builder()
                .add("yzm", validationCode)
                .build();
        // 拿到PORTAL_TICKET
        Request loginRequest = new Request.Builder()
                .url(LOGIN_URL)
                .post(loginBody)
                .header("Cookie", cookie)
                .build();
        LoginResult loginResult = this.sendRequest(loginRequest, LoginResult.class);
        ExceptionUtils.isConditionThrowServer(loginResult == null, "学生认证失败，请重试");
        HttpUrl tokenUrl = generateBaseQueryUrl(TOKEN_URL)
                .addQueryParameter("PORTAL_TICKET", loginResult.ticket())
                .build();
        Request tokenRequest = new Request.Builder()
                .url(tokenUrl.url())
                .header("Cookie", cookie)
                .build();
        // 拿到获取用户信息需要的Token
        TokenResult tokenResult = this.sendRequest(tokenRequest, TokenResult.class);
        this.validationTokenResult(tokenResult);
        String token = tokenResult.content().get("token");
        HttpUrl infoUrl = generateBaseQueryUrl(USERINFO_URL)
                .addQueryParameter("id", "16394822077693")
                .addQueryParameter("pageNum", "1")
                .addQueryParameter("pageSize", "10")
                .build();
        Request infoRequest = new Request.Builder()
                .url(infoUrl.url())
                .header("Token", token)
                .build();
        TokenResult infoResult = this.sendRequest(infoRequest, TokenResult.class);
        this.validationTokenResult(infoResult);
        // 拿到学生信息并返回Student对象
        Map<String, String> content = infoResult.content();
        return Student.builder()
                .studentId(studentId)
                .studentName(content.get("15487"))
                .studentPhone(content.get("47044"))
                .studentGender(content.get("14048").contains("男") ? 0 : 1)
                .academyName(content.get("18357"))
                .className(content.get("32951"))
                .dormName(content.get("48612") + content.get("84199"))
                .school(content.get("25720"))
                .build();
    }
    private void validationTokenResult(TokenResult result) {
        ExceptionUtils.isConditionThrowServer(result == null || !"40001".equals(result.code), "学生认证失败，请重试");
    }

    private <T> T sendRequest(Request request, Class<T> clazz) {
        try{
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String body = response.body().string();
                return objectMapper.readValue(body, clazz);
            }
        }catch (IOException ex) {
            logger.error("请求失败，错误信息：{}", ex.getMessage());
        }
        return null;
    }
    private HttpUrl.Builder generateBaseQueryUrl(String url) {
        return HttpUrl.get(url).newBuilder()
                .addQueryParameter("universityId", "100007")
                .addQueryParameter("operatorId", "0")
                .addQueryParameter("clientCategory", "pc")
                .addQueryParameter("timestamp", String.valueOf(System.currentTimeMillis()));
    }
    // 承载数据的临时对象
    record PhoneResult(String phone, String msg, Boolean success){}
    record LoginResult(String ticket, Map<String, Object> data, Boolean success){}
    record TokenResult(String code, String message, String otherMsg, Map<String, String> content) {}
}
