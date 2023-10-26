package com.zeroxn.bbs.web.controller;

import com.mybatisflex.core.paginate.Page;
import com.zeroxn.bbs.base.entity.UserMessage;
import com.zeroxn.bbs.core.utils.BbsUtils;
import com.zeroxn.bbs.web.dto.PageQueryDto;
import com.zeroxn.bbs.web.dto.Result;
import com.zeroxn.bbs.web.dto.UserPublicMessageDto;
import com.zeroxn.bbs.web.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: lisang
 * @DateTime: 2023-10-20 17:15:33
 * @Description: 消息管理 控制层
 */
@RestController
@RequestMapping("/message")
public class MessageController {
    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/public/list")
    @Operation(description = "获取所有公共消息列表")
    public Result<Page<UserPublicMessageDto>> listPublicMessage(@Validated PageQueryDto pageDto, @AuthenticationPrincipal Jwt jwt) {
        Long userId = BbsUtils.formJwtGetUserId(jwt);
        Page<UserPublicMessageDto> publicMessagePage = messageService.pagePublicMessage(pageDto, userId);
        return Result.success(publicMessagePage);
    }

    @GetMapping("/user/list")
    @Operation(description = "获取用户信息列表")
    public Result<Page<UserMessage>> listUserMessage(@Validated PageQueryDto pageDto, @AuthenticationPrincipal Jwt jwt) {
        Long userId = BbsUtils.formJwtGetUserId(jwt);
        Page<UserMessage> userMessagePage = messageService.pageUserMessage(pageDto, userId);
        return Result.success(userMessagePage);
    }

    @PutMapping("/public/{id}")
    @Operation(description = "已读公共消息")
    @Parameter(name = "id", description = "公共消息ID", required = true)
    public Result<Void> readPublicMessage(@PathVariable("id") Integer messageId, @AuthenticationPrincipal Jwt jwt) {
        Long userId = BbsUtils.formJwtGetUserId(jwt);
        messageService.readPublicMessage(messageId, userId);
        return Result.ok();
    }

    @DeleteMapping("/public/{id}")
    @Operation(description = "用户删除公共消息")
    @Parameter(name = "id", description = "公共消息ID", required = true)
    public Result<Void> deletePublicMessage(@PathVariable("id") Integer messageId, @AuthenticationPrincipal Jwt jwt) {
        Long userId = BbsUtils.formJwtGetUserId(jwt);
        messageService.deletePublicMessage(messageId, userId);
        return Result.ok();
    }

    @PutMapping("/user/{id}")
    @Operation(description = "已读用户消息")
    @Parameter(name = "id", description = "用户消息ID", required = true)
    public Result<Void> readUserMessage(@PathVariable("id") Integer messageId, @AuthenticationPrincipal Jwt jwt) {
        Long userId = BbsUtils.formJwtGetUserId(jwt);
        messageService.readUserMessage(messageId, userId);
        return Result.ok();
    }

    @DeleteMapping("/user/{id}")
    @Operation(description = "删除用户消息")
    @Parameter(name = "id", description = "用户消息Id", required = true)
    public Result<Void> deleteUserMessage(@PathVariable("id") Integer messageId, @AuthenticationPrincipal Jwt jwt) {
        Long userId = BbsUtils.formJwtGetUserId(jwt);
        messageService.deleteUserMessage(messageId, userId);
        return Result.ok();
    }
}
