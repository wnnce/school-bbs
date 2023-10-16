package com.zeroxn.bbs.core.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.sql.Array;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户画像表 实体类。
 *
 * @author lisang
 * @since 2023-10-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "bbs_user_profile")
public class UserProfile implements Serializable {

    /**
     * ID 主键 自增
     */
    @Id(keyType = KeyType.Auto)
    private Integer id;

    /**
     * 用户ID 唯一
     */
    private Long userId;

    /**
     * 最后更新时间
     */
    @Column(onInsertValue = "current_timestamp", onUpdateValue = "current_timestamp")
    private LocalDateTime updateTime;

    /**
     * 用户标签列表
     */
    private List<String> userLabels;

}
