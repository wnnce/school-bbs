package com.zeroxn.bbs.core.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 话题标签表 实体类。
 *
 * @author lisang
 * @since 2023-10-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "bbs_topic_label")
public class TopicLabel implements Serializable {

    /**
     * ID 主键 自增
     */
    @Id(keyType = KeyType.Auto)
    private Integer id;

    /**
     * 标签名称 唯一 非空
     */
    private String name;

    /**
     * 标签创建时间
     */
    private Timestamp createTime;

    /**
     * 状态 0：正常 1：删除
     */
    private Integer status;

}
