package com.zeroxn.bbs.base.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import com.zeroxn.bbs.base.mybatis.handlers.ArrayToListTypeHandler;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: lisang
 * @DateTime: 2023-10-28 17:02:10
 * @Description: 用户好友表实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Table("bbs_user_friends")
public class UserFriends {
    /**
     * Id 主键 自增
     */
    private Integer id;
    /**
     * 用户Id 唯一
     */
    private Long userId;
    /**
     * 该用户的好友ID列表
     */
    @Column(typeHandler = ArrayToListTypeHandler.class)
    private List<Long> friendsIds;
    @Column(onUpdateValue = "current_timestamp")
    private LocalDateTime updateTime;
}
