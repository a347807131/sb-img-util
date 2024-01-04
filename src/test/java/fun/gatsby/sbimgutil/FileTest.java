package fun.gatsby.sbimgutil;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson2.JSON;
import fun.gatsby.sbimgutil.task.FiveBackspaceReplaceTask;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
public class FileTest {
    Path path = Path.of("D:\\IdeaProjects\\sb-img-util\\src\\main\\resources\\chts");
    @Test
    public void t1() throws IOException {
        var chts = new HashSet<String>();
        List<File> files = FileUtil.loopFiles(path.toFile());
        for (File file : files) {
            String content = Files.readString(file.toPath());
            Map<String ,String> dict = JSON.parseObject(content,Map.class);
            var subChts = dict.keySet();
            chts.addAll(subChts);
        }
       var sorted = chts.stream().sorted().toList();

        Files.write(Path.of("D:\\IdeaProjects\\sb-img-util\\src\\main\\resources\\my_chinese_chts_dit.txt"),sorted);
    }

    @Test
    public void t2(){
        File myccd = path.resolve("my_chinese_chts_dit.txt").toFile();
        File ccd = path.resolve("chinese_cht_dict.txt").toFile();

        //合并
        var myChts = FileUtil.readLines(myccd, "utf-8");
        var chts = FileUtil.readLines(ccd, "utf-8");
        var allChts = new HashSet<String>();
        allChts.addAll(myChts);
        allChts.addAll(chts);
        var sorted = allChts.stream().sorted().toList();
        FileUtil.writeLines(sorted, path.resolve("my_chinese_cht_dict.txt").toFile(),"utf-8");
    }
}
