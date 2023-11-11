package com.zeroxn.bbs.core.solr;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: lisang
 * @DateTime: 2023-11-07 20:23:27
 * @Description: Solr记录实体类注解
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Document {
    String indexName();
}
