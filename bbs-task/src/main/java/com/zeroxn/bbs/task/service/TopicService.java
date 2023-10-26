package com.zeroxn.bbs.task.service;

import com.zeroxn.bbs.base.entity.ForumTopic;
import com.zeroxn.bbs.task.dao.TopicDao;
import scala.Int;
import tech.powerjob.worker.log.OmsLogger;

import java.util.List;

/**
 * @Author: lisang
 * @DateTime: 2023-10-19 18:45:37
 * @Description: 话题管理 服务层
 */
public interface TopicService {
    /**
     * 处理生成话题关键字
     * @param topic 待生成关键字的话题
     */
    void handlerTopicKeyword(ForumTopic topic);

    /**
     * 获取所有状态为正常的话题
     * @return 返回话题列表或空
     */
    List<TopicDao> listAllTopic();

    /**
     * 更新热门话题列表
     * @param hotTopicIdList 需要设置为热门话题的话题Id列表
     * @return 返回更新结果
     */
    boolean updateTopicHot(List<Integer> hotTopicIdList);

    /**
     * 获取所有状态为正常的非热门话题
     * @return 返回话题列表
     */
    List<TopicDao> listNotHotTopic();

    /**
     * 通过话题Id列表批量删除话题
     * @param topicIdList 话题Id列表
     * @param logger 任务平台Logger
     * @return 返回删除结果
     */
    boolean deleteTopicByTopicIdList(List<Integer> topicIdList, OmsLogger logger);

    /**
     * 更新话题状态
     * @param topicId 话题Id
     * @param status 话题的新状态
     */
    void updateTopicStatus(Integer topicId, int status);

    /**
     * 通过Id查询话题详情
     * @param topicId 话题id
     * @return 返回空或话题详情
     */
    ForumTopic queryTopic(Integer topicId);
}
