package com.zeroxn.bbs.task.mapper;

import com.mybatisflex.core.BaseMapper;
import com.zeroxn.bbs.base.entity.ForumTopic;
import org.apache.ibatis.annotations.Param;
import scala.Int;

/**
 * @Author: lisang
 * @DateTime: 2023-10-18 21:52:20
 * @Description:
 */
public interface ForumTopicMapper extends BaseMapper<ForumTopic> {
    int updateTopicStatus(@Param("topicId") Integer topicId,@Param("status") int status);
}
