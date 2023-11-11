package com.zeroxn.bbs.core.solr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * @Author: lisang
 * @DateTime: 2023-11-08 11:06:16
 * @Description: 有方法回滚降级策略的接口
 */
public interface Fallback {
    Logger logger = LoggerFactory.getLogger(Fallback.class);

    /**
     * 具有降级逻辑的执行方法，具有默认实现，实现类也可以重写
     * @param execute 主方法，可以抛出所有异常，如果觉得返回数据为空也要使用降级逻辑的话，可以在返回数据为空时抛出一个运行时异常即可
     * @param fallback 降级方法，不处理异常，需要确保能获取数据
     * @return 返回具体方法执行的返回数据或空
     * @param <T> 泛型
     */
    default <T> T execute(RunFunc<T> execute, Supplier<T> fallback) {
      T t = null;
      try {
          t = execute.run();
      }catch (Exception e) {
          logger.error("主方法运行错误，使用降级逻辑，错误信息：{}", e.getMessage());
          t = fallback.get();
      }
      return t;
    }
}