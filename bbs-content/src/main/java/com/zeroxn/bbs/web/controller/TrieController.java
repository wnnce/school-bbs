package com.zeroxn.bbs.web.controller;

import com.zeroxn.bbs.core.filter.TrieSensitiveTextFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: lisang
 * @DateTime: 2023-10-22 20:00:54
 * @Description:
 */
@RestController
@RequestMapping("/trie")
public class TrieController {
    @Autowired
    TrieSensitiveTextFilter trieFilter;
    @GetMapping
    public String trieFilter(@RequestParam("text") String text) {
        return trieFilter.filterText(text);
    }
}
