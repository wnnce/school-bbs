package com.zeroxn.bbs.web.controller;

import com.zeroxn.bbs.core.entity.User;
import com.zeroxn.bbs.core.utils.BbsUtils;
import com.zeroxn.bbs.web.dto.Result;
import com.zeroxn.bbs.web.dto.UpdateUserDto;
import com.zeroxn.bbs.web.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @Author: lisang
 * @DateTime: 2023-10-12 20:11:14
 * @Description: 论坛用户控制层
 */
@RestController
@RequestMapping("/user")
@Tag(name = "用户接口")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    @Operation(description = "用户登陆接口")
    public Result<Map<String, String>> login(@RequestParam("code") String code) {
        String token = userService.login(code);
        return Result.success(Map.of("token", token));
    }

    @PostMapping("/phone")
    @Operation(description = "获取用户手机号接口")
    public Result<String> getUserPhone(@RequestParam("code") String code) {
        String phone = userService.getUserPhone(code);
        return Result.ok(phone);
    }

    @GetMapping("/userInfo")
    @Operation(description = "获取用户信息接口")
    public Result<User> getUserInfo(@AuthenticationPrincipal Jwt jwt) {
        Long userId = BbsUtils.formJwtGetUserId(jwt);
        User user = userService.queryUserInfo(userId);
        return Result.success(user);
    }

    @PutMapping("/userInfo")
    @Operation(description = "更新用户信息接口")
    public Result<String> updateUserInfo(@RequestBody @Validated UpdateUserDto userDto, @AuthenticationPrincipal Jwt jwt) {
        Long userId = BbsUtils.formJwtGetUserId(jwt);
        userService.updateUserInfo(userId, userDto);
        return Result.ok();
    }

    @PostMapping("/student/code")
    @Operation(description = "学生认证请求验证码接口")
    public Result<Boolean> getAuthCode(@RequestParam("studentId") String studentId) {
        boolean result = userService.sendStudentAuthCode(studentId);
        return Result.success(result);
    }

    @PostMapping("/student/auth")
    @Operation(description = "学生认证接口")
    public Result<Map<String, Object>> studentAuth(@RequestParam("studentId") String studentId, @RequestParam("code") String code,
                                       @AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getId());
        Map<String, Object> result = userService.studentAuth(studentId, code, userId);
        return Result.success(result);
    }
}
