package com.zeroxn.bbs.web.mapper;

import com.mybatisflex.core.BaseMapper;
import com.zeroxn.bbs.base.entity.ForumTopic;
import com.zeroxn.bbs.web.dto.UserTopicDto;
import org.apache.ibatis.annotations.Param;

/**
 * 论坛帖子/话题信息表 映射层。
 *
 * @author lisang
 * @since 2023-10-12
 */
public interface ForumTopicMapper extends BaseMapper<ForumTopic> {
    UserTopicDto queryTopic(@Param("topicId") Integer topicId, @Param("userId") Long userId);
}
