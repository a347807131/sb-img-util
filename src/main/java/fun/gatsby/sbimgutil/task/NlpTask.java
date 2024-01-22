package fun.gatsby.sbimgutil.task;

import baidumodel.entity.chat.*;
import baidumodel.service.BaiduService;
import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson2.JSON;
import fun.gatsby.lang.tuple.Tuple2;
import fun.gatsby.lang.tuple.Tuples;
import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.schedule.ITask;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class NlpTask extends BaseTask{

    private final List<File> rawTextFiles;
    private final File isf;
    private final File containerDir;
    final static Promot PROMOT=new Promot();

    File outDir;
    int maxShardSize=300;

    public NlpTask(Map.Entry<File, List<File>> entry, File outDir) {
        this.containerDir=entry.getKey();
        this.rawTextFiles = entry.getValue();
        this.outDir = outDir;
        this.isf = new File(outDir, "intermediate_results.txt");
        this.name="%s的%s页断句翻译任务".formatted(containerDir.getAbsolutePath(),rawTextFiles.size());
    }

    @Override
    public void doWork() throws Throwable {
        if(!isf.exists()){
            FileUtils.forceMkdirParent(isf);
            Files.createFile(isf.toPath());
        }
        /*
         * 查漏补缺
         */
        var preProcessedTextFileNameSet = FileUtil.readLines(isf, StandardCharsets.UTF_8).stream()
                .filter(e -> !StringUtils.isBlank(e))
                .map(line -> JSON.parseObject(line, NlpResult.class))
                .map(NlpResult::getFileName)
                .collect(Collectors.toSet());

        List<File> rawTextFiles = this.rawTextFiles.stream()
                .filter(e -> !preProcessedTextFileNameSet.contains(e.getName()))
                .sorted(Comparator.naturalOrder())
                .toList();

        for (File pageTextFile : rawTextFiles) {
            NlpResult nlpResult = processTextFile(pageTextFile);
            FileUtils.writeStringToFile(isf, JSON.toJSONString(nlpResult) + "\n", "UTF-8", true);
            log.info("[{}]的[{}]页文本处理完成", name, pageTextFile.getName());
        }

        //重排序
        List<String> sortedJsonResults = FileUtil.readLines(isf, StandardCharsets.UTF_8).stream()
                .filter(e -> !StringUtils.isBlank(e))
                .map(line -> JSON.parseObject(line, NlpResult.class))
                .sorted(Comparator.comparing(NlpResult::getFileName))
                .map(JSON::toJSONString).toList();
        FileUtils.writeLines(new File(outDir, "nlp.txt"),sortedJsonResults);
    }

    private NlpResult processTextFile(File pageTextFile) throws Exception {
        String rawText = FileUtil.readString(pageTextFile, StandardCharsets.UTF_8)
                            .replace("\r", "").replace("\n", "");
        NlpResult nlpResult=new NlpResult();
        nlpResult.setRawText(rawText);
        nlpResult.setStartTime(new Date());
        nlpResult.setFileName(pageTextFile.getName());

        StringBuilder ptsb = new StringBuilder();
        var shardedRawTexts = new LinkedList<String>();
        for (int j = 0; j < rawText.length(); j += maxShardSize) {
            int endIndex = Math.min(j + maxShardSize, rawText.length());
            shardedRawTexts.add(rawText.substring(j, endIndex));
        }
        for (String shardedRawText : shardedRawTexts) {
            var outText = doNlp(shardedRawText);
            log.debug(String.valueOf(outText));
            ptsb.append(outText);
        }

        nlpResult.punctuatedText=ptsb.toString();
        nlpResult.setEndTime(new Date());
        return nlpResult;
    }

    /**
     * 对原始的古文进行断句和翻译
     * @param rawText 原始古文
     */
    public final BaiduService baiduService = new BaiduService("XHbymTtGiG4fcaKXv6TuqM63", "9uftaQhGoluK78NbgMajmeGiB8gQ1Fji");
    private String doNlp( String rawText) {
        if(StringUtils.isBlank(rawText)||rawText.length()<=2)
            return rawText;

        String content = Promot.genComposedPromot(rawText);
        BaiduChatMessage msg = new BaiduChatMessage("user", content);
        ErnieBotTurboParam param = ErnieBotTurboParam.builder()
                .penalty_score(1f)
                .user_id("1")
                .temperature(0.95f)
                .messages(Collections.singletonList(
                        msg))
                .build();

        for (int i = 1; i <= 3; i++) {
            ErnieBotTurboResponse response = baiduService.ernieBotTurbo(param, baiduService.getToken());
            String rawResult = response.getResult();
            try {
                return Promot.extractFromResponseResult(rawResult);
            }catch (Exception e){
                log.warn("出现结果解析失败，开始重新请求并处理，{}/3",i);
            }
        }
        throw new RuntimeException("nlp处理失败:"+rawText);
    }


    public static class TaskGenerator extends BaseTaskGenerator {
        public TaskGenerator(AppConfig.GlobalTaskConfig gtc, AppConfig.ProcessTask processTask) {
            super(gtc, processTask, TaskTypeEnum.NLP);
        }

        @Override
        public List<ITask> generate() throws IOException {
            LinkedList<ITask> tasks = new LinkedList<>();
            LinkedHashMap<File, List<File>> dirToFiles = loadSortedDirToFilesMap();
            for (Map.Entry<File, List<File>> entry : dirToFiles.entrySet()) {
                File dir = entry.getKey();
//                if(!dir.getName().equals("0009")) continue;
                File outDir = genOutFile(dir);
                if(new File(outDir,"nlp.txt").exists())
                    continue;
                NlpTask task = new NlpTask(entry, outDir);
                tasks.add(task);
            }
            return tasks;
        }
    }
    /**
     * 片段化结果
     */
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class NlpResult {

        String fileName;
        /**
         * 断句结果
         */
        String punctuatedText;
        /**
         * 翻译结果
         */
        String chineseText;
        /**
         * 原始文本
         */
        String rawText;
        Date endTime;
        Date startTime;
    }
}

@Slf4j
class Promot {
    public static final String PUNCTUATE_TRANSLATE_PROMOT =
            "对古文进行断句，遵循古文的语法和语义规则。\n" +
                    "输出范围: 禁止输出其它额外说明信息。输出格式只能为：断句结果如下：- \n" +
                    "指令: 对下列古文进行断句添加标点。古文原文如下：%s";

    Promot() {

    }

    static String genComposedPromot(String... strs) {
        return String.format(PUNCTUATE_TRANSLATE_PROMOT, strs);
    }

    static String extractFromResponseResult(String rawResultText) throws Exception {
        var text = rawResultText;
        text = text.replace("\n", "");
        String punctuatedResultGuideText = "断句结果如下：";
        int punctuatedResultGuideTextIndex = text.indexOf(punctuatedResultGuideText);

        try {
            return text.substring(
                    punctuatedResultGuideTextIndex + punctuatedResultGuideText.length()
            );
        } catch (Exception e) {
            log.error("原始nlp文本解析失败，原始结果文本:[{}]", text, e);
            throw new Exception(e);
        }
    }
}
