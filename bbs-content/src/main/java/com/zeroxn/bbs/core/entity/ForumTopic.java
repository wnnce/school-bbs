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
 * 论坛帖子/话题信息表 实体类。
 *
 * @author lisang
 * @since 2023-10-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "bbs_forum_topic")
public class ForumTopic implements Serializable {

    /**
     * 帖子/话题ID 自增主键
     */
    @Id(keyType = KeyType.Auto)
    private Integer id;

    /**
     * 帖子/话题标题
     */
    private String title;

    /**
     * 帖子/话题内容
     */
    private String content;

    /**
     * 帖子/话题关键字
     */
    private String contentKey;

    /**
     * 图片连接数组
     */
    private Array imageUrls;

    /**
     * 视频链接
     */
    private String videoUrl;

    /**
     * 类型 0:帖子 1：话题
     */
    private Integer type;

    /**
     * 帖子标签 0：求助信息 1：讨论交流 2：学习资料 3：二手交易 4：失物招领 话题则空
     */
    private Integer flag;

    /**
     * 话题的标签列表，帖子则为空
     */
    private Array labelIds;

    /**
     * 是否热门话题
     */
    private Boolean isHot;

    /**
     * 查看次数
     */
    private Integer viewCount;

    /**
     * 收藏次数
     */
    private Integer startCount;

    /**
     * 创建时间
     */
    private Timestamp createTime;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 状态 0：正常 1：二手交易已售出 2：已删除
     */
    private Integer status;

}
