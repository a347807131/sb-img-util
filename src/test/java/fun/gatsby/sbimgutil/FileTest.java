package fun.gatsby.sbimgutil;

import cn.hutool.core.io.FileUtil;
import fun.gatsby.sbimgutil.task.FiveBackspaceReplaceTask;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
public class FileTest {
    @Test
    public void t1() {


        List<File> trainLabelFiles = FileUtil.loopFiles("D:\\datasets\\det", file ->
                file.getName().equals("train_labels.txt") || file.getName().equals("train_label.txt")
        );
        List<File> testLabelFiles = FileUtil.loopFiles("D:\\datasets\\det", file ->
                file.getName().equals("test_labels.txt") ||
                file.getName().equals("test_label.txt")
        );

        //用stream从各文件读取行并汇总
        List<String> trainLines = trainLabelFiles.stream().flatMap(file -> {
            try {
                return FileUtil.readUtf8Lines(file).stream();
            } catch (Exception e) {
                log.error("读取文件失败: " + file, e);
                List<String> strings = Collections.emptyList();
                return strings.stream();
            }
        }).toList();

        //用stream从各文件读取行并汇总
        List<String> testLines = testLabelFiles.stream().flatMap(file -> {
            try {
                return FileUtil.readUtf8Lines(file).stream();
            } catch (Exception e) {
                log.error("读取文件失败: " + file, e);
                List<String> strings = Collections.emptyList();
                return strings.stream();
            }
        }).toList();

        FileUtil.writeLines(trainLines, "D:\\datasets\\det\\train_labels.txt", "utf-8");
        FileUtil.writeLines(testLines, "D:\\datasets\\det\\test_labels.txt", "utf-8");
    }
}
