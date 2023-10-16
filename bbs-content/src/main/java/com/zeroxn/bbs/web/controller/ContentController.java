package com.zeroxn.bbs.web.controller;

import com.zeroxn.bbs.core.entity.ForumTopic;
import com.zeroxn.bbs.core.utils.BbsUtils;
import com.zeroxn.bbs.core.validation.ValidationGroups.SaveTopicValidation;
import com.zeroxn.bbs.core.validation.ValidationGroups.SavePostValidation;
import com.zeroxn.bbs.web.dto.Result;
import com.zeroxn.bbs.web.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: lisang
 * @DateTime: 2023-10-14 10:53:02
 * @Description: 帖子/话题控制层
 */
@RestController
@RequestMapping("/content")
@Tag(name = "话题/帖子接口")
public class ContentController {
    private final ContentService contentService;
    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    @PostMapping("/topic")
    @Operation(description = "保存话题接口")
    public Result<Void> saveTopic(@RequestBody @Validated(SaveTopicValidation.class) ForumTopic topic,
                                  @AuthenticationPrincipal Jwt jwt) {
        Long userId = BbsUtils.formJwtGetUserId(jwt);
        topic.setUserId(userId);
        contentService.saveTopic(topic);
        return Result.ok();
    }
    @PostMapping("/post")
    @Operation(description = "保存帖子接口")
    public Result<Void> savePost(@RequestBody @Validated(SavePostValidation.class) ForumTopic post,
                                 @AuthenticationPrincipal Jwt jwt) {
        Long userId = BbsUtils.formJwtGetUserId(jwt);
        post.setUserId(userId);
        contentService.savePost(post);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(description = "删除帖子接口")
    public Result<Void> deleteTopic(@PathVariable("id") Integer topicId, @AuthenticationPrincipal Jwt jwt) {
        Long userId = BbsUtils.formJwtGetUserId(jwt);
        contentService.deleteTopic(topicId, userId);
        return Result.ok();
    }

}
