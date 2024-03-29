package com.zeroxn.bbs.web.controller;

import com.zeroxn.bbs.base.entity.UserAction;
import com.zeroxn.bbs.core.util.BbsUtils;
import com.zeroxn.bbs.web.dto.SaveOrbitDto;
import com.zeroxn.bbs.web.service.OtherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: lisang
 * @DateTime: 2023-10-12 16:44:10
 * @Description: 不重要的接口 controller层
 */
@Tag(name = "其他接口")
@RestController
@RequestMapping("/extend")
public class OtherController {

    private final OtherService otherService;

    public OtherController(OtherService otherService) {
        this.otherService = otherService;
    }

    /**
     * 保存用户行为信息
     * @param userAction 用户行为数据
     */
    @PostMapping("/action")
    @Operation(description = "保存用户行为接口")
    public void saveUserAction(@RequestBody @Validated UserAction userAction, @AuthenticationPrincipal Jwt jwt) {
        Long userId = BbsUtils.formJwtGetUserId(jwt);
        userAction.setUserId(userId);
        otherService.saveUserAction(userAction);
    }

    /**
     * 保存用户位置信息
     * @param request http请求体
     * @param saveOrbit 用户位置信息
     */
    @PostMapping("/orbit")
    @Operation(description = "保存用户位置信息接口")
    public void saveUserOrbit(HttpServletRequest request, @RequestBody @Validated SaveOrbitDto saveOrbit,
                              @AuthenticationPrincipal Jwt jwt){
        Long userId = BbsUtils.formJwtGetUserId(jwt);
        saveOrbit.setUserId(userId);
        String ipAddress = BbsUtils.getRequestIpAddress(request);
        otherService.saveUserOrbit(saveOrbit, ipAddress);
    }
}
