package com.zeroxn.bbs.web.service;

import com.mybatisflex.core.paginate.Page;
import com.zeroxn.bbs.base.entity.ForumTopic;
import com.zeroxn.bbs.web.dto.*;

import java.util.List;

/**
 * @Author: lisang
 * @DateTime: 2023-10-14 10:54:14
 * @Description: 帖子/话题服务层
 */
public interface ContentService {
    /**
     *  保存话题
     * @param topic 话题信息
     */
    void saveTopic(ForumTopic topic);

    /**
     * 保存帖子
     * @param post 帖子信息
     */
    void savePost(ForumTopic post);

    /**
     * 获取帖子列表
     * @param postDto 查询帖子列表的查询参数
     * @return 返回帖子列表和分页信息
     */
    Page<UserTopicDto> pageForumPost(QueryPostDto postDto);

    /**
     * 获取热门话题列表
     * @param pageDto 分页查询参数
     * @return 返回热门话题列表和分页信息
     */
    Page<UserTopicDto> pageHotTopic(PageQueryDto pageDto);

    /**
     * 获取用户推荐话题列表
     * @param pageDto 分页查询参数
     * @param userId 用户id
     * @return 返回给该用户推荐的话题列表和分页信息
     */
    Page<UserTopicDto> pageProposeTopic(PageQueryDto pageDto, Long userId);

    /**
     * 通过标签id获取话题列表
     * @param topicDto 查询参数
     * @return 返回该标签id下的话题列表和分页信息
     */
    Page<UserTopicDto> pageTopicByLabelId(QueryTopicDto topicDto);

    /**
     * 获取帖子/话题详情
     * @param topicId 帖子/话题Id
     * @param userId 用户id
     * @return 返回帖子/话题详情或空
     */
    UserTopicDto queryTopic(Integer topicId, Long userId);

    /**
     * 删除帖子/话题
     * @param topicId 帖子/话题Id
     * @param userId 用户id
     */
    void deleteTopic(Integer topicId, Long userId);

    /**
     * 用户收藏帖子
     * @param topicId 帖子/话题Id
     * @param userId 用户Id
     */
    void starTopic(Integer topicId, Long userId);

    /**
     * 用户取消收藏帖子/话题
     * @param topicId 话题/帖子id
     * @param userId 用户id
     */
    void unStartTopic(Integer topicId, Long userId);

    /**
     * 获取用户发布的帖子/话题列表
     * @param userTopicDto 查询参数
     * @param userId 用户Id
     * @return 返回当前用户发布的帖子/话题列表或空
     */
    Page<UserTopicDto> pageUserPublishTopic(UserTopicQueryDto userTopicDto, Long userId);

    /**
     * 获取用户收藏的帖子/话题列表
     * @param userTopicDto 查询查询
     * @param userId 用户Id
     * @return 返回当前用户收藏的帖子/话题列表或空
     */
    Page<UserTopicDto> pageUserStarTopic(UserTopicQueryDto userTopicDto, Long userId);

    /**
     * 通过帖子/话题ID列表获取帖子/话题详情列表
     * @param topicIdList 帖子/话题ID列表
     * @return 返回帖子/话题详情列表
     */
    List<UserTopicDto> listTopicByTopicIdList(List<Integer> topicIdList);

    /**
     * 搜素帖子/话题列表 Solr搜索失败的降级逻辑
     * @param keyword 搜索关键字
     * @param page 页码
     * @param size 每页记录数
     * @return 返回搜索结果
     */
    Page<UserTopicDto> search(String keyword, int page, int size);
}
