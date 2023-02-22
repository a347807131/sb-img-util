package com.example.sbimgutil.utils;

import lombok.Data;

/**
 * @desc 定义书签
 */
@Data
public class Bookmark {
    //书签id
    Integer id;
    //页码
    Integer page;
    //标题
    String title;
    //层级
    Integer level;
    //父id
    Integer pid;
}