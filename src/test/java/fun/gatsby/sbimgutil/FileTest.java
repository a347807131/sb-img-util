package fun.gatsby.sbimgutil;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson2.JSON;
import fun.gatsby.sbimgutil.task.FiveBackspaceReplaceTask;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
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

    @Test
    public void t3(){
        Path in=Path.of("D:\\temp\\提取txt文档");
        List<File> files = FileUtil.loopFiles(in.toFile());
        LinkedHashMap<File, List<File>> map = files.stream().collect(
                LinkedHashMap::new,
                (m, k) -> {
                    File parent = k.getParentFile();
                    m.computeIfAbsent(parent, v -> new LinkedList<>()).add(k);
                },
                LinkedHashMap::putAll
        );

        int total=0;
        for (Map.Entry<File, List<File>> entry : map.entrySet()) {
            File key = entry.getKey();
            List<File> filesT = entry.getValue();
            filesT=filesT.stream().sorted(Comparator.naturalOrder()).toList();
            StringBuilder sb = new StringBuilder();
            for (File file : filesT) {
                String text = FileUtil.readString(file, StandardCharsets.UTF_8);
                sb.append(text.replace("\n","").replace("\r",""));
            }
            total+=sb.length();
            log.info(sb.toString());
            log.info("{}:{}字数",key.getName(),sb.length());
        }
        log.info("total:{}",total);
    }
}
