package com.zeroxn.bbs.core.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户轨迹表 实体类。
 *
 * @author lisang
 * @since 2023-10-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "bbs_user_orbit")
public class UserOrbit implements Serializable {

    /**
     * ID 主键 自增
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 轨迹上传时间
     */
    @Column(onInsertValue = "current_timestamp")
    private LocalDateTime createTime;

    /**
     * 用户IP地址
     */
    private String ipAddress;

    /**
     * 用户坐标，gps经纬度和高度
     */
    private String coordinate;

}
