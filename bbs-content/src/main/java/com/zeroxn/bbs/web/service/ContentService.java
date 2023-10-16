package com.zeroxn.bbs.web.service;

import com.zeroxn.bbs.core.entity.ForumTopic;
import com.zeroxn.bbs.web.dto.UserTopicDto;

/**
 * @Author: lisang
 * @DateTime: 2023-10-14 10:54:14
 * @Description: 帖子/话题服务层
 */
public interface ContentService {
    void saveTopic(ForumTopic topic);
    void savePost(ForumTopic post);
    UserTopicDto queryTopic(Integer topicId, Long userId);
    void deleteTopic(Integer topicId, Long userId);
    void starTopic(Integer topicId, Long userId);
    void unStartTopic(Integer topicId, Long userId);
}
