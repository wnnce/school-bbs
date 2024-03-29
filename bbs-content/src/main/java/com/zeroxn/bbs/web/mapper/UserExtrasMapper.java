package com.zeroxn.bbs.web.mapper;

import com.mybatisflex.core.BaseMapper;
import com.zeroxn.bbs.base.entity.UserExtras;
import org.apache.ibatis.annotations.Param;

/**
 * 用户额外信息表 映射层。
 *
 * @author lisang
 * @since 2023-10-12
 */
public interface UserExtrasMapper extends BaseMapper<UserExtras> {
    int deleteTopicAfterUpdateUserStars(@Param("topicId") Integer topicId);
    int countUserStarByTopicId(@Param("topicId") Integer topicId, @Param("userId") Long userId);
    int saveTopicStar(@Param("topicId") Integer topicId, @Param("userId") Long userId);
    int deleteTopicStar(@Param("topicId") Integer topicId, @Param("userId") Long userId);
}
