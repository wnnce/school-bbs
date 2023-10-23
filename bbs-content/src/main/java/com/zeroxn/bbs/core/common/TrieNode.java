package com.zeroxn.bbs.core.common;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: lisang
 * @DateTime: 2023-10-22 19:00:19
 * @Description: trie树
 */
@Getter
public class TrieNode {
    private boolean isEnd;
    private final Map<Character, TrieNode> children;

    public TrieNode() {
        isEnd = false;
        children = new HashMap<>();
    }

    public void setIsEnd(boolean isEnd) {
        this.isEnd = isEnd;
    }
}
