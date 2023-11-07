package com.zeroxn.bbs.test;

import com.zeroxn.bbs.core.search.SolrRecordOperate;
import com.zeroxn.bbs.core.search.TopicIndex;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Author: lisang
 * @DateTime: 2023-11-07 21:01:07
 * @Description:
 */
@SpringBootTest
public class TestSolrRecord {
    @Autowired
    SolrRecordOperate recordOperate;

    @Test
    public void testSaveRecord() {
        TopicIndex index = TopicIndex.builder()
                .id(1)
                .title("测试标题")
                .content("测试呢绒")
                .contentKey("测试")
                .flag(null)
                .type(1)
                .userId(1L)
                .build();
        recordOperate.saveRecord(index);
    }
    @Test
    public void testDeleteRecord() {
        TopicIndex index = TopicIndex.builder().id(1).build();
        recordOperate.deleteRecord(index);
    }
}
