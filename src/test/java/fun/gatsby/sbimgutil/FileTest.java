package fun.gatsby.sbimgutil;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson2.JSON;
import fun.gatsby.sbimgutil.task.FiveBackspaceReplaceTask;
import fun.gatsby.sbimgutil.task.NlpTask;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
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
        FileUtil.writeLines(sorted, path.resolve("my_chinese_chts_dict.txt").toFile(),"utf-8");
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

    @Test
    public void t4(){

        String text="断句结果：\n" +
                "森茂、洪茂，合序吾里中，素稱守分安命、順時聽天者，不勝枚舉，而以魏君森茂、洪茂兄弟二人為最優。其居家也，老成謹慎，口不出傲言，耳不聽滛聲。日出而作，日入而息，此外更無關心之世事。不特城市中罕有其跡，卽茶坊酒肆亦不聞其名，而家業之日積月累，有非他人所可幾及者。問其稼，則如茨如梁；問其庾，則如坻如京。兄能愛弟，弟克恭兄，兄弟同心，和樂且湛。既富而又有好德，宜乎康甯與壽考隨之矣！今年森茂六十有五歲，洪茂六十有一嵗，依然鋤雲犁雨。不第。四十世森茂洪茂合序。\n" +
                "\n" +
                "翻译结果：\n" +
                "森茂和洪茂是我们乡里中一直遵守本分、安于命运、顺应时势、听从天意的人中的佼佼者。他们在家中表现成熟稳重、言行谨慎，从不说傲慢的话，也不听淫秽之声。每天日出而作，日落而息，除此之外不关心世事。不仅在城市中很少看到他们的身影，就连茶馆酒肆也未曾听闻他们的名字。然而他们的家业却日益壮大，这是其他人所难以企及的。问他们的庄稼，就像屋顶上的茅草和屋梁一样茂盛；问他们的谷仓，就像河边的沙洲和高地一样堆积如山。兄长能够爱护弟弟，弟弟也能恭敬兄长，兄弟同心协力，和睦相处。他们既富有又拥有美德，因此康宁和长寿也随之而来。今年森茂六十五岁，洪茂六十一岁，依然勤劳耕作。他们是四十世同堂的森茂洪茂兄弟。"
                        ;
        String punctuatedResultGuideText = "断句结果：";
        String translatedResultGuideText = "翻译结果：";
        int punctuatedResultGuideTextIndex = text.indexOf("断句结果：");
        int translatedResultGuideTextIndex = text.indexOf("翻译结果：");

        String punctuatedText = text.substring(
                punctuatedResultGuideTextIndex + punctuatedResultGuideText.length(),
                translatedResultGuideTextIndex
        );
        String chineseText = text.substring(
                translatedResultGuideTextIndex+translatedResultGuideText.length()
        );
    }

    @Test
    public void t5() throws IOException {

        LinkedHashMap<File, List<File>> dirToFiles=FileUtil.loopFiles("D:\\原始备份\\提取txt文档\\NLP").stream()
                .collect(
                LinkedHashMap::new,
                (m, k) -> {
                    File parent = k.getParentFile();
                    m.computeIfAbsent(parent, v -> new LinkedList<>()).add(k);
                },
                LinkedHashMap::putAll
        );

        Path outDir = Path.of("out");
        for (Map.Entry<File, List<File>> entry : dirToFiles.entrySet()) {
            File dir = entry.getKey();
            var files = entry.getValue();
            files=files.stream().sorted(Comparator.naturalOrder()).toList();
            Path txtSaveDir = outDir.resolve(dir.getName());
            FileUtils.forceMkdir(txtSaveDir.toFile());
            for (File file : files) {
                List<NlpTask.NlpResult> nlpResults= FileUtil.readLines(file, StandardCharsets.UTF_8)
                        .stream()
                        .filter(e-> !StringUtils.isBlank(e))
                        .map(e -> JSON.parseObject(e, NlpTask.NlpResult.class))
                        .toList();
                for (NlpTask.NlpResult nlpResult : nlpResults) {
                    File textFile = txtSaveDir.resolve(nlpResult.getFileName()).toFile();
                    FileUtils.writeStringToFile(textFile,nlpResult.getPunctuatedText(),"utf-8");
                }
            }
        }
    }
}
