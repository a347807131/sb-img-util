package fun.gatsby.sbimgutil.task;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TextToBlogMdTask extends BaseTask<TextToBlogMdTask.Config>{

    record Config(){

    }

    public TextToBlogMdTask(File inFile, File outFile, Config config) {
        super(inFile, outFile, config);
    }

    @Override
    public void doWork() throws Throwable {

        var fileName = inFile.getName();
        String bv = fileName.split("\\.")[0];
        String title = inFile.getParentFile().getName();
        String text = Files.readString(inFile.toPath());
        //获取文件创建时间
        var createdTime = Files.getLastModifiedTime(inFile.toPath()).toInstant();
        //格式化日期 为 yyy-MM-dd HH:mm:ss
        LocalDateTime  LocalDateTime = createdTime.atZone(ZoneId.systemDefault()).toLocalDateTime();
        String time = LocalDateTime.format(DateTimeFormatter.ofPattern("yyy-MM-dd HH:mm:ss"));
        var url = "https://www.bilibili.com/video/" + bv;
        //插入一个markdown文本链接≈
        var mdLink = "[原视频链接](%s)".formatted(url);

        //在用正则text文本中找到"关键词"所在行，并在该行末尾添加\n aaaa
        String keywords = "**关键词：**";
        String regex = "(?m)^(" + Pattern.quote(keywords) + ".*)$";
        var pattern = Pattern.compile(regex);
        var matcher = pattern.matcher(text);
        String tagString="";
        if (matcher.find()) {
            var start = matcher.start();
            var end = matcher.end();
            tagString = text.substring(start+keywords.length(), end);
            text = text.substring(0, start) + "\n" + text.substring(start, end) + "\n<!--more-->\n" + text.substring(end);
        }else {
            text = "\n<!--more-->\n" + text;
        }

        if(tagString.contains(";")){
           tagString = Arrays.stream(tagString.split(";"))
                    .map(tag -> "- " + tag.replace(";", ""))
                    .collect(Collectors.joining("\n"));
        }else if(tagString.contains("，")){
            tagString = Arrays.stream(tagString.split("，"))
                    .map(tag -> "- " + tag.replace("，", ""))
                    .collect(Collectors.joining("\n"));
        }else if(tagString.contains("；")){
            tagString = Arrays.stream(tagString.split("；"))
                    .map(tag -> "- " + tag.replace("；", ""))
                    .collect(Collectors.joining("\n"));
        }else {
            tagString="- "+tagString;
        }

        var newText =
                """
                ---
                title: %s
                tags:
                %s
                categories:
                - 视频转录文本分析
                - 未明子
                date: %s
                bv: %s
                ---
                            
                %s
                
                %s
                """.formatted(title, tagString, time, bv,
                mdLink,text
                );
        Files.writeString(outFile.toPath(), newText);
    }
}
