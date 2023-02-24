package com.example.sbimgutil.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class CataParser {

    public static PdfBookmark parseTxt(File cataTxtFile) throws Exception {
        List<String> lines = FileUtils.readLines(cataTxtFile, StandardCharsets.UTF_8);
        LinkedList<PdfBookmark> bookmarks = new LinkedList<>();

        lines = lines.stream().filter(e -> !e.isBlank()).collect(Collectors.toList());
        //目录txt文件提取目录
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            line = line.stripTrailing();
            String[] split = line.split(" ");
            int level = 0;
            int titleStartIndex = 0;
            for (int i1 = 0; i1 < split.length; i1++) {
                if (!StringUtils.isEmpty(split[i1])) {
                    level = i1 / 5 + 1;
                    titleStartIndex = i1;
                    break;
                }
            }
            String pageRawStr = split[split.length - 1].trim();
            Matcher numberMatcher = Pattern.compile("^[0-9]*").matcher(pageRawStr);
            if (!numberMatcher.find()) {
                log.error("目录文件{}格式不正确,数据为{}", cataTxtFile, line);
                throw new RuntimeException("目录文件格式不正确" + cataTxtFile);
            }
            pageRawStr = numberMatcher.group();
            if (StringUtils.isBlank(pageRawStr)) {
                log.error("目录文件{}格式不正确,标题数据为{}", cataTxtFile, line);
                throw new Exception("目录文件格式不正确" + cataTxtFile);
            }

            String title = line.substring(titleStartIndex, line.length() - pageRawStr.length() - 1).stripTrailing();
            Integer page = Integer.parseInt(pageRawStr);
            PdfBookmark bookmark = new PdfBookmark(level, title, page);
            bookmark.order = i;
            bookmarks.add(bookmark);
        }
        return parseBookmarks(bookmarks);
    }

    public static PdfBookmark parseBookmarks(List<PdfBookmark> bookmarks) {
        for (int i = 1; i < bookmarks.size(); i++) {
            PdfBookmark bookmark = bookmarks.get(i);
            PdfBookmark previous = bookmarks.get(i - 1);
            bookmark.order = i;
            PdfBookmark parent;
            //这里的gap在目录结构下只会是1,0和小于0的三种情况
            int gap = bookmark.level-previous.level;
            parent=previous;
            for (int j = gap; j <1; j++) {
                parent = parent.parent;
            }
            bookmark.setParent(parent);
            if(parent!=null)
                parent.addChild(bookmark);
        }
        return bookmarks.get(0);
    }
}