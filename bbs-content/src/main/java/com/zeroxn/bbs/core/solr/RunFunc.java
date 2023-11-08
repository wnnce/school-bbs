package com.zeroxn.bbs.core.solr;

/**
 * @Author: lisang
 * @DateTime: 2023-11-08 12:00:41
 * @Description: 可以抛出所有异常的接口方法
 */
@FunctionalInterface
public interface RunFunc<T> {
    T run() throws Exception;
}
