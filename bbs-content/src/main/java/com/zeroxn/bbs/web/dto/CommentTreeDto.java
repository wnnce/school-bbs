package com.zeroxn.bbs.web.dto;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.core.paginate.Page;
import com.zeroxn.bbs.core.entity.Comment;
import lombok.*;

/**
 * @Author: lisang
 * @DateTime: 2023-10-18 08:12:11
 * @Description: 评论树形结构Dto类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CommentTreeDto extends Comment {

    /**
     * 发表评论的用户昵称
     */
    private String nickName;

    /**
     * 发表评论的用户头像地址
     */
    private String avatar;

    /**
     * 二级评论回复的上级用户昵称
     */
    private String recoverNickName;

    /**
     * 该评论下所有的子评论
     */
    @Column(ignore = true)
    private Page<CommentTreeDto> childrenPage = null;
}
