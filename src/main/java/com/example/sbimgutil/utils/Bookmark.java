package com.example.sbimgutil.utils;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.TreeSet;


/**
 * @desc 定义书签
 */
@NoArgsConstructor
@Data
public class Bookmark implements Comparable<Bookmark> {
    //页码
    Integer page;
    //标题
    String title;
    //层级
    Integer level;
    //父id
    Bookmark parent;

    TreeSet<Bookmark> children = new TreeSet<>();

    public Bookmark(int level, String title, String page) {
        this.level = level;
        this.title = title;
        this.page = Integer.valueOf(page);
    }

    @Override
    public int compareTo(Bookmark o) {
        return this.page.compareTo(o.page);
    }

    @Override
    public String toString() {
        return "Bookmark{" +
                ", page=" + page +
                ", title='" + title + '\'' +
                ", level=" + level +
//                ", parent=" + parent +
//                ", children=" + children +
                '}';
    }

    public void addChild(Bookmark bookmark) {
        children.add(bookmark);
    }
}