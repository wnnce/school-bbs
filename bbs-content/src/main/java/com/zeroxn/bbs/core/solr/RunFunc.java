package com.zeroxn.bbs.core.solr;

/**
 * @Author: lisang
 * @DateTime: 2023-11-08 12:00:41
 * @Description:
 */
@FunctionalInterface
public interface RunFunc<T> {
    T run() throws Exception;
}
