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
import com.zeroxn.bbs.web.service.async.GlobalAsyncTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static com.zeroxn.bbs.base.entity.table.CommentTableDef.COMMENT;
import static com.zeroxn.bbs.base.entity.table.UserTableDef.USER;

/**
 * @Author: lisang
 * @DateTime: 2023-10-17 18:36:55
 * @Description: 评论管理服务层实现类
 */
@Service
public class CommentServiceImpl implements CommentService {
    private static final Logger logger = LoggerFactory.getLogger(ContentServiceImpl.class);
    private final CommentMapper commentMapper;
    private final GlobalAsyncTask asyncTask;
    public CommentServiceImpl(CommentMapper commentMapper, GlobalAsyncTask asyncTask) {
        this.commentMapper = commentMapper;
        this.asyncTask = asyncTask;
    }

    /**
     * 先往数据库添加评论信息，然后判断评论的上级评论Id是否为0，
     * 如果为0，那么调用异步任务方法发送帖子/话题被评论的用户消息
     * 如果为1,同样调用异步方法发送评论被回复的用户消息
     * @param comment 封装评论数据
     */
    @Override
    public void saveComment(Comment comment) {
        int result = commentMapper.insertSelective(comment);
        logger.info("插入评论成功，影响行数：{}", result);
        if (comment.getRid() == 0) {
            asyncTask.sendTopicUserMessage(comment);
        }else {
            asyncTask.sendCommentUserMessage(comment);
        }
    }

    /**
     * 先查询出该帖子所有的一级评论信息，再使用异步线程对一级评论信息进行遍历，获取其中可能存在的子评论信息和分页参数
     * 一级评论的子评论默认获取第一页和五条，按照发布时间倒序排序
     * @param topicId 帖子/话题Id
     * @param pageDto 分页参数
     * @return 返回封装后的评论树形信息
     */
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

    /**
     * 通过一级评论id获取一级评论的子评论列表
     * @param commentId 一级评论Id
     * @param page 页码
     * @param size 每页记录数
     * @return 返回空或者该评论的子评论列表
     */
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

    /**
     * 通过评论Id获取评论的详细信息，先获取到评论信息判断是否为空以及是否存在上级评论，如果存在上级评论那么再获取上级评论的用户昵称
     * @param commentId 评论Id
     * @return 返回单条评论详细信息
     */
    @Override
    public CommentTreeDto queryCommentInfo(Long commentId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(COMMENT.ID, COMMENT.CONTENT, COMMENT.CREATE_TIME, COMMENT.USER_ID, COMMENT.RID)
                .select(USER.NICK_NAME, USER.AVATAR)
                .from(COMMENT)
                .leftJoin(USER).on(USER.ID.eq(COMMENT.USER_ID))
                .where(COMMENT.ID.eq(commentId));
        // 先获取评论西信息
        CommentTreeDto commentDto = commentMapper.selectOneByQueryAs(queryWrapper, CommentTreeDto.class);
        // 判断是否为空和是否存在上级评论
        ExceptionUtils.isConditionThrow(commentDto == null, HttpStatus.NOT_FOUND, "评论不存在");
        if (commentDto.getRid() != null && commentDto.getRid() != 0) {
            // 存在上级评论则获取上级评论的用户昵称
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

    /**
     * 删除评论，针对一级评论直接删除后在删除所有fid = commentId的评论信息
     * 如果不是一级评论，则通过rid进行递归删除，删除其下所有的子评论信息
     * @param commentId 评论id
     * @param userId 当前操作的用户id
     */
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

    @Override
    public int deleteCommentListByTopicId(Integer topicId) {
        return commentMapper.deleteByQuery(new QueryWrapper().where(COMMENT.TOPIC_ID.eq(topicId)));
    }
}
