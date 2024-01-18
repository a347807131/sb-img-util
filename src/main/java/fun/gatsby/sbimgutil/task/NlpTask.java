package fun.gatsby.sbimgutil.task;

import baidumodel.entity.chat.BaiduChatMessage;
import baidumodel.entity.chat.ErnieBot4Param;
import baidumodel.entity.chat.ErnieBot4Response;
import baidumodel.service.BaiduService;
import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
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
import java.util.stream.Stream;

@Slf4j
public class NlpTask extends BaseTask{
    public static final String PUNCTUATE_TRANSLATE_TEMPLATE=
            "对古文进行解析。注意准确无误，并遵循古文的语法和语义规则。"+
                    "输出范围: 不要其它额外信息。输出格式为json：{\"punctuated_text\":\"-\",\"chinese_text\":\"-\"}" +
                    "指令: 对下列古文进行断句并翻译成现代中文。古文原文如下%s";
    private final List<File> rawTextFiles;
    private final File isf;

    File outDir;
    int shardSize=300;

    public NlpTask(List<File> rawTextFiles, File outDir) {
        this.rawTextFiles = rawTextFiles;
        this.outDir = outDir;
        this.isf = new File(outDir, "intermediate_results.txt");
        if(!isf.exists()){
            try {
                FileUtils.forceMkdirParent(isf);
                Files.createFile(isf.toPath());
            } catch (IOException e) {
                log.error("isf 中间文件创建失败",e);
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void doWork() throws Throwable {
        StringBuilder sb = new StringBuilder();
        rawTextFiles.forEach(e->{
            String text = FileUtil.readString(e, StandardCharsets.UTF_8);
            String replaced = text.replace("\r", "").replace("\n", "");
            sb.append(replaced);
        });
        var strs = FileUtil.readLines(isf, StandardCharsets.UTF_8).stream().filter(e -> !StringUtils.isEmpty(e)).toList();
        String[] shardedRawTexts = sb.toString().split("(?<=\\G.{" + shardSize + "})");
        int startIndex=strs.size();
        for (int i = startIndex; i < shardedRawTexts.length; i++) {
            String text = shardedRawTexts[i];
            NlpResult nlpResult = doNlp(text);
            nlpResult.setIndex(i);
            FileUtils.writeStringToFile(isf, JSON.toJSONString(nlpResult) + "\n", "UTF-8", true);
            log.debug("code:{}的{}/{}段文本处理完成", hashCode(), i + 1, shardedRawTexts.length);
        }
    }

    /**
     * 对原始的古文进行断句和翻译
     * @param rawText 原始古文
     */
    public static final BaiduService baiduService = new BaiduService("5stQ7R7IxVlDoMzSQBBGGmiS", "73QKH26Cq5SvmftOTVkjZRH53So8ysXC");
    private NlpResult doNlp(String rawText) throws Exception{
        String content = String.format(PUNCTUATE_TRANSLATE_TEMPLATE, rawText);
        BaiduChatMessage msg = new BaiduChatMessage("user", content);
        ErnieBot4Param param = ErnieBot4Param.builder()
                .user_id("1")
                .temperature(0.95f).penalty_score(1.0f)
                .messages(Collections.singletonList(
                        msg
                )).build();
        // 构建请求参数
        // 发起请求，获取请求响应
        ErnieBot4Response ernieBot4Response;
        NlpResult nlpResult = new NlpResult();
        nlpResult.setStartTime(new Date());
        try {
            ernieBot4Response = baiduService.ernieBot4(param, baiduService.getToken());
        }catch (Exception e){
            log.error("调用百度接口失败",e);
            throw new Exception("调用百度接口失败");
        }
        // 解析响应，获取结果
        String rawResult = ernieBot4Response.getResult();
        String replaced = rawResult
                .replace("```json", "")
                .replace("```", "")
                .replace("\n", "");
        JSONObject jsonObject = JSON.parseObject(replaced);
        String punctuatedText = jsonObject.getString("punctuated_text");
        String translatedText = jsonObject.getString("chinese_text");
        nlpResult.setRawText(rawText);
        nlpResult.setPunctuatedText(punctuatedText);
        nlpResult.setChineseText(translatedText);
        nlpResult.setEndTime(new Date());
        return nlpResult;
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
                File outDir = genOutFile(dir);
                List<File> files = entry.getValue().stream().sorted(Comparator.naturalOrder()).toList();
                NlpTask task = new NlpTask(files, outDir);
                tasks.add(task);
            }
            return tasks;
        }
    }
}
/**
 * 片段化结果
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
class NlpResult {
    float confidence;
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

    /**
     * 排序
     */
    Integer index;
}
