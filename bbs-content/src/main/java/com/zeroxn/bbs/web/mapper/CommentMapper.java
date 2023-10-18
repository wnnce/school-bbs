package com.zeroxn.bbs.web.mapper;

import com.mybatisflex.core.BaseMapper;
import com.zeroxn.bbs.base.entity.Comment;
import org.apache.ibatis.annotations.Param;

/**
 * 统一评论表 映射层。
 *
 * @author lisang
 * @since 2023-10-12
 */
public interface CommentMapper extends BaseMapper<Comment> {
    /**
     * 递归删除一条评论下面所有的子评论
     * @param commentId 删除的评论ID
     * @return 返回删除的评论条数
     */
    int deleteChildrenComment(@Param("rid") Long commentId);
}
