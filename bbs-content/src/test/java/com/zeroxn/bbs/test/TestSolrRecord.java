package com.zeroxn.bbs.test;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.zeroxn.bbs.base.entity.ForumTopic;
import com.zeroxn.bbs.core.solr.SolrRecordOperate;
import com.zeroxn.bbs.core.solr.TopicIndex;
import com.zeroxn.bbs.web.dto.UserTopicDto;
import com.zeroxn.bbs.web.mapper.ForumTopicMapper;
import com.zeroxn.bbs.web.service.ContentService;
import com.zeroxn.bbs.web.service.SearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @Author: lisang
 * @DateTime: 2023-11-07 21:01:07
 * @Description:
 */
@SpringBootTest
public class TestSolrRecord {
    @Autowired
    SolrRecordOperate recordOperate;
    @Autowired
    ForumTopicMapper topicMapper;
    @Autowired
    ContentService contentService;
    @Autowired
    SearchService searchService;

    @Test
    public void testSaveRecord() {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .limit(100);
        List<ForumTopic> topicList = topicMapper.selectListByQuery(queryWrapper);
        List<TopicIndex> indexList = topicList.stream().map(topic -> TopicIndex.builder()
                .id(topic.getId())
                .title(topic.getTitle())
                .content(topic.getContent())
                .contentKey(topic.getContentKey())
                .type(topic.getType())
                .flag(topic.getFlag())
                .userId(topic.getUserId())
                .build()).toList();
        indexList.forEach(index -> {
            recordOperate.saveRecord(index);
        });
    }
    @Test
    public void testDeleteRecord() {
        TopicIndex index = TopicIndex.builder().id(1).build();
        recordOperate.deleteRecord(index);
    }

    @Test
    public void testSearch() {
        Page<UserTopicDto> topicDtoPage = searchService.search("重庆", 1, 5);
        if (topicDtoPage.getRecords().size() > 0) {
            topicDtoPage.getRecords().forEach(topic -> {
                System.out.println(topic.getId());
                System.out.println(topic.getTitle());
            });
        }
    }
}
