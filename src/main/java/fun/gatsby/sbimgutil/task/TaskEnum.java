package fun.gatsby.sbimgutil.task;

import cn.hutool.core.util.ReflectUtil;
import fun.gatsby.sbimgutil.config.AppConfig;
import fun.gatsby.sbimgutil.schedule.ITask;
import fun.gatsby.sbimgutil.task.builder.*;
import lombok.Getter;

import java.lang.invoke.CallSite;
import java.util.Map;

@Getter
public enum TaskEnum {
    IMAGE_TRANSFORM("图片转换", ImageTransformTaskBuilder.class),
    PDF_MERGE("pdf合并", PdfMergeTaskBuilder.class),
    JP2_COMPRESS("JP2图片压缩", Jp2CompressTaskBuilder.class),
    DRAW_BLUR("绘制水印", DrawBlurTaskBuilder.class),
    PDF_SPLIT("pdf拆分", PdfSplitTaskBuilder.class),
    PDF_ADD_CATA("pdf添加目录", PdfAddCateTaskBuilder.class),
    PUNCTUATE("自然语言断句", PunctuateTaskBuilder.class),
    SOUNDTRACK_EXTRACT("音频提取", SoundtrackExtractTaskBuilder.class),
    TRANSCRIBE("音频转录", TranscribeBuilder.class),
    CHAT_API("测试", DsTaskBuilder.class),
    TEXT_TO_BLOG_MD("文本转markdown", TextToBlogMdTaskBuilder.class),
    ;
    public final String cnName;
    public final Class<? extends TaskBuilder<? extends ITask>> builderClass;

    TaskEnum(String taskCnName,Class<? extends TaskBuilder<? extends ITask>> builderClass) {
        this.cnName = taskCnName;
        this.builderClass =builderClass;
    }

    public TaskBuilder<? extends Runnable> getBuilder(AppConfig.GlobalTaskConfig gtc, Map<String,Object> configMap) {
        try {
            return ReflectUtil.newInstance(builderClass, gtc, configMap);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
