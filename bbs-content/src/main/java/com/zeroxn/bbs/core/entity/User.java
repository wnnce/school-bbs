package com.zeroxn.bbs.core.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import com.mybatisflex.core.keygen.KeyGenerators;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.crypto.KeyGenerator;

/**
 * 校园论坛用户表 实体类。
 *
 * @author lisang
 * @since 2023-10-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "bbs_user")
public class User implements Serializable {

    /**
     * 用户ID,雪花ID生成
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 用户在微信中的Openid
     */
    private String openid;

    /**
     * 用户在微信中的手机号
     */
    private String phone;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 用户头像地址
     */
    private String avatar;

    /**
     * 用户年龄
     */
    private Integer age;

    /**
     * 用户性别 0：男 1:女
     */
    private Integer gender;

    /**
     * 用户所在地区 省-市，湖北-武汉
     */
    private String address;

    /**
     * 用户简介
     */
    private String introduction;

    /**
     * 用户创建时间
     */
    @Column(onInsertValue = "current_timestamp")
    private LocalDateTime createTime;

    /**
     * 用户角色 0：普通用户 1：学生用户
     */
    private Integer role;

    /**
     * 用户授权 0：正常 1：已禁用
     */
    private Integer userAuth;

    /**
     * 是否删除 0：正常  2：已删除
     */
    @Column(isLogicDelete = true)
    private Integer status;

}