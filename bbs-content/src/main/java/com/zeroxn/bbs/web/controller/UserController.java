package com.zeroxn.bbs.web.controller;

import com.zeroxn.bbs.core.common.StudentAuthService;
import com.zeroxn.bbs.core.entity.Student;
import com.zeroxn.bbs.web.dto.Result;
import com.zeroxn.bbs.web.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    private final StudentAuthService authService;

    public UserController(UserService userService, StudentAuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(description = "用户登陆接口")
    public Result<String> login(@RequestParam("code") String code) {
        String result = userService.login(code);
        return Result.ok(result);
    }

    @PostMapping("/phone")
    @Operation(description = "获取用户手机号接口")
    public Result<String> getUserPhone(@RequestParam("code") String code) {
        String phone = userService.getUserPhone(code);
        return Result.ok(phone);
    }

    @PostMapping("/student/code")
    @Operation(description = "学生认证请求验证码接口")
    public Result<Boolean> getAuthCode(@RequestParam("studentId") String studentId) {
        boolean result = authService.sendStudentLoginCode(studentId);
        return Result.ok(result);
    }

    @PostMapping("/student/auth")
    @Operation(description = "学生认证接口")
    public Result<Student> studentAuth(@RequestParam("studentId") String studentId, @RequestParam("code") String code) {
        Student studentInfo = authService.getStudentInfo(studentId, code);
        return Result.ok(studentInfo);
    }
}
