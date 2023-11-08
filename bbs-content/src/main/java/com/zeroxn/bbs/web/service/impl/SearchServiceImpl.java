package com.zeroxn.bbs.web.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.zeroxn.bbs.core.solr.Document;
import com.zeroxn.bbs.core.solr.Fallback;
import com.zeroxn.bbs.core.solr.TopicIndex;
import com.zeroxn.bbs.web.dto.UserTopicDto;
import com.zeroxn.bbs.web.service.ContentService;
import com.zeroxn.bbs.web.service.SearchService;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: lisang
 * @DateTime: 2023-11-08 11:10:11
 * @Description: 搜索接口实现类
 */
@Service
public class SearchServiceImpl implements SearchService, Fallback {
    private static final Logger logger = LoggerFactory.getLogger(SearchServiceImpl.class);
    private final ContentService contentService;
    private final HttpSolrClient solrClient;
    public SearchServiceImpl(ContentService contentService, HttpSolrClient solrClient) {
        this.contentService = contentService;
        this.solrClient = solrClient;
    }

    @Override
    public Page<UserTopicDto> search(String keyword, Integer page, Integer size) {
        return this.execute(() -> {
            final int start = (page - 1) * size;
            final SolrQuery query = new SolrQuery("bbs_title:%s or bbs_content:%s or bbs_content_key:%s "
                    .formatted(keyword, keyword, keyword));
            query.addField("id");
            query.setStart(start);
            query.setRows(size);
            QueryResponse response = solrClient.query(TopicIndex.class.getAnnotation(Document.class).indexName(), query);
            final SolrDocumentList results = response.getResults();
            final long total = results.getNumFound();
            final List<Integer> topicIdList = results.stream().map(doc -> Integer.parseInt(doc.getFieldValue("id").toString())).toList();
            logger.info("solr调用成功，page：{}, size:{}, solr:{}", page, size, topicIdList.size());
            final List<UserTopicDto> topicDtoList = contentService.listTopicByTopicIdList(topicIdList);
            return new Page<>(topicDtoList, page, size, total);
        }, () -> contentService.search(keyword, page, size));
    }
}