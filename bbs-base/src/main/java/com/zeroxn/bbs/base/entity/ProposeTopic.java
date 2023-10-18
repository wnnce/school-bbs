package com.zeroxn.bbs.base.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户话题推送表 实体类。
 *
 * @author lisang
 * @since 2023-10-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "bbs_propose_topic")
public class ProposeTopic implements Serializable {

    /**
     * ID主键 自增
     */
    @Id(keyType = KeyType.Auto)
    private Integer id;

    /**
     * 推送的用户ID
     */
    private Long userId;

    /**
     * 推送的话题ID
     */
    private Integer topicId;

    /**
     * 推送创建时间
     */
    @Column(onInsertValue = "current_timestamp")
    private LocalDateTime createTime;

    /**
     * 话题与用户的相关度
     */
    private Double similarity;

}
