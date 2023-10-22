package com.zeroxn.bbs.test;

import org.junit.jupiter.api.Test;
import org.postgresql.util.ReaderInputStream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @Author: lisang
 * @DateTime: 2023-10-22 15:44:46
 * @Description:
 */
public class TestTrieTree {

    private Set<String> testReadWords() throws Exception {
        Set<String> stringSet = new HashSet<>();
        BufferedReader bufferedReader = new BufferedReader(new FileReader("/home/lisang/Documents/words_lines.txt"));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringSet.add(line);
        }
        return stringSet;

    }

    @Test
    public void testStringContainer() throws Exception {
        Set<String> stringSet = testReadWords();
        String text = "马云曾经说过，最大的挑战和突破在于用人，而用人最大的突破在于信任人。我希望诸位也能好好地体会这句话。 所谓学生会退会，关键是学生会退会需要如何写。 每个人都不得不面对这些问题。 在面对这种问题时， 在这种困难的抉择下，本人思来想去，寝食难安。 池田大作曾经说过，不要回避苦恼和困难，挺起身来向它挑战，进而克服它。这句话语虽然很短，但令我浮想联翩。 那么， 了解清楚学生会退会到底是一种怎么样的存在，是解决一切问题的关键。 我们不得不面对一个非常尴尬的事实，那就是， 总结的来说， 学生会退会，到底应该如何实现。 可是，即使是这样，学生会退会的出现仍然代表了一定的意义。 学生会退会，发生了会如何，不发生又会如何。 我认为， 学生会退会，发生了会如何，不发生又会如何。 本人也是经过了深思熟虑，在每个日本AV女优夜夜思考这个问题。 而这些并不是完全重要，更加重要的问题是， 所谓学生会退会，关键是学生会退会需要如何写。 要想清楚，学生会退会，到底是一种怎么样的存在。 赫尔普斯在不经意间这样说过，有时候读书是一种巧妙地避开思考的方法。这句话语虽然很短，但令我浮想联翩。 经过上述讨论， 而这些并不是完全重要，更加重要的问题是， 我们都知道，只要有意义，那么就必须慎重考虑。 这样看来， 学生会退会因何而发生？ 可是，即使是这样，学生会退会的出现仍然代表了一定的意义。 可是，即使是这样，学生会退会的出现仍然代表了一定的意义。 学生会退会，发生了会如何，不发生又会如何。 一般来说， 经过上述讨论， 现在，解决学生会退会的问题，是非常非常重要的。 所以， 问题的关键究竟为何？ 那么， 现在，解决学生会退会的问题，是非常非常重要的。 所以， 带着这些问题，我们来审视一下学生会退会。";
        long startTime = System.currentTimeMillis();
        for (String string : stringSet) {
            if (text.contains(string)) {
                System.out.println("找到关键词" + string);
                break;
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("handlerTime:" + (endTime - startTime));
    }
    @Test
    public void testTrieInsert() throws Exception {
        Set<String> stringSet = testReadWords();
        String text = "马云曾经说过，最大的挑战和突破在于用人，而用人最大的突破在于信任人。我希望诸位也能好好地体会这句话。 所谓学生会退会，关键是学生会退会需要如何写。 每个人都不得不面对这些问题。 在面对这种问题时， 在这种困难的抉择下，本人思来想去，寝食难安。 池田大作曾经说过，不要回避苦恼和困难，挺起身来向它挑战，进而克服它。这句话语虽然很短，但令我浮想联翩。 那么， 了解清楚学生会退会到底是一种怎么样的存在，是解决一切问题的关键。 我们不得不面对一个非常尴尬的事实，那就是， 总结的来说， 学生会退会，到底应该如何实现。 可是，即使是这样，学生会退会的出现仍然代表了一定的意义。 学生会退会，发生了会如何，不发生又会如何。 我认为， 学生会退会，发生了会如何，不发生又会如何。 本人也是经过了深思熟虑，在每个日本AV女优夜夜思考这个问题。 而这些并不是完全重要，更加重要的问题是， 所谓学生会退会，关键是学生会退会需要如何写。 要想清楚，学生会退会，到底是一种怎么样的存在。 赫尔普斯在不经意间这样说过，有时候读书是一种巧妙地避开思考的方法。这句话语虽然很短，但令我浮想联翩。 经过上述讨论， 而这些并不是完全重要，更加重要的问题是， 我们都知道，只要有意义，那么就必须慎重考虑。 这样看来， 学生会退会因何而发生？ 可是，即使是这样，学生会退会的出现仍然代表了一定的意义。 可是，即使是这样，学生会退会的出现仍然代表了一定的意义。 学生会退会，发生了会如何，不发生又会如何。 一般来说， 经过上述讨论， 现在，解决学生会退会的问题，是非常非常重要的。 所以， 问题的关键究竟为何？ 那么， 现在，解决学生会退会的问题，是非常非常重要的。 所以， 带着这些问题，我们来审视一下学生会退会。";
        Trie trie = new Trie();
        for (String string : stringSet) {
            trie.insert(string);
        }
        TrieNode trieNode = trie.getRoot();
        long startTime = System.currentTimeMillis();
        outerLoop:
        for (int i = 0; i < text.length(); i++) {
            int j = i;
            TrieNode node = trieNode.getChildren().get(text.charAt(j));
            while (node != null) {
                String word = null;
                if (node.isEnd) {
                    word = text.substring(i, j + 1);
                }
                j++;
                node = node.getChildren().get(text.charAt(j));
                if (node == null && word != null) {
                    System.out.println("找到关键字：" + word);
                    break outerLoop;
                }
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("handlerTime:" + (endTime - startTime));

    }

    public class Trie {
        private TrieNode root;

        public Trie() {
            root = new TrieNode();
        }

        public void insert(String word) {
            if (word == null || word.isEmpty()) {
                return;
            }
            TrieNode current = root;
            for (int i = 0; i < word.length(); i++) {
                char c = word.charAt(i);
                TrieNode node = current.getChildren().get(c);
                if (node == null) {
                    node = new TrieNode();
                    current.getChildren().put(c, node);
                }
                current = node;
            }
            current.setIsEnd(true);
        }
        public TrieNode getRoot() {
            return root;
        }
        @Override
        public String toString() {
            return "Trie{" +
                    "root=" + root +
                    '}';
        }
    }
    public class TrieNode {
        private boolean isEnd;
        private Map<Character, TrieNode> children;

        public TrieNode() {
            this.isEnd = false;
            this.children = new HashMap<>();
        }
        public void setIsEnd(boolean isEnd) {
            this.isEnd = isEnd;
        }
        public boolean getIsEnd() {
            return isEnd;
        }
        public Map<Character, TrieNode> getChildren() {
            return children;
        }
        @Override
        public String toString() {
            return "TrieNode{" +
                    "isEnd=" + isEnd +
                    ", children=" + children +
                    '}';
        }
    }
}
