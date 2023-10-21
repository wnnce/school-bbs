package com.zeroxn.bbs.web.service;

import com.zeroxn.bbs.base.entity.TopicLabel;

import java.util.List;

/**
 * @Author: lisang
 * @DateTime: 2023-10-18 15:36:41
 * @Description: 话题标签接口 服务层
 */
public interface LabelService {
    /**
     * 获取所有标签列表
     * @return 返回空或者标签列表信息
     */
    List<TopicLabel> listTopicLabel();
}
