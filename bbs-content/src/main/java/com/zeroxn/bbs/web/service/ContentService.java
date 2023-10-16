package com.zeroxn.bbs.web.service;

import com.zeroxn.bbs.core.entity.ForumTopic;

/**
 * @Author: lisang
 * @DateTime: 2023-10-14 10:54:14
 * @Description: 帖子/话题服务层
 */
public interface ContentService {
    void saveTopic(ForumTopic topic);
    void savePost(ForumTopic post);
    void deleteTopic(Integer topicId, Long userId);
}
