package com.zeroxn.bbs.core.solr.config;

import org.apache.solr.client.solrj.SolrClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @Author: lisang
 * @DateTime: 2023-11-07 20:03:48
 * @Description: Solr自动配置类
 */
@Configuration
@ConditionalOnClass(SolrClient.class)
@EnableConfigurationProperties(SolrProperties.class)
@Import({ SolrConfigurations.SolrClientConfiguration.class, SolrConfigurations.SearchConfiguration.class })
public class SolrAutoConfiguration {
}
