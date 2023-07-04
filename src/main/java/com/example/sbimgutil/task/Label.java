package com.example.sbimgutil.task;


import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.json.JsonParser;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

@Data
public class Label{
    File markedImageFile;
    List<Mark> marks;


    @Data
    static class Mark{
        String transcription;
        int[][] points;
    }

    public static Label parse(Path fileDirPath,String labelValue){
        String[] subStrings = labelValue.split("\t");
        String fileRelativePath = subStrings[0];
        File file = new File(fileDirPath.toFile(), fileRelativePath);
        List<Mark> marks = JSON.parseArray(subStrings[1], Mark.class);
        Label label = new Label();
        label.markedImageFile=file;
        label.marks = marks;
        return label;
    }
}
