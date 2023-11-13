package fun.gatsby.sbimgutil.utils;


import com.alibaba.fastjson2.JSON;
import lombok.Data;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

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
        if(!file.exists()){
            Optional.ofNullable(
                    file.getParentFile().listFiles(e->e.getName().startsWith(file.getName().split("\\.")[0]))
            ).ifPresent(files->{
                if(files.length==1){
                    label.markedImageFile=files[0];
                }
            });
        }
        label.detections = JSON.parseArray(subStrings[1], Detection.class);
        return label;
    }
}
