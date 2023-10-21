package com.zeroxn.bbs.web.service.async;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.zeroxn.bbs.base.entity.*;
import com.zeroxn.bbs.web.mapper.*;
import com.zeroxn.bbs.web.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.zeroxn.bbs.base.entity.table.CommentTableDef.COMMENT;
import static com.zeroxn.bbs.base.entity.table.ForumTopicTableDef.FORUM_TOPIC;
import static com.zeroxn.bbs.base.entity.table.UserTableDef.USER;

/**
 * @Author: lisang
 * @DateTime: 2023-10-13 20:58:13
 * @Description: 全局异步任务类，使用自定义的线程池执行异步任务
 */
@Service
public class GlobalAsyncTask {
    private static final Logger logger = LoggerFactory.getLogger(GlobalAsyncTask.class);
    /**
     * 异步任务线程池，线程数量为当前CPU的内核数量
     */
    private static final ExecutorService executors = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final MessageService messageService;
    private final FileUploadMapper uploadMapper;
    private final UserExtrasMapper extrasMapper;
    private final UserProfileMapper profileMapper;
    private final UserMapper userMapper;
    private final ForumTopicMapper topicMapper;
    private final CommentMapper commentMapper;

    public GlobalAsyncTask(FileUploadMapper uploadMapper, UserExtrasMapper extrasMapper, UserProfileMapper profileMapper,
                           ForumTopicMapper topicMapper, UserMapper userMapper, MessageService messageService,
                           CommentMapper commentMapper) {
        this.messageService = messageService;
        this.uploadMapper = uploadMapper;
        this.extrasMapper = extrasMapper;
        this.profileMapper = profileMapper;
        this.topicMapper = topicMapper;
        this.userMapper = userMapper;
        this.commentMapper = commentMapper;
    }

    /**
     * 内部方法，用以运行所有没有放回值的异步方法，使用统一的线程池
     * @param func 函数式接口方法
     */
    private void executeVoidAsyncFunction(Runnable func) {
        CompletableFuture.runAsync(func, executors);
    }

    /**
     * 初始化用户的额外信息
     * @param userId 用户ID
     */
    public void initUserExtraInit(Long userId) {
        // 初始化用户额外信息表
        this.executeVoidAsyncFunction(() -> {
            UserExtras extras = UserExtras.builder()
                    .userId(userId)
                    .build();
            int result = extrasMapper.insertSelective(extras);
            logger.info("用户额外信息表新增用户记录，添加结果:{}", result);
        });
        // 初始化用户画像表
        this.executeVoidAsyncFunction(() -> {
            UserProfile profile = UserProfile.builder()
                    .userId(userId)
                    .build();
            int result = profileMapper.insertSelective(profile);
            logger.info("用户画像表新增用户记录，添加结果:{}", result);
        });
    }

    /**
     * 文件上传成功后保存文件的上传记录
     * @param md5 文件Md5
     * @param originName 文件的原始名称
     * @param fileName 文件保存的名称
     * @param size 文件大小
     * @param fileUrl 文件在七牛云中的url
     */
    public void saveFileUploadLog(String md5, String originName, String fileName, Long size, String fileUrl) {
        this.executeVoidAsyncFunction(() -> {
            FileUpload fileUpload = new FileUpload(md5, originName, fileName, size, fileUrl);
            int result = uploadMapper.insertSelective(fileUpload);
            logger.info("保存文件上传日志，文件名称：{},影响行数：{}", originName, result);
        });
    }

    /**
     * 发送帖子/话题被评论的用户消息，先判断评论的帖子/话题是否存在，再判断是不是用户自身的评论消息，条件都符号再写入用户消息表
     * @param comment 评论对象
     */
    public void sendTopicUserMessage(Comment comment) {
        this.executeVoidAsyncFunction(() -> {
            // 两个异步线程查询发表评论的用户昵称以及帖子详情
            CompletableFuture<String> nickNaeFuture = this.getUserNickName(comment.getUserId());
            CompletableFuture<ForumTopic> topicFuture = CompletableFuture.supplyAsync(() -> topicMapper.selectOneByQuery(new QueryWrapper()
                    .select(FORUM_TOPIC.ID, FORUM_TOPIC.TYPE, FORUM_TOPIC.USER_ID)
                    .where(FORUM_TOPIC.ID.eq(comment.getTopicId()))), executors);
            try {
                CompletableFuture.allOf(nickNaeFuture, topicFuture).get();
                ForumTopic findTopic = topicFuture.get();
                if (findTopic == null) {
                    logger.info("评论对应的帖子/话题不存在，跳过写入消息，topicId：{}", comment.getTopicId());
                    return;
                }
                if (findTopic.getUserId().equals(comment.getUserId())) {
                    logger.info("用户自身评论，跳过写入消息");
                    return;
                }
                String messageContent = nickNaeFuture.get() + "评论了你的" + (findTopic.getType() == 0 ? "帖子" : "话题");
                int result = messageService.sendUserMessage(new UserMessage(findTopic.getUserId(), messageContent, 0, findTopic.getId(), null));
                logger.info("用户帖子/话题消息发送完成，影响行数：{}", result);
            } catch (Exception ex) {
                logger.error("发送用户帖子/话题消息失败，错误信息：{}", ex.getMessage());
            }
        });
    }

    /**
     * 发送评论被回复的异步消息，先判断回复的评论是否存在，再判断是不是用户自身回复。条件都符合再写入用户消息表
     * @param comment 评论对象
     */
    public void sendCommentUserMessage(Comment comment) {
        this.executeVoidAsyncFunction(() -> {
            // 两条异步线程查询发表回复的用户昵称以及上级评论的用户Id
            CompletableFuture<String> nickNameFuture = this.getUserNickName(comment.getUserId());
            CompletableFuture<Long> userIdFuture = CompletableFuture.supplyAsync(() -> commentMapper.selectOneByQueryAs(new QueryWrapper()
                    .select(COMMENT.USER_ID)
                    .where(COMMENT.ID.eq(COMMENT.ID.eq(comment.getRid()))), Long.class));
            try {
                CompletableFuture.allOf(nickNameFuture, userIdFuture).get();
                Long userId = userIdFuture.get();
                if (userId == null) {
                    logger.info("回复的评论不存在，跳过写入消息，commentId：{}", comment.getRid());
                    return;
                }
                if (userId.equals(comment.getUserId())) {
                    logger.info("用户自身回复，跳过写入消息");
                    return;
                }
                String nickName = nickNameFuture.get();
                String messageContent = nickName + "回复了你的评论";
                messageService.sendUserMessage(new UserMessage(userId, messageContent, 1, comment.getTopicId(), comment.getId()));
            } catch (Exception ex) {
                logger.error("发送用户评论回复消息失败，错误信息：{}", ex.getMessage());
            }
        });
    }

    /**
     * 更新帖子的收藏次数
     * @param topicId 帖子/话题Id
     * @param count 需要更新的数量
     * @param isAdd 添加还是建设 true：添加 false：减少
     */
    public void updateTopicStarCount(Integer topicId, int count, boolean isAdd) {
        String rowOption;
        if (isAdd) {
            rowOption = "+" + count;
        }else {
            rowOption = "-" + count;
        }
        this.executeVoidAsyncFunction(() -> {
            boolean result = UpdateChain.of(ForumTopic.class)
                    .setRaw(ForumTopic::getStarCount, "star_count" + rowOption)
                    .where(ForumTopic::getId).eq(topicId)
                    .update();
            logger.info("更新帖子收藏次数，TopicId：{}, count:{}, isAdd:{},影响行数：{}", topicId, count, isAdd, result);
        });
    }

    /**
     * 添加帖子/话题的查看次数
     * @param topicId 帖子/话题Id
     * @param count 需要添加的查看次数
     */
    public void appendTopicViewCount(Integer topicId, int count) {
        this.executeVoidAsyncFunction(() -> {
            UpdateChain.of(ForumTopic.class)
                    .setRaw(ForumTopic::getViewCount, "view_count + " + count)
                    .where(ForumTopic::getId).eq(topicId)
                    .update();
        });
    }

    /**
     * 私有方法，通过用户Id获取用户昵称
     * @param userId 用户Id
     * @return 返回用户昵称或空
     */
    private CompletableFuture<String> getUserNickName(Long userId) {
        return CompletableFuture.supplyAsync(() -> userMapper.selectOneByQueryAs(new QueryWrapper()
                .select(USER.NICK_NAME)
                .where(USER.ID.eq(userId)), String.class), executors);
    }
}
