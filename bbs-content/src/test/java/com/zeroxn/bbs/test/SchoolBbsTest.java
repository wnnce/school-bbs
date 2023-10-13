package com.zeroxn.bbs.test;

import com.zeroxn.bbs.core.cache.MemoryCacheService;
import com.zeroxn.bbs.core.common.StudentAuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.Map;
import java.util.Scanner;

/**
 * @Author: lisang
 * @DateTime: 2023-10-12 19:41:43
 * @Description:
 */

@SpringBootTest
public class SchoolBbsTest {

    @Autowired
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
}
