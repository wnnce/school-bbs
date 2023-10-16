package com.zeroxn.bbs.web.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.zeroxn.bbs.core.entity.ForumTopic;
import com.zeroxn.bbs.core.exception.ExceptionUtils;
import com.zeroxn.bbs.web.mapper.ForumTopicMapper;
import com.zeroxn.bbs.web.service.ContentService;
import com.zeroxn.bbs.web.service.UserService;
import com.zeroxn.bbs.web.service.async.GlobalAsyncTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static com.zeroxn.bbs.core.entity.table.ForumTopicTableDef.FORUM_TOPIC;

/**
 * @Author: lisang
 * @DateTime: 2023-10-14 11:35:14
 * @Description: 帖子/话题服务层实现类
 */
@Service
public class ContentServiceImpl implements ContentService {
    private static final Logger logger = LoggerFactory.getLogger(ContentServiceImpl.class);
    private final ForumTopicMapper topicMapper;
    private final UserService userService;
    private final GlobalAsyncTask asyncTask;

    public ContentServiceImpl(ForumTopicMapper topicMapper, GlobalAsyncTask asyncTask, UserService userService) {
        this.topicMapper = topicMapper;
        this.asyncTask = asyncTask;
        this.userService = userService;
    }

    @Override
    public void saveTopic(ForumTopic topic) {
        topic.setType(1);
        topicMapper.insertSelective(topic);
        logger.info("话题保存成功，TopicId:{}", topic.getId());
        // 调用异步方法生成话题关键字
        asyncTask.handlerTopicContentKey(topic);
        // 调用异步方法处理话题推送
        asyncTask.handlerTopicPropose(topic);
    }

    @Override
    public void savePost(ForumTopic post) {
        post.setType(0);
        topicMapper.insertSelective(post);
        logger.info("帖子保存成功，TopicId：{}", post.getId());
        // 调用异步方法生成帖子关键字
        asyncTask.handlerTopicContentKey(post);
        logger.info("return");
    }

    @Override
    public void deleteTopic(Integer topicId, Long userId) {
        ForumTopic findTopic = topicMapper.selectOneByQuery(new QueryWrapper()
                .select(FORUM_TOPIC.ID, FORUM_TOPIC.USER_ID)
                .where(FORUM_TOPIC.ID.eq(topicId)));
        ExceptionUtils.isConditionThrowRequest(findTopic == null, "需要删除的内容不存在");
        ExceptionUtils.isConditionThrowRequest(findTopic.getUserId() != userId, "该用户无删除权限");
        topicMapper.deleteById(topicId);
        userService.deleteTopicAfterUpdateUserStars(topicId);
        logger.info("删除Topic成功，topicId：{}", topicId);
    }


}
