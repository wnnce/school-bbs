package com.zeroxn.bbs.web.service;

import com.mybatisflex.core.paginate.Page;
import com.zeroxn.bbs.core.entity.ForumTopic;
import com.zeroxn.bbs.web.dto.PageQueryDto;
import com.zeroxn.bbs.web.dto.QueryPostDto;
import com.zeroxn.bbs.web.dto.UserTopicDto;

/**
 * @Author: lisang
 * @DateTime: 2023-10-14 10:54:14
 * @Description: 帖子/话题服务层
 */
public interface ContentService {
    void saveTopic(ForumTopic topic);
    void savePost(ForumTopic post);
    Page<UserTopicDto> pageForumPost(QueryPostDto postDto);
    Page<UserTopicDto> pageHotTopic(PageQueryDto pageDto);
    Page<UserTopicDto> pageProposeTopic(PageQueryDto pageDto, Long userId);
    UserTopicDto queryTopic(Integer topicId, Long userId);
    void deleteTopic(Integer topicId, Long userId);
    void starTopic(Integer topicId, Long userId);
    void unStartTopic(Integer topicId, Long userId);
}
