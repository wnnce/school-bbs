package com.zeroxn.bbs.web.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.zeroxn.bbs.base.entity.TopicLabel;
import com.zeroxn.bbs.web.mapper.TopicLabelMapper;
import com.zeroxn.bbs.web.service.LabelService;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.zeroxn.bbs.core.entity.table.TopicLabelTableDef.TOPIC_LABEL;

/**
 * @Author: lisang
 * @DateTime: 2023-10-18 15:37:20
 * @Description: 话题标签服务层实现类
 */
@Service
public class LabelServiceImpl implements LabelService {
    private final TopicLabelMapper labelMapper;
    public LabelServiceImpl(TopicLabelMapper labelMapper) {
        this.labelMapper = labelMapper;
    }
    @Override
    public List<TopicLabel> listTopicLabel() {
        return labelMapper.selectListByQuery(new QueryWrapper()
                .select(TOPIC_LABEL.ID, TOPIC_LABEL.NAME, TOPIC_LABEL.CREATE_TIME));
    }
}
