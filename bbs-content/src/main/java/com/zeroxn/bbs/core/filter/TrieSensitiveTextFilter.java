package com.zeroxn.bbs.core.filter;

import com.zeroxn.bbs.core.common.TrieNode;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @Author: lisang
 * @DateTime: 2023-10-22 19:04:21
 * @Description: 基于Trie树的敏感词过滤器
 */
@Getter
public class TrieSensitiveTextFilter implements SensitiveTextFilter {
    private static final Logger logger = LoggerFactory.getLogger(TrieSensitiveTextFilter.class);

    private final TrieNode root;

    private final ResourceLoader resourceLoader;

    public TrieSensitiveTextFilter(ResourceLoader resourceLoader) {
        this.root = new TrieNode();
        this.resourceLoader = resourceLoader;
    }

    /**
     * 敏感词词典路径
     */
    @Value("${trie.path}")
    private String wordsPath;

    /**
     * 初始化Trie字典树
     */
    @PostConstruct
    private void init() {
        try {
            InputStream inputStream = null;
            if (wordsPath.startsWith("classpath")) {
                inputStream = resourceLoader.getResource(wordsPath).getInputStream();
            }else {
                inputStream = new FileInputStream(wordsPath);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            Set<String> stringSet = new HashSet<>();
            String line = null;
            while ((line = reader.readLine()) != null) {
                stringSet.add(line.trim());
            }
            if (stringSet.isEmpty()) {
                return;
            }
            logger.info("词典解析完毕，待导入的敏感词 {} 个", stringSet.size());
            stringSet.forEach(this::insert);
            logger.info("敏感词导入完成");
        }catch (IOException ex) {
            logger.error("导入敏感词异常，错误信息：{}", ex.getMessage());
        }
    }

    /**
     * 过滤敏感词，如果匹配到铭感词，让其一直向下匹配，直至匹配到最终节点，然后将匹配到的敏感词返回
     * @param text 需要进行敏感词匹配的文本
     * @return 返回匹配到的敏感词，如果没有则为空
     */
    @Override
    public String filterText(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        text = text.replace(" ", "");
        for (int i = 0; i < text.length(); i++) {
            int j = i;
            TrieNode node = root.getChildren().get(text.charAt(j));
            while (node != null) {
                String word = null;
                if (node.isEnd()) {
                    word = text.substring(i, j + 1);
                }
                if (j == text.length() - 1) {
                    if (word != null) {
                        return word;
                    }else {
                        node = null;
                    }
                }else {
                    j++;
                    node = node.getChildren().get(text.charAt(j));
                    if (node == null && word != null) {
                        return word;
                    }
                }
            }
        }
        return null;
    }


    /**
     * 插入关键词到trie树中
     * @param word 需要插入的关键词
     */
    private void insert(CharSequence word) {
        if (word == null || word.isEmpty()) {
            return;
        }
        TrieNode current = root;
        for (int i = 0; i < word.length(); i++) {
            char key = word.charAt(i);
            TrieNode node = current.getChildren().get(key);
            if (node == null) {
                node = new TrieNode();
                current.getChildren().put(key, node);
            }
            current = node;
        }
        current.setIsEnd(true);
    }
}
