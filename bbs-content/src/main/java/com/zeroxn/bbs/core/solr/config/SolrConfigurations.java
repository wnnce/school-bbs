package com.zeroxn.bbs.core.solr.config;

import com.zeroxn.bbs.core.solr.SolrRecordOperate;
import com.zeroxn.bbs.web.service.ContentService;
import com.zeroxn.bbs.web.service.SearchService;
import com.zeroxn.bbs.web.service.impl.SearchServiceImpl;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.context.annotation.Bean;

/**
 * @Author: lisang
 * @DateTime: 2023-11-07 20:07:31
 * @Description: Solr和搜索配置类
 */
public class SolrConfigurations {
    static class SolrClientConfiguration {
        @Bean
        HttpSolrClient solrClient(SolrProperties properties) {
            return new HttpSolrClient.Builder()
                    .withBaseSolrUrl(properties.getAddress())
                    .withConnectionTimeout(properties.getHttpTimeOut())
                    .withSocketTimeout(properties.getSocketTimeOut())
                    .build();
        }
    }
    static class SearchConfiguration {
        @Bean
        SolrRecordOperate solrRecordOperate(HttpSolrClient solrClient) {
            return new SolrRecordOperate(solrClient);
        }
        @Bean
        SearchService searchService(ContentService contentService, HttpSolrClient solrClient) {
            return new SearchServiceImpl(contentService, solrClient);
        }
    }

}
