package com.zeroxn.bbs.web.controller;

import com.mybatisflex.core.paginate.Page;
import com.zeroxn.bbs.base.entity.ForumTopic;
import com.zeroxn.bbs.core.utils.BbsUtils;
import com.zeroxn.bbs.base.validation.ValidationGroups.SaveTopicValidation;
import com.zeroxn.bbs.base.validation.ValidationGroups.SavePostValidation;
import com.zeroxn.bbs.web.dto.*;
import com.zeroxn.bbs.web.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

    @GetMapping("/post/list")
    @Operation(description = "获取帖子列表接口")
    public Result<Page<UserTopicDto>> listPageForumPost(@Validated QueryPostDto postDto) {
        Page<UserTopicDto> topicDtoPage = contentService.pageForumPost(postDto);
        return Result.success(topicDtoPage);
    }

    @GetMapping("/topic/hot")
    @Operation(description = "获取热门话题")
    public Result<Page<UserTopicDto>> listHotTopic(@Validated PageQueryDto pageDto) {
        Page<UserTopicDto> hotTopicPage = contentService.pageHotTopic(pageDto);
        return Result.success(hotTopicPage);
    }
    @GetMapping("/topic/propose")
    @Operation(description = "获取用户最相关话题")
    public Result<Page<UserTopicDto>> listUserProposeTopic(@Validated PageQueryDto pageDto, @AuthenticationPrincipal Jwt jwt) {
        Long userId = BbsUtils.formJwtGetUserId(jwt);
        Page<UserTopicDto> proposeTopicPage = contentService.pageProposeTopic(pageDto, userId);
        return Result.success(proposeTopicPage);
    }

    @GetMapping("/{id}")
    @Operation(description = "获取帖子/话题详情接口")
    @Parameter(name = "id", description = "话题/帖子ID", required = true)
    public Result<UserTopicDto> queryTopicInfo(@PathVariable("id") Integer topicId, @AuthenticationPrincipal Jwt jwt) {
        Long userId = BbsUtils.formJwtGetUserId(jwt);
        UserTopicDto topicDto = contentService.queryTopic(topicId, userId);
        return Result.success(topicDto);
    }

    @DeleteMapping("/{id}")
    @Operation(description = "删除帖子接口")
    @Parameter(name = "id", description = "帖子/话题ID", required = true)
    public Result<Void> deleteTopic(@PathVariable("id") Integer topicId, @AuthenticationPrincipal Jwt jwt) {
        Long userId = BbsUtils.formJwtGetUserId(jwt);
        contentService.deleteTopic(topicId, userId);
        return Result.ok();
    }

    @PostMapping("/star/{id}")
    @Operation(description = "用户收藏帖子接口")
    @Parameter(name = "id", description = "话题/帖子ID", required = true)
    public Result<Void> userStarTopic(@PathVariable("id") Integer topicId, @AuthenticationPrincipal Jwt jwt) {
        Long userId = BbsUtils.formJwtGetUserId(jwt);
        contentService.starTopic(topicId, userId);
        return Result.ok();
    }

    @DeleteMapping("/unstar/{id}")
    @Operation(description = "用户取消收藏按钮")
    @Parameter(name = "id", description = "帖子/话题ID", required = true)
    public Result<Void> userUnStarTopic(@PathVariable("id") Integer topicId, @AuthenticationPrincipal Jwt jwt) {
        Long userId = BbsUtils.formJwtGetUserId(jwt);
        contentService.unStartTopic(topicId, userId);
        return Result.ok();
    }

    @GetMapping("/user/list")
    @Operation(description = "获取用户发布的所有帖子/话题")
    public Result<Page<UserTopicDto>> listUserPublishTopic(@Validated UserTopicQueryDto userTopicDto,
                                                           @AuthenticationPrincipal Jwt jwt) {
        Long userId = BbsUtils.formJwtGetUserId(jwt);
        Page<UserTopicDto> topicDtoPage = contentService.pageUserPublishTopic(userTopicDto, userId);
        return Result.success(topicDtoPage);
    }

    @GetMapping("/star/list")
    @Operation(description = "获取用户收藏的所有帖子/话题")
    public Result<Page<UserTopicDto>> listUserStarTopic(@Validated UserTopicQueryDto userTopicDto,
                                                        @AuthenticationPrincipal Jwt jwt) {
        Long userId = BbsUtils.formJwtGetUserId(jwt);
        Page<UserTopicDto> topicDtoPage = contentService.pageUserStarTopic(userTopicDto, userId);
        return Result.success(topicDtoPage);
    }
}
