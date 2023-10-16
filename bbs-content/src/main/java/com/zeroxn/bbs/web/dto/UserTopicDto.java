package com.zeroxn.bbs.web.dto;

import com.zeroxn.bbs.core.entity.ForumTopic;
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
    private boolean isStar;
}
