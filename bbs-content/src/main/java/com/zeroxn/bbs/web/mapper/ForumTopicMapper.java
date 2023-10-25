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
    /**
     * 获取帖子/话题详情时需要判断当前帖子/话题是否被用户收藏，涉及到数组操作，使用原生SQL实现
     * @param topicId 帖子/话题Id
     * @param userId 用户Id
     * @return 返回帖子/话题的详细信息
     */
    UserTopicDto queryTopic(@Param("topicId") Integer topicId, @Param("userId") Long userId);

    int deleteTopic(@Param("topicId") Integer topicId);
}
