package fun.gatsby.sbimgutil.utils;


import com.alibaba.fastjson2.JSON;
import lombok.Data;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

@Data
public class Label{
    File markedImageFile;
    List<Detection> detections;

    public static Label parse(Path rootPath, File lableFile) {
        return null;
    }


    @Data
    public class Detection {
        String transcription;
        int[][] points;
    }

    public static Label parse(Path rootDir,String labelValue){
        String[] subStrings = labelValue.split("\t");
        String fileRelativePath = subStrings[0];
        File file = new File(rootDir.toFile(), fileRelativePath);
        Label label = new Label();
        label.markedImageFile=file;
        List<Detection> marks = JSON.parseArray(subStrings[1], Label.Detection.class);
        label.detections = marks;
        return label;
    }
}
