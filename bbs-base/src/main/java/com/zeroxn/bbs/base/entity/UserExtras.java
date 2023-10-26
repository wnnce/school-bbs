package com.zeroxn.bbs.base.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.zeroxn.bbs.base.mybatis.handlers.ArrayTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.JdbcType;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户额外信息表 实体类。
 *
 * @author lisang
 * @since 2023-10-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "bbs_user_extras")
public class UserExtras implements Serializable {

    /**
     * ID主键 自增
     */
    @Id(keyType = KeyType.Auto)
    private Integer id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 最后更新时间
     */
    @Column(onInsertValue = "current_timestamp", onUpdateValue = "current_timestamp")
    private LocalDateTime updateTime;

    /**
     * 用户收藏的帖子/话题列表
     */
    @Column(jdbcType = JdbcType.ARRAY, typeHandler = ArrayTypeHandler.class)
    private Integer[] topicStars;

}
