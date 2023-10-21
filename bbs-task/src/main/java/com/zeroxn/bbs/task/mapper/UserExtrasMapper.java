package com.zeroxn.bbs.task.mapper;

import com.mybatisflex.core.BaseMapper;
import com.zeroxn.bbs.base.entity.UserExtras;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: lisang
 * @DateTime: 2023-10-21 19:44:45
 * @Description:
 */
public interface UserExtrasMapper extends BaseMapper<UserExtras> {
    int batchDeleteUserStarByTopicIdList(@Param("topicIds") String topicIds);
}
