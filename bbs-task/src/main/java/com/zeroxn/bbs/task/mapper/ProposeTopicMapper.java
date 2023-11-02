package com.zeroxn.bbs.task.mapper;

import com.mybatisflex.core.BaseMapper;
import com.zeroxn.bbs.base.entity.ProposeTopic;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: lisang
 * @DateTime: 2023-10-21 20:03:30
 * @Description:
 */
public interface ProposeTopicMapper extends BaseMapper<ProposeTopic> {
    List<Long> listProposeTopicUserId();

    /**
     * 通过用户ID删除话题推荐表中较旧的推荐数据
     * 话题推荐记录通过创建时间和相关度倒叙排序
     * @param userid 用户id
     * @param limit 保留的话题推荐数量
     * @return 返回删除记录数
     */
    int deleteUserOldPropose(@Param("userId") Long userid, @Param("limit") int limit);
}
