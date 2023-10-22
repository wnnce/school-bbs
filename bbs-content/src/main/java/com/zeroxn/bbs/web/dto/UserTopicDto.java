package com.zeroxn.bbs.web.dto;

import com.zeroxn.bbs.base.entity.ForumTopic;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: lisang
 * @DateTime: 2023-10-16 18:36:12
 * @Description: 帖子/话题的Dto数据传输类
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class UserTopicDto extends ForumTopic {
    /**
     * 发帖用户昵称
     */
    private String nickName;
    /**
     * 发帖用户头像地址
     */
    private String avatar;
    /**
     * 帖子是否被当前用户收藏
     */
    private Boolean isStar;
    /**
     * 帖子的评论数
     */
    private Integer commentCount;
}
