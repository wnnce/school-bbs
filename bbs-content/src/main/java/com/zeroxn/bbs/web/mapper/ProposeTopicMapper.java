package com.zeroxn.bbs.web.mapper;

import com.mybatisflex.core.BaseMapper;
import com.zeroxn.bbs.core.entity.ProposeTopic;
import org.apache.ibatis.annotations.Param;

/**
 * 用户话题推送表 映射层。
 *
 * @author lisang
 * @since 2023-10-12
 */
public interface ProposeTopicMapper extends BaseMapper<ProposeTopic> {
    int deleteProposeTopicByTopicId(@Param("topicId") Integer topicId);
}
