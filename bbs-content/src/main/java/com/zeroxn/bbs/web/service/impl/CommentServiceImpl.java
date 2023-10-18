package com.zeroxn.bbs.web.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryWrapper;
import com.zeroxn.bbs.base.entity.Comment;
import com.zeroxn.bbs.core.exception.ExceptionUtils;
import com.zeroxn.bbs.web.dto.CommentTreeDto;
import com.zeroxn.bbs.web.dto.PageQueryDto;
import com.zeroxn.bbs.web.mapper.CommentMapper;
import com.zeroxn.bbs.web.service.CommentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static com.zeroxn.bbs.core.entity.table.CommentTableDef.COMMENT;
import static com.zeroxn.bbs.core.entity.table.UserTableDef.USER;

/**
 * @Author: lisang
 * @DateTime: 2023-10-17 18:36:55
 * @Description: 评论管理服务层实现类
 */
@Service
public class CommentServiceImpl implements CommentService {
    private static final Logger logger = LoggerFactory.getLogger(ContentServiceImpl.class);
    private final CommentMapper commentMapper;
    public CommentServiceImpl(CommentMapper commentMapper) {
        this.commentMapper = commentMapper;
    }
    @Override
    public void saveComment(Comment comment) {
        int result = commentMapper.insertSelective(comment);
        logger.info("插入评论成功，影响行数：{}", result);
    }

    @Override
    public Page<CommentTreeDto> pageTopicCommentList(Integer topicId, PageQueryDto pageDto) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(COMMENT.ID, COMMENT.CONTENT, COMMENT.CREATE_TIME, COMMENT.USER_ID, COMMENT.RID)
                .select(USER.NICK_NAME, USER.AVATAR)
                .from(COMMENT)
                .leftJoin(USER).on(USER.ID.eq(COMMENT.USER_ID))
                .where(COMMENT.TOPIC_ID.eq(topicId))
                .and(COMMENT.RID.eq(0))
                .orderBy(COMMENT.CREATE_TIME.asc());
        Page<CommentTreeDto> commentTreePage = commentMapper.paginateAs(pageDto.getPage(), pageDto.getSize(), queryWrapper, CommentTreeDto.class);
        if (commentTreePage.getRecords() != null && !commentTreePage.getRecords().isEmpty()) {
            // 使用多线程安全的Map
            Map<Long, CompletableFuture<Page<CommentTreeDto>>> childrenFutureMap = new ConcurrentHashMap<>(commentTreePage.getRecords().size());
            commentTreePage.getRecords().forEach(comment -> {
                CompletableFuture<Page<CommentTreeDto>> childrenCommentFuture = CompletableFuture.supplyAsync(() ->
                        pageCommentChildrenNodes(comment.getId(), 1, 5));
                childrenFutureMap.put(comment.getId(), childrenCommentFuture);
            });
            // 等待所有线程执行完毕
            try {
                CompletableFuture.allOf(childrenFutureMap.values().toArray(new CompletableFuture[]{})).get();
            }catch (Exception ex) {
                logger.error("等待所有异步线程执行完毕报错，错误信息：{}", ex.getMessage());
                return commentTreePage;
            }
            commentTreePage.getRecords().forEach(comment -> {
                try {
                    Page<CommentTreeDto> childrenCommentPage = childrenFutureMap.get(comment.getId()).get();
                    if (childrenCommentPage != null && childrenCommentPage.getRecords() != null && !childrenCommentPage.getRecords().isEmpty()) {
                        comment.setChildrenPage(childrenCommentPage);
                    }
                } catch (Exception ex) {
                    logger.error("异步获取一级评论的子评论报错，错误信息：{}", ex.getMessage());
                }
            });
        }
        return commentTreePage;
    }
    public Page<CommentTreeDto> pageCommentChildrenNodes(Long commentId, int page, int size) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select("c1.id", "c1.content", "c1.create_time", "c1.user_id", "c1.rid", "u1.nick_name",
                        "u1.avatar", "u2.nick_name as recover_nick_name")
                .from(COMMENT).as("c1")
                .leftJoin(USER).as("u1").on("u1.id = c1.user_id")
                .innerJoin(COMMENT).as("c2").on("c2.id = c1.rid")
                .leftJoin(USER).as("u2").on("u2.id = c2.user_id")
                .where("c1.fid = " + commentId)
                .and("c1.rid != 0")
                .orderBy("c1.id", true);
        return commentMapper.paginateAs(page, size, queryWrapper, CommentTreeDto.class);
    }

    @Override
    public CommentTreeDto queryCommentInfo(Long commentId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(COMMENT.ID, COMMENT.CONTENT, COMMENT.CREATE_TIME, COMMENT.USER_ID, COMMENT.RID)
                .select(USER.NICK_NAME, USER.AVATAR)
                .from(COMMENT)
                .leftJoin(USER).on(USER.ID.eq(COMMENT.USER_ID))
                .where(COMMENT.ID.eq(commentId));
        CommentTreeDto commentDto = commentMapper.selectOneByQueryAs(queryWrapper, CommentTreeDto.class);
        ExceptionUtils.isConditionThrow(commentDto == null, HttpStatus.NOT_FOUND, "评论不存在");
        if (commentDto.getRid() != null && commentDto.getRid() != 0) {
            String recoverNickName = QueryChain.of(Comment.class)
                    .select(USER.NICK_NAME)
                    .from(COMMENT)
                    .leftJoin(USER).on(USER.ID.eq(COMMENT.USER_ID))
                    .where(COMMENT.ID.eq(commentDto.getRid()))
                    .oneAs(String.class);
            commentDto.setRecoverNickName(recoverNickName);
        }
        return commentDto;
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentMapper.selectOneById(commentId);
        ExceptionUtils.isConditionThrowRequest(comment == null, "删除的评论不存在");
        ExceptionUtils.isConditionThrowRequest(!comment.getUserId().equals(userId), "用户无权限删除");
        commentMapper.deleteById(commentId);
        if (comment.getRid() == 0) {
            int result = commentMapper.deleteByQuery(new QueryWrapper().where(COMMENT.FID.eq(commentId)));
            logger.info("一级评论ID：{}，删除一级评论下所有的子评论，删除条数：{}", commentId, result);
        }else {
            int result = commentMapper.deleteChildrenComment(commentId);
            logger.info("评论ID：{}，递归删除其下的所有子评论，删除条数：{}", commentId, result);
        }
        logger.info("评论删除成功，评论ID:{}", commentId);
    }
}
