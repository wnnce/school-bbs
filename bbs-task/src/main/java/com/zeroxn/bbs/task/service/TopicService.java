package com.zeroxn.bbs.task.service;

import com.zeroxn.bbs.base.entity.ForumTopic;
import com.zeroxn.bbs.task.dao.TopicDao;
import scala.Int;

import java.util.List;

/**
 * @Author: lisang
 * @DateTime: 2023-10-19 18:45:37
 * @Description:
 */
public interface TopicService {
    void handlerTopicKeyword(ForumTopic topic);
    List<TopicDao> listAllTopic();
    boolean updateTopicHot(List<Integer> hotTopicIdList);
}
