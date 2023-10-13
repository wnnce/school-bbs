package com.zeroxn.bbs.core.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.sql.Array;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 公共消息表 实体类。
 *
 * @author lisang
 * @since 2023-10-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "bbs_public_message")
public class PublicMessage implements Serializable {

    /**
     * ID 主键 自增
     */
    @Id(keyType = KeyType.Auto)
    private Integer id;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息发送时间
     */
    private Timestamp sendTime;

    /**
     * 已读该消息的用户
     */
    private Array readUserIds;

    /**
     * 删除该消息的用户
     */
    private Array delUserIds;

    /**
     * 状态 0：正常 1：删除
     */
    private Integer status;

}
