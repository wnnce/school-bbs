package com.zeroxn.bbs.web.service;

import com.mybatisflex.core.paginate.Page;
import com.zeroxn.bbs.core.entity.Comment;
import com.zeroxn.bbs.web.dto.CommentTreeDto;
import com.zeroxn.bbs.web.dto.PageQueryDto;

/**
 * @Author: lisang
 * @DateTime: 2023-10-17 18:36:08
 * @Description: 评论管理 服务层
 */
public interface CommentService {
    void saveComment(Comment comment);
    Page<CommentTreeDto> pageTopicCommentList(Integer topicId, PageQueryDto pageDto);
    Page<CommentTreeDto> pageCommentChildrenNodes(Long commentId, int page, int size);
    CommentTreeDto queryCommentInfo(Long commentId);
    void deleteComment(Long commentId, Long userId);
}
