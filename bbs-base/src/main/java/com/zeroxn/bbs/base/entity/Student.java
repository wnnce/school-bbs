package com.zeroxn.bbs.base.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 校园论坛学生认证信息表 实体类。
 *
 * @author lisang
 * @since 2023-10-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "bbs_student")
public class Student implements Serializable {

    /**
     * 学生学号
     */
    @Id
    private String studentId;

    /**
     * 关联的用户ID
     */
    private Long userId;

    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 学生手机号
     */
    private String studentPhone;

    /**
     * 学生性别 0：男 1：女
     */
    private Integer studentGender;

    /**
     * 学院名称
     */
    private String academyName;

    /**
     * 班级名称
     */
    private String className;

    /**
     * 宿舍号
     */
    private String dormName;

    /**
     * 毕业院校
     */
    private String school;

    /**
     * 状态 0:正常 1：删除
     */
    @Column(isLogicDelete = true)
    private Integer status;

}
