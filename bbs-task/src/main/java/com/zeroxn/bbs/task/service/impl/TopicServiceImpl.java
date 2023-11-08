package com.zeroxn.bbs.task.service.impl;

import com.hankcs.hanlp.HanLP;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.zeroxn.bbs.base.entity.ForumTopic;
import com.zeroxn.bbs.base.entity.ProposeTopic;
import com.zeroxn.bbs.task.analytics.TextAnalytics;
import com.zeroxn.bbs.task.dao.TopicDao;
import com.zeroxn.bbs.task.mapper.CommentMapper;
import com.zeroxn.bbs.task.mapper.ForumTopicMapper;
import com.zeroxn.bbs.task.mapper.ProposeTopicMapper;
import com.zeroxn.bbs.task.mapper.UserExtrasMapper;
import com.zeroxn.bbs.task.service.TopicService;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import tech.powerjob.worker.log.OmsLogger;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.mybatisflex.core.query.QueryMethods.count;
import static com.mybatisflex.core.query.QueryMethods.max;
import static com.zeroxn.bbs.base.entity.table.CommentTableDef.COMMENT;
import static com.zeroxn.bbs.base.entity.table.ForumTopicTableDef.FORUM_TOPIC;
import static com.zeroxn.bbs.base.entity.table.ProposeTopicTableDef.PROPOSE_TOPIC;

/**
 * @Author: lisang
 * @DateTime: 2023-10-19 18:46:38
 * @Description:
 */
@Service
public class TopicServiceImpl implements TopicService {
    private static final Logger logger = LoggerFactory.getLogger(TopicServiceImpl.class);
    private static final int MAX_DELETE_BATCH = 20;
    private final TextAnalytics textAnalytics;
    private final ForumTopicMapper topicMapper;
    private final CommentMapper commentMapper;
    private final UserExtrasMapper extrasMapper;
    private final ProposeTopicMapper proposeMapper;
    private final RedisTemplate<String, List<Integer>> redisTemplate;
    private final PlatformTransactionManager transactionManager;
    @Value("${analysis.size}")
    private Integer keywordSize = 5;

    public TopicServiceImpl(TextAnalytics textAnalytics, ForumTopicMapper topicMapper, CommentMapper commentMapper,
                            UserExtrasMapper extrasMapper, RedisTemplate<String, List<Integer>> redisTemplate,
                            ProposeTopicMapper proposeMapper, PlatformTransactionManager transactionManager) {
        this.textAnalytics = textAnalytics;
        this.topicMapper = topicMapper;
        this.commentMapper = commentMapper;
        this.extrasMapper = extrasMapper;
        this.proposeMapper = proposeMapper;
        this.redisTemplate = redisTemplate;
        this.transactionManager = transactionManager;
    }

    /**
     * 生成关键字先使用讯飞接口生成，如果讯飞接口调用失败再使用本地库
     * @param topic 待生成关键字的话题
     */
    @Override
    public void handlerTopicKeyword(ForumTopic topic) {
        String document = topic.getTitle() + topic.getContent();
        List<String> keywordList = null;
        if (document.length() > 30000) {
            logger.warn("文本长度超过讯飞限制，本地生成关键字。");
            keywordList = HanLP.extractKeyword(document, keywordSize);
        }else {
            keywordList = textAnalytics.keywordsExtraction(document, keywordSize);
            if (keywordList == null) {
                logger.error("讯飞生成文章关键字失败，调用本地生成");
            }
            keywordList = HanLP.extractKeyword(document, keywordSize);
        }
        logger.info("关键字生成完成，数量：{}", keywordList.size());
        long count = QueryChain.of(ForumTopic.class)
                .where(FORUM_TOPIC.ID.eq(topic.getId()))
                .count();
        if (count <= 0) {
            logger.warn("帖子已被删除，放弃保存，topicId:{}", topic.getId());
        }else {
            String contentKey = Strings.join(keywordList, ',');
            topic.setContentKey(contentKey);
            boolean result = UpdateChain.of(ForumTopic.class)
                    .set(FORUM_TOPIC.CONTENT_KEY, contentKey)
                    .where(FORUM_TOPIC.ID.eq(topic.getId()))
                    .update();
            logger.info("更新帖子关键字完成，topicId：{}，影响行数：{}", topic.getId(), result);
        }
    }

    @Override
    public List<TopicDao> listAllTopic() {
        return QueryChain.of(ForumTopic.class)
                .select(FORUM_TOPIC.ID, FORUM_TOPIC.CREATE_TIME, FORUM_TOPIC.IS_HOT, FORUM_TOPIC.VIEW_COUNT, FORUM_TOPIC.STAR_COUNT)
                .select(count(COMMENT.ID).as("comment_count"), max(COMMENT.CREATE_TIME).as("last_comment_time"))
                .from(FORUM_TOPIC)
                .leftJoin(COMMENT).on(COMMENT.TOPIC_ID.eq(FORUM_TOPIC.ID))
                .where(FORUM_TOPIC.TYPE.eq(1))
                .and(FORUM_TOPIC.STATUS.eq(0))
                .groupBy(FORUM_TOPIC.ID)
                .listAs(TopicDao.class);
    }

    @Override
    @Transactional
    public boolean updateTopicHot(List<Integer> hotTopicIdList) {
        UpdateChain.of(ForumTopic.class)
                .set(FORUM_TOPIC.IS_HOT, false)
                .where(FORUM_TOPIC.ID.notIn(hotTopicIdList))
                .and(FORUM_TOPIC.TYPE.eq(1))
                .update();
        return UpdateChain.of(ForumTopic.class)
                .set(FORUM_TOPIC.IS_HOT, true)
                .where(FORUM_TOPIC.ID.in(hotTopicIdList))
                .and(FORUM_TOPIC.TYPE.eq(1))
                .update();
    }

    @Override
    public List<TopicDao> listNotHotTopic() {
        return QueryChain.of(ForumTopic.class)
                .select(FORUM_TOPIC.ID, FORUM_TOPIC.CREATE_TIME, FORUM_TOPIC.VIEW_COUNT, FORUM_TOPIC.STAR_COUNT)
                .select(count(COMMENT.ID).as("comment_count"), max(COMMENT.CREATE_TIME).as("last_comment_time"))
                .from(FORUM_TOPIC)
                .leftJoin(COMMENT).on(COMMENT.TOPIC_ID.eq(FORUM_TOPIC.ID))
                .where(FORUM_TOPIC.TYPE.eq(1))
                .and(FORUM_TOPIC.STATUS.eq(0))
                .and(FORUM_TOPIC.IS_HOT.eq(false))
                .and("bbs_forum_topic.create_time < current_timestamp - interval '1 day'")
                .groupBy(FORUM_TOPIC.ID)
                .listAs(TopicDao.class);
    }

    @Override
    public boolean deleteTopicByTopicIdList(List<Integer> topicIdList, OmsLogger logger) {
        TransactionStatus transaction = transactionManager.getTransaction(new DefaultTransactionDefinition());
        boolean funcResult = false;
        try {
            // 手动管理事务，删除redis的话题id缓存可能耗时很久，数据库操作成功就提交事务 redis的数据一致性不做要求
            int result1 = topicMapper.deleteBatchByIds(topicIdList);
            logger.info("话题表删除完成，影响行数：{}", result1);
            int result2 = commentMapper.deleteByQuery(new QueryWrapper().where(COMMENT.TOPIC_ID.in(topicIdList)));
            logger.info("评论表删除完成，影响行数：{}", result2);
            String topicIds = String.join(",", topicIdList.stream().map(Object::toString).toArray(String[]::new));
            int result3 = extrasMapper.batchDeleteUserStarByTopicIdList(topicIds);
            logger.info("用户收藏表删除完成，影响行数：{}", result3);
            int result4 = proposeMapper.deleteByQuery(new QueryWrapper().where(PROPOSE_TOPIC.TOPIC_ID.in(topicIdList)));
            logger.info("用户推荐表删除完成，影响行数：{}", result4);
            logger.info("{}个话题删除成功", topicIdList.size());
            transactionManager.commit(transaction);
            funcResult = true;
        } catch (Exception e) {
            logger.error("批量删除话题失败，开始回滚，错误信息：{}", e.getMessage());
            transactionManager.rollback(transaction);
        }
        // 数据库事务执行成功再操作redis
        if (funcResult) {
            this.batchUpdateRedisIdListByTopicIdList(topicIdList);
        }
        return funcResult;
    }

    @Override
    public void updateTopicStatus(Integer topicId, int status) {
        int result = topicMapper.updateTopicStatus(topicId, status);
        logger.info("更新帖子状态完成，topicId：{}, newStatus:{}, 影响行数:{}", topicId, status, result);
    }

    @Override
    public ForumTopic queryReviewTopic(Integer topicId) {
        return topicMapper.selectOneByQuery(new QueryWrapper()
                .where(FORUM_TOPIC.ID.eq(topicId).and(FORUM_TOPIC.STATUS.eq(1))));
    }

    public void addTopicToRedisIdList(Integer topicId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(FORUM_TOPIC.CONTENT_KEY)
                .where(FORUM_TOPIC.ID.eq(topicId))
                .and(FORUM_TOPIC.STATUS.eq(0))
                .and(FORUM_TOPIC.TYPE.eq(1));
        String contentKey = topicMapper.selectOneByQueryAs(queryWrapper, String.class);
        if (contentKey == null || contentKey.isEmpty()) {
            logger.warn("该ID不为话题或话题关键字为空，跳过操作，topicId：{}", topicId);
            return;
        }
        String[] contentKeys = contentKey.split(",");
        logger.info("获取话题关键字成功，关键字数量：{}", contentKeys.length);
        List<CompletableFuture<Void>> asyncList = new ArrayList<>();
        // 多线程处理指定关键字id列表添加id
        Arrays.stream(contentKeys).forEach(key -> {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                List<Integer> idList = redisTemplate.opsForValue().get(key);
                if (idList == null || idList.isEmpty()) {
                    idList = Collections.singletonList(topicId);
                } else {
                    idList.add(0, topicId);
                }
                redisTemplate.opsForValue().set(key, idList);
            });
            asyncList.add(future);
        });
        CompletableFuture.allOf(asyncList.toArray(CompletableFuture[]::new)).join();
        logger.info("处理关键字推荐Id列表完毕");
    }

    @Override
    public void handlerViewTopicAfterPropose(Long userId, Integer topicId) {
        ForumTopic findTopic = topicMapper.selectOneByQuery(new QueryWrapper()
                .where(FORUM_TOPIC.ID.eq(topicId))
                .and(FORUM_TOPIC.STATUS.eq(0))
                .and(FORUM_TOPIC.TYPE.eq(1)));
        if (findTopic == null) {
            logger.warn("话题状态错误或者该ID不是话题ID,方法结束。topicId：{}", topicId);
            return;
        }
        String contentKey = findTopic.getContentKey();
        if (contentKey == null || contentKey.isEmpty()) {
            logger.warn("话题关键字为空，结束方法");
            return;
        }
        String[] contentKeys = contentKey.split(",");
        // 装载推荐话题Id的线程安全List
        List<Integer> proposeTopicIdList = Collections.synchronizedList(new ArrayList<>());
        // 遍历关键字 通过多线程查询Redis 并发插入话题ID
        List<CompletableFuture<Void>> asyncList = new ArrayList<>();
        Arrays.stream(contentKeys).forEach(key -> {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                List<Integer> idList = redisTemplate.opsForValue().get(key);
                if (idList != null && !idList.isEmpty()) {
                    proposeTopicIdList.add(idList.get(0));
                }
            });
            asyncList.add(future);
        });
        // 等待所有异步线程执行完毕
        CompletableFuture.allOf(asyncList.toArray(CompletableFuture[]::new)).join();
        // 遍历集合 创建推荐话题列表  然后批量插入数据库
        List<ProposeTopic> proposeTopicList = proposeTopicIdList.stream().map(proposeId ->
                new ProposeTopic(userId, proposeId, 50.0)).toList();
        int result = proposeMapper.insertBatch(proposeTopicList);
        logger.info("批量插入用户查看话题关键字推荐列表成功，推荐条数：{}，插入结果：{}", proposeTopicList.size(), result);
    }

    @Override
    public List<Long> listProposeTopicUserid() {
        return proposeMapper.listProposeTopicUserId();
    }

    @Override
    public int deleteUserOldPropose(Long userId, Integer limit) {
        return proposeMapper.deleteUserOldPropose(userId, limit);
    }

    @Override
    public void batchUpdateRedisIdListByTopicIdList(List<Integer> deleteTopicIdList) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(FORUM_TOPIC.CONTENT_KEY)
                .where(FORUM_TOPIC.ID.in(deleteTopicIdList))
                .and(FORUM_TOPIC.STATUS.eq(3))
                .and(FORUM_TOPIC.TYPE.eq(1));
        List<String> contentKeyList = topicMapper.selectListByQueryAs(queryWrapper, String.class);
        if (contentKeyList == null || contentKeyList.isEmpty()) {
            logger.warn("获取到的话题关键字为空，结束方法");
            return;
        }
        // 关键字去重
        Set<String> keySet = new HashSet<>(Arrays.asList(String.join(",", contentKeyList).split(",")));
        List<List<String>> batches = new ArrayList<>();
        // 如果关键字太多 那么进行分批处理
        if (keySet.size() > MAX_DELETE_BATCH) {
            logger.info("需要清理的关键字超过最大处理大小，数量：{}，开始分批处理", keySet.size());
            for (int i = 0; i < keySet.size(); i += MAX_DELETE_BATCH) {
                int endIndex = Math.min(i + MAX_DELETE_BATCH, keySet.size());
                List<String> batch = new ArrayList<>(keySet).subList(i, endIndex);
                batches.add(batch);
            }
        }else {
            batches.add(new ArrayList<>(keySet));
        }
        batches.forEach(keyList -> {
            // 每个关键字启动一个线程 多线程并发删除
            List<CompletableFuture<Void>> asyncList = new ArrayList<>();
            keyList.forEach(key -> {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    List<Integer> idList = redisTemplate.opsForValue().get(key);
                    // 如果Key获取到的idList为空或者删除了当前id后为空 那么就将key删除
                    if (idList != null && !idList.isEmpty()) {
                        idList.removeAll(deleteTopicIdList);
                        if (idList.isEmpty()) {
                            redisTemplate.delete(key);
                        }else {
                            redisTemplate.opsForValue().set(key, idList);
                        }
                    }else {
                        redisTemplate.delete(key);
                    }
                });
                asyncList.add(future);
            });
            // 等待所有线程执行完毕
            CompletableFuture.allOf(asyncList.toArray(CompletableFuture[]::new)).join();
        });
    }
}