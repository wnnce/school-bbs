package com.zeroxn.bbs.web.controller;

import com.mybatisflex.core.paginate.Page;
import com.zeroxn.bbs.base.entity.Comment;
import com.zeroxn.bbs.core.util.BbsUtils;
import com.zeroxn.bbs.web.dto.CommentTreeDto;
import com.zeroxn.bbs.web.dto.PageQueryDto;
import com.zeroxn.bbs.web.dto.Result;
import com.zeroxn.bbs.web.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: lisang
 * @DateTime: 2023-10-17 18:20:28
 * @Description: 评论管理 控制层
 */
@RestController
@RequestMapping("/comment")
@Tag(name = "评论管理接口")
public class CommentController {
    private final CommentService commentService;
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }
    @PostMapping
    @Operation(description = "保存评论")
    public Result<Void> saveComment(@RequestBody @Validated Comment comment, @AuthenticationPrincipal Jwt jwt) {
        Long userId = BbsUtils.formJwtGetUserId(jwt);
        comment.setUserId(userId);
        commentService.saveComment(comment);
        return Result.ok();
    }
    @GetMapping("/{id}")
    @Operation(description = "获取单个评论详情接口")
    @Parameter(name = "id", description = "评论ID", required = true)
    public Result<CommentTreeDto> queryComment(@PathVariable("id") Long commentId) {
        CommentTreeDto commentTreeDto = commentService.queryCommentInfo(commentId);
        return Result.success(commentTreeDto);
    }

    @GetMapping("/children/{commentId}/list")
    @Operation(description = "获取一级评论的子评论列表")
    @Parameter(name = "commentId", description = "一级评论的ID", required = true)
    public Result<Page<CommentTreeDto>> pageCommentChildren(@PathVariable("commentId") Long commentId, @Validated PageQueryDto pageDto) {
        Page<CommentTreeDto> commentTreeDtoPage = commentService.pageCommentChildrenNodes(commentId, pageDto.getPage(), pageDto.getSize());
        return Result.success(commentTreeDtoPage);
    }

    @GetMapping("/topic/{topicId}/list")
    @Operation(description = "获取帖子评论列表")
    @Parameter(name = "topicId", description = "帖子/话题ID", required = true)
    public Result<Page<CommentTreeDto>> pageCommentByTopicId(@PathVariable("topicId") Integer topicId, @Validated PageQueryDto pageDto) {
        Page<CommentTreeDto> commentTreeDtoPage = commentService.pageTopicCommentList(topicId, pageDto);
        return Result.success(commentTreeDtoPage);
    }

    @DeleteMapping("/{id}")
    @Operation(description = "删除评论接口")
    @Parameter(name = "id", description = "评论ID", required = true)
    public Result<Void> deleteComment(@PathVariable("id") Long commentId, @AuthenticationPrincipal Jwt jwt) {
        Long userId = BbsUtils.formJwtGetUserId(jwt);
        commentService.deleteComment(commentId, userId);
        return Result.ok();
    }
}
