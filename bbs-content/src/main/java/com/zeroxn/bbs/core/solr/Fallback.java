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
    default <T> T execute(RunFunc<T> execute, Supplier<T> fallback) {
      T t = null;
      try {
          t = execute.run();
      }catch (Exception e) {
          logger.error("主方法运行错误，使用降级逻辑，错误信息：{}", e.getMessage());
      }
      if (t == null) {
          t = fallback.get();
      }
      return t;
    }
}