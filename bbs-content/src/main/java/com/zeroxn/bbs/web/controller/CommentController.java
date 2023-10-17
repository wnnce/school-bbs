package com.zeroxn.bbs.web.controller;

import com.zeroxn.bbs.core.entity.Comment;
import com.zeroxn.bbs.web.dto.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: lisang
 * @DateTime: 2023-10-17 18:20:28
 * @Description: 评论管理 控制层
 */
@RestController
@RequestMapping("/comment")
@Tag(name = "评论管理接口")
public class CommentController {

    @PostMapping
    public Result<Void> saveComment(@RequestBody @Validated Comment comment, @AuthenticationPrincipal Jwt jwt) {

    }
}
