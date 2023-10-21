package com.zeroxn.bbs.web.service;

import com.mybatisflex.core.paginate.Page;
import com.zeroxn.bbs.base.entity.Comment;
import com.zeroxn.bbs.web.dto.CommentTreeDto;
import com.zeroxn.bbs.web.dto.PageQueryDto;

/**
 * @Author: lisang
 * @DateTime: 2023-10-17 18:36:08
 * @Description: 评论管理 服务层
 */
public interface CommentService {
    /**
     * 添加评论
     * @param comment 封装评论数据
     */
    void saveComment(Comment comment);

    /**
     * 通过帖子/话题Id以及分页信息获取帖子/话题的一级评论信息
     * @param topicId 帖子/话题Id
     * @param pageDto 分页参数
     * @return 返回封装的评论树形信息
     */
    Page<CommentTreeDto> pageTopicCommentList(Integer topicId, PageQueryDto pageDto);

    /**
     * 通过一级评论Id和分页信息获取子评论信息
     * @param commentId 一级评论Id
     * @param page 页码
     * @param size 每页记录数
     * @return 返回评论树形信息
     */
    Page<CommentTreeDto> pageCommentChildrenNodes(Long commentId, int page, int size);

    /**
     * 获取单条评论的详细信息
     * @param commentId 评论Id
     * @return 返回评论信息
     */
    CommentTreeDto queryCommentInfo(Long commentId);

    /**
     * 删除单条评论
     * @param commentId 评论id
     * @param userId 当前操作的用户id
     */
    void deleteComment(Long commentId, Long userId);

    /**
     * 通过帖子/话题Id删除该帖子下所有的评论
     * @param topicId 帖子/话题Id
     * @return 返回删除的影响行数
     */
    int deleteCommentListByTopicId(Integer topicId);
}
