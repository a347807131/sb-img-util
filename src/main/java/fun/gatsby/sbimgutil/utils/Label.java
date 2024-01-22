package fun.gatsby.sbimgutil.utils;


import com.alibaba.fastjson2.JSON;
import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Data
public class Label{
    File markedImageFile;
    List<Detection> detections;

    public static List<Label> parse(Path rootPath, File labelFile) throws IOException {
        List<String> labelLines = Files.readAllLines(labelFile.toPath());
        LinkedList<Label> labels = new LinkedList<>();
        for (String line : labelLines) {
            var label = Label.parse(rootPath, line);
            labels.add(label);
        }
        return labels;
    }


    @Data
    static public class Detection {
        String transcription;
        int[][] points;
    }

    public static Label parse(Path rootDir, String labelValue){
        String[] subStrings = labelValue.split("\t");
        String fileRelativePath = subStrings[0];
        File file = new File(rootDir.toFile(), fileRelativePath);
        Label label = new Label();
        label.markedImageFile=file;
        if(!file.exists()){
            Optional<File[]> files = Optional.ofNullable(
                    file.getParentFile().listFiles(e ->
                            Objects.equals(e.getName().split("\\.")[0], file.getName().split("\\.")[0]))
            );
            if(files.isPresent() && files.get().length==1){
                label.markedImageFile=files.get()[0];
            }
        }
        label.detections = JSON.parseArray(subStrings[1], Detection.class);
        return label;
    }
}
